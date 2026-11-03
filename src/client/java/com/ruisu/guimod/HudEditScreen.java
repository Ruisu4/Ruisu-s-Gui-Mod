package com.ruisu.guimod;

import com.ruisu.guimod.hud.HudLayout;
import com.ruisu.guimod.hud.HudWidget;
import com.ruisu.guimod.hud.HudWidgets;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

/**
 * Edits every HUD widget on one screen: drag to move, scroll to resize, right-click to hide or show.
 * The buttons in the middle change whichever widget was clicked last. Closing the screen saves everything.
 * <p>
 * While dragging, boxes snap to the screen edges and centre and to the edges and centres of other boxes.
 * Hold Shift to drag without snapping.
 */
public class HudEditScreen extends Screen {
	private static final int OUTLINE = 0xFFAAAAAA;
	private static final int OUTLINE_HOVER = 0xFFFFFFFF;
	private static final int OUTLINE_SELECTED = 0xFFFFFF55;
	private static final int HIDDEN_OVERLAY = 0xA0303030;
	private static final int GUIDE_COLOR = 0xFF55FFFF;
	private static final int SNAP_DISTANCE = 4;
	private static final float SCALE_STEP = 0.1f;
	private static final int OPACITY_STEP = 25;

	private record NamedColor(String key, int rgb) {
	}

	private static final List<NamedColor> TEXT_COLORS = List.of(
			new NamedColor("white", 0xFFFFFF),
			new NamedColor("gray", 0xAAAAAA),
			new NamedColor("yellow", 0xFFFF55),
			new NamedColor("gold", 0xFFAA00),
			new NamedColor("green", 0x55FF55),
			new NamedColor("aqua", 0x55FFFF),
			new NamedColor("blue", 0x5555FF),
			new NamedColor("pink", 0xFF55FF),
			new NamedColor("red", 0xFF5555)
	);

	private static boolean open;

	private final Screen parent;
	private HudWidget selected;
	private boolean dragging;
	private double grabOffsetX;
	private double grabOffsetY;
	/** Where the dragged box snapped to, drawn as guide lines. -1 when not snapped. */
	private int guideX = -1;
	private int guideY = -1;

	private Button shownButton;
	private Button shadowButton;
	private Button colorButton;
	private Button backgroundButton;
	private Button hotbarKeysButton;
	private Button hotbarKeysLayerButton;

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

		int left = width / 2 - 102;
		int right = width / 2 + 2;
		int top = height / 2 - 24;

		addRenderableWidget(Button.builder(Component.literal("-"), button -> changeScale(selected, -SCALE_STEP))
				.bounds(left, top, 20, 20).build());
		addRenderableWidget(Button.builder(Component.literal("+"), button -> changeScale(selected, SCALE_STEP))
				.bounds(width / 2 + 82, top, 20, 20).build());

		shownButton = addRenderableWidget(Button.builder(CommonComponents.EMPTY, button -> ifSelected(widget ->
				HudLayout.setEnabled(widget, !HudLayout.isEnabled(widget))
		)).bounds(left, top + 24, 100, 20).build());

		shadowButton = addRenderableWidget(Button.builder(CommonComponents.EMPTY, button -> ifSelected(widget ->
				HudLayout.setTextShadow(widget, !HudLayout.textShadow(widget))
		)).bounds(right, top + 24, 100, 20).build());

		colorButton = addRenderableWidget(Button.builder(CommonComponents.EMPTY, button -> ifSelected(widget ->
				HudLayout.setTextColor(widget, TEXT_COLORS.get((colorIndex(widget) + 1) % TEXT_COLORS.size()).rgb())
		)).bounds(left, top + 48, 100, 20).build());

		backgroundButton = addRenderableWidget(Button.builder(CommonComponents.EMPTY, button -> ifSelected(widget ->
				HudLayout.setBackgroundOpacity(widget, (HudLayout.backgroundOpacity(widget) / OPACITY_STEP + 1) % (100 / OPACITY_STEP + 1) * OPACITY_STEP)
		)).bounds(right, top + 48, 100, 20).build());

