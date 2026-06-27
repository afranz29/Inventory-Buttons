/*
 * Copyright (C) 2026 Panda/afranz29
 * This file is part of Inventory-Buttons, licensed under the LGPLv3.
 */

package com.panda.inventorybuttons.mixin.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface HandledScreenAccessor {
    // This tells Fabric: "Find the field named 'x' and let me read it with this method"
    @Accessor("leftPos")
    int getXPosition();

    // Find 'y'
    @Accessor("topPos")
    int getYPosition();

    // Accessors for dynamic GUI sizing
    @Accessor("imageWidth")
    int getBackgroundWidth();

    @Accessor("imageHeight")
    int getBackgroundHeight();
}
