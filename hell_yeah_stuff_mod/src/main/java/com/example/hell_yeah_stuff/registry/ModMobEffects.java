package com.example.hell_yeah_stuff.registry;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Эффекты, используемые оставшимся контентом мода. */
public final class ModMobEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, HellYeahStuffMod.MODID);

    /** Метка от взрывного дротика; её истечение обрабатывает ExplosivenessHandler. */
    public static final DeferredHolder<MobEffect, MobEffect> EXPLOSIVENESS =
            MOB_EFFECTS.register("explosiveness",
                    () -> new MobEffect(MobEffectCategory.HARMFUL, 0xFF6A00) {});

    private ModMobEffects() {}
}
