package com.ruisu.guimod.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/**
 * A widget that shows one line of text.
 */
public abstract class TextHudWidget extends HudWidget {
	protected TextHudWidget(String id, double defaultX, double defaultY) {
		super(id, defaultX, defaultY);
	}

	/** The text to show in game. Only called while a world is loaded. */
	protected abstract Component text();

	/** The text to show in the editor when no world is loaded, e.g. when opened from Mod Menu. */
	protected Component previewText() {
		return text();
	}

	/**
	 * Width of the text before scaling. Override to return a fixed width when the text changes length,
	 * so the box doesn't jump around.
	 */
	protected int textWidth(Font font, Component text) {
		return font.width(text);
	}

	@Override
	protected final int contentWidth(Font font, boolean editing) {
		return textWidth(font, content(editing));
	}

	@Override
	protected final int contentHeight(Font font, boolean editing) {
		return font.lineHeight - 1;
	}

	@Override
	protected final void drawContent(GuiGraphicsExtractor graphics, Font font, boolean editing, WidgetStyle style) {
		graphics.text(font, content(editing), 0, 0, style.textColor(), style.textShadow());
	}

	private Component content(boolean editing) {
		return editing && Minecraft.getInstance().player == null ? previewText() : text();
	}
}
