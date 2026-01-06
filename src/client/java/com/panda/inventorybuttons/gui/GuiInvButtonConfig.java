package com.panda.inventorybuttons.gui;

import com.panda.inventorybuttons.InventoryButtons;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class GuiInvButtonConfig extends Screen {

    private static final Identifier TRASH_ICON = Identifier.of("inventorybuttons", "textures/gui/icons/delete_trashcan.png");

    private final Screen parent;
    private Tab currentTab = Tab.GENERAL;

    // Layout
    private int boxX, boxY, boxW, boxH;

    // Components
    private final List<ConfigToggle> generalButtons = new ArrayList<>();
    private final List<ProfileButton> profileButtons = new ArrayList<>();
    private final List<ProfileButton> deleteButtons = new ArrayList<>();
    private TextFieldWidget profileNameField;
    private ProfileButton profileSaveBtn;

    // Scrolling for profiles
    private final LerpingInteger profileScroll = new LerpingInteger(0, 0);

    // Status Text Logic (Time-based)
    private String statusText = "";
    private long statusEndTime = 0;

    public GuiInvButtonConfig(Screen parent) {
        super(Text.literal("Configuration"));
        this.parent = parent;
    }

    private enum Tab { GENERAL, PROFILES }

    @Override
    protected void init() {
        // Define Box Dimensions
        boxW = 240;
        boxH = 160;
        boxX = (this.width - boxW) / 2;
        boxY = (this.height - boxH) / 2;

        // --- General Tab ---
        generalButtons.clear();
        int startY = boxY + 30; // padding below tabs
        int spacing = 22;
        int btnW = 50;
        int btnH = 16;
        int btnX = boxX + boxW - btnW - 10; // Right align

        addGeneralSetting(boxX + 10, startY, btnX, btnW, btnH, "Inventory Buttons",
                () -> InventoryButtons.instance.enabled, v -> InventoryButtons.instance.enabled = v);
        startY += spacing;

        addGeneralSetting(boxX + 10, startY, btnX, btnW, btnH, "Show Tooltips",
                () -> InventoryButtons.instance.showTooltips, v -> InventoryButtons.instance.showTooltips = v);
        startY += spacing;

        addGeneralSetting(boxX + 10, startY, btnX, btnW, btnH, "Hide in Creative Mode",
                () -> InventoryButtons.instance.hideInCreative, v -> InventoryButtons.instance.hideInCreative = v);
        startY += spacing;

        addGeneralSetting(boxX + 10, startY, btnX, btnW, btnH, "Grid Snap Default",
                () -> InventoryButtons.instance.gridSnap, v -> InventoryButtons.instance.gridSnap = v);

        // --- Profiles Tab ---
        // TextField
        int fieldW = 140;
        this.profileNameField = new TextFieldWidget(this.textRenderer, boxX + 10, boxY + 42, fieldW, 16, Text.literal("Profile Name"));
        this.profileNameField.setMaxLength(32);

        // Save Button
        this.profileSaveBtn = new ProfileButton(boxX + 10 + fieldW + 5, boxY + 42, 75, 16, "Save Profile", () -> {
            String name = profileNameField.getText().trim();
            if (!name.isEmpty()) {
                InventoryButtons.saveProfile(name);
                setStatus("Saved: " + name);
                refreshProfileList();
                profileNameField.setText("");
            }
        });

        refreshProfileList();
    }

    private void addGeneralSetting(int labelX, int y, int btnX, int btnW, int btnH, String label, BooleanSupplier get, BooleanConsumer set) {
        generalButtons.add(new ConfigToggle(labelX, y, btnX, btnW, btnH, label, get, set));
    }

    private void refreshProfileList() {
        profileButtons.clear();
        deleteButtons.clear();
        List<String> profiles = InventoryButtons.getProfileNames();
        int itemH = 18;
        int listY = 0;

        int deleteW = 18;
        int spacing = 4;
        int loadW = boxW - 30 - deleteW - spacing;

        for (String p : profiles) {
            String fName = p;

            // Load Button
            profileButtons.add(new ProfileButton(boxX + 10, listY, loadW, itemH, "Load: " + p, () -> {
                InventoryButtons.loadProfile(fName);
                setStatus("Loaded: " + fName);
            }));

            // Delete Button
            deleteButtons.add(new ProfileButton(boxX + 10 + loadW + spacing, listY, deleteW, itemH, "", () -> {
                InventoryButtons.deleteProfile(fName);
                setStatus("Deleted: " + fName);
                refreshProfileList();
            }));

            listY += itemH + 2;
        }
    }

    private void setStatus(String msg) {
        this.statusText = msg;
        this.statusEndTime = System.currentTimeMillis() + 3000;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        // Title
        context.getMatrices().push();
        context.getMatrices().translate(this.width / 2.0f, boxY - 25, 0.0f);
        float scale = 1.5f;
        context.getMatrices().scale(scale, scale, 1.0f);
        String titleText = "Configuration";
        int tW = this.textRenderer.getWidth(titleText);
        context.drawText(this.textRenderer, titleText, -tW / 2, -this.textRenderer.fontHeight / 2, 0xFFFFFFFF, true);
        context.getMatrices().pop();

        // Background
        context.fill(boxX, boxY, boxX + boxW, boxY + boxH, 0xAA000000);
        context.drawBorder(boxX, boxY, boxW, boxH, 0xFF505050);

        // Render Tabs
        int tabH = 16;
        int tabW = boxW / 2;

        // Tab 1: General
        boolean hoverGen = (mouseX >= boxX && mouseX < boxX + tabW && mouseY >= boxY && mouseY < boxY + tabH);
        int colorGen = (currentTab == Tab.GENERAL) ? 0xCC404040 : (hoverGen ? 0xAA303030 : 0x88202020);
        context.fill(boxX, boxY, boxX + tabW, boxY + tabH, colorGen);
        context.drawCenteredTextWithShadow(textRenderer, "General", boxX + tabW / 2, boxY + 4, (currentTab == Tab.GENERAL) ? 0xFFFFFF : 0xAAAAAA);

        // Tab 2: Profiles
        boolean hoverProf = (mouseX >= boxX + tabW && mouseX < boxX + boxW && mouseY >= boxY && mouseY < boxY + tabH);
        int colorProf = (currentTab == Tab.PROFILES) ? 0xCC404040 : (hoverProf ? 0xAA303030 : 0x88202020);
        context.fill(boxX + tabW, boxY, boxX + boxW, boxY + tabH, colorProf);
        context.drawCenteredTextWithShadow(textRenderer, "Profiles", boxX + tabW + tabW / 2, boxY + 4, (currentTab == Tab.PROFILES) ? 0xFFFFFF : 0xAAAAAA);

        context.fill(boxX, boxY + tabH, boxX + boxW, boxY + tabH + 1, 0xFF808080);

        // Render Content
        if (currentTab == Tab.GENERAL) {
            renderGeneral(context, mouseX, mouseY);
        } else {
            renderProfiles(context, mouseX, mouseY);
        }
    }

    private void renderGeneral(DrawContext context, int mouseX, int mouseY) {
        for (ConfigToggle btn : generalButtons) {
            context.drawTextWithShadow(textRenderer, btn.label, btn.labelX, btn.y + 4, 0xFFFFFFFF);

            boolean isHovered = mouseX >= btn.btnX && mouseX < btn.btnX + btn.btnW && mouseY >= btn.y && mouseY < btn.y + btn.btnH;
            int bgColor = isHovered ? 0xA0000000 : 0x80000000;
            context.fill(btn.btnX, btn.y, btn.btnX + btn.btnW, btn.y + btn.btnH, bgColor);

            boolean state = btn.getter.get();
            String stateTxt = state ? "ON" : "OFF";
            int color = state ? 0xFF55FF55 : 0xFFFF5555;

            int txtW = textRenderer.getWidth(stateTxt);
            context.drawText(textRenderer, stateTxt, btn.btnX + (btn.btnW - txtW) / 2, btn.y + 4, color, false);
        }
    }

    private void renderProfiles(DrawContext context, int mouseX, int mouseY) {
        // Render "Profile Name" label
        context.drawTextWithShadow(textRenderer, "Profile Name", boxX + 10, boxY + 30, 0xFFA0A0A0);

        this.profileNameField.render(context, mouseX, mouseY, 0);

        // Save Button
        ProfileButton saveBtn = this.profileSaveBtn;
        boolean hoverSave = mouseX >= saveBtn.x && mouseX < saveBtn.x + saveBtn.w && mouseY >= saveBtn.y && mouseY < saveBtn.y + saveBtn.h;
        context.fill(saveBtn.x, saveBtn.y, saveBtn.x + saveBtn.w, saveBtn.y + saveBtn.h, hoverSave ? 0xA0000000 : 0x80000000);
        int saveTW = textRenderer.getWidth(saveBtn.label);
        context.drawText(textRenderer, saveBtn.label, saveBtn.x + (saveBtn.w - saveTW) / 2, saveBtn.y + 4, 0xFFFFFFFF, false);

        // Status
        if (System.currentTimeMillis() < statusEndTime) {
            context.drawTextWithShadow(textRenderer, statusText, boxX + 10, boxY + boxH - 12, 0xFF55FF55);
        }

        // List Area
        int listY = boxY + 70;
        int listH = boxH - 85;
        int listW = boxW - 20;

        context.enableScissor(boxX + 10, listY, boxX + 10 + listW, listY + listH);
        profileScroll.tick();

        int currentY = listY - profileScroll.getValue();

        // Render List Buttons
        for (int i = 0; i < profileButtons.size(); i++) {
            ProfileButton btn = profileButtons.get(i);
            ProfileButton delBtn = deleteButtons.get(i);

            if (currentY + btn.h < listY) {
                currentY += btn.h + 2;
                continue;
            }
            if (currentY > listY + listH) break;

            int drawY = currentY;

            // --- Load Button ---
            boolean isHovered = mouseX >= btn.x && mouseX < btn.x + btn.w && mouseY >= drawY && mouseY < drawY + btn.h;
            if (mouseY < listY || mouseY > listY + listH) isHovered = false;

            context.fill(btn.x, drawY, btn.x + btn.w, drawY + btn.h, isHovered ? 0xA0000000 : 0x80000000);
            context.drawText(textRenderer, btn.label, btn.x + 5, drawY + 5, 0xFFFFFFFF, false);

            // --- Delete Button ---
            boolean isDelHovered = mouseX >= delBtn.x && mouseX < delBtn.x + delBtn.w && mouseY >= drawY && mouseY < drawY + delBtn.h;
            if (mouseY < listY || mouseY > listY + listH) isDelHovered = false;

            context.fill(delBtn.x, drawY, delBtn.x + delBtn.w, drawY + delBtn.h, isDelHovered ? 0xA0000000 : 0x80000000);

            // Draw Trashcan Icon
            context.drawTexture(RenderLayer::getGuiTextured, TRASH_ICON, delBtn.x + 1, drawY + 1, 0, 0, 16, 16, 16, 16);

            currentY += btn.h + 2;
        }

        context.disableScissor();

        int totalH = profileButtons.size() * 20;
        if (totalH > listH) {
            int barH = (int)((float)listH / totalH * listH);
            if (barH < 10) barH = 10;
            int barTrackH = listH;
            float ratio = (float)profileScroll.getValue() / (totalH - listH);
            int barY = listY + (int)((barTrackH - barH) * ratio);
            context.fill(boxX + boxW - 8, barY, boxX + boxW - 4, barY + barH, 0xFF808080);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int tabW = boxW / 2;
            if (mouseY >= boxY && mouseY < boxY + 16) {
                if (mouseX >= boxX && mouseX < boxX + tabW) {
                    currentTab = Tab.GENERAL;
                    if(profileNameField != null) profileNameField.setFocused(false);
                    if(this.client != null) this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return true;
                }
                if (mouseX >= boxX + tabW && mouseX < boxX + boxW) {
                    currentTab = Tab.PROFILES;
                    if(this.client != null) this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return true;
                }
            }

            if (currentTab == Tab.GENERAL) {
                for (ConfigToggle btn : generalButtons) {
                    if (mouseX >= btn.btnX && mouseX < btn.btnX + btn.btnW && mouseY >= btn.y && mouseY < btn.y + btn.btnH) {
                        btn.setter.accept(!btn.getter.get());
                        if(this.client != null) this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        return true;
                    }
                }
            } else {
                boolean fieldClicked = profileNameField.mouseClicked(mouseX, mouseY, button);
                profileNameField.setFocused(fieldClicked);
                if (fieldClicked) return true;

                if (mouseX >= profileSaveBtn.x && mouseX < profileSaveBtn.x + profileSaveBtn.w && mouseY >= profileSaveBtn.y && mouseY < profileSaveBtn.y + profileSaveBtn.h) {
                    profileSaveBtn.action.run();
                    if(this.client != null) this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return true;
                }

                int listY = boxY + 70; // Adjusted for label
                int listH = boxH - 85;
                if (mouseY >= listY && mouseY <= listY + listH) {
                    int currentY = listY - profileScroll.getValue();
                    for (int i = 0; i < profileButtons.size(); i++) {
                        ProfileButton btn = profileButtons.get(i);
                        ProfileButton delBtn = deleteButtons.get(i);

                        if (mouseX >= btn.x && mouseX < btn.x + btn.w && mouseY >= currentY && mouseY < currentY + btn.h) {
                            btn.action.run();
                            if(this.client != null) this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                            return true;
                        }

                        if (mouseX >= delBtn.x && mouseX < delBtn.x + delBtn.w && mouseY >= currentY && mouseY < currentY + delBtn.h) {
                            delBtn.action.run();
                            if(this.client != null) this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                            return true;
                        }

                        currentY += btn.h + 2;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (currentTab == Tab.PROFILES) {
            int scroll = (int)(-verticalAmount * 10);
            int target = profileScroll.getTarget() + scroll;
            int totalH = profileButtons.size() * 20;
            int listH = boxH - 85;
            int maxScroll = Math.max(0, totalH - listH);

            if (target < 0) target = 0;
            if (target > maxScroll) target = maxScroll;
            profileScroll.setTarget(target);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && !profileNameField.isFocused()) {
            this.close();
            return true;
        }
        if (currentTab == Tab.PROFILES && profileNameField.isFocused()) {
            if (profileNameField.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (currentTab == Tab.PROFILES && profileNameField.isFocused()) {
            if (profileNameField.charTyped(chr, modifiers)) return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void close() {
        InventoryButtons.save();
        if (this.client != null) this.client.setScreen(parent);
    }

    interface BooleanSupplier { boolean get(); }
    interface BooleanConsumer { void accept(boolean val); }

    private record ConfigToggle(int labelX, int y, int btnX, int btnW, int btnH, String label, BooleanSupplier getter, BooleanConsumer setter) {}
    private record ProfileButton(int x, int y, int w, int h, String label, Runnable action) {}

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