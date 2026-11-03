package com.ruisu.guimod;

import com.ruisu.guimod.hud.HudLayout;
import com.ruisu.guimod.hud.HudWidget;
import com.ruisu.guimod.hud.HudWidgets;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;

/**
 * Edits every HUD widget: drag to move, scroll to resize, right-click to hide or show.
 * The −/+ buttons resize whichever widget was clicked last. Closing the screen saves everything.
 */
public class HudEditScreen extends Screen {
	private static final int OUTLINE = 0xFFAAAAAA;
	private static final int OUTLINE_HOVER = 0xFFFFFFFF;
	private static final int OUTLINE_SELECTED = 0xFFFFFF55;
	private static final int HIDDEN_OVERLAY = 0xA0303030;
	private static final int SNAP_DISTANCE = 4;
	private static final float SCALE_STEP = 0.1f;

	private static boolean open;

	private final Screen parent;
	private HudWidget selected;
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
		if (selected == null && !HudWidgets.all().isEmpty()) {
			selected = HudWidgets.all().getFirst();
		}

		addRenderableWidget(Button.builder(Component.literal("-"), button -> changeScale(selected, -SCALE_STEP))
				.bounds(width / 2 - 102, height / 2 + 10, 20, 20).build());

		addRenderableWidget(Button.builder(Component.literal("+"), button -> changeScale(selected, SCALE_STEP))
				.bounds(width / 2 + 82, height / 2 + 10, 20, 20).build());

		addRenderableWidget(Button.builder(Component.translatable("screen.ruisusgui.reset"), button -> {
			if (selected != null) {
				HudLayout.reset(selected);
			}
		}).bounds(width / 2 - 102, height / 2 + 34, 100, 20).build());

		addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
				.bounds(width / 2 + 2, height / 2 + 34, 100, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);

		graphics.centeredText(font, Component.translatable("screen.ruisusgui.edit_hud.hint"), width / 2, height / 2 - 20, 0xFFFFFFFF);
		graphics.centeredText(font, Component.translatable("screen.ruisusgui.edit_hud.hint2"), width / 2, height / 2 - 8, 0xFFAAAAAA);
		if (selected != null) {
			int percent = Math.round(HudLayout.scale(selected) * 100);
			graphics.centeredText(font, Component.translatable("screen.ruisusgui.size", selected.name(), percent), width / 2, height / 2 + 16, 0xFFFFFFFF);
		}

		HudWidget hovered = dragging ? selected : widgetAt(mouseX, mouseY);
		for (HudWidget widget : HudWidgets.all()) {
			int x = x(widget);
			int y = y(widget);
			int w = widget.width(font, true);
			int h = widget.height(font, true);

			widget.draw(graphics, font, x, y, true);
			if (!HudLayout.isEnabled(widget)) {
				graphics.fill(x, y, x + w, y + h, HIDDEN_OVERLAY);
			}

			int outline = widget == selected ? OUTLINE_SELECTED : widget == hovered ? OUTLINE_HOVER : OUTLINE;
			graphics.outline(x - 1, y - 1, w + 2, h + 2, outline);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (super.mouseClicked(event, doubleClick)) {
			return true;
		}

		HudWidget widget = widgetAt(event.x(), event.y());
		if (widget == null) {
			return false;
		}

		selected = widget;
		if (event.button() == 0) {
			dragging = true;
			grabOffsetX = event.x() - x(widget);
			grabOffsetY = event.y() - y(widget);
		} else if (event.button() == 1) {
			HudLayout.setEnabled(widget, !HudLayout.isEnabled(widget));
		}
		return true;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (dragging && selected != null) {
			int maxX = width - selected.width(font, true);
			int maxY = height - selected.height(font, true);
			int x = snap(Mth.clamp((int) Math.round(event.x() - grabOffsetX), 0, maxX), maxX);
			int y = snap(Mth.clamp((int) Math.round(event.y() - grabOffsetY), 0, maxY), maxY);
			HudLayout.setPosition(selected, x, y, width, height, font, true);
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
		HudWidget widget = widgetAt(mouseX, mouseY);
		if (scrollY != 0 && widget != null) {
			selected = widget;
			changeScale(widget, scrollY > 0 ? SCALE_STEP : -SCALE_STEP);
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
		HudLayout.save();
		open = false;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	/** Resizes from the top-left corner, keeping that corner where it is on screen if possible. */
	private void changeScale(HudWidget widget, float delta) {
		if (widget == null) {
			return;
		}
		int x = x(widget);
		int y = y(widget);
		HudLayout.setScale(widget, HudLayout.scale(widget) + delta);
		int maxX = Math.max(width - widget.width(font, true), 0);
		int maxY = Math.max(height - widget.height(font, true), 0);
		HudLayout.setPosition(widget, Mth.clamp(x, 0, maxX), Mth.clamp(y, 0, maxY), width, height, font, true);
	}

	/** Topmost widget under the mouse, or null. Later widgets are drawn on top, so check them first. */
	private HudWidget widgetAt(double mouseX, double mouseY) {
		List<HudWidget> widgets = HudWidgets.all();
		for (int i = widgets.size() - 1; i >= 0; i--) {
			HudWidget widget = widgets.get(i);
			int x = x(widget);
			int y = y(widget);
			if (mouseX >= x && mouseX < x + widget.width(font, true) && mouseY >= y && mouseY < y + widget.height(font, true)) {
				return widget;
			}
		}
		return null;
	}

	private int x(HudWidget widget) {
		return HudLayout.pixelX(widget, width, font, true);
	}

	private int y(HudWidget widget) {
		return HudLayout.pixelY(widget, height, font, true);
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
