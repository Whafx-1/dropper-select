package com.dropperselect;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import com.dropperselect.mixin.DispenserBlockInvoker;

public class DropperSelect implements ModInitializer {
	public static final String MOD_ID = "dropperselect";

	/** Max distance (blocks) from the player's eyes to the dropper. */
	private static final double MAX_REACH = 8.0;

	/** Fastest a held key can fire: once every 4 game ticks (5 items/second), like a fast redstone clock. */
	public static final int FIRE_INTERVAL_TICKS = 4;

	/**
	 * Slot the dropper currently being fired by this mod must use, or -1 for vanilla behaviour.
	 * Only ever set for the duration of one dispenseFrom call on the server thread.
	 */
	public static int forcedSlot = -1;

	private static final Map<UUID, Long> lastFireTick = new ConcurrentHashMap<>();

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.serverboundPlay().register(SelectorPayload.TYPE, SelectorPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(SelectorPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			player.level().getServer().execute(() -> handle(player, payload));
		});
	}

	private static void handle(ServerPlayer player, SelectorPayload payload) {
		ServerLevel level = (ServerLevel) player.level();
		BlockPos pos = payload.pos();
		int slot = payload.slot();

		// Don't trust the client: valid slot, block loaded and close by, and actually a dropper/dispenser.
		if (slot < 0 || slot > 8 || player.isSpectator()) return;
		if (!level.isLoaded(pos)) return;
		if (player.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos)) > MAX_REACH * MAX_REACH) return;

		BlockState state = level.getBlockState(pos);
		BlockEntity be = level.getBlockEntity(pos);
		if (!(state.getBlock() instanceof DispenserBlock) || !(be instanceof DispenserBlockEntity dispenser)) return;

		// Server-side rate limit so a modified client can't machine-gun items.
		long now = level.getGameTime();
		Long last = lastFireTick.get(player.getUUID());
		if (last != null && now - last < FIRE_INTERVAL_TICKS && now >= last) return;
		lastFireTick.put(player.getUUID(), now);

		ItemStack stack = dispenser.getItem(slot);
		if (stack.isEmpty()) {
			player.sendOverlayMessage(Component.literal("Slot " + (slot + 1) + " is empty").withStyle(ChatFormatting.RED));
		} else {
			Component name = stack.getHoverName();
			player.sendOverlayMessage(Component.literal("Dropper: Slot " + (slot + 1) + " - ").withStyle(ChatFormatting.GREEN)
					.append(name.copy().withStyle(ChatFormatting.WHITE)));
		}

		forcedSlot = slot;
		try {
			// Same code path as a redstone pulse: droppers drop the item or push it into a
			// container in front; dispensers use the item (arrows, water buckets, etc).
			((DispenserBlockInvoker) state.getBlock()).dropperselect$dispenseFrom(level, state, pos);
		} finally {
			forcedSlot = -1;
		}
	}
}
