package com.example.hell_yeah_stuff.enchantment.effect;

import com.example.hell_yeah_stuff.registry.ModMobEffects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.function.Function;

/**
 * Эффект зачарования "Забвение" (Oblivion).
 * При приседании (Shift) применяет эффект скрытности, делающий броню невидимой.
 * Аналог turn_invisible_when_crouching из Caverns and Chasms.
 */
public record OblivionEffect() implements EnchantmentEntityEffect {

    public static final MapCodec<OblivionEffect> CODEC = MapCodec.unit(OblivionEffect::new);

    @Override
    public void apply(ServerLevel level, int enchantmentLevel, EnchantedItemInUse item, Entity entity, Vec3 origin) {
        if (!level.isClientSide() && entity instanceof Player player) {
            if (player.isCrouching()) {
                Holder<net.minecraft.world.effect.MobEffect> stealthHolder = level.registryAccess()
                        .lookupOrThrow(Registries.MOB_EFFECT)
                        .getOrThrow(ModMobEffects.STEALTH.getKey());
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        stealthHolder, 200, 0, false, false, true
                ));
            }
        }
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> codec() {
        return CODEC;
    }
}
