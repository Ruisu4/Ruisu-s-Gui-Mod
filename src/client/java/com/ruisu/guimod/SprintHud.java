package com.ruisu.guimod;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Draws the "Toggle Sprint: ON/OFF" box. Shared by the in-game HUD and the edit screen.
 */
public final class SprintHud {
	public static final int PADDING = 3;

	private static final int COLOR_ON = 0xFF55FF55;
	private static final int COLOR_OFF = 0xFFFF5555;
	private static final int COLOR_LABEL = 0xFFFFFFFF;
	private static final int BACKGROUND = 0x80000000;

	private static final String LABEL = "Toggle Sprint: ";

	private SprintHud() {
	}

	static void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || HudEditScreen.isOpen()) {
			return;
		}

		int x = SprintHudConfig.pixelX(graphics.guiWidth(), width(mc.font));
		int y = SprintHudConfig.pixelY(graphics.guiHeight(), height(mc.font));
		draw(graphics, mc.font, x, y);
	}

	/**
	 * With "Sprint: Toggle" set in Controls, the sprint key reports whether the toggle is on.
	 * With "Sprint: Hold" it reports whether the key is being held.
	 */
	public static boolean isSprintToggled() {
		return Minecraft.getInstance().options.keySprint.isDown();
	}

	public static int width(Font font) {
		return font.width(LABEL + "OFF") + PADDING * 2;
	}

	public static int height(Font font) {
		return font.lineHeight - 1 + PADDING * 2;
	}

	public static void draw(GuiGraphicsExtractor graphics, Font font, int x, int y) {
		boolean on = isSprintToggled();
		graphics.fill(x, y, x + width(font), y + height(font), BACKGROUND);
		graphics.text(font, LABEL, x + PADDING, y + PADDING, COLOR_LABEL, true);
		graphics.text(font, on ? "ON" : "OFF", x + PADDING + font.width(LABEL), y + PADDING, on ? COLOR_ON : COLOR_OFF, true);
	}
}
