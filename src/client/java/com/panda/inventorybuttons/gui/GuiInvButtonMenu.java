package com.panda.inventorybuttons.gui;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GuiInvButtonMenu extends Screen {

    // List to hold our custom button objects
    private final List<MenuButton> menuButtons = new ArrayList<>();

    public GuiInvButtonMenu() {
        super(Text.literal("Inventory Buttons"));
    }

    @Override
    protected void init() {
        menuButtons.clear();
        int btnWidth = 200;
        int btnHeight = 20;
        int spacing = 24;

        int startY = this.height / 4 + 40;
        int centerX = this.width / 2 - btnWidth / 2;

        // Config Button
        menuButtons.add(new MenuButton(centerX, startY, btnWidth, btnHeight, "Config", () -> {
            if (this.client != null) {
                this.client.setScreen(new GuiInvButtonConfig(this));
            }
        }));

        // Edit Buttons Button
        menuButtons.add(new MenuButton(centerX, startY + spacing, btnWidth, btnHeight, "Edit Buttons", () -> {
            if (this.client != null) {
                this.client.setScreen(new GuiInvButtonEditor(this));
            }
        }));

        // Open Config Folder Button
        menuButtons.add(new MenuButton(centerX, startY + spacing * 2, btnWidth, btnHeight, "Open Config Folder", () -> {
            Path configDir = FabricLoader.getInstance().getConfigDir().resolve("inventorybuttons");
            File file = configDir.toFile();
            if (!file.exists()) {
                file.mkdirs();
            }
            Util.getOperatingSystem().open(file);
        }));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        // --- Render Title ---
        context.getMatrices().push();

        context.getMatrices().translate(this.width / 2.0f, 60.0f, 0.0f);
        float scale = 2.0f;
        context.getMatrices().scale(scale, scale, 1.0f);

        int titleWidth = this.textRenderer.getWidth(this.title);
        context.drawText(this.textRenderer, this.title, -titleWidth / 2, -this.textRenderer.fontHeight / 2, 0xFFFFFFFF, true);

        context.getMatrices().pop();

        // --- Render Custom Buttons ---
        // draw each button in list
        for (MenuButton btn : menuButtons) {
            boolean isHovered = mouseX >= btn.x && mouseX < btn.x + btn.width &&
                    mouseY >= btn.y && mouseY < btn.y + btn.height;

            // Style: Darker transparent black
            // Normal: 0x80 (50% opacity) | Hover: 0xA0 (62% opacity)
            int color = isHovered ? 0xA0000000 : 0x80000000;

            context.fill(btn.x, btn.y, btn.x + btn.width, btn.y + btn.height, color);

            // button
            int textWidth = this.textRenderer.getWidth(btn.label);
            int textX = btn.x + (btn.width - textWidth) / 2;
            int textY = btn.y + (btn.height - this.textRenderer.fontHeight) / 2;

            // label
            context.drawText(this.textRenderer, btn.label, textX, textY, 0xFFFFFFFF, false);
        }

        // Do NOT call super.render() to avoid drawing default widget layers if any exist
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left Click
            for (MenuButton btn : menuButtons) {
                if (mouseX >= btn.x && mouseX < btn.x + btn.width &&
                        mouseY >= btn.y && mouseY < btn.y + btn.height) {

                    // play click sound
                    if (this.client != null) {
                        this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    }
                    // Run action
                    btn.action.run();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    // Helper Record for Custom Buttons
    private record MenuButton(int x, int y, int width, int height, String label, Runnable action) {}
}