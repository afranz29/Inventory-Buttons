package com.panda.inventorybuttons.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.panda.inventorybuttons.InventoryButtons;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class HypixelItemManager {
    public static final List<HypixelItem> SKULL_ITEMS = new ArrayList<>();

    private static boolean loaded = false;

    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("%%.*?%%");

    public record HypixelItem(String name, String id, ItemStack iconStack, String configId) {}

    public static void loadAsync() {
        if (loaded) return;
        loaded = true;

        CompletableFuture.runAsync(() -> {
            try {
                InventoryButtons.LOGGER.info("Fetching Hypixel Skyblock items...");
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.hypixel.net/v2/resources/skyblock/items"))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    parseItems(response.body());
                    InventoryButtons.LOGGER.info("Loaded " + SKULL_ITEMS.size() + " skulls from Hypixel.");
                } else {
                    InventoryButtons.LOGGER.error("Failed to fetch Hypixel items: HTTP " + response.statusCode());
                }
            } catch (Exception e) {
                InventoryButtons.LOGGER.error("Error loading Hypixel items", e);
            }
        });
    }

    private static void parseItems(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (!root.has("items")) return;

            JsonArray items = root.getAsJsonArray("items");
            for (JsonElement el : items) {
                if (!el.isJsonObject()) continue;

                try {
                    JsonObject obj = el.getAsJsonObject();

                    String rawName = getSafeString(obj, "name", "Unknown");
                    String name = cleanName(rawName); // FIX: Clean the name
                    String id = getSafeString(obj, "id", "UNKNOWN_ID");
                    String material = getSafeString(obj, "material", "STONE");

                    if ((material.equals("SKULL_ITEM") || material.equals("PLAYER_HEAD")) && obj.has("skin")) {
                        String skinBase64 = null;
                        JsonElement skinEl = obj.get("skin");

                        if (skinEl.isJsonPrimitive()) {
                            skinBase64 = skinEl.getAsString();
                        } else if (skinEl.isJsonObject()) {
                            JsonObject skinObj = skinEl.getAsJsonObject();
                            if (skinObj.has("value")) {
                                skinBase64 = skinObj.get("value").getAsString();
                            }
                        }

                        if (skinBase64 != null) {
                            String hash = extractHash(skinBase64);
                            if (hash != null) {
                                String configId = "skull:" + hash;
                                ItemStack stack = InventoryButtons.CustomButtonData.getSkullStack(configId);
                                stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name));

                                synchronized (SKULL_ITEMS) {
                                    SKULL_ITEMS.add(new HypixelItem(name, id, stack, configId));
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    // continue
                }
            }
        } catch (Exception e) {
            InventoryButtons.LOGGER.error("Error parsing Hypixel items JSON", e);
        }
    }

    private static String cleanName(String name) {
        if (name == null) return "";
        return COLOR_CODE_PATTERN.matcher(name).replaceAll("").trim();
    }

    private static String getSafeString(JsonObject obj, String key, String def) {
        if (obj.has(key)) {
            JsonElement el = obj.get(key);
            if (el.isJsonPrimitive()) {
                return el.getAsString();
            }
        }
        return def;
    }

    private static String extractHash(String base64) {
        try {
            String json = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
            int urlIndex = json.indexOf("http://textures.minecraft.net/texture/");
            if (urlIndex == -1) return null;

            int hashStart = urlIndex + "http://textures.minecraft.net/texture/".length();
            int hashEnd = json.indexOf("\"", hashStart);
            if (hashEnd == -1) hashEnd = json.length();

            return json.substring(hashStart, hashEnd);
        } catch (Exception e) {
            return null;
        }
    }
}