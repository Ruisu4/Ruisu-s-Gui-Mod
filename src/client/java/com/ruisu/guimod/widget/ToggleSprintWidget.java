package com.ruisu.guimod.widget;

import com.ruisu.guimod.hud.TextHudWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

/**
 * Shows "Toggle Sprint: ON/OFF".
 * <p>
 * With "Sprint: Toggle" set in Controls, the sprint key reports whether the toggle is on.
 * With "Sprint: Hold" it reports whether the key is being held.
 */
public class ToggleSprintWidget extends TextHudWidget {
	public ToggleSprintWidget() {
		super("toggle_sprint", 0.0, 0.0);
	}

	@Override
	protected Component text() {
		return label(Minecraft.getInstance().options.keySprint.isDown());
	}

	@Override
	protected int textWidth(Font font, Component text) {
		// Always as wide as "OFF" so the box doesn't change size when it switches.
		return font.width(label(false));
	}

	private static Component label(boolean on) {
		Component state = on
				? Component.translatable("hud.ruisusgui.on").withStyle(ChatFormatting.GREEN)
				: Component.translatable("hud.ruisusgui.off").withStyle(ChatFormatting.RED);
		return Component.translatable("hud.ruisusgui.toggle_sprint.text", state);
	}
}
