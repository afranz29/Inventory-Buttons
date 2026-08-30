/*
 * Copyright (C) 2026 Panda/afranz29
 * This file is part of Inventory-Buttons, licensed under the LGPLv3.
 */

package com.panda.inventorybuttons.gui;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Tab completion for a command text field, backed by the command tree the server
 * sends to the client, so server commands complete too (including argument
 * suggestions the server answers asynchronously).
 *
 * <p>Typing refreshes the suggestion list, Tab applies the highlighted entry and
 * further Tab presses cycle through the alternatives, arrow keys move the
 * highlight, Escape closes the popup.
 */
public class CommandSuggestor {

    private static final int MAX_VISIBLE = 7;
    private static final int ENTRY_HEIGHT = 12;

    private static final int BACKGROUND_COLOR = 0xEE100010;
    private static final int BORDER_COLOR = 0xFF505050;
    private static final int TEXT_COLOR = 0xFFAAAAAA;
    private static final int SELECTED_COLOR = 0xFFFFFF55;
    private static final int HIGHLIGHT_COLOR = 0x60FFFFFF;

    private final MinecraftClient client;
    private final TextFieldWidget field;

    private CompletableFuture<Suggestions> pending;
    private String pendingInput = "";

    private List<Suggestion> entries = Collections.emptyList();
    private String entryInput = "";
    private int entryRangeStart;

    private int selectedIndex;
    private int firstVisible;
    private boolean visible;
    private boolean tabCycles;
    private boolean applying;

    private int windowX, windowY, windowW, windowH;

    public CommandSuggestor(MinecraftClient client, TextFieldWidget field) {
        this.client = client;
        this.field = field;
    }

    /** Asks the command tree for completions of the current field contents. */
    public void refresh() {
        if (applying) return;
        hide();

        ClientPlayNetworkHandler handler = client != null ? client.getNetworkHandler() : null;
        if (handler == null) return;

        String text = field.getText();
        StringReader reader = new StringReader(text);
        if (!reader.canRead() || reader.peek() != '/') return;
        reader.skip();

        try {
            CommandDispatcher<ClientCommandSource> dispatcher = handler.getCommandDispatcher();
            ParseResults<ClientCommandSource> parse = dispatcher.parse(reader, handler.getCommandSource());
            pendingInput = text;
            pending = dispatcher.getCompletionSuggestions(parse, field.getCursor());
        } catch (Exception e) {
            pending = null;
        }
    }

    public void hide() {
        pending = null;
        entries = Collections.emptyList();
        entryInput = "";
        selectedIndex = 0;
        firstVisible = 0;
        visible = false;
        tabCycles = false;
        field.setSuggestion(null);
    }

    public boolean isOpen() {
        return visible && !entries.isEmpty();
    }

    private void pollPending() {
        if (pending == null || !pending.isDone()) return;

        Suggestions result = null;
        try {
            result = pending.join();
        } catch (Exception ignored) {
        }
        pending = null;

        if (result == null || result.isEmpty() || !field.getText().equals(pendingInput)) {
            entries = Collections.emptyList();
            visible = false;
            field.setSuggestion(null);
            return;
        }

        entries = result.getList();
        entryInput = pendingInput;
        entryRangeStart = result.getRange().getStart();
        selectedIndex = 0;
        firstVisible = 0;
        visible = true;
        tabCycles = false;
        updateGhostText();
    }

    private void updateGhostText() {
        if (!isOpen()) {
            field.setSuggestion(null);
            return;
        }
        String text = field.getText();
        String completed = entries.get(selectedIndex).apply(entryInput);
        field.setSuggestion(completed.startsWith(text) && completed.length() > text.length()
                ? completed.substring(text.length())
                : null);
    }

    private void cycle(int delta) {
        selectedIndex = Math.floorMod(selectedIndex + delta, entries.size());
        if (selectedIndex < firstVisible) {
            firstVisible = selectedIndex;
        } else if (selectedIndex >= firstVisible + MAX_VISIBLE) {
            firstVisible = selectedIndex - MAX_VISIBLE + 1;
        }
        updateGhostText();
    }

    /** Writes the highlighted suggestion into the field. */
    private boolean applySelected() {
        if (!isOpen()) return false;

        Suggestion suggestion = entries.get(selectedIndex);
        String completed;
        try {
            completed = suggestion.apply(entryInput);
        } catch (Exception e) {
            return false;
        }

        // Keep the current suggestion list alive so repeated Tab presses cycle
        // through the alternatives instead of completing the same entry again.
        applying = true;
        field.setText(completed);
        applying = false;

        int cursor = Math.min(suggestion.getRange().getStart() + suggestion.getText().length(), field.getText().length());
        field.setCursor(cursor, false);
        tabCycles = true;
        updateGhostText();
        return true;
    }

