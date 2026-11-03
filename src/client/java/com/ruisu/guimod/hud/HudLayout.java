package com.ruisu.guimod.hud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ruisu.guimod.RuisusGuiClient;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.Font;
import net.minecraft.util.Mth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Saves each widget's position, size and visibility to {@code config/ruisusgui.json}.
 * <p>
 * Positions are a fraction of the free screen space (0 = left/top edge, 1 = right/bottom edge),
 * so widgets stay in the same spot when the window or GUI scale changes.
 */
public final class HudLayout {
	private static final Logger LOGGER = LoggerFactory.getLogger(RuisusGuiClient.MOD_ID);
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve(RuisusGuiClient.MOD_ID + ".json");

	public static final float DEFAULT_SCALE = 1.0f;
	public static final float MIN_SCALE = 0.5f;
	public static final float MAX_SCALE = 4.0f;
	public static final int DEFAULT_TEXT_COLOR = 0xFFFFFF;
	public static final int DEFAULT_BACKGROUND_OPACITY = 50;

	private static Data data = new Data();

	private HudLayout() {
	}

	private static final class Data {
		boolean hotbarKeys = true;
		boolean hotbarKeysBehindItems = false;
		Map<String, Entry> widgets = new LinkedHashMap<>();
	}

	/** Whether hotbar key labels are drawn behind the items instead of in front of them. */
	public static boolean hotbarKeysBehindItems() {
		return data.hotbarKeysBehindItems;
	}

	public static void setHotbarKeysBehindItems(boolean behind) {
		data.hotbarKeysBehindItems = behind;
	}

	/** Whether to label hotbar slots with their keys. See {@link HotbarKeys}. */
	public static boolean showHotbarKeys() {
		return data.hotbarKeys;
	}

	public static void setShowHotbarKeys(boolean show) {
		data.hotbarKeys = show;
	}

	private static final class Entry {
		double x;
		double y;
		float scale = DEFAULT_SCALE;
		boolean enabled = true;
		/** RGB, no alpha. */
		int textColor = DEFAULT_TEXT_COLOR;
		boolean textShadow = true;
		/** 0 (no background) to 100 (solid black). */
		int backgroundOpacity = DEFAULT_BACKGROUND_OPACITY;

		Entry() {
		}

		Entry(HudWidget widget) {
			x = widget.defaultX();
			y = widget.defaultY();
			backgroundOpacity = widget.defaultBackgroundOpacity();
		}
	}

	private static Entry entry(HudWidget widget) {
		return data.widgets.computeIfAbsent(widget.id(), id -> new Entry(widget));
	}

	public static void load() {
		if (!Files.exists(FILE)) {
			return;
		}
		try (Reader reader = Files.newBufferedReader(FILE)) {
			Data loaded = GSON.fromJson(reader, Data.class);
			if (loaded != null && loaded.widgets != null) {
				data = loaded;
			}
		} catch (Exception e) {
			LOGGER.warn("Couldn't read {}, using defaults", FILE, e);
		}
	}

	public static void save() {
		try (Writer writer = Files.newBufferedWriter(FILE)) {
			GSON.toJson(data, writer);
		} catch (IOException e) {
			LOGGER.warn("Couldn't save {}", FILE, e);
		}
	}

	public static int pixelX(HudWidget widget, int screenWidth, Font font, boolean editing) {
		return toPixels(entry(widget).x, screenWidth - widget.width(font, editing));
	}

	public static int pixelY(HudWidget widget, int screenHeight, Font font, boolean editing) {
		return toPixels(entry(widget).y, screenHeight - widget.height(font, editing));
	}

	public static void setPosition(HudWidget widget, int x, int y, int screenWidth, int screenHeight, Font font, boolean editing) {
		Entry entry = entry(widget);
		entry.x = toFraction(x, screenWidth - widget.width(font, editing));
		entry.y = toFraction(y, screenHeight - widget.height(font, editing));
	}

	public static float scale(HudWidget widget) {
		return Mth.clamp(entry(widget).scale, MIN_SCALE, MAX_SCALE);
	}

	public static void setScale(HudWidget widget, float scale) {
		// Round to the nearest 10% so repeated steps don't drift.
		entry(widget).scale = Mth.clamp(Math.round(scale * 10) / 10f, MIN_SCALE, MAX_SCALE);
	}

	public static boolean isEnabled(HudWidget widget) {
		return entry(widget).enabled;
	}

	public static void setEnabled(HudWidget widget, boolean enabled) {
		entry(widget).enabled = enabled;
	}

	/** The text colour, shadow and background a widget should be drawn with. */
	public static WidgetStyle style(HudWidget widget) {
		Entry entry = entry(widget);
		int opacity = Mth.clamp(entry.backgroundOpacity, 0, 100);
		int backgroundAlpha = Math.round(opacity * 255 / 100f);
		return new WidgetStyle(0xFF000000 | entry.textColor, entry.textShadow, backgroundAlpha << 24);
	}

	/** RGB, no alpha. */
	public static int textColor(HudWidget widget) {
		return entry(widget).textColor & 0xFFFFFF;
	}

	public static void setTextColor(HudWidget widget, int rgb) {
		entry(widget).textColor = rgb & 0xFFFFFF;
	}

	public static boolean textShadow(HudWidget widget) {
		return entry(widget).textShadow;
	}

	public static void setTextShadow(HudWidget widget, boolean shadow) {
		entry(widget).textShadow = shadow;
	}

	/** 0 (no background) to 100 (solid black). */
	public static int backgroundOpacity(HudWidget widget) {
		return Mth.clamp(entry(widget).backgroundOpacity, 0, 100);
	}

	public static void setBackgroundOpacity(HudWidget widget, int opacity) {
		entry(widget).backgroundOpacity = Mth.clamp(opacity, 0, 100);
	}

	/** Puts position, size and style back to the defaults. Keeps whether the widget is shown. */
	public static void reset(HudWidget widget) {
		boolean enabled = isEnabled(widget);
		Entry entry = new Entry(widget);
		entry.enabled = enabled;
		data.widgets.put(widget.id(), entry);
	}

	private static int toPixels(double fraction, int freeSpace) {
		return (int) Math.round(Mth.clamp(fraction, 0.0, 1.0) * Math.max(freeSpace, 0));
	}

	private static double toFraction(int pixels, int freeSpace) {
		return freeSpace <= 0 ? 0.0 : Mth.clamp((double) pixels / freeSpace, 0.0, 1.0);
	}
}
