/*
 * Copyright (C) 2026 Panda/afranz29
 * This file is part of Inventory-Buttons, licensed under the LGPLv3.
 */

package com.panda.inventorybuttons.gui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.panda.inventorybuttons.InventoryButtons;
import com.panda.inventorybuttons.util.HypixelItemManager;
import org.lwjgl.glfw.GLFW;

import java.awt.Point;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class GuiInvButtonEditor extends Screen {

    private static final Identifier INVENTORY_TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/gui/container/inventory.png");
    private static final Identifier BUTTONS_TEXTURE = Identifier.fromNamespaceAndPath("inventorybuttons", "textures/gui/buttons.png");
    private static final Identifier INFO_ICON_TEXTURE = Identifier.fromNamespaceAndPath("inventorybuttons", "textures/gui/icons/info.png");

    private static final int TEXTURE_WIDTH = 90;
    private static final int TEXTURE_HEIGHT = 36;

    private static final int BUTTON_SIZE = 18;
    private static final int OUTER_PADDING = 2;
    private static final int OUTER_GRID_SIZE = BUTTON_SIZE + OUTER_PADDING;
    private static final int TOP_BOTTOM_START_X = 8;

    private static final List<Point> INVENTORY_FIXED_SLOTS = new ArrayList<>();
    static {
        INVENTORY_FIXED_SLOTS.add(new Point(25, 8));
        INVENTORY_FIXED_SLOTS.add(new Point(57, 8));
        INVENTORY_FIXED_SLOTS.add(new Point(25, 60));
        INVENTORY_FIXED_SLOTS.add(new Point(57, 60));
        INVENTORY_FIXED_SLOTS.add(new Point(97, 17));
        INVENTORY_FIXED_SLOTS.add(new Point(115, 17));
        INVENTORY_FIXED_SLOTS.add(new Point(97, 35));
        INVENTORY_FIXED_SLOTS.add(new Point(115, 35));
        INVENTORY_FIXED_SLOTS.add(new Point(153, 27));
        INVENTORY_FIXED_SLOTS.add(new Point(98, 61));
        INVENTORY_FIXED_SLOTS.add(new Point(116, 61));
        INVENTORY_FIXED_SLOTS.add(new Point(134, 61));
        INVENTORY_FIXED_SLOTS.add(new Point(152, 61));
    }

    private static final Map<String, String> SKULL_ICONS = new HashMap<>();
    static {
        SKULL_ICONS.put("Personal Bank", "skull:e36e94f6c34a35465fce4a90f2e25976389eb9709a12273574ff70fd4daa6852");
        SKULL_ICONS.put("Skyblock Hub", "skull:d7cc6687423d0570d556ac53e0676cb563bbdd9717cd8269bdebed6f6d4e7bf8");
        SKULL_ICONS.put("Private Island", "skull:c9c8881e42915a9d29bb61a16fb26d059913204d265df5b439b3d792acd56");
        SKULL_ICONS.put("Castle", "skull:f4559d75464b2e40a518e4de8e6cf3085f0a3ca0b1b7012614c4cd96fed60378");
        SKULL_ICONS.put("Sirius Shack", "skull:7ab83858ebc8ee85c3e54ab13aabfcc1ef2ad446d6a900e471c3f33b78906a5b");
        SKULL_ICONS.put("Crypts", "skull:25d2f31ba162fe6272e831aed17f53213db6fa1c4cbe4fc827f3963cc98b9");
        SKULL_ICONS.put("Spiders Den", "skull:c754318a3376f470e481dfcd6c83a59aa690ad4b4dd7577fdad1c2ef08d8aee6");
        SKULL_ICONS.put("Top Of The Nest", "skull:9d7e3b19ac4f3dee9c5677c135333b9d35a7f568b63d1ef4ada4b068b5a25");
        SKULL_ICONS.put("The End", "skull:7840b87d52271d2a755dedc82877e0ed3df67dcc42ea479ec146176b02779a5");
        SKULL_ICONS.put("The End Dragons Nest", "skull:a1cd6d2d03f135e7c6b5d6cdae1b3a68743db4eb749faf7341e9fb347aa283b");
        SKULL_ICONS.put("The Park", "skull:a221f813dacee0fef8c59f76894dbb26415478d9ddfc44c2e708a6d3b7549b");
        SKULL_ICONS.put("The Park Jungle", "skull:79ca3540621c1c79c32bf42438708ff1f5f7d0af9b14a074731107edfeb691c");
        SKULL_ICONS.put("The Park Howling Cave", "skull:1832d53997b451635c9cf9004b0f22bb3d99ab5a093942b5b5f6bb4e4de47065");
        SKULL_ICONS.put("Gold Mines", "skull:73bc965d579c3c6039f0a17eb7c2e6faf538c7a5de8e60ec7a719360d0a857a9");
        SKULL_ICONS.put("Deep Caverns", "skull:569a1f114151b4521373f34bc14c2963a5011cdc25a6554c48c708cd96ebfc");
        SKULL_ICONS.put("The Barn", "skull:4d3a6bd98ac1833c664c4909ff8d2dc62ce887bdcf3cc5b3848651ae5af6b");
        SKULL_ICONS.put("Mushroom Desert", "skull:6b20b23c1aa2be0270f016b4c90d6ee6b8330a17cfef87869d6ad60b2ffbf3b5");
        SKULL_ICONS.put("Dungeon Hub", "skull:9b56895b9659896ad647f58599238af532d46db9c1b0389b8bbeb70999dab33d");
        SKULL_ICONS.put("Dwarven Mines", "skull:51539dddf9ed255ece6348193cd75012c82c93aec381f05572cecf7379711b3b");
        SKULL_ICONS.put("HOTM Heart Of The Mountain", "skull:86f06eaa3004aeed09b3d5b45d976de584e691c0e9cade133635de93d23b9edb");
        SKULL_ICONS.put("Bazaar Dude", "skull:c232e3820897429157619b0ee099fec0628f602fff12b695de54aef11d923ad7");
        SKULL_ICONS.put("Museum", "skull:438cf3f8e54afc3b3f91d20a49f324dca1486007fe545399055524c17941f4dc");
        SKULL_ICONS.put("Crystal Hollows", "skull:21dbe30b027acbceb612563bd877cd7ebb719ea6ed1399027dcee58bb9049d4a");
        SKULL_ICONS.put("Dwarven Forge", "skull:5cbd9f5ec1ed007259996491e69ff649a3106cf920227b1bb3a71ee7a89863f");
        SKULL_ICONS.put("Forgotton Skull", "skull:6becc645f129c8bc2faa4d8145481fab11ad2ee75749d628dcd999aa94e7");
        SKULL_ICONS.put("Crystal Nucleus", "skull:34d42f9c461cee1997b67bf3610c6411bf852b9e5db607bbf626527cfb42912c");
        SKULL_ICONS.put("Void Sepulture", "skull:eb07594e2df273921a77c101d0bfdfa1115abed5b9b2029eb496ceba9bdbb4b3");
        SKULL_ICONS.put("Crimson Isle", "skull:c3687e25c632bce8aa61e0d64c24e694c3eea629ea944f4cf30dcfb4fbce071");
        SKULL_ICONS.put("Trapper Den", "skull:6102f82148461ced1f7b62e326eb2db3a94a33cba81d4281452af4d8aeca4991");
        SKULL_ICONS.put("Arachne Sanctuary", "skull:35e248da2e108f09813a6b848a0fcef111300978180eda41d3d1a7a8e4dba3c3");
        SKULL_ICONS.put("Garden", "skull:f4880d2c1e7b86e87522e20882656f45bafd42f94932b2c5e0d6ecaa490cb4c");
        SKULL_ICONS.put("Winter", "skull:6dd663136cafa11806fdbca6b596afd85166b4ec02142c8d5ac8941d89ab7");
        SKULL_ICONS.put("Wizard Tower", "skull:838564e28aba98301dbda5fafd86d1da4e2eaeef12ea94dcf440b883e559311c");
        SKULL_ICONS.put("Dwarven Mines Base Camp", "skull:2461ec3bd654f62ca9a393a32629e21b4e497c877d3f3380bcf2db0e20fc0244");
    }

    private enum FilterMode {
        ALL(Items.BOOK),
        ITEMS(Items.DIAMOND_SWORD),
        BLOCKS(Items.BEDROCK),
        SKULLS(Items.SKELETON_SKULL),
        MISC(Items.BUCKET);

        final ItemStack icon;
        FilterMode(Item item) { this.icon = new ItemStack(item); }
    }
    private FilterMode currentMode = FilterMode.ALL;

    private interface IconResult {
        void extractRenderState(GuiGraphicsExtractor context, int x, int y);
        String getDisplayName();
        String getConfigId();
    }

    private record ItemStackResult(ItemStack stack) implements IconResult {
        @Override
        public void extractRenderState(GuiGraphicsExtractor context, int x, int y) {
            context.item(stack, x, y);
        }
        @Override
        public String getDisplayName() { return stack.getHoverName().getString(); }
        @Override
        public String getConfigId() {
            if (stack.getItem() == net.minecraft.world.item.Items.PLAYER_HEAD) {
                String name = stack.getHoverName().getString();
                if (SKULL_ICONS.containsKey(name)) return SKULL_ICONS.get(name);
            }
            return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        }
    }

    private record TextureResult(String name, Identifier textureId) implements IconResult {
        @Override
        public void extractRenderState(GuiGraphicsExtractor context, int x, int y) {
            context.blit(RenderPipelines.GUI_TEXTURED, textureId, x, y, 0.0f, 0.0f, 16, 16, 16, 16);
        }
        @Override
        public String getDisplayName() { return name; }
        @Override
        public String getConfigId() { return textureId.toString(); }
    }

    private record HypixelResult(HypixelItemManager.HypixelItem item) implements IconResult {
        @Override
        public void extractRenderState(GuiGraphicsExtractor context, int x, int y) {
            context.item(item.iconStack(), x, y);
        }
        @Override
        public String getDisplayName() { return item.name(); }
        @Override
        public String getConfigId() { return item.configId(); }
    }

    private final Screen parent;
    private final int xSize = 176;
    private final int ySize = 166;
    private int guiLeft;
    private int guiTop;

    private int dragOffsetX, dragOffsetY;
    private boolean isDragging = false;
    private boolean isEditorOpen = false;
    private boolean isInfoPanelOpen = false;

    private boolean isSavePanelOpen = false;
    private EditBox saveProfileField;

    private final int editorWidth = 150;
    private final int editorHeight = 224;
    private int editorLeft;
    private int editorTop;

    private EditBox commandTextField;
    private EditBox iconTextField;
    private EditBox addSkullField;

    private InventoryButtons.CustomButtonData editingButton = null;

    private final List<IconResult> searchedIcons = new ArrayList<>();
    private final LerpingInteger itemScroll = new LerpingInteger(0, 100);

    private String actionStatusText = "";
    private long actionStatusEndTime = 0;

    private boolean localGridSnap;

    public GuiInvButtonEditor(Screen parent) {
        super(Component.literal("NEU Button Editor Port"));
        this.parent = parent;
        this.localGridSnap = InventoryButtons.instance.gridSnap;
    }

    @Override
    protected void init() {
        this.guiLeft = (this.width - xSize) / 2;
        this.guiTop = (this.height - ySize) / 2;

        this.commandTextField = new EditBox(this.font, 0, 0, editorWidth - 14, 16, Component.literal("Command"));
        this.commandTextField.setMaxLength(256);
        this.commandTextField.setResponder(s -> {
            if (editingButton != null) {
                String text = s;
                if (text.isEmpty()) {
                    text = "/";
                    commandTextField.setValue(text);
                    commandTextField.moveCursorTo(1, false);
                } else if (!text.startsWith("/")) {
                    text = "/" + text.replace("/", "");
                    commandTextField.setValue(text);
                    commandTextField.moveCursorTo(text.length(), false);
                }
                editingButton.command = text;
            }
        });

        this.iconTextField = new EditBox(this.font, 0, 0, editorWidth - 14, 16, Component.literal("Icon"));
        this.iconTextField.setMaxLength(256);
        this.iconTextField.setResponder(this::search);

        this.addSkullField = new EditBox(this.font, 0, 0, editorWidth - 14, 16, Component.literal("Skull ID"));
        this.addSkullField.setMaxLength(512);
        this.addSkullField.setResponder(s -> {
            if (editingButton != null && !s.isEmpty()) {
                editingButton.itemId = s;
            }
        });

        this.saveProfileField = new EditBox(this.font, 0, 0, 140, 20, Component.literal("Profile Name"));
        this.saveProfileField.setMaxLength(32);

        search("");
    }

    private void updateEditorCoordinates() {
        if (editingButton == null) return;
        int btnX = guiLeft + editingButton.x;
        int btnY = guiTop + editingButton.y;
        if (editingButton.anchorRight) btnX += xSize;
        if (editingButton.anchorBottom) btnY += ySize;

        editorLeft = btnX + 25;
        editorTop = btnY - 20;

        if (editorLeft + editorWidth > width) editorLeft = btnX - editorWidth - 5;
        if (editorTop + editorHeight > height) editorTop = height - editorHeight - 5;
        if (editorTop < 0) editorTop = 5;
    }

    private void drawBorderLocal(GuiGraphicsExtractor context, int x, int y, int w, int h, int color) {
        context.fill(x, y, x + w, y + 1, color);
        context.fill(x, y + h - 1, x + w, y + h, color);
        context.fill(x, y + 1, x + 1, y + h - 1, color);
        context.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {

        guiLeft = (this.width - xSize) / 2;
        guiTop = (this.height - ySize) / 2;

        context.blit(RenderPipelines.GUI_TEXTURED, INVENTORY_TEXTURE, guiLeft, guiTop, 0.0f, 0.0f, xSize, ySize, 256, 256);

        for (InventoryButtons.CustomButtonData button : InventoryButtons.instance.buttons) {
            int x = guiLeft + button.x;
            int y = guiTop + button.y;
            if (button.anchorRight) x += xSize;
            if (button.anchorBottom) y += ySize;

            if (button == editingButton) {
                context.fill(x, y, x + 18, y + 18, 0x8000FF00);
                drawBorderLocal(context, x, y, 18, 18, 0xFFFFFFFF);
            } else {
                context.blit(RenderPipelines.GUI_TEXTURED, BUTTONS_TEXTURE, x, y, (float)(button.backgroundIndex * 18), 18.0f, 18, 18, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            }

            if (button.itemId != null && !button.itemId.isEmpty()) {
                Identifier customTex = null;
                for(Map.Entry<String, Identifier> entry : InventoryButtons.CUSTOM_TEXTURES.entrySet()) {
                    if (entry.getValue().toString().equals(button.itemId)) {
                        customTex = entry.getValue();
                        break;
                    }
                }

                if (customTex != null) {
                    context.blit(RenderPipelines.GUI_TEXTURED, customTex, x + 1, y + 1, 0.0f, 0.0f, 16, 16, 16, 16);
                } else {
                    ItemStack stack = button.getItemStack();
                    if (!stack.isEmpty()) {
                        context.item(stack, x + 1, y + 1);
                    }
                }
            } else {
                context.centeredText(font, Component.literal("?"), x + 9, y + 5, 0xFFFFFFFF);
            }
        }

        if (editingButton != null && isEditorOpen) {
            updateEditorCoordinates();
            context.pose().pushMatrix();
            context.pose().translate(0.0f, 0.0f);
            renderEditorPanel(context, mouseX, mouseY, delta);
            context.pose().popMatrix();
        }

        if (editingButton == null) {
            context.centeredText(font, Component.literal("Click to select/drag, Click again to edit"), width / 2, 10, 0xFFFFFFFF);
            context.centeredText(font, Component.literal("Backspace while selected to delete"), width / 2, 22, 0xFFAAAAAA);
            context.centeredText(font, Component.literal("Right Click empty space to add new"), width / 2, 34, 0xFFAAAAAA);
        }

        String snapText = "Grid Snap (S): " + (this.localGridSnap ? "ON" : "OFF");
        int snapColor = this.localGridSnap ? 0xFF55FF55 : 0xFFAAAAAA;
        context.text(font, Component.literal(snapText), 5, height - 15, snapColor, false);

        renderIOButtons(context, mouseX, mouseY);

        if (isInfoPanelOpen) {
            renderSkullInfoPanel(context, mouseX, mouseY);
        }

        if (isSavePanelOpen) {
            context.pose().pushMatrix();
            context.pose().translate(0.0f, 0.0f);
            renderSaveProfilePanel(context, mouseX, mouseY, delta);
            context.pose().popMatrix();
        }
    }

    private void renderIOButtons(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        int btnH = 20;
        int spacing = 5;
        int startX = 10;
        int startY = 10;

        int saveW = 100;
        int saveY = startY;
        boolean hoverSave = mouseX >= startX && mouseX <= startX + saveW && mouseY >= saveY && mouseY <= saveY + btnH;
        context.fill(startX, saveY, startX + saveW, saveY + btnH, hoverSave ? 0xFF606060 : 0xFF404040);
        drawBorderLocal(context, startX, saveY, saveW, btnH, 0xFFFFFFFF);
        context.centeredText(font, Component.literal("Save as Profile"), startX + saveW / 2, saveY + 6, 0xFFFFFFFF);

        int exportW = 50;
        int exportY = saveY + btnH + spacing;
        boolean hoverExport = mouseX >= startX && mouseX <= startX + exportW && mouseY >= exportY && mouseY <= exportY + btnH;
        context.fill(startX, exportY, startX + exportW, exportY + btnH, hoverExport ? 0xFF606060 : 0xFF404040);
        drawBorderLocal(context, startX, exportY, exportW, btnH, 0xFFFFFFFF);
        context.centeredText(font, Component.literal("Export"), startX + exportW / 2, exportY + 6, 0xFFFFFFFF);

        int importW = 50;
        int importY = exportY + btnH + spacing;
        boolean hoverImport = mouseX >= startX && mouseX <= startX + importW && mouseY >= importY && mouseY <= importY + btnH;
        context.fill(startX, importY, startX + importW, importY + btnH, hoverImport ? 0xFF606060 : 0xFF404040);
        drawBorderLocal(context, startX, importY, importW, btnH, 0xFFFFFFFF);
        context.centeredText(font, Component.literal("Import"), startX + importW / 2, importY + 6, 0xFFFFFFFF);

        if (System.currentTimeMillis() < actionStatusEndTime) {
            context.text(font, Component.literal(actionStatusText), startX + saveW + 5, startY + 6, 0xFF55FF55, false);
        }
    }

    private void renderSaveProfilePanel(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.fillGradient(0, 0, this.width, this.height, 0xAA000000, 0xAA000000);

        int panelW = 200;
        int panelH = 100;
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        context.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xFF202020);
        drawBorderLocal(context, panelX, panelY, panelW, panelH, 0xFF505050);

        context.centeredText(font, Component.literal("Profile Name"), panelX + panelW / 2, panelY + 10, 0xFFFFFFFF);

        int closeSize = 12;
        int closeX = panelX + panelW - closeSize - 5;
        int closeY = panelY + 5;
        boolean hoverClose = mouseX >= closeX && mouseX <= closeX + closeSize && mouseY >= closeY && mouseY <= closeY + closeSize;
        context.fill(closeX, closeY, closeX + closeSize, closeY + closeSize, hoverClose ? 0xFFFF0000 : 0xFFCC0000);
        context.centeredText(font, Component.literal("x"), closeX + closeSize / 2, closeY + 2, 0xFFFFFFFF);

        saveProfileField.setX(panelX + 30);
        saveProfileField.setY(panelY + 35);
        saveProfileField.extractRenderState(context, mouseX, mouseY, delta);

        int btnW = 60;
        int btnH = 20;
        int btnX = panelX + (panelW - btnW) / 2;
        int btnY = panelY + 65;
        boolean hoverConfirm = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
        context.fill(btnX, btnY, btnX + btnW, btnY + btnH, hoverConfirm ? 0xFF408040 : 0xFF206020);
        drawBorderLocal(context, btnX, btnY, btnW, btnH, 0xFFFFFFFF);
        context.centeredText(font, Component.literal("Save"), btnX + btnW / 2, btnY + 6, 0xFFFFFFFF);
    }

    private void handleExport() {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(InventoryButtons.instance.buttons);
            String encoded = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
            if (this.minecraft != null) {
                this.minecraft.keyboardHandler.setClipboard(encoded);
                actionStatusText = "Exported to Clipboard!";
                actionStatusEndTime = System.currentTimeMillis() + 3000;
            }
        } catch (Exception e) {
            actionStatusText = "Export Failed!";
            actionStatusEndTime = System.currentTimeMillis() + 3000;
            e.printStackTrace();
        }
    }

    private void handleImport() {
        try {
            if (this.minecraft != null) {
                String clipboard = this.minecraft.keyboardHandler.getClipboard();
                if (clipboard == null || clipboard.isEmpty()) return;

                String json = new String(Base64.getDecoder().decode(clipboard), StandardCharsets.UTF_8);
                Gson gson = new Gson();
                List<InventoryButtons.CustomButtonData> loaded = gson.fromJson(json, new TypeToken<List<InventoryButtons.CustomButtonData>>(){}.getType());

                if (loaded != null) {
                    InventoryButtons.instance.buttons = loaded;
                    InventoryButtons.save();
                    actionStatusText = "Imported Successfully!";
                    actionStatusEndTime = System.currentTimeMillis() + 3000;
                }
            }
        } catch (Exception e) {
            actionStatusText = "Invalid Clipboard!";
            actionStatusEndTime = System.currentTimeMillis() + 3000;
            e.printStackTrace();
        }
    }

    private void renderEditorPanel(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.fill(editorLeft, editorTop, editorLeft + editorWidth, editorTop + editorHeight, 0xFF202020);
        drawBorderLocal(context, editorLeft, editorTop, editorWidth, editorHeight, 0xFF505050);

        context.text(font, Component.literal("Command"), editorLeft + 7, editorTop + 7, 0xFFA0A0A0, false);
        commandTextField.setX(editorLeft + 7);
        commandTextField.setY(editorTop + 19);
        commandTextField.extractRenderState(context, mouseX, mouseY, delta);

        context.text(font, Component.literal("Background Style"), editorLeft + 7, editorTop + 40, 0xFFA0A0A0, false);
        for (int i = 0; i < 5; i++) {
            int bx = editorLeft + 7 + (i * 20);
            int by = editorTop + 52;

            context.blit(RenderPipelines.GUI_TEXTURED, BUTTONS_TEXTURE, bx, by, (float)(i * 18), 0.0f, 18, 18, TEXTURE_WIDTH, TEXTURE_HEIGHT);

            if (editingButton.backgroundIndex == i) {
                drawBorderLocal(context, bx - 1, by - 1, 20, 20, 0xFF00FF00);
            }
        }

        int filterY = editorTop + 75;
        int btnHeight = 20;
        int totalWidth = editorWidth - 14;
        int btnWidth = totalWidth / FilterMode.values().length;

        for (int i = 0; i < FilterMode.values().length; i++) {
            FilterMode mode = FilterMode.values()[i];
            int bx = editorLeft + 7 + (i * btnWidth);

            boolean isActive = (currentMode == mode);
            int bgColor = isActive ? 0xFF606060 : 0xFF303030;
            int borderColor = isActive ? 0xFFFFFFFF : 0xFF505050;

            context.fill(bx, filterY, bx + btnWidth, filterY + btnHeight, bgColor);
            drawBorderLocal(context, bx, filterY, btnWidth, btnHeight, borderColor);

            context.item(mode.icon, bx + (btnWidth - 16) / 2, filterY + (btnHeight - 16) / 2);
        }

        context.text(font, Component.literal("Search Icon"), editorLeft + 7, editorTop + 100, 0xFFA0A0A0, false);
        iconTextField.setX(editorLeft + 7);
        iconTextField.setY(editorTop + 112);
        iconTextField.extractRenderState(context, mouseX, mouseY, delta);

        int listY = editorTop + 135;
        int listH = 82;

        if (currentMode == FilterMode.SKULLS) {
            String infoText = "Add Skull by ID";
            int titleX = editorLeft + 7;
            int titleY = editorTop + 135;
            context.text(font, Component.literal(infoText), titleX, titleY, 0xFFA0A0A0, false);

            int iconSize = 10;
            int infoIconX = titleX + font.width(infoText) + 5;
            int infoIconY = titleY - 1;

            context.blit(RenderPipelines.GUI_TEXTURED, INFO_ICON_TEXTURE, infoIconX, infoIconY, 0.0f, 0.0f, iconSize, iconSize, iconSize, iconSize);

            if (mouseX >= infoIconX && mouseX < infoIconX + iconSize && mouseY >= infoIconY && mouseY < infoIconY + iconSize) {
                context.fill(infoIconX, infoIconY, infoIconX + iconSize, infoIconY + iconSize, 0x40FFFFFF);
            }

            addSkullField.setX(editorLeft + 7);
            addSkullField.setY(editorTop + 147);
            addSkullField.extractRenderState(context, mouseX, mouseY, delta);

            listY = editorTop + 170;
            listH = 47;
        }

        renderIconList(context, mouseX, mouseY, listY, listH);
    }

    private void renderSkullInfoPanel(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        int panelW = 150;
        int panelH = 190;
        int panelX = 10;
        int panelY = (this.height - panelH) / 2;

        context.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xFF202020);
        drawBorderLocal(context, panelX, panelY, panelW, panelH, 0xFF505050);

        int closeSize = 10;
        int closeX = panelX + panelW - closeSize - 4;
        int closeY = panelY + 4;

        if (mouseX >= closeX && mouseX <= closeX + closeSize && mouseY >= closeY && mouseY <= closeY + closeSize) {
            context.fill(closeX, closeY, closeX + closeSize, closeY + closeSize, 0xFFFF0000);
        } else {
            context.fill(closeX, closeY, closeX + closeSize, closeY + closeSize, 0xFFCC0000);
        }
        context.centeredText(font, Component.literal("x"), closeX + closeSize / 2, closeY + 1, 0xFFFFFFFF);

        int textX = panelX + 8;
        int textY = panelY + 8;

        context.text(font, Component.literal("How to find Skull IDs").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD), textX, textY, 0xFFFFFFFF, false);
        textY += 18;

        String[] steps = {
                "1. Go to a site like minecraft-heads.com",
                "2. Find and select a head.",
                "3. Look for the 'Texture URL' or 'Minecraft-URL' section.",
                "4. Copy only the long alphanumeric string at the very end of the URL.",
                "5. Paste it into the 'Add Skull ID' field preceded by 'skull:'.",
                "   Example: skull:a6cc4..."
        };

        for (String step : steps) {
            List<FormattedCharSequence> lines = font.split(Component.literal(step), panelW - 16);
            for (FormattedCharSequence line : lines) {
                context.text(font, line, textX, textY, 0xFFA0A0A0, false);
                textY += font.lineHeight + 2;
            }
            textY += 4;
        }
    }

    private void renderIconList(GuiGraphicsExtractor context, int mouseX, int mouseY, int listY, int listH) {
        int listX = editorLeft + 7;
        int listW = editorWidth - 14;

        context.fill(listX, listY, listX + listW, listY + listH, 0xFF101010);
        context.enableScissor(listX, listY, listX + listW, listY + listH);

        itemScroll.tick();

        int cols = 6;
        int scroll = itemScroll.getValue();
        int startIndex = (scroll / 20) * cols;
        int endIndex = Math.min(startIndex + 42, searchedIcons.size());

        IconResult resultToTooltip = null;

        for (int i = startIndex; i < endIndex; i++) {
            if (i >= searchedIcons.size()) break;

            IconResult result = searchedIcons.get(i);
            int col = (i - startIndex) % cols;
            int row = (i - startIndex) / cols;

            int ix = listX + 2 + (col * 20);
            int iy = listY + 2 + (row * 20) - (scroll % 20);

            result.extractRenderState(context, ix, iy);

            if (mouseX >= ix && mouseX < ix + 18 && mouseY >= iy && mouseY < iy + 18) {
                context.fill(ix, iy, ix + 18, iy + 18, 0x40FFFFFF);
                resultToTooltip = result;
            }
        }

        context.disableScissor();

        if (resultToTooltip != null) {
            context.setTooltipForNextFrame(font, Component.literal(resultToTooltip.getDisplayName()), mouseX, mouseY);
        }

        int totalRows = (int) Math.ceil((double) searchedIcons.size() / cols);
        int visibleRows = listH / 20;
        if (totalRows > visibleRows) {
            float ratio = (float) scroll / (float) ((totalRows - visibleRows) * 20);
            if(ratio > 1) ratio = 1;
            int barHeight = (int) (listH * ((float)visibleRows / totalRows));
            if(barHeight < 10) barHeight = 10;
            int barY = listY + (int)((listH - barHeight) * ratio);
            context.fill(listX + listW - 2, barY, listX + listW, barY + barHeight, 0xFF808080);
        }
    }

    private void search(String query) {
        searchedIcons.clear();
        String lower = query.toLowerCase().trim();

        if (currentMode == FilterMode.ALL || currentMode == FilterMode.SKULLS) {
            for (Map.Entry<String, String> entry : SKULL_ICONS.entrySet()) {
                if (entry.getKey().toLowerCase().contains(lower)) {
                    ItemStack skullStack = InventoryButtons.CustomButtonData.getSkullStack(entry.getValue());
                    skullStack.set(DataComponents.CUSTOM_NAME, Component.literal(entry.getKey()));
                    searchedIcons.add(new ItemStackResult(skullStack));
                }
            }
            synchronized (HypixelItemManager.SKULL_ITEMS) {
                for (HypixelItemManager.HypixelItem hItem : HypixelItemManager.SKULL_ITEMS) {
                    if (hItem.name().toLowerCase().contains(lower)) {
                        searchedIcons.add(new HypixelResult(hItem));
                    }
                }
            }
        }

        if (currentMode == FilterMode.ALL || currentMode == FilterMode.MISC) {
            for (Map.Entry<String, Identifier> entry : InventoryButtons.CUSTOM_TEXTURES.entrySet()) {
                if (entry.getKey().toLowerCase().contains(lower)) {
                    searchedIcons.add(new TextureResult(entry.getKey(), entry.getValue()));
                }
            }
        }

        if (currentMode == FilterMode.ALL || currentMode == FilterMode.ITEMS || currentMode == FilterMode.BLOCKS) {
            for (Item item : BuiltInRegistries.ITEM) {
                boolean isBlock = item instanceof BlockItem;
                if (currentMode == FilterMode.BLOCKS && !isBlock) continue;
                if (currentMode == FilterMode.ITEMS && isBlock) continue;

                Identifier id = BuiltInRegistries.ITEM.getKey(item);
                if (id.toString().contains(lower) || new ItemStack(item).getHoverName().getString().toLowerCase().contains(lower)) {
                    searchedIcons.add(new ItemStackResult(new ItemStack(item)));
                    if (searchedIcons.size() > 500 && !lower.isEmpty()) break;
                }
            }
        }

        searchedIcons.sort(
                Comparator.comparing((IconResult r) -> {
                            String name = r.getDisplayName().toLowerCase();
                            return name.startsWith(lower) ? 0 : 1;
                        })
                        .thenComparingInt(r -> (r instanceof HypixelResult) ? 1 : 0)
                        .thenComparing(r -> r.getDisplayName().toLowerCase())
        );
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        if (isSavePanelOpen) {
            int panelW = 200;
            int panelH = 100;
            int panelX = (this.width - panelW) / 2;
            int panelY = (this.height - panelH) / 2;

            int closeSize = 12;
            int closeX = panelX + panelW - closeSize - 5;
            int closeY = panelY + 5;
            if (mouseX >= closeX && mouseX <= closeX + closeSize && mouseY >= closeY && mouseY <= closeY + closeSize) {
                isSavePanelOpen = false;
                saveProfileField.setFocused(false);
                return true;
            }

            boolean fieldClicked = saveProfileField.mouseClicked(click, doubled);
            saveProfileField.setFocused(fieldClicked);
            if(fieldClicked) return true;

            int btnW = 60;
            int btnH = 20;
            int btnX = panelX + (panelW - btnW) / 2;
            int btnY = panelY + 65;

            if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                String name = saveProfileField.getValue().trim();
                if (!name.isEmpty()) {
                    InventoryButtons.saveProfile(name);
                    actionStatusText = "Saved: " + name;
                    actionStatusEndTime = System.currentTimeMillis() + 3000;
                    isSavePanelOpen = false;
                    saveProfileField.setValue("");
                }
                return true;
            }

            if (mouseX >= panelX && mouseX <= panelX + panelW && mouseY >= panelY && mouseY <= panelY + panelH) {
                return true;
            }
            return true;
        }

        int btnH = 20;
        int spacing = 5;
        int startX = 10;
        int startY = 10;

        int saveW = 100;
        int saveY = startY;

        int exportW = 50;
        int exportY = saveY + btnH + spacing;

        int importW = 50;
        int importY = exportY + btnH + spacing;

        if (mouseX >= startX && mouseX <= startX + saveW && mouseY >= saveY && mouseY <= saveY + btnH) {
            isSavePanelOpen = true;
            saveProfileField.setFocused(true);
            return true;
        }

        if (mouseX >= startX && mouseX <= startX + exportW && mouseY >= exportY && mouseY <= exportY + btnH) {
            handleExport();
            return true;
        }

        if (mouseX >= startX && mouseX <= startX + importW && mouseY >= importY && mouseY <= importY + btnH) {
            handleImport();
            return true;
        }

        if (isInfoPanelOpen) {
            int panelW = 150;
            int panelH = 190;
            int panelX = 10;
            int panelY = (this.height - panelH) / 2;

            int closeSize = 10;
            int closeX = panelX + panelW - closeSize - 4;
            int closeY = panelY + 4;

            if (mouseX >= closeX && mouseX <= closeX + closeSize && mouseY >= closeY && mouseY <= closeY + closeSize) {
                isInfoPanelOpen = false;
                return true;
            }
            if (mouseX >= panelX && mouseX <= panelX + panelW && mouseY >= panelY && mouseY <= panelY + panelH) {
                return true;
            }
        }

        if (editingButton != null && isEditorOpen) {
            updateEditorCoordinates();

            int cmdY = editorTop + 19;
            int iconY = editorTop + 112;

            if (mouseX >= editorLeft && mouseX <= editorLeft + editorWidth &&
                    mouseY >= editorTop && mouseY <= editorTop + editorHeight) {

                boolean inCmd = (mouseX >= editorLeft + 7 && mouseX <= editorLeft + 7 + commandTextField.getWidth() &&
                        mouseY >= cmdY && mouseY <= cmdY + commandTextField.getHeight());
                boolean inIcon = (mouseX >= editorLeft + 7 && mouseX <= editorLeft + 7 + iconTextField.getWidth() &&
                        mouseY >= iconY && mouseY <= iconY + iconTextField.getHeight());

                boolean inSkull = false;
                if (currentMode == FilterMode.SKULLS) {
                    int skullTitleY = editorTop + 135;
                    int skullFieldY = editorTop + 147;

                    inSkull = (mouseX >= editorLeft + 7 && mouseX <= editorLeft + 7 + addSkullField.getWidth() &&
                            mouseY >= skullFieldY && mouseY <= skullFieldY + addSkullField.getHeight());

                    int iconSize = 10;
                    int infoIconX = editorLeft + 7 + font.width("Add Skull by ID") + 5;
                    int infoIconY = skullTitleY - 1;
                    if (mouseX >= infoIconX && mouseX < infoIconX + iconSize && mouseY >= infoIconY && mouseY < infoIconY + iconSize) {
                        isInfoPanelOpen = !isInfoPanelOpen;
                        return true;
                    }
                }

                commandTextField.setFocused(inCmd);
                iconTextField.setFocused(inIcon);
                addSkullField.setFocused(inSkull);

                if (inCmd) commandTextField.mouseClicked(click, doubled);
                if (inIcon) iconTextField.mouseClicked(click, doubled);
                if (inSkull) addSkullField.mouseClicked(click, doubled);

                if (mouseY >= editorTop + 52 && mouseY <= editorTop + 52 + 18) {
                    for(int i=0; i<5; i++) {
                        int bx = editorLeft + 7 + (i * 20);
                        if (mouseX >= bx && mouseX <= bx + 18) {
                            editingButton.backgroundIndex = i;
                            return true;
                        }
                    }
                }

                int filterY = editorTop + 75;
                int btnHeight = 20;
                if (mouseY >= filterY && mouseY <= filterY + btnHeight) {
                    int btnWidth = (editorWidth - 14) / FilterMode.values().length;
                    int index = (int)((mouseX - (editorLeft + 7)) / btnWidth);
                    if (index >= 0 && index < FilterMode.values().length) {
                        currentMode = FilterMode.values()[index];
                        search(iconTextField.getValue());
                        itemScroll.setTarget(0);
                        return true;
                    }
                }

                int listY = (currentMode == FilterMode.SKULLS) ? editorTop + 170 : editorTop + 135;
                int listH = (currentMode == FilterMode.SKULLS) ? 47 : 82;

                if (mouseY >= listY && mouseY <= listY + listH) {
                    handleListClick(mouseX, mouseY);
                    return true;
                }
                return true;
            }
        }

        for (InventoryButtons.CustomButtonData btn : InventoryButtons.instance.buttons) {
            int x = guiLeft + btn.x;
            int y = guiTop + btn.y;
            if (btn.anchorRight) x += xSize;
            if (btn.anchorBottom) y += ySize;

            if (mouseX >= x && mouseX <= x + 18 && mouseY >= y && mouseY <= y + 18) {
                if (editingButton != btn) {
                    editingButton = btn;
                    isEditorOpen = false;
                    commandTextField.setFocused(false);
                    iconTextField.setFocused(false);
                    addSkullField.setFocused(false);
                    isInfoPanelOpen = false;
                } else {
                    if (!isEditorOpen) {
                        isEditorOpen = true;
                        if (!btn.command.startsWith("/")) {
                            btn.command = "/" + btn.command;
                        }
                        commandTextField.setValue(btn.command);
                        commandTextField.moveCursorTo(btn.command.length(), false);
                        commandTextField.setFocused(true);
                        iconTextField.setFocused(false);
                        addSkullField.setFocused(false);
                        if (btn.itemId.startsWith("skull:")) {
                            addSkullField.setValue(btn.itemId);
                        } else {
                            addSkullField.setValue("");
                        }
                        currentMode = FilterMode.ALL;
                        search(iconTextField.getValue());
                    }
                }
                isDragging = true;
                dragOffsetX = (int)mouseX - x;
                dragOffsetY = (int)mouseY - y;
                return true;
            }
        }

        if (button == 1) {
            int relX = (int)mouseX - guiLeft;
            int relY = (int)mouseY - guiTop;
            InventoryButtons.CustomButtonData newBtn = new InventoryButtons.CustomButtonData(relX, relY, "/cmd", "minecraft:stone");
            InventoryButtons.instance.buttons.add(newBtn);
            editingButton = newBtn;
            isEditorOpen = true;
            commandTextField.setValue(newBtn.command);
            commandTextField.setFocused(true);
            isDragging = true;
            dragOffsetX = 9;
            dragOffsetY = 9;
            currentMode = FilterMode.ALL;
            search("");
            isInfoPanelOpen = false;
            return true;
        }

        editingButton = null;
        isDragging = false;
        isEditorOpen = false;
        isInfoPanelOpen = false;
        return super.mouseClicked(click, doubled);
    }

    private void handleListClick(double mouseX, double mouseY) {
        int listX = editorLeft + 7;
        int listY = (currentMode == FilterMode.SKULLS) ? editorTop + 170 : editorTop + 135;

        int cols = 6;
        int scroll = itemScroll.getValue();
        int startIndex = (scroll / 20) * cols;

        int relativeY = (int)mouseY - listY + (scroll % 20);
        int relativeX = (int)mouseX - listX - 2;

        int col = relativeX / 20;
        int row = relativeY / 20;

        if (col >= 0 && col < cols && row >= 0) {
            int index = startIndex + (row * cols) + col;
            if (index >= 0 && index < searchedIcons.size()) {
                String id = searchedIcons.get(index).getConfigId();
                editingButton.itemId = id;
                if (id.startsWith("skull:")) {
                    addSkullField.setValue(id);
                }
            }
        }
    }

    @Override
    public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent click) {
        isDragging = false;
        return super.mouseReleased(click);
    }

    private boolean isOverlapping(int x, int y) {
        for (InventoryButtons.CustomButtonData btn : InventoryButtons.instance.buttons) {
            if (btn == editingButton) continue;
            if (Math.abs(btn.x - x) < BUTTON_SIZE && Math.abs(btn.y - y) < BUTTON_SIZE) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent click, double deltaX, double deltaY) {
        double mouseX = click.x();
        double mouseY = click.y();

        if (isDragging && editingButton != null) {
            int newScreenX = (int)mouseX - dragOffsetX;
            int newScreenY = (int)mouseY - dragOffsetY;

            int relativeX = newScreenX - guiLeft;
            int relativeY = newScreenY - guiTop;

            if (editingButton.anchorRight) relativeX -= xSize;
            if (editingButton.anchorBottom) relativeY -= ySize;

            int proposedX = editingButton.x;
            int proposedY = editingButton.y;

            if (this.localGridSnap) {
                boolean isOutsideX = (relativeX < 0) || (relativeX > xSize - BUTTON_SIZE);
                boolean isOutsideY = (relativeY < 0) || (relativeY > ySize - BUTTON_SIZE);

                if (!isOutsideX && !isOutsideY) {
                    Point bestMatch = null;
                    double closestDistSq = Double.MAX_VALUE;
                    for (Point p : INVENTORY_FIXED_SLOTS) {
                        double distSq = Math.pow(relativeX - p.x, 2) + Math.pow(relativeY - p.y, 2);
                        if (distSq < closestDistSq) {
                            closestDistSq = distSq;
                            bestMatch = p;
                        }
                    }
                    if (bestMatch != null) {
                        proposedX = bestMatch.x;
                        proposedY = bestMatch.y;
                    }
                } else {
                    if (relativeX < 0) {
                        int col = (relativeX + OUTER_PADDING) / OUTER_GRID_SIZE;
                        proposedX = -OUTER_PADDING - BUTTON_SIZE + (col * OUTER_GRID_SIZE);
                    } else if (relativeX > xSize - BUTTON_SIZE) {
                        int col = (relativeX - xSize + OUTER_PADDING) / OUTER_GRID_SIZE;
                        proposedX = xSize + OUTER_PADDING + (col * OUTER_GRID_SIZE);
                    } else {
                        if (isOutsideY) {
                            int col = Math.round((float)(relativeX - TOP_BOTTOM_START_X) / OUTER_GRID_SIZE);
                            proposedX = TOP_BOTTOM_START_X + (col * OUTER_GRID_SIZE);
                        }
                    }

                    if (relativeY < 0) {
                        int row = (relativeY + OUTER_PADDING) / OUTER_GRID_SIZE;
                        proposedY = -OUTER_PADDING - BUTTON_SIZE + (row * OUTER_GRID_SIZE);
                    } else if (relativeY > ySize - BUTTON_SIZE) {
                        int row = (relativeY - ySize + OUTER_PADDING) / OUTER_GRID_SIZE;
                        proposedY = ySize + OUTER_PADDING + (row * OUTER_GRID_SIZE);
                    } else {
                        if (isOutsideX) {
                            int row = Math.round((float)(relativeY - OUTER_PADDING) / OUTER_GRID_SIZE);
                            proposedY = OUTER_PADDING + (row * OUTER_GRID_SIZE);
                        }
                    }
                }
            } else {
                proposedX = relativeX;
                proposedY = relativeY;

                boolean insideBottomZone = (relativeX + BUTTON_SIZE > 0 && relativeX < 176) &&
                        (relativeY + BUTTON_SIZE > 83 && relativeY < 166);

                boolean insideLeftZone = (relativeX + BUTTON_SIZE > 0 && relativeX < 26) &&
                        (relativeY + BUTTON_SIZE > 7 && relativeY < 83);

                if (insideBottomZone || insideLeftZone) {
                    Point bestMatch = null;
                    double closestDistSq = Double.MAX_VALUE;
                    for (Point p : INVENTORY_FIXED_SLOTS) {
                        double distSq = Math.pow(relativeX - p.x, 2) + Math.pow(relativeY - p.y, 2);
                        if (distSq < closestDistSq) {
                            closestDistSq = distSq;
                            bestMatch = p;
                        }
                    }
                    if (bestMatch != null) {
                        proposedX = bestMatch.x;
                        proposedY = bestMatch.y;
                    }
                }
            }

            if (!isOverlapping(proposedX, proposedY)) {
                editingButton.x = proposedX;
                editingButton.y = proposedY;
                updateEditorCoordinates();
            }

            return true;
        }

        if (editingButton != null && isEditorOpen && mouseX >= editorLeft && mouseX <= editorLeft + editorWidth) {
            int scroll = (int)(-deltaY * 10);
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (editingButton != null && isEditorOpen && mouseX >= editorLeft && mouseX <= editorLeft + editorWidth) {
            int scroll = (int)(-verticalAmount * 10);
            int target = itemScroll.getTarget() + scroll;
            int totalRows = (int) Math.ceil((double) searchedIcons.size() / 6.0);
            int listH = (currentMode == FilterMode.SKULLS) ? 47 : 82;
            int visibleRows = listH / 20;
            int maxScroll = Math.max(0, (totalRows - visibleRows) * 20);
            if (target < 0) target = 0;
            if (target > maxScroll) target = maxScroll;
            itemScroll.setTarget(target);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent input) {
        boolean inputsFocused = (isEditorOpen && (commandTextField.isFocused() || iconTextField.isFocused() || addSkullField.isFocused()))
                || (isSavePanelOpen && saveProfileField.isFocused());

        if (input.key() == GLFW.GLFW_KEY_S && !inputsFocused) {
            this.localGridSnap = !this.localGridSnap;
            return true;
        }

        if (isSavePanelOpen) {
            if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
                isSavePanelOpen = false;
                saveProfileField.setFocused(false);
                return true;
            }
            if (input.key() == GLFW.GLFW_KEY_ENTER || input.key() == GLFW.GLFW_KEY_KP_ENTER) {
                String name = saveProfileField.getValue().trim();
                if (!name.isEmpty()) {
                    InventoryButtons.saveProfile(name);
                    actionStatusText = "Saved: " + name;
                    actionStatusEndTime = System.currentTimeMillis() + 3000;
                    isSavePanelOpen = false;
                    saveProfileField.setValue("");
                }
                return true;
            }
            if (saveProfileField.keyPressed(input)) return true;
            return super.keyPressed(input);
        }

        if (editingButton != null) {
            if (isEditorOpen) {
                if (commandTextField.isFocused()) {
                    if (input.key() == GLFW.GLFW_KEY_BACKSPACE) {
                        String txt = commandTextField.getValue();
                        int cursor = commandTextField.getCursorPosition();
                        if (txt.equals("/") || (cursor <= 1 && commandTextField.getHighlighted().isEmpty())) {
                            return true;
                        }
                    }
                    return commandTextField.keyPressed(input);
                }
                if (iconTextField.isFocused()) return iconTextField.keyPressed(input);
                if (addSkullField.isFocused()) return addSkullField.keyPressed(input);
            }
            if (input.key() == GLFW.GLFW_KEY_DELETE || input.key() == GLFW.GLFW_KEY_BACKSPACE) {
                if (!isEditorOpen || (!commandTextField.isFocused() && !iconTextField.isFocused() && !addSkullField.isFocused())) {
                    InventoryButtons.instance.buttons.remove(editingButton);
                    editingButton = null;
                    isDragging = false;
                    isEditorOpen = false;
                    isInfoPanelOpen = false;
                    return true;
                }
            }
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(net.minecraft.client.input.CharacterEvent input) {
        if (isSavePanelOpen) {
            if (saveProfileField.charTyped(input)) return true;
            return super.charTyped(input);
        }
        if (editingButton != null && isEditorOpen) {
            if (commandTextField.isFocused() && commandTextField.charTyped(input)) {
                return true;
            }
            if (iconTextField.isFocused() && iconTextField.charTyped(input)) {
                search(iconTextField.getValue());
                return true;
            }
            if (addSkullField.isFocused() && addSkullField.charTyped(input)) {
                return true;
            }
        }
        return super.charTyped(input);
    }

    @Override
    public void onClose() {
        InventoryButtons.save();
        if (parent != null) minecraft.setScreen(parent);
        else super.onClose();
    }

    private static class LerpingInteger {
        private int value, target;
        public LerpingInteger(int value, int target) { this.value = value; this.target = target; }
        public void tick() {
            if (value != target) {
                int diff = target - value;
                int change = diff / 5;
                if (change == 0) change = diff > 0 ? 1 : -1;
                value += change;
            }
        }
        public int getValue() { return value; }
        public int getTarget() { return target; }
        public void setTarget(int t) { this.target = t; }
    }
}