    public boolean keyPressed(KeyInput input) {
        if (!field.isFocused()) return false;
        pollPending();
        if (!isOpen()) return false;

        int key = input.key();
        if (key == GLFW.GLFW_KEY_TAB) {
            if (tabCycles && entries.size() > 1) {
                cycle(input.hasShift() ? -1 : 1);
            }
            return applySelected();
        }
        if (key == GLFW.GLFW_KEY_UP) {
            cycle(-1);
            return true;
        }
        if (key == GLFW.GLFW_KEY_DOWN) {
            cycle(1);
            return true;
        }
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            return applySelected();
        }
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            hide();
            return true;
        }
        return false;
    }

    public boolean mouseClicked(double mouseX, double mouseY) {
        if (!field.isFocused() || !isOpen()) return false;
        if (!isOverWindow(mouseX, mouseY)) return false;

        int index = firstVisible + (int) ((mouseY - windowY) / ENTRY_HEIGHT);
        if (index >= 0 && index < entries.size()) {
            selectedIndex = index;
            applySelected();
        }
        return true;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!field.isFocused() || !isOpen()) return false;
        if (!isOverWindow(mouseX, mouseY)) return false;

        int max = Math.max(0, entries.size() - MAX_VISIBLE);
        firstVisible = MathHelper.clamp(firstVisible - (int) Math.signum(amount), 0, max);
        return true;
    }

    private boolean isOverWindow(double mouseX, double mouseY) {
        return mouseX >= windowX && mouseX < windowX + windowW
                && mouseY >= windowY && mouseY < windowY + windowH;
    }

    public void render(DrawContext context, int mouseX, int mouseY, int screenWidth, int screenHeight) {
        if (!field.isFocused()) {
            if (visible || pending != null) hide();
            return;
        }

        pollPending();
        if (!isOpen()) return;

        if (firstVisible >= entries.size()) firstVisible = 0;
        int count = Math.min(entries.size() - firstVisible, MAX_VISIBLE);

        int textWidth = 0;
        for (int i = firstVisible; i < firstVisible + count; i++) {
            textWidth = Math.max(textWidth, client.textRenderer.getWidth(entries.get(i).getText()));
        }

        windowW = textWidth + 6;
        windowH = count * ENTRY_HEIGHT;

        int anchorX = field.getCharacterX(Math.max(0, entryRangeStart - 1));
        windowX = MathHelper.clamp(anchorX - 3, 0, Math.max(0, screenWidth - windowW));

        windowY = field.getY() + field.getHeight() + 1;
        if (windowY + windowH > screenHeight) {
            windowY = Math.max(0, field.getY() - windowH - 1);
        }

        context.fill(windowX, windowY, windowX + windowW, windowY + windowH, BACKGROUND_COLOR);
        drawBorder(context, windowX, windowY, windowW, windowH, BORDER_COLOR);

        for (int i = 0; i < count; i++) {
            int index = firstVisible + i;
            int entryY = windowY + i * ENTRY_HEIGHT;

            if (mouseX >= windowX && mouseX < windowX + windowW && mouseY >= entryY && mouseY < entryY + ENTRY_HEIGHT) {
                context.fill(windowX + 1, entryY, windowX + windowW - 1, entryY + ENTRY_HEIGHT, HIGHLIGHT_COLOR);
            }

            context.drawText(client.textRenderer, entries.get(index).getText(), windowX + 3, entryY + 2,
                    index == selectedIndex ? SELECTED_COLOR : TEXT_COLOR, false);
        }

        if (entries.size() > count) {
            String counter = (selectedIndex + 1) + "/" + entries.size();
            context.drawText(client.textRenderer, counter,
                    windowX + windowW - client.textRenderer.getWidth(counter) - 3,
                    windowY + windowH - ENTRY_HEIGHT + 2, 0xFF808080, false);
        }
    }

    private static void drawBorder(DrawContext context, int x, int y, int w, int h, int color) {
        context.fill(x, y, x + w, y + 1, color);
        context.fill(x, y + h - 1, x + w, y + h, color);
        context.fill(x, y + 1, x + 1, y + h - 1, color);
        context.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }
}
