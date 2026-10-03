package com.example.hell_yeah_stuff.network;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Клиент -> сервер: тап V — свап главного предмета с содержимым за спиной.
 */
public record BeltSwapPayload() implements CustomPacketPayload {

    public static final BeltSwapPayload INSTANCE = new BeltSwapPayload();

    public static final CustomPacketPayload.Type<BeltSwapPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "belt_swap"));

    public static final StreamCodec<ByteBuf, BeltSwapPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
