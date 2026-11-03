package com.ruisu.guimod;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * Drag the sprint indicator around with the left mouse button and scroll over it to resize.
 * Closing the screen saves the position and size.
 */
public class HudEditScreen extends Screen {
	private static final int OUTLINE = 0xFFFFFFFF;
	private static final int OUTLINE_HOVER = 0xFFFFFF55;
	private static final int SNAP_DISTANCE = 4;
	private static final float SCALE_STEP = 0.1f;

	private static boolean open;

	private final Screen parent;
	private int hudX;
	private int hudY;
	private boolean dragging;
	private double grabOffsetX;
	private double grabOffsetY;

	/**
	 * @param parent screen to go back to when closed, or null to return to the game
	 */
	public HudEditScreen(Screen parent) {
		super(Component.translatable("screen.ruisusgui.edit_hud"));
		this.parent = parent;
	}

	public static boolean isOpen() {
		return open;
	}

	@Override
	protected void init() {
		open = true;
		hudX = SprintHudConfig.pixelX(width, SprintHud.width(font));
		hudY = SprintHudConfig.pixelY(height, SprintHud.height(font));

		addRenderableWidget(Button.builder(Component.literal("-"), button -> changeScale(-SCALE_STEP))
				.bounds(width / 2 - 102, height / 2 + 10, 20, 20).build());

		addRenderableWidget(Button.builder(Component.literal("+"), button -> changeScale(SCALE_STEP))
				.bounds(width / 2 + 82, height / 2 + 10, 20, 20).build());

		addRenderableWidget(Button.builder(Component.translatable("screen.ruisusgui.reset"), button -> {
			SprintHudConfig.reset();
			hudX = SprintHudConfig.pixelX(width, SprintHud.width(font));
			hudY = SprintHudConfig.pixelY(height, SprintHud.height(font));
		}).bounds(width / 2 - 102, height / 2 + 34, 100, 20).build());

		addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
				.bounds(width / 2 + 2, height / 2 + 34, 100, 20).build());
	}

	/** Resizes the box from its top-left corner, then pulls it back on screen if it grew past an edge. */
	private void changeScale(float delta) {
		SprintHudConfig.setScale(SprintHudConfig.scale() + delta);
		hudX = Mth.clamp(hudX, 0, Math.max(width - SprintHud.width(font), 0));
		hudY = Mth.clamp(hudY, 0, Math.max(height - SprintHud.height(font), 0));
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);

		graphics.centeredText(font, Component.translatable("screen.ruisusgui.edit_hud.hint"), width / 2, height / 2 - 10, 0xFFFFFFFF);
		int percent = Math.round(SprintHudConfig.scale() * 100);
		graphics.centeredText(font, Component.translatable("screen.ruisusgui.size", percent), width / 2, height / 2 + 16, 0xFFFFFFFF);

		SprintHud.draw(graphics, font, hudX, hudY);
		boolean highlighted = dragging || isOverHud(mouseX, mouseY);
		graphics.outline(hudX - 1, hudY - 1, SprintHud.width(font) + 2, SprintHud.height(font) + 2, highlighted ? OUTLINE_HOVER : OUTLINE);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() == 0 && isOverHud(event.x(), event.y())) {
			dragging = true;
			grabOffsetX = event.x() - hudX;
			grabOffsetY = event.y() - hudY;
			return true;
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (dragging) {
			int w = SprintHud.width(font);
			int h = SprintHud.height(font);
			hudX = snap(Mth.clamp((int) Math.round(event.x() - grabOffsetX), 0, width - w), width - w);
			hudY = snap(Mth.clamp((int) Math.round(event.y() - grabOffsetY), 0, height - h), height - h);
			return true;
		}
		return super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (dragging && event.button() == 0) {
			dragging = false;
			return true;
		}
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (scrollY != 0 && isOverHud(mouseX, mouseY)) {
			changeScale(scrollY > 0 ? SCALE_STEP : -SCALE_STEP);
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public void onClose() {
		minecraft.setScreenAndShow(parent);
	}

	@Override
	public void removed() {
		SprintHudConfig.setFromPixels(hudX, hudY, width, height, SprintHud.width(font), SprintHud.height(font));
		SprintHudConfig.save();
		open = false;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private boolean isOverHud(double x, double y) {
		return x >= hudX && x < hudX + SprintHud.width(font) && y >= hudY && y < hudY + SprintHud.height(font);
	}

	/** Pulls the box flush against a screen edge when it's within a few pixels of it. */
	private static int snap(int pos, int max) {
		if (pos <= SNAP_DISTANCE) {
			return 0;
		}
		if (pos >= max - SNAP_DISTANCE) {
			return max;
		}
		return pos;
	}
}
