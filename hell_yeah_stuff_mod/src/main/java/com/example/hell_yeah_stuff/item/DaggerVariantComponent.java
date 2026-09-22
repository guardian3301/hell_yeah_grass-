package com.example.hell_yeah_stuff.item;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * Data Component для хранения варианта кортика.
 * Сохраняется в NBT, синхронизируется с клиентом.
 */
public record DaggerVariantComponent(DaggerVariant variant) {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
        HellYeahStuffMod.MODID, "dagger_variant"
    );

    /**
     * Безопасное значение по умолчанию.
     */
    public static final DaggerVariantComponent DEFAULT = new DaggerVariantComponent(DaggerVariant.NORMAL);

    /**
     * Codec для сохранения в NBT.
     */
    public static final Codec<DaggerVariantComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("variant").forGetter(comp -> comp.variant().getSerializedName())
            ).apply(instance, DaggerVariantComponent::fromString)
    );

    /**
     * StreamCodec для сетевой синхронизации (String-based для совместимости).
     */
    public static final StreamCodec<ByteBuf, DaggerVariantComponent> STREAM_CODEC = StreamCodec.of(
            (buf, comp) -> ByteBufCodecs.STRING_UTF8.encode(buf, comp.variant().getSerializedName()),
            buf -> new DaggerVariantComponent(DaggerVariant.fromStringSafe(ByteBufCodecs.STRING_UTF8.decode(buf)))
    );

    /**
     * Создаёт компонент с безопасным значением по умолчанию.
     */
    public static DaggerVariantComponent safeDefault() {
        return DEFAULT;
    }

    /**
     * Создаёт компонент из строки, безопасно обрабатывая неизвестные значения.
     */
    public static DaggerVariantComponent fromString(String name) {
        return new DaggerVariantComponent(DaggerVariant.fromStringSafe(name));
    }
}
