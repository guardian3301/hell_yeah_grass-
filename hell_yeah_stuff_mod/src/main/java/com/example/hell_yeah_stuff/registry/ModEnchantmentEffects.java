package com.example.hell_yeah_stuff.registry;

import com.mojang.serialization.MapCodec;
import com.example.hell_yeah_stuff.HellYeahStuffMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Реестр типов эффектов зачарований.
 *
 * <p>Кастомные эффекты были удалены вместе с контентом накидки и кортиков;
 * реестр сохранён как корректная точка расширения NeoForge.</p>
 */
public final class ModEnchantmentEffects {

    public static final DeferredRegister<MapCodec<? extends EnchantmentEntityEffect>> ENCHANTMENT_ENTITY_EFFECTS =
            DeferredRegister.create(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, HellYeahStuffMod.MODID);

    private ModEnchantmentEffects() {}
}
