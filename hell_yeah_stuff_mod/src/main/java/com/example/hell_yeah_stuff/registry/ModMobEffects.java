package com.example.hell_yeah_stuff.registry;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import com.example.hell_yeah_stuff.effect.StealthEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Эффекты (статусы) мода. */
public final class ModMobEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, HellYeahStuffMod.MODID);

    /**
     * «Взрывоопасность» — метка от взрывного дротика: висит фиксированное
     * время (3 с), сама по себе ничего не тикает, а по истечении срабатывает
     * взрыв (см. {@link com.example.hell_yeah_stuff.event.ExplosivenessHandler}).
     * Оранжевый цвет частиц статуса.
     */
    public static final DeferredHolder<MobEffect, MobEffect> EXPLOSIVENESS =
            MOB_EFFECTS.register("explosiveness",
                    () -> new MobEffect(MobEffectCategory.HARMFUL, 0xFF6A00) {});

    /**
     * «Скрытность» — делает невидимой броню и предметы игрока при приседании
     * с зачарованием «Забвение». Накидка (cowl) остаётся видимой.
     * Аналог SubtleMobEffect из Caverns and Chasms.
     */
    public static final DeferredHolder<MobEffect, MobEffect> STEALTH =
            MOB_EFFECTS.register("stealth",
                    () -> new StealthEffect(MobEffectCategory.BENEFICIAL, 0x202025));

    private ModMobEffects() {}
}