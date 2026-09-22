package com.example.hell_yeah_stuff.network;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/**
 * S2C-пакет синхронизации скрытия ника для игрока со скрытной накидкой.
 * Отправляется всем игрокам, чтобы они скрывали ник владельца накидки.
 *
 * @param targetUuid UUID владельца накидки
 * @param hidden     true — ник должен быть скрыт, false — показать
 */
public record CloakNameHidePayload(UUID targetUuid, boolean hidden) implements CustomPacketPayload {

    public static final Type<CloakNameHidePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "cloak_name_hide"));

    public static final StreamCodec<ByteBuf, CloakNameHidePayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeLong(payload.targetUuid().getMostSignificantBits());
                buf.writeLong(payload.targetUuid().getLeastSignificantBits());
                buf.writeBoolean(payload.hidden());
            },
            buf -> new CloakNameHidePayload(
                    new UUID(buf.readLong(), buf.readLong()),
                    buf.readBoolean()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
