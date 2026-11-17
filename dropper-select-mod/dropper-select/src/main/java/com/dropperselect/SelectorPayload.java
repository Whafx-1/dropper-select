package com.dropperselect;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Sent from client to server while a Dropper Select key (1-9) is held and the player is looking
 * at a dropper/dispenser.
 *
 * @param pos  the block being looked at
 * @param slot which slot to fire, 0-8 (key "1" = slot 0, the top-left slot)
 */
public record SelectorPayload(BlockPos pos, int slot) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<SelectorPayload> TYPE = new CustomPacketPayload.Type<>(DropperSelect.id("fire_slot"));

	public static final StreamCodec<RegistryFriendlyByteBuf, SelectorPayload> CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, SelectorPayload::pos,
			ByteBufCodecs.VAR_INT, SelectorPayload::slot,
			SelectorPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
