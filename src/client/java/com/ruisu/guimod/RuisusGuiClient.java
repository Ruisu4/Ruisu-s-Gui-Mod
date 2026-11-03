package com.ruisu.guimod;

import com.mojang.blaze3d.platform.InputConstants;
import com.ruisu.guimod.hud.HotbarKeys;
import com.ruisu.guimod.hud.HudLayout;
import com.ruisu.guimod.hud.HudWidgets;
import com.ruisu.guimod.widget.ArmorWidget;
import com.ruisu.guimod.widget.PotionEffectsWidget;
import com.ruisu.guimod.widget.ToggleSprintWidget;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class RuisusGuiClient implements ClientModInitializer {
	public static final String MOD_ID = "ruisusgui";

	private static KeyMapping editHudKey;

	@Override
	public void onInitializeClient() {
		// Add new widgets here. Their order is the drawing order (later ones go on top).
		PotionEffectsWidget potionEffects = new PotionEffectsWidget();
		HudWidgets.register(new ToggleSprintWidget());
		HudWidgets.register(potionEffects);
		HudWidgets.register(new ArmorWidget());

		HudLayout.load();

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

		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "widgets"), HudWidgets::extract);
		HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR, Identifier.fromNamespaceAndPath(MOD_ID, "hotbar_keys"), HotbarKeys::extract);

		// Hide vanilla's effect icons in the top-right corner while our potion widget is shown, so effects don't appear twice.
		HudElementRegistry.replaceElement(VanillaHudElements.MOB_EFFECTS, vanilla -> (graphics, deltaTracker) -> {
			if (!HudLayout.isEnabled(potionEffects)) {
				vanilla.extractRenderState(graphics, deltaTracker);
			}
		});
	}
}
