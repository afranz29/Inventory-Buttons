/*
 * Copyright (C) 2026 Panda/afranz29
 * This file is part of Inventory-Buttons, licensed under the LGPLv3.
 */

package com.panda.inventorybuttons.gui;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Util;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GuiInvButtonMenu extends Screen {

    private final List<MenuButton> menuButtons = new ArrayList<>();

    public GuiInvButtonMenu() {
        super(Component.literal("Inventory Buttons"));
    }

    @Override
    protected void init() {
        menuButtons.clear();
        int btnWidth = 200;
        int btnHeight = 20;
        int spacing = 24;

        int startY = this.height / 4 + 40;
        int centerX = this.width / 2 - btnWidth / 2;

        menuButtons.add(new MenuButton(centerX, startY, btnWidth, btnHeight, "Config", () -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new GuiInvButtonConfig(this));
            }
        }));

        menuButtons.add(new MenuButton(centerX, startY + spacing, btnWidth, btnHeight, "Edit Buttons", () -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new GuiInvButtonEditor(this));
            }
        }));

        menuButtons.add(new MenuButton(centerX, startY + spacing * 2, btnWidth, btnHeight, "Open Config Folder", () -> {
            Path configDir = FabricLoader.getInstance().getConfigDir().resolve("inventorybuttons");
            File file = configDir.toFile();
            if (!file.exists()) {
                file.mkdirs();
            }
            Util.getPlatform().openFile(file);
        }));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.pose().pushMatrix();
        context.pose().translate(this.width / 2.0f, 60.0f);
        float scale = 2.0f;
        context.pose().scale(scale, scale);

        int titleWidth = this.font.width(this.title);
        context.text(this.font, this.title, -titleWidth / 2, -this.font.lineHeight / 2, 0xFFFFFFFF, true);
        context.pose().popMatrix();

        for (MenuButton btn : menuButtons) {
            boolean isHovered = mouseX >= btn.x && mouseX < btn.x + btn.width &&
                    mouseY >= btn.y && mouseY < btn.y + btn.height;

            int color = isHovered ? 0xA0000000 : 0x80000000;
            context.fill(btn.x, btn.y, btn.x + btn.width, btn.y + btn.height, color);

            int textWidth = this.font.width(btn.label);
            int textX = btn.x + (btn.width - textWidth) / 2;
            int textY = btn.y + (btn.height - this.font.lineHeight) / 2;
            context.text(this.font, Component.literal(btn.label), textX, textY, 0xFFFFFFFF, false);
        }
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        if (button == 0) {
            for (MenuButton btn : menuButtons) {
                if (mouseX >= btn.x && mouseX < btn.x + btn.width &&
                        mouseY >= btn.y && mouseY < btn.y + btn.height) {

                    if (this.minecraft != null) {
                        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    }
                    btn.action.run();
                    return true;
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    private record MenuButton(int x, int y, int width, int height, String label, Runnable action) {}
}
