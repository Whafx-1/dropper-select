package com.dropperselect.client;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.sdl.SDLScancode;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import com.dropperselect.DropperSelect;
import com.dropperselect.SelectorPayload;

/**
 * Hold 1-9 while looking at a dropper (or dispenser) to fire that slot.
 * Tap = one item. Hold = one item every 4 ticks (5 per second).
 * Rebind in Options > Controls > Key Binds (under "Miscellaneous").
 */
public class DropperSelectClient implements ClientModInitializer {
	private static final int[] DEFAULT_KEYS = {
			SDLScancode.SDL_SCANCODE_1, SDLScancode.SDL_SCANCODE_2, SDLScancode.SDL_SCANCODE_3,
			SDLScancode.SDL_SCANCODE_4, SDLScancode.SDL_SCANCODE_5, SDLScancode.SDL_SCANCODE_6,
			SDLScancode.SDL_SCANCODE_7, SDLScancode.SDL_SCANCODE_8, SDLScancode.SDL_SCANCODE_9
	};

	private static final KeyMapping[] SLOT_KEYS = new KeyMapping[9];

	/** Ticks until a held key may fire again. 0 = ready. */
	private int cooldown = 0;

	@Override
	public void onInitializeClient() {
		for (int i = 0; i < 9; i++) {
			SLOT_KEYS[i] = KeyMappingHelper.registerKeyMapping(new KeyMapping(
					"key.dropperselect.slot_" + (i + 1),
					InputConstants.Type.KEYBOARD,
					DEFAULT_KEYS[i],
					KeyMapping.Category.MISC));
		}

		ClientTickEvents.END_CLIENT_TICK.register(this::tick);
	}

	private void tick(Minecraft client) {
		if (cooldown > 0) cooldown--;

		// Drain click counters so presses made while not looking at a dropper don't queue up.
		boolean[] clicked = new boolean[9];
		for (int i = 0; i < 9; i++) {
			while (SLOT_KEYS[i].consumeClick()) clicked[i] = true;
		}

		if (client.player == null || client.level == null || client.getConnection() == null || client.screen != null) {
			return;
		}

		// Lowest-numbered key that is held (or was tapped this tick) wins.
		int slot = -1;
		boolean freshPress = false;
		for (int i = 0; i < 9; i++) {
			if (clicked[i] || SLOT_KEYS[i].isDown()) {
				slot = i;
				freshPress = clicked[i];
				break;
			}
		}
		if (slot < 0) {
			cooldown = 0; // nothing held: next press fires instantly
			return;
		}

		BlockPos target = lookedAtDropper(client);
		if (target == null) return;

		if (freshPress || cooldown == 0) {
			ClientPlayNetworking.send(new SelectorPayload(target, slot));
			cooldown = DropperSelect.FIRE_INTERVAL_TICKS;
		}
	}

	private static BlockPos lookedAtDropper(Minecraft client) {
		HitResult hit = client.hitResult;
		if (hit == null || hit.getType() != HitResult.Type.BLOCK) return null;

		BlockPos pos = ((BlockHitResult) hit).getBlockPos();
		if (!(client.level.getBlockState(pos).getBlock() instanceof DispenserBlock)) return null;
		return pos;
	}
}
