/*
 * Copyright (C) 2026 Panda/afranz29
 * This file is part of Inventory-Buttons, licensed under the LGPLv3.
 */

package com.panda.inventorybuttons.util;

import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.InputConstants;
import com.panda.inventorybuttons.InventoryButtons;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.nio.file.Path;

public class MinecraftCompat {

    // Read at runtime: javac would inline different GLFW/SDL values into a shared JAR.
    public static final int KEY_TAB = inputConstant("KEY_TAB");
    public static final int KEY_UP = inputConstant("KEY_UP");
    public static final int KEY_DOWN = inputConstant("KEY_DOWN");
    public static final int KEY_RETURN = inputConstant("KEY_RETURN");
    public static final int KEY_NUMPADENTER = inputConstant("KEY_NUMPADENTER");
    public static final int KEY_ESCAPE = inputConstant("KEY_ESCAPE");
    public static final int KEY_S = inputConstant("KEY_S");
    public static final int KEY_BACKSPACE = inputConstant("KEY_BACKSPACE");
    public static final int KEY_DELETE = inputConstant("KEY_DELETE");
    public static final int MOUSE_BUTTON_LEFT = inputConstant("MOUSE_BUTTON_LEFT");
    public static final int MOUSE_BUTTON_RIGHT = inputConstant("MOUSE_BUTTON_RIGHT");

    private static int inputConstant(String name) {
        try {
            return InputConstants.class.getField(name).getInt(null);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static void openPath(Path path) {
        try {
            try {
                Blaze3D.class.getMethod("openPath", Path.class).invoke(null, path);
            } catch (NoSuchMethodException e) {
                Util.OS.class.getMethod("openPath", Path.class).invoke(Util.getPlatform(), path);
            }
        } catch (ReflectiveOperationException e) {
            InventoryButtons.LOGGER.error("Failed to open config folder", e);
        }
    }

    public static void blit(GuiGraphicsExtractor context, Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        // This overload avoids the RenderPipeline type moved in 26.3.
        context.blit(texture, x, y, x + width, y + height,
                u / textureWidth, (u + width) / textureWidth,
                v / textureHeight, (v + height) / textureHeight);
    }
}
