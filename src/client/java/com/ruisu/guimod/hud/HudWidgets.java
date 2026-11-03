package com.ruisu.guimod.hud;

import com.ruisu.guimod.HudEditScreen;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Every widget the mod knows about. Register new ones in {@code RuisusGuiClient}.
 */
public final class HudWidgets {
	private static final List<HudWidget> WIDGETS = new ArrayList<>();

	private HudWidgets() {
	}

	public static void register(HudWidget widget) {
		WIDGETS.add(widget);
	}

	public static List<HudWidget> all() {
		return Collections.unmodifiableList(WIDGETS);
	}

	/** Draws every visible widget on the in-game HUD. */
	public static void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		Minecraft mc = Minecraft.getInstance();
		// The editor draws its own copies, so skip these to avoid drawing every box twice.
		if (mc.player == null || HudEditScreen.isOpen()) {
			return;
		}

		for (HudWidget widget : WIDGETS) {
			if (!HudLayout.isEnabled(widget) || !widget.hasContent()) {
				continue;
			}
			int x = HudLayout.pixelX(widget, graphics.guiWidth(), mc.font, false);
			int y = HudLayout.pixelY(widget, graphics.guiHeight(), mc.font, false);
			widget.draw(graphics, mc.font, x, y, false);
		}
	}
}