		addRenderableWidget(Button.builder(Component.translatable("screen.ruisusgui.reset"), button -> ifSelected(HudLayout::reset))
				.bounds(left, top + 72, 100, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
				.bounds(right, top + 72, 100, 20).build());

		// Not tied to the selected box: labels the vanilla hotbar with its keys.
		hotbarKeysButton = addRenderableWidget(Button.builder(CommonComponents.EMPTY, button ->
				HudLayout.setShowHotbarKeys(!HudLayout.showHotbarKeys())
		).bounds(left, top + 96, 100, 20).build());

		hotbarKeysLayerButton = addRenderableWidget(Button.builder(CommonComponents.EMPTY, button ->
				HudLayout.setHotbarKeysBehindItems(!HudLayout.hotbarKeysBehindItems())
		).bounds(right, top + 96, 100, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		// Boxes first so the buttons are drawn on top of any box that overlaps them; clicks go to buttons first too.
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

		if (dragging) {
			if (guideX >= 0) {
				int lineX = Math.min(guideX, width - 1);
				graphics.fill(lineX, 0, lineX + 1, height, GUIDE_COLOR);
			}
			if (guideY >= 0) {
				int lineY = Math.min(guideY, height - 1);
				graphics.fill(0, lineY, width, lineY + 1, GUIDE_COLOR);
			}
		}

		updateButtonLabels();
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);

		int top = height / 2 - 24;
		graphics.centeredText(font, Component.translatable("screen.ruisusgui.edit_hud.hint"), width / 2, top - 38, 0xFFFFFFFF);
		graphics.centeredText(font, Component.translatable("screen.ruisusgui.edit_hud.hint2"), width / 2, top - 26, 0xFFAAAAAA);
		if (selected != null) {
			int percent = Math.round(HudLayout.scale(selected) * 100);
			graphics.centeredText(font, selected.name(), width / 2, top - 12, 0xFFFFFF55);
			graphics.centeredText(font, Component.translatable("screen.ruisusgui.size", percent), width / 2, top + 6, 0xFFFFFFFF);
		}
	}

	private void updateButtonLabels() {
		hotbarKeysButton.setMessage(Component.translatable("screen.ruisusgui.hotbar_keys", onOff(HudLayout.showHotbarKeys())));
		hotbarKeysLayerButton.active = HudLayout.showHotbarKeys();
		hotbarKeysLayerButton.setMessage(Component.translatable(HudLayout.hotbarKeysBehindItems()
				? "screen.ruisusgui.hotbar_keys.behind"
				: "screen.ruisusgui.hotbar_keys.front"));

		boolean hasSelection = selected != null;
		shownButton.active = hasSelection;
		shadowButton.active = hasSelection;
		colorButton.active = hasSelection;
		backgroundButton.active = hasSelection;
		if (!hasSelection) {
			return;
		}

		shownButton.setMessage(Component.translatable("screen.ruisusgui.shown", onOff(HudLayout.isEnabled(selected))));
		shadowButton.setMessage(Component.translatable("screen.ruisusgui.shadow", onOff(HudLayout.textShadow(selected))));

		int colorIndex = colorIndex(selected);
		Component colorName = colorIndex >= 0
				? Component.translatable("screen.ruisusgui.color." + TEXT_COLORS.get(colorIndex).key())
				: Component.translatable("screen.ruisusgui.color.custom");
		colorButton.setMessage(Component.translatable("screen.ruisusgui.text_color",
				colorName.copy().withColor(HudLayout.textColor(selected))));

		int opacity = HudLayout.backgroundOpacity(selected);
		backgroundButton.setMessage(Component.translatable("screen.ruisusgui.background", opacity == 0
				? CommonComponents.OPTION_OFF
				: Component.translatable("screen.ruisusgui.percent", opacity)));
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
		if (!dragging || selected == null) {
			return super.mouseDragged(event, dragX, dragY);
		}

		int w = selected.width(font, true);
		int h = selected.height(font, true);
		int maxX = Math.max(width - w, 0);
		int maxY = Math.max(height - h, 0);
		int x = (int) Math.round(event.x() - grabOffsetX);
		int y = (int) Math.round(event.y() - grabOffsetY);

		guideX = -1;
		guideY = -1;
		if (!event.hasShiftDown()) {
			List<Integer> linesX = new ArrayList<>(List.of(0, width / 2, width));
			List<Integer> linesY = new ArrayList<>(List.of(0, height / 2, height));
			for (HudWidget other : HudWidgets.all()) {
				if (other == selected) {
					continue;
				}
				int ox = x(other);
				int oy = y(other);
				int ow = other.width(font, true);
				int oh = other.height(font, true);
				linesX.addAll(List.of(ox, ox + ow / 2, ox + ow));
				linesY.addAll(List.of(oy, oy + oh / 2, oy + oh));
			}

			int[] snappedX = snap(x, w, linesX);
			int[] snappedY = snap(y, h, linesY);
			x = snappedX[0];
			guideX = snappedX[1];
			y = snappedY[0];
			guideY = snappedY[1];
		}

		HudLayout.setPosition(selected, Mth.clamp(x, 0, maxX), Mth.clamp(y, 0, maxY), width, height, font, true);
		return true;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (dragging && event.button() == 0) {
			dragging = false;
			guideX = -1;
			guideY = -1;
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

	private void ifSelected(java.util.function.Consumer<HudWidget> action) {
		if (selected != null) {
			action.accept(selected);
		}
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

	/** Index of the widget's text colour in {@link #TEXT_COLORS}, or -1 if it's not one of them. */
	private static int colorIndex(HudWidget widget) {
		int rgb = HudLayout.textColor(widget);
		for (int i = 0; i < TEXT_COLORS.size(); i++) {
			if (TEXT_COLORS.get(i).rgb() == rgb) {
				return i;
			}
		}
		return -1;
	}

	private static Component onOff(boolean on) {
		return on ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF;
	}

	/**
	 * Lines up the box's start, centre or end with the nearest line within {@link #SNAP_DISTANCE}.
	 *
	 * @return the new position, and the line it snapped to (or -1 if it didn't snap)
	 */
	private static int[] snap(int pos, int size, List<Integer> lines) {
		int bestPos = pos;
		int bestLine = -1;
		int bestDistance = SNAP_DISTANCE + 1;
		for (int offset : new int[]{0, size / 2, size}) {
			for (int line : lines) {
				int distance = Math.abs(pos + offset - line);
				if (distance < bestDistance) {
					bestDistance = distance;
					bestPos = line - offset;
					bestLine = line;
				}
			}
		}
		return new int[]{bestPos, bestLine};
	}
}
