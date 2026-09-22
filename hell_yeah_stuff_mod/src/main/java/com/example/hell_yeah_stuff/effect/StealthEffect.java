package com.example.hell_yeah_stuff.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Эффект скрытности: делает невидимой броню и предметы игрока,
 * но оставляет саму модель накидки (cowl).
 * Аналог SubtleMobEffect из Caverns and Chasms.
 */
public class StealthEffect extends MobEffect {

    public StealthEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    /**
     * Проверяет, должна ли броня быть скрыта.
     * Вызывается из миксина для рендера.
     */
    public static boolean shouldHideArmor(LivingEntity entity) {
        return entity.hasEffect(com.example.hell_yeah_stuff.registry.ModMobEffects.STEALTH);
    }
}
