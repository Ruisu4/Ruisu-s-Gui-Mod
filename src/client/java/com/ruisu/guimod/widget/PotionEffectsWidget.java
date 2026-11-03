package com.ruisu.guimod.widget;

import com.ruisu.guimod.hud.HudWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;

import java.util.List;

/**
 * Lists active potion effects, one row each: the effect's icon and how long is left.
 * Hidden in game when there are no effects.
 */
public class PotionEffectsWidget extends HudWidget {
	private static final int ICON_SIZE = 18;
	private static final int ICON_GAP = 3;
	private static final int ROW_GAP = 1;
	private static final int TEXT_COLOR = 0xFFFFFFFF;
	private static final int ENDING_COLOR = 0xFFFF5555;
	/** Timers turn red in the last 10 seconds. */
	private static final int ENDING_TICKS = 200;

	/** Shown in the editor when there are no real effects, so the box can still be moved. */
	private static final List<MobEffectInstance> SAMPLE = List.of(
			new MobEffectInstance(MobEffects.SPEED, 20 * 90, 1),
			new MobEffectInstance(MobEffects.STRENGTH, 20 * 45, 0),
			new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 8, 0)
	);

	public PotionEffectsWidget() {
		super("potion_effects", 1.0, 0.5);
	}

	@Override
	public boolean hasContent() {
		return !activeEffects().isEmpty();
	}

	@Override
	protected int contentWidth(Font font, boolean editing) {
		int widest = 0;
		for (MobEffectInstance effect : effects(editing)) {
			widest = Math.max(widest, font.width(timer(effect)));
		}
		return ICON_SIZE + ICON_GAP + widest;
	}

	@Override
	protected int contentHeight(Font font, boolean editing) {
		int rows = effects(editing).size();
		return rows * ICON_SIZE + Math.max(rows - 1, 0) * ROW_GAP;
	}

	@Override
	protected void drawContent(GuiGraphicsExtractor graphics, Font font, boolean editing) {
		int y = 0;
		for (MobEffectInstance effect : effects(editing)) {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Hud.getMobEffectSprite(effect.getEffect()), 0, y, ICON_SIZE, ICON_SIZE);

			boolean ending = !effect.isInfiniteDuration() && effect.endsWithin(ENDING_TICKS);
			int textY = y + (ICON_SIZE - font.lineHeight) / 2 + 1;
			graphics.text(font, timer(effect), ICON_SIZE + ICON_GAP, textY, ending ? ENDING_COLOR : TEXT_COLOR, true);

			y += ICON_SIZE + ROW_GAP;
		}
	}

	private static List<MobEffectInstance> effects(boolean editing) {
		List<MobEffectInstance> active = activeEffects();
		return active.isEmpty() && editing ? SAMPLE : active;
	}

	/** Same order as vanilla, skipping effects that hide their icon. */
	private static List<MobEffectInstance> activeEffects() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			return List.of();
		}
		return mc.player.getActiveEffects().stream()
				.filter(MobEffectInstance::showIcon)
				.sorted()
				.toList();
	}

	private static Component timer(MobEffectInstance effect) {
		Minecraft mc = Minecraft.getInstance();
		float tickRate = mc.level != null ? mc.level.tickRateManager().tickrate() : 20.0f;
		return MobEffectUtil.formatDuration(effect, 1.0f, tickRate);
	}
}
