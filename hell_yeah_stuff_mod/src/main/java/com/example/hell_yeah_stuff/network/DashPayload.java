package com.example.hell_yeah_stuff.network;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Клиент -> сервер: «я сделал рывок зачарованием «Рывок»».
 *
 * Само движение применяется на клиенте (движение игрока
 * клиент-авторитативно, как у рывка крюка-кошки); сервер по этому
 * пакету проверяет зачарование/кулдаун и включает след частиц,
 * видимый всем игрокам.
 */
public record DashPayload() implements CustomPacketPayload {

    public static final DashPayload INSTANCE = new DashPayload();

    public static final CustomPacketPayload.Type<DashPayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "dash"));

    public static final StreamCodec<ByteBuf, DashPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
