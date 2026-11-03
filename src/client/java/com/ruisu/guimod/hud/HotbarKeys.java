package com.ruisu.guimod.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Writes the key bound to each hotbar slot in that slot's top-left corner of the vanilla hotbar.
 * Unlike widgets this isn't movable; it follows the hotbar. Turned on and off in the layout editor.
 * <p>
 * The GUI puts whatever is drawn later on top when things overlap. To show labels in front of items,
 * {@link #extract} draws them all after the hotbar. To show them behind items, {@code HudHotbarMixin}
 * calls {@link #beforeSlot} just before vanilla draws each slot's item.
 */
public final class HotbarKeys {
	private static final int HOTBAR_WIDTH = 182;
	private static final int HOTBAR_HEIGHT = 22;
	private static final int SLOT_SIZE = 20;
	/** Roughly how big the labels are compared to normal text. The real size is snapped by {@link #textScale}. */
	private static final float TARGET_TEXT_SCALE = 0.75f;
	/** Longest label that fits in a slot, in on-screen pixels. Longer key names are cut short. */
	private static final int MAX_LABEL_WIDTH = 16;
	/** The main grey of the vanilla hotbar frame, so the labels blend in with it. */
	private static final int TEXT_COLOR = 0xFF939393;

	/** Which slot vanilla draws next while the hotbar is being drawn. */
	private static int nextSlot;

	private HotbarKeys() {
	}

	/** Draws every label on top of the finished hotbar, when labels go in front of items. */
	public static void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		if (!shouldShow() || HudLayout.hotbarKeysBehindItems()) {
			return;
		}
		for (int slot = 0; slot < Minecraft.getInstance().options.keyHotbarSlots.length; slot++) {
			drawLabel(graphics, slot);
		}
	}

	/** Called as vanilla starts drawing the item hotbar. */
	public static void startHotbar() {
		nextSlot = 0;
	}

	/** Called just before vanilla draws the next slot's item, when labels go behind items. */
	public static void beforeSlot(GuiGraphicsExtractor graphics) {
		int slot = nextSlot++;
		if (shouldShow() && HudLayout.hotbarKeysBehindItems()) {
			drawLabel(graphics, slot);
		}
	}

	private static boolean shouldShow() {
		Minecraft mc = Minecraft.getInstance();
		// Spectators get a different hotbar, so the slot keys don't apply.
		return mc.player != null && !mc.player.isSpectator() && HudLayout.showHotbarKeys();
	}

	/**
	 * A text scale close to {@link #TARGET_TEXT_SCALE} where every font pixel covers a whole number of screen pixels,
	 * so the numbers stay sharp. For example at GUI scale 3, 0.75 would make font pixels 2.25 screen pixels wide
	 * (uneven and blurry), so this picks 2/3 instead (exactly 2 screen pixels).
	 */
	private static float textScale(Minecraft mc) {
		int guiScale = Math.max(mc.getWindow().getGuiScale(), 1);
		int screenPixelsPerFontPixel = Math.max((int) (TARGET_TEXT_SCALE * guiScale), 1);
		return (float) screenPixelsPerFontPixel / guiScale;
	}

	private static void drawLabel(GuiGraphicsExtractor graphics, int slot) {
		Minecraft mc = Minecraft.getInstance();
		KeyMapping[] keys = mc.options.keyHotbarSlots;
		if (slot < 0 || slot >= keys.length || keys[slot].isUnbound()) {
			return;
		}

		Font font = mc.font;
		float scale = textScale(mc);
		int left = graphics.guiWidth() / 2 - HOTBAR_WIDTH / 2;
		int top = graphics.guiHeight() - HOTBAR_HEIGHT;
		String label = font.plainSubstrByWidth(keys[slot].getTranslatedKeyMessage().getString(), (int) (MAX_LABEL_WIDTH / scale));

		graphics.pose().pushMatrix();
		graphics.pose().translate(left + slot * SLOT_SIZE + 4, top + 4);
		graphics.pose().scale(scale, scale);
		graphics.text(font, label, 0, 0, TEXT_COLOR, true);
		graphics.pose().popMatrix();
	}
}
