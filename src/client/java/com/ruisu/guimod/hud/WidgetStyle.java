package com.ruisu.guimod.hud;

/**
 * How a widget should be drawn, from the player's settings in {@link HudLayout}.
 *
 * @param textColor       ARGB colour for plain text. Parts of a text that have their own colour keep it.
 * @param textShadow      whether to draw text with a shadow
 * @param backgroundColor ARGB colour of the box behind the content; alpha 0 means no background
 */
public record WidgetStyle(int textColor, boolean textShadow, int backgroundColor) {
	public boolean hasBackground() {
		return (backgroundColor >>> 24) != 0;
	}
}
