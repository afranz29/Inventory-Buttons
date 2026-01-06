package com.panda.inventorybuttons.mixin.client;

import com.panda.inventorybuttons.InventoryButtons;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(HandledScreen.class)
public abstract class MixinHandledScreen extends Screen {

    private static final Identifier BUTTONS_TEXTURE = Identifier.of("inventorybuttons", "textures/gui/buttons.png");
    private static final int STANDARD_INV_HEIGHT = 166;
    private static final int STANDARD_INV_WIDTH = 176;

    protected MixinHandledScreen(Text title) { super(title); }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderInvButtons(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!((Object)this instanceof HandledScreen)) return;

        if (!InventoryButtons.instance.enabled) return;

        if (InventoryButtons.instance.hideInCreative && this.client != null && this.client.interactionManager != null && this.client.interactionManager.getCurrentGameMode().isCreative()) {
            return;
        }

        HandledScreenAccessor accessor = (HandledScreenAccessor) this;
        int guiLeft = accessor.getXPosition();
        int guiTop = accessor.getYPosition();
        int xSize = accessor.getBackgroundWidth();
        int ySize = accessor.getBackgroundHeight();

        boolean isPlayerInventory = ((Object)this instanceof InventoryScreen);
        int containerOffset = ySize - STANDARD_INV_HEIGHT;

        InventoryButtons.CustomButtonData hoveredBtn = null;

        for (InventoryButtons.CustomButtonData btn : InventoryButtons.instance.buttons) {
            int renderOffsetY = 0;

            if (!isPlayerInventory) {
                if (!btn.anchorBottom) {
                    // --- Logic for Top Buttons (Y < 80) ---
                    if (btn.y < 80) {
                        // Check if it's an "Internal" button (Crafting/Armor/Player Model)

                        boolean isInsideX = (btn.x >= 0 && btn.x <= STANDARD_INV_WIDTH);
                        boolean isInsideY = (btn.y >= 0);

                        if (!btn.anchorRight && isInsideX && isInsideY) {
                            continue; // hide
                        }
                        // If it's above (y < 0) or to the side, it stays visible at renderOffsetY = 0 (pinned to top)
                    }
                    // --- Logic for Inventory Buttons (Y >= 80) ---
                    else {
                        // Shift down to align with the player inventory in this container
                        renderOffsetY = containerOffset;
                    }
                }
            }

            int x = guiLeft + btn.x;
            int y = guiTop + btn.y + renderOffsetY;

            if (btn.anchorRight) x += xSize;
            if (btn.anchorBottom) y += ySize;

            context.drawTexture(RenderLayer::getGuiOpaqueTexturedBackground, BUTTONS_TEXTURE, x, y,
                    (float)(btn.backgroundIndex * 18), 18.0F, 18, 18, 90, 36);

            Identifier customTex = null;
            if (btn.itemId != null) {
                for(Map.Entry<String, Identifier> entry : InventoryButtons.CUSTOM_TEXTURES.entrySet()) {
                    if (entry.getValue().toString().equals(btn.itemId)) {
                        customTex = entry.getValue();
                        break;
                    }
                }
            }

            if (customTex != null) {
                context.drawTexture(RenderLayer::getGuiTextured, customTex, x + 1, y + 1, 0, 0, 16, 16, 16, 16);
            } else {
                ItemStack stack = btn.getItemStack();
                if (!stack.isEmpty()) context.drawItem(stack, x + 1, y + 1);
                else context.drawCenteredTextWithShadow(textRenderer, "?", x + 9, y + 5, 0xFFFFFFFF);
            }

            if (mouseX >= x && mouseX <= x + 18 && mouseY >= y && mouseY <= y + 18) {
                hoveredBtn = btn;
            }
        }

        if (hoveredBtn != null) {
            int renderOffsetY = 0;
            if (!isPlayerInventory && !hoveredBtn.anchorBottom && hoveredBtn.y >= 80) {
                renderOffsetY = containerOffset;
            }

            int finalX = guiLeft + hoveredBtn.x + (hoveredBtn.anchorRight ? xSize : 0);
            int finalY = guiTop + hoveredBtn.y + renderOffsetY + (hoveredBtn.anchorBottom ? ySize : 0);

            context.drawBorder(finalX, finalY, 18, 18, 0xFFFFFFFF);

            if (InventoryButtons.instance.showTooltips) {
                context.drawTooltip(textRenderer, Text.literal(hoveredBtn.command), mouseX, mouseY);
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!InventoryButtons.instance.enabled) return;

        if (InventoryButtons.instance.hideInCreative && this.client != null && this.client.interactionManager != null && this.client.interactionManager.getCurrentGameMode().isCreative()) {
            return;
        }

        if (button == 0 && (Object)this instanceof HandledScreen) {
            HandledScreenAccessor accessor = (HandledScreenAccessor) this;
            int guiLeft = accessor.getXPosition();
            int guiTop = accessor.getYPosition();
            int xSize = accessor.getBackgroundWidth();
            int ySize = accessor.getBackgroundHeight();

            boolean isPlayerInventory = ((Object)this instanceof InventoryScreen);
            int containerOffset = ySize - STANDARD_INV_HEIGHT;

            for (InventoryButtons.CustomButtonData btn : InventoryButtons.instance.buttons) {
                int renderOffsetY = 0;

                if (!isPlayerInventory) {
                    if (!btn.anchorBottom) {
                        if (btn.y < 80) {
                            boolean isInsideX = (btn.x >= 0 && btn.x <= STANDARD_INV_WIDTH);
                            boolean isInsideY = (btn.y >= 0);

                            if (!btn.anchorRight && isInsideX && isInsideY) {
                                continue;
                            }
                        } else {
                            renderOffsetY = containerOffset;
                        }
                    }
                }

                int btnX = guiLeft + btn.x;
                int btnY = guiTop + btn.y + renderOffsetY;

                if (btn.anchorRight) btnX += xSize;
                if (btn.anchorBottom) btnY += ySize;

                if (mouseX >= btnX && mouseX <= btnX + 18 && mouseY >= btnY && mouseY <= btnY + 18) {
                    if (this.client != null && this.client.player != null) {
                        String cmd = btn.command;
                        if (cmd.startsWith("/")) cmd = cmd.substring(1);
                        this.client.player.networkHandler.sendChatCommand(cmd);
                        this.client.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.master(net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    }

                    cir.setReturnValue(true);
                    return;
                }
            }
        }
    }
}