package com.example.hell_yeah_stuff.network;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Клиент -> сервер: попытка использовать предмет за спиной (зажать).
 */
public record BeltUsePayload() implements CustomPacketPayload {

    public static final BeltUsePayload INSTANCE = new BeltUsePayload();

    public static final CustomPacketPayload.Type<BeltUsePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "belt_use"));

    public static final StreamCodec<ByteBuf, BeltUsePayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
