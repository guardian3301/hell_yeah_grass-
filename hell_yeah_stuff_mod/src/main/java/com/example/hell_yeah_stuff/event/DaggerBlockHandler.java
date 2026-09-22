package com.example.hell_yeah_stuff.event;

import com.example.hell_yeah_stuff.item.DaggerItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Обработчик блокирования ударов кортиком (аналог щита).
 * Серверная проверка результата блокирования.
 *
 * <p>Добавлен кулдаун после успешного блока (по умолчанию 500мс = 10 тиков).
 * Во время кулдауна блокирование не срабатывает.
 */
public final class DaggerBlockHandler {

    /** Кулдаун после успешного блока в тиках (500мс = 10 тиков). */
    private static final int BLOCK_COOLDOWN_TICKS = 10;

    /** Игроки на кулдауне: UUID -> оставшиеся тики. */
    private static final Map<UUID, Integer> BLOCK_COOLDOWNS = new HashMap<>();

    private DaggerBlockHandler() {}

    @EventBusSubscriber(modid = "hell_yeah_stuff")
    public static final class Events {

        private Events() {}

        /**
         * Перехватывает входящий урон и обрабатывает блокирование кортиком.
         * LivingIncomingDamageEvent — до вычисления брони, как LivingHurtEvent в 1.20.
         */
        @SubscribeEvent(priority = EventPriority.HIGH)
        public static void onIncomingDamage(LivingIncomingDamageEvent event) {
            if (event.getEntity().level().isClientSide) {
                return;
            }

            LivingEntity victim = event.getEntity();
            if (!(victim instanceof Player player)) {
                return;
            }

            // Проверяем кулдаун
            UUID uuid = player.getUUID();
            if (BLOCK_COOLDOWNS.getOrDefault(uuid, 0) > 0) {
                return; // На кулдауне — блок не срабатывает
            }

            ItemStack useItem = player.getUseItem();
            if (!(useItem.getItem() instanceof DaggerItem dagger)) {
                return;
            }
            if (!player.isUsingItem()) {
                return;
            }

            DamageSource source = event.getSource();

            // Проверяем, может ли урон быть заблокирован (исключаем ванильные обходы щитов)
            if (isUnblockableDamage(source)) {
                return;
            }

            // Проверяем направление — источник должен быть спереди
            if (!isBlockableDirection(player, source)) {
                return;
            }

            // Полный блок урона
            event.setCanceled(true);

            // Тратим прочность кортика
            if (!useItem.isEmpty()) {
                useItem.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
            }

            // Устанавливаем кулдаун
            BLOCK_COOLDOWNS.put(uuid, BLOCK_COOLDOWN_TICKS);

            // Звук блокирования
            player.level().playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SHIELD_BLOCK,
                    SoundSource.PLAYERS,
                    1.0F, 1.0F
            );

            // Частицы блокирования
            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.CRIT,
                        player.getX(),
                        player.getY() + player.getBbHeight() / 2,
                        player.getZ(),
                        10, 0.3, 0.3, 0.3, 0.1
                );
            }
        }

        /**
         * Тикер для уменьшения кулдаунов.
         */
        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            if (event.getEntity().level().isClientSide) return;
            Player player = event.getEntity();
            UUID uuid = player.getUUID();
            Integer remaining = BLOCK_COOLDOWNS.get(uuid);
            if (remaining != null) {
                if (remaining <= 1) {
                    BLOCK_COOLDOWNS.remove(uuid);
                } else {
                    BLOCK_COOLDOWNS.put(uuid, remaining - 1);
                }
            }
        }

        /**
         * Проверяет, находится ли источник урона спереди от игрока.
         */
        private static boolean isBlockableDirection(Player player, DamageSource source) {
            Entity attacker = source.getEntity();
            Vec3 sourcePos;

            if (attacker != null) {
                sourcePos = attacker.getEyePosition();
            } else {
                return true;
            }

            Vec3 lookVec = player.getLookAngle();
            Vec3 toSource = sourcePos.subtract(player.getEyePosition()).normalize();

            double dot = lookVec.dot(toSource);
            return dot > 0.0;
        }

        /**
         * Проверяет, является ли урон неблокируемым.
         */
        private static boolean isUnblockableDamage(DamageSource source) {
            return source.is(DamageTypes.WITHER)
                    || source.is(DamageTypes.THORNS)
                    || source.is(DamageTypes.DROWN)
                    || source.is(DamageTypes.FELL_OUT_OF_WORLD)
                    || source.is(DamageTypes.STARVE)
                    || source.is(DamageTypes.IN_WALL);
        }
    }
}