package com.panda.inventorybuttons;

import com.panda.inventorybuttons.gui.GuiInvButtonEditor;
import com.panda.inventorybuttons.gui.GuiInvButtonMenu;
import com.panda.inventorybuttons.util.HypixelItemManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

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
}