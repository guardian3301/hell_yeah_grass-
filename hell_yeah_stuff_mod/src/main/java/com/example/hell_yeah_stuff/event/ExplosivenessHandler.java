package com.example.hell_yeah_stuff.event;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import com.example.hell_yeah_stuff.entity.ExplosiveDartEntity;
import com.example.hell_yeah_stuff.registry.ModMobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

/**
 * Детонация «Взрывоопасности»: когда эффект от взрывного дротика истекает
 * (через 3 секунды после попадания по мобу), в точке цели происходит тот же
 * взрыв, что и при попадании дротика в блок ({@link ExplosiveDartEntity#detonate}).
 * Если моб умер раньше — эффект снимается, а не истекает, и взрыва не будет.
 */
@EventBusSubscriber(modid = HellYeahStuffMod.MODID)
public final class ExplosivenessHandler {

    @SubscribeEvent
    static void onEffectExpired(MobEffectEvent.Expired event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) {
            return;
        }
        MobEffectInstance instance = event.getEffectInstance();
        if (instance == null || instance.getEffect().value() != ModMobEffects.EXPLOSIVENESS.get()) {
            return;
        }
        ExplosiveDartEntity.detonate(entity.level(), entity,
                entity.getX(), entity.getY(0.5D), entity.getZ());
    }

    private ExplosivenessHandler() {}
}
