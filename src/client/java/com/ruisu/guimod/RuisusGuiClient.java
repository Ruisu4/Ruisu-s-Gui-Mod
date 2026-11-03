package com.ruisu.guimod;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class RuisusGuiClient implements ClientModInitializer {
	public static final String MOD_ID = "ruisusgui";

	private static KeyMapping editHudKey;

	@Override
	public void onInitializeClient() {
		SprintHudConfig.load();

		editHudKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.ruisusgui.edit_hud",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_RIGHT_SHIFT,
				KeyMapping.Category.MISC
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (editHudKey.consumeClick()) {
				client.setScreenAndShow(new HudEditScreen(null));
			}
		});

		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "sprint_status"), SprintHud::extract);
	}
}
