package com.dropperselect.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;

import com.dropperselect.DropperSelect;

/**
 * Droppers and dispensers pick which slot to fire with getRandomSlot. While the mod is firing a
 * dropper because you're holding a number key, we return that slot instead of a random one.
 * If that slot is empty we return -1, so the dropper just clicks instead of firing a different item.
 * Redstone-triggered firing is untouched (still random).
 */
@Mixin(DispenserBlockEntity.class)
public abstract class DispenserBlockEntityMixin {
	@Inject(method = "getRandomSlot", at = @At("HEAD"), cancellable = true)
	private void dropperselect$useForcedSlot(RandomSource random, CallbackInfoReturnable<Integer> cir) {
		int forced = DropperSelect.forcedSlot;
		if (forced < 0) return;

		DispenserBlockEntity self = (DispenserBlockEntity) (Object) this;
		if (forced >= self.getContainerSize() || self.getItem(forced).isEmpty()) {
			cir.setReturnValue(-1);
		} else {
			cir.setReturnValue(forced);
		}
	}
}
