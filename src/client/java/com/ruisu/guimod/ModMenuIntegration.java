package com.ruisu.guimod;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Adds a config button in Mod Menu that opens the HUD editor. Only loaded when Mod Menu is installed.
 */
public class ModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return HudEditScreen::new;
	}
}
