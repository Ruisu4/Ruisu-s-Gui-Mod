package com.ruisu.guimod.mixin;

import com.ruisu.guimod.hud.HotbarKeys;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lets hotbar key labels be drawn after the hotbar frame but before each item, so items cover them.
 */
@Mixin(Hud.class)
public class HudHotbarMixin {
	@Inject(method = "extractItemHotbar", at = @At("HEAD"))
	private void ruisusgui$startHotbar(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		HotbarKeys.startHotbar();
	}

	// Ordinal 0 is the call inside the loop over the nine hotbar slots; the later ones are the offhand slot.
	@Inject(
			method = "extractItemHotbar",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/Hud;extractSlot(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IILnet/minecraft/client/DeltaTracker;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V",
					ordinal = 0
			)
	)
	private void ruisusgui$beforeSlot(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		HotbarKeys.beforeSlot(graphics);
	}
}
