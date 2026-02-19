package com.panda.inventorybuttons;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InventoryButtons {
	public static final Logger LOGGER = LoggerFactory.getLogger("InventoryButtons");

	private static final Path BASE_DIR = FabricLoader.getInstance().getConfigDir().resolve("inventorybuttons");
	private static final Path CONFIG_PATH = BASE_DIR.resolve("invbuttons.json");
	private static final Path PROFILES_DIR = BASE_DIR.resolve("profiles");

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static InventoryButtons instance = new InventoryButtons();

	public boolean enabled = true;
	public boolean showTooltips = true;
	public boolean gridSnap = false;
	public boolean hideInCreative = false;

	public List<CustomButtonData> buttons = new ArrayList<>();

	public static final Map<String, Identifier> CUSTOM_TEXTURES = new LinkedHashMap<>();
	static {
		registerCustom("baubles ring", "baubles");
		registerCustom("baubles gold ring", "baubles_gold");
		registerCustom("cross x", "cross");
		registerCustom("green check mark", "green_check");
		registerCustom("white check mark", "white_check");
		registerCustom("question mark help", "question");
		registerCustom("settings cog config", "settings");
		registerCustom("accessory ring", "accessory");
		registerCustom("accessory ring gold", "accessory_gold");
		registerCustom("armor chestplate", "armor");
		registerCustom("armor gold chestplate", "armor_gold");
		registerCustom("pet cat", "pet");
		registerCustom("pet cat gold", "pet_gold");
		registerCustom("skyblock menu", "skyblock_menu");
		registerCustom("recipe book", "recipe");
		registerCustom("search glass", "search");
	}

	private static void registerCustom(String name, String fileName) {
		CUSTOM_TEXTURES.put(name, Identifier.of("inventorybuttons", "textures/icons/custom/" + fileName + ".png"));
	}

	public static class CustomButtonData {
		public int x, y;
		public String command;
		public String itemId;
		public int backgroundIndex = 0;
		public boolean anchorRight = false;
		public boolean anchorBottom = false;

		public CustomButtonData(int x, int y, String cmd, String item) {
			this.x = x; this.y = y; this.command = cmd; this.itemId = item;
		}

		public ItemStack getItemStack() {
			if (itemId == null || itemId.isEmpty()) return ItemStack.EMPTY;
			if (itemId.startsWith("skull:")) return getSkullStack(itemId);
			try {
				if (!itemId.contains(":")) return ItemStack.EMPTY;
				return new ItemStack(Registries.ITEM.get(Identifier.of(itemId)));
			} catch (Exception e) { return ItemStack.EMPTY; }
		}

		public static ItemStack getSkullStack(String skullStr) {
			try {
				String textureId = skullStr.substring("skull:".length());
				ItemStack head = new ItemStack(Items.PLAYER_HEAD);
				String textureUrl = "http://textures.minecraft.net/texture/" + textureId;
				String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + textureUrl + "\"}}}";
				String base64 = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
				UUID uuid = UUID.nameUUIDFromBytes(textureId.getBytes(StandardCharsets.UTF_8));

				Multimap<String, Property> multimap = ArrayListMultimap.create();
				multimap.put("textures", new Property("textures", base64));
				PropertyMap properties = new PropertyMap(multimap);

				GameProfile profile = new GameProfile(uuid, "Skull", properties);

				ProfileComponent profileComponent = ProfileComponent.ofStatic(profile);
				head.set(DataComponentTypes.PROFILE, profileComponent);

				return head;
			} catch (Exception e) {
				LOGGER.error("Failed to create skull stack", e);
				return new ItemStack(Items.PLAYER_HEAD);
			}
		}
	}

	public static void load() {
		if (Files.exists(CONFIG_PATH)) {
			try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
				instance = GSON.fromJson(reader, InventoryButtons.class);
			} catch (IOException e) {
				LOGGER.error("Failed to load config", e);
			}
		} else { save(); }
	}

	public static void save() {
		try {
			if (!Files.exists(BASE_DIR)) Files.createDirectories(BASE_DIR);
			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
				GSON.toJson(instance, writer);
			}
		} catch (IOException e) {
			LOGGER.error("Failed to save config", e);
		}
	}

	public static void saveProfile(String name) {
		try {
			if (!Files.exists(PROFILES_DIR)) Files.createDirectories(PROFILES_DIR);
			Path profilePath = PROFILES_DIR.resolve(name + ".json");
			try (Writer writer = Files.newBufferedWriter(profilePath)) {
				GSON.toJson(instance.buttons, writer);
			}
		} catch (IOException e) {
			LOGGER.error("Failed to save profile: " + name, e);
		}
	}

	public static void loadProfile(String name) {
		try {
			Path profilePath = PROFILES_DIR.resolve(name + ".json");
			if (Files.exists(profilePath)) {
				try (Reader reader = Files.newBufferedReader(profilePath)) {
					@SuppressWarnings("unchecked")
					List<CustomButtonData> loaded = (List<CustomButtonData>) GSON.fromJson(reader, new TypeToken<List<CustomButtonData>>(){}.getType());
					if (loaded != null) {
						instance.buttons = loaded;
						save();
					}
				}
			}
		} catch (IOException e) {
			LOGGER.error("Failed to load profile: " + name, e);
		}
	}

	public static void deleteProfile(String name) {
		try {
			Path profilePath = PROFILES_DIR.resolve(name + ".json");
			Files.deleteIfExists(profilePath);
		} catch (IOException e) {
			LOGGER.error("Failed to delete profile: " + name, e);
		}
	}

	public static List<String> getProfileNames() {
		if (!Files.exists(PROFILES_DIR)) return Collections.emptyList();
		try (Stream<Path> stream = Files.list(PROFILES_DIR)) {
			return stream
					.filter(file -> !Files.isDirectory(file) && file.getFileName().toString().endsWith(".json"))
					.map(file -> file.getFileName().toString().replace(".json", ""))
					.collect(Collectors.toList());
		} catch (IOException e) {
			LOGGER.error("Failed to list profiles", e);
			return Collections.emptyList();
		}
	}
}