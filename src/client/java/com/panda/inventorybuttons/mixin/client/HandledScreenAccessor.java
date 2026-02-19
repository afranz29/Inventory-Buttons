package com.panda.inventorybuttons.mixin.client;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {
    // This tells Fabric: "Find the field named 'x' and let me read it with this method"
    @Accessor("x")
    int getXPosition();

    // Find 'y'
    @Accessor("y")
    int getYPosition();

    // Accessors for dynamic GUI sizing
    @Accessor("backgroundWidth")
    int getBackgroundWidth();

    @Accessor("backgroundHeight")
    int getBackgroundHeight();
}