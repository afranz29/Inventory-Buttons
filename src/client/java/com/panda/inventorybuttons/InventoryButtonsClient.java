/*
 * Copyright (C) 2026 Panda/afranz29
 * This file is part of Inventory-Buttons, licensed under the LGPLv3.
 */

package com.panda.inventorybuttons;

import com.panda.inventorybuttons.gui.GuiInvButtonEditor;
import com.panda.inventorybuttons.gui.GuiInvButtonMenu;
import com.panda.inventorybuttons.util.HypixelItemManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class InventoryButtonsClient implements ClientModInitializer {

	private static boolean openMenuNextTick = false;
	private static boolean openEditorNextTick = false;

	@Override
	public void onInitializeClient() {
		InventoryButtons.load();

		// Fetch Hypixel items in background
		HypixelItemManager.loadAsync();

		// Register commands
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {

			// Command: /invbuttons
			dispatcher.register(literal("invbuttons")
					.executes(context -> {
						openMenuNextTick = true;
						return 1;
					})
					.then(literal("edit").executes(context -> {
						openEditorNextTick = true;
						return 1;
					}))
					.then(literal("help").executes(context -> {
						sendHelpMessage(context.getSource(), "invbuttons");
						return 1;
					}))
					.then(literal("profile")
							.then(argument("name", StringArgumentType.greedyString())
									.suggests((context, builder) -> {
										for (String name : InventoryButtons.getProfileNames()) {
											builder.suggest(name);
										}
										return builder.buildFuture();
									})
									.executes(context -> {
										String name = StringArgumentType.getString(context, "name");
										List<String> profiles = InventoryButtons.getProfileNames();
										String matchedProfile = null;
										for (String p : profiles) {
											if (p.equalsIgnoreCase(name)) {
												matchedProfile = p;
												break;
											}
										}
										if (matchedProfile != null) {
											InventoryButtons.loadProfile(matchedProfile);
											context.getSource().sendFeedback(Text.literal("Loaded profile: " + matchedProfile).formatted(Formatting.GREEN));
										} else {
											context.getSource().sendFeedback(Text.literal("Profile not found: " + name).formatted(Formatting.RED));
										}
										return 1;
									})
							)
					)
			);

			// Command: /inventorybuttons
			dispatcher.register(literal("inventorybuttons")
					.executes(context -> {
						openMenuNextTick = true;
						return 1;
					})
					.then(literal("edit").executes(context -> {
						openEditorNextTick = true;
						return 1;
					}))
					.then(literal("help").executes(context -> {
						sendHelpMessage(context.getSource(), "inventorybuttons");
						return 1;
					}))
					.then(literal("profile")
							.then(argument("name", StringArgumentType.greedyString())
									.suggests((context, builder) -> {
										for (String name : InventoryButtons.getProfileNames()) {
											builder.suggest(name);
										}
										return builder.buildFuture();
									})
									.executes(context -> {
										String name = StringArgumentType.getString(context, "name");
										List<String> profiles = InventoryButtons.getProfileNames();
										String matchedProfile = null;
										for (String p : profiles) {
											if (p.equalsIgnoreCase(name)) {
												matchedProfile = p;
												break;
											}
										}
										if (matchedProfile != null) {
											InventoryButtons.loadProfile(matchedProfile);
											context.getSource().sendFeedback(Text.literal("Loaded profile: " + matchedProfile).formatted(Formatting.GREEN));
										} else {
											context.getSource().sendFeedback(Text.literal("Profile not found: " + name).formatted(Formatting.RED));
										}
										return 1;
									})
							)
					)
			);
		});

		// Ticker to handle opening the screens safely on the client thread
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (openMenuNextTick) {
				openMenuNextTick = false;
				client.setScreen(new GuiInvButtonMenu());
			}
			if (openEditorNextTick) {
				openEditorNextTick = false;
				client.setScreen(new GuiInvButtonEditor(null));
			}
		});

		InventoryButtons.LOGGER.info("InventoryButtons loaded successfully!");
	}

	private static void sendHelpMessage(net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource source, String commandPrefix) {
		source.sendFeedback(Text.literal("--- Inventory Buttons Help ---").formatted(Formatting.GOLD));
		source.sendFeedback(Text.literal("/" + commandPrefix + " - Opens the configuration menu").formatted(Formatting.YELLOW));
		source.sendFeedback(Text.literal("/" + commandPrefix + " edit - Opens the button layout editor").formatted(Formatting.YELLOW));
		source.sendFeedback(Text.literal("/" + commandPrefix + " profile <name> - Loads a saved button layout profile").formatted(Formatting.YELLOW));
		source.sendFeedback(Text.literal("/" + commandPrefix + " help - Displays this help message").formatted(Formatting.YELLOW));
	}
}