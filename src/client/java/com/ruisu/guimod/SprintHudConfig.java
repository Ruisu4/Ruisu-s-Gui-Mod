package com.ruisu.guimod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Mth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Stores the HUD position as a fraction of the free screen space (0 = left/top edge, 1 = right/bottom edge),
 * so it stays in the same spot when the window or GUI scale changes.
 */
public final class SprintHudConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(RuisusGuiClient.MOD_ID);
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve(RuisusGuiClient.MOD_ID + ".json");

	public static final double DEFAULT_X = 0.0;
	public static final double DEFAULT_Y = 0.0;

	private static Data data = new Data();

	private SprintHudConfig() {
	}

	private static final class Data {
		double x = DEFAULT_X;
		double y = DEFAULT_Y;
	}

	public static void load() {
		if (!Files.exists(FILE)) {
			return;
		}
		try (Reader reader = Files.newBufferedReader(FILE)) {
			Data loaded = GSON.fromJson(reader, Data.class);
			if (loaded != null) {
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

	public static int pixelX(int screenWidth, int elementWidth) {
		return toPixels(data.x, screenWidth - elementWidth);
	}

	public static int pixelY(int screenHeight, int elementHeight) {
		return toPixels(data.y, screenHeight - elementHeight);
	}

	public static void setFromPixels(int x, int y, int screenWidth, int screenHeight, int elementWidth, int elementHeight) {
		data.x = toFraction(x, screenWidth - elementWidth);
		data.y = toFraction(y, screenHeight - elementHeight);
	}

	public static void reset() {
		data = new Data();
	}

	private static int toPixels(double fraction, int freeSpace) {
		return (int) Math.round(Mth.clamp(fraction, 0.0, 1.0) * Math.max(freeSpace, 0));
	}

	private static double toFraction(int pixels, int freeSpace) {
		return freeSpace <= 0 ? 0.0 : Mth.clamp((double) pixels / freeSpace, 0.0, 1.0);
	}
}
