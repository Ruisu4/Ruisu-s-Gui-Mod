package com.ruisu.guimod.hud;

import com.ruisu.guimod.RuisusGuiClient;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * One movable, resizable box on the HUD. Subclasses only draw their content;
 * position, size, visibility and saving are handled by {@link HudLayout} and the editor.
 * <p>
 * For a box that just shows a line of text, extend {@link TextHudWidget} instead.
 * <p>
 * The {@code editing} flag is true inside the layout editor, where a widget should always show something
 * (sample content if it has nothing real to show) so it can be moved.
 */
public abstract class HudWidget {
	public static final int PADDING = 3;

	private static final int BACKGROUND = 0x80000000;

	private final String id;
	private final double defaultX;
	private final double defaultY;

	/**
	 * @param id       saved in the config and used for the name's translation key ({@code hud.ruisusgui.<id>})
	 * @param defaultX starting horizontal position, 0 = left edge, 1 = right edge
	 * @param defaultY starting vertical position, 0 = top edge, 1 = bottom edge
	 */
	protected HudWidget(String id, double defaultX, double defaultY) {
		this.id = id;
		this.defaultX = defaultX;
		this.defaultY = defaultY;
	}

	/** Width of the content before scaling, not counting padding. */
	protected abstract int contentWidth(Font font, boolean editing);

	/** Height of the content before scaling, not counting padding. */
	protected abstract int contentHeight(Font font, boolean editing);

	/** Draws the content with its top-left corner at (0, 0), before scaling. */
	protected abstract void drawContent(GuiGraphicsExtractor graphics, Font font, boolean editing);

	/** Whether to draw the box in game. Return false when there's nothing to show. */
	public boolean hasContent() {
		return true;
	}

	public final String id() {
		return id;
	}

	public Component name() {
		return Component.translatable("hud." + RuisusGuiClient.MOD_ID + "." + id);
	}

	public final double defaultX() {
		return defaultX;
	}

	public final double defaultY() {
		return defaultY;
	}

	/** On-screen width, including the size setting. */
	public final int width(Font font, boolean editing) {
		return Mth.ceil((contentWidth(font, editing) + PADDING * 2) * HudLayout.scale(this));
	}

	/** On-screen height, including the size setting. */
	public final int height(Font font, boolean editing) {
		return Mth.ceil((contentHeight(font, editing) + PADDING * 2) * HudLayout.scale(this));
	}

	public final void draw(GuiGraphicsExtractor graphics, Font font, int x, int y, boolean editing) {
		float scale = HudLayout.scale(this);

		graphics.pose().pushMatrix();
		graphics.pose().translate(x, y);
		graphics.pose().scale(scale, scale);
		graphics.fill(0, 0, contentWidth(font, editing) + PADDING * 2, contentHeight(font, editing) + PADDING * 2, BACKGROUND);
		graphics.pose().translate(PADDING, PADDING);
		drawContent(graphics, font, editing);
		graphics.pose().popMatrix();
	}
}
