package com.ruisu.guimod.widget;

import com.ruisu.guimod.hud.HudWidget;
import com.ruisu.guimod.hud.WidgetStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * Shows worn armour in a row of hotbar-style slots, helmet to boots, with vanilla durability bars.
 * Empty slots show a faint outline of the missing piece, and a blinking warning appears above
 * any piece that's about to break.
 */
public class ArmorWidget extends HudWidget {
	private static final Identifier HOTBAR = Identifier.withDefaultNamespace("hud/hotbar");
	private static final int HOTBAR_WIDTH = 182;
	private static final int HOTBAR_HEIGHT = 22;
	private static final int SLOT_SIZE = 20;

	private static final List<EquipmentSlot> SLOTS = List.of(
			EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
	private static final List<Identifier> EMPTY_SLOT_SPRITES = List.of(
			Identifier.withDefaultNamespace("container/slot/helmet"),
			Identifier.withDefaultNamespace("container/slot/chestplate"),
			Identifier.withDefaultNamespace("container/slot/leggings"),
			Identifier.withDefaultNamespace("container/slot/boots"));
	private static final float EMPTY_SLOT_ALPHA = 0.35f;

	/** Warn when a piece has this fraction of its durability or less left. */
	private static final float LOW_DURABILITY = 0.1f;
	private static final int WARNING_SIZE = 9;
	private static final int WARNING_GAP = 2;
	private static final int WARNING_COLOR = 0xFFD42A2A;
	private static final int WARNING_BORDER = 0xFF6E0F0F;

	/** Shown in the editor when no armour is worn, so the box can still be placed. Made on first use. */
	private static List<ItemStack> sample;

	public ArmorWidget() {
		super("armor", 1.0, 1.0);
	}

	@Override
	protected int padding() {
		// The hotbar frame is the border; no extra space around it.
		return 0;
	}

	@Override
	public int defaultBackgroundOpacity() {
		return 0;
	}

	@Override
	public boolean hasContent() {
		return worn().stream().anyMatch(stack -> !stack.isEmpty());
	}

	@Override
	protected int contentWidth(Font font, boolean editing) {
		return SLOTS.size() * SLOT_SIZE + 2;
	}

	@Override
	protected int contentHeight(Font font, boolean editing) {
		return HOTBAR_HEIGHT;
	}

	@Override
	protected void drawContent(GuiGraphicsExtractor graphics, Font font, boolean editing, WidgetStyle style) {
		List<ItemStack> armor = armor(editing);
		int frameWidth = SLOTS.size() * SLOT_SIZE + 1;

		// The left part of the vanilla hotbar, closed off with its right-hand edge.
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR, HOTBAR_WIDTH, HOTBAR_HEIGHT, 0, 0, 0, 0, frameWidth, HOTBAR_HEIGHT);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR, HOTBAR_WIDTH, HOTBAR_HEIGHT, HOTBAR_WIDTH - 1, 0, frameWidth, 0, 1, HOTBAR_HEIGHT);

		boolean blinkOn = Util.getMillis() / 500 % 2 == 0;
		for (int i = 0; i < armor.size(); i++) {
			ItemStack stack = armor.get(i);
			int x = i * SLOT_SIZE + 3;
			int y = 3;

			if (stack.isEmpty()) {
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, EMPTY_SLOT_SPRITES.get(i), x, y, 16, 16, EMPTY_SLOT_ALPHA);
				continue;
			}

			graphics.item(stack, x, y);
			graphics.itemDecorations(font, stack, x, y);

			if (blinkOn && isLow(stack)) {
				drawWarning(graphics, font, x + 8 - WARNING_SIZE / 2, -WARNING_SIZE - WARNING_GAP);
			}
		}
	}

	/** A small red badge with a "!" in it. */
	private static void drawWarning(GuiGraphicsExtractor graphics, Font font, int x, int y) {
		graphics.fill(x, y, x + WARNING_SIZE, y + WARNING_SIZE, WARNING_BORDER);
		graphics.fill(x + 1, y + 1, x + WARNING_SIZE - 1, y + WARNING_SIZE - 1, WARNING_COLOR);
		graphics.text(font, "!", x + (WARNING_SIZE - font.width("!")) / 2 + 1, y + 1, 0xFFFFFFFF, false);
	}

	private static boolean isLow(ItemStack stack) {
		if (!stack.isDamageableItem()) {
			return false;
		}
		int left = stack.getMaxDamage() - stack.getDamageValue();
		return left <= stack.getMaxDamage() * LOW_DURABILITY;
	}

	private static List<ItemStack> armor(boolean editing) {
		List<ItemStack> worn = worn();
		boolean wearingNothing = worn.stream().allMatch(ItemStack::isEmpty);
		return wearingNothing && editing ? sample() : worn;
	}

	private static List<ItemStack> sample() {
		if (sample == null) {
			sample = List.of(
					damaged(new ItemStack(Items.DIAMOND_HELMET), 0.95f),
					ItemStack.EMPTY,
					new ItemStack(Items.IRON_LEGGINGS),
					damaged(new ItemStack(Items.GOLDEN_BOOTS), 0.3f));
		}
		return sample;
	}

	private static List<ItemStack> worn() {
		Player player = Minecraft.getInstance().player;
		if (player == null) {
			return List.of(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY);
		}
		return SLOTS.stream().map(player::getItemBySlot).toList();
	}

	private static ItemStack damaged(ItemStack stack, float fractionUsed) {
		stack.setDamageValue(Math.round(stack.getMaxDamage() * fractionUsed));
		return stack;
	}
}
