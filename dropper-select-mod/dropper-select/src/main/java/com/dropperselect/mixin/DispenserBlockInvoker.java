package com.dropperselect.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Exposes the protected DispenserBlock#dispenseFrom so the "fire" key can trigger a dropper
 * without redstone. DropperBlock overrides this method, so calling it on a dropper drops the
 * item (or pushes it into a container in front) exactly like a redstone pulse would.
 */
@Mixin(DispenserBlock.class)
public interface DispenserBlockInvoker {
	@Invoker("dispenseFrom")
	void dropperselect$dispenseFrom(ServerLevel level, BlockState state, BlockPos pos);
}
