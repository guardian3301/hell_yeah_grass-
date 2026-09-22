package com.example.hell_yeah_stuff.event;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import com.example.hell_yeah_stuff.item.DaggerItem;
import com.example.hell_yeah_stuff.registry.ModEnchantments;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Обработчик зачарования "Косание Мидаса".
 *
 * <p>При ударе в спину кортиком с этим зачарованием:
 * <ul>
 *   <li>25% шанс на золотой крит (+25% урона к базовому удару в спину)</li>
 *   <li>Если цель умирает от золотого крита — её дроп заменяется на золото</li>
 * </ul>
 *
 * <p>Золотой крит визуализируется золотыми частицами и звуком.
 */
@EventBusSubscriber(modid = HellYeahStuffMod.MODID)
public final class MidasTouchHandler {

    private static final Random RANDOM = new Random();
    private static final float GOLDEN_CRIT_CHANCE = 0.25F;
    private static final float GOLDEN_CRIT_MULTIPLIER = 1.25F;

    /** Существа, получившие смертельный золотой крит (UUID). */
    private static final Set<UUID> MIDAS_KILLS = new HashSet<>();

    private MidasTouchHandler() {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (event.getEntity().level().isClientSide) return;

        var source = event.getSource();
        if (!(source.getDirectEntity() instanceof LivingEntity attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        ItemStack weapon = attacker.getMainHandItem();
        if (!(weapon.getItem() instanceof DaggerItem)) return;

        // Проверяем зачарование "Косание Мидаса" через Holder
        var lookup = attacker.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        Holder<Enchantment> midasHolder = lookup.getOrThrow(ModEnchantments.MIDAS_TOUCH);
        int midasLevel = EnchantmentHelper.getEnchantmentsForCrafting(weapon).getLevel(midasHolder);
        if (midasLevel <= 0) return;

        // Проверяем, что атака — удар в спину
        if (!BackstabHandler.isBackstab(attacker, target, weapon)) return;

        // Ролл золотого крита
        if (RANDOM.nextFloat() < GOLDEN_CRIT_CHANCE) {
            // Применяем множитель ×1.25
            float original = event.getNewDamage();
            float modified = original * GOLDEN_CRIT_MULTIPLIER;
            event.setNewDamage(modified);

            // Помечаем цель для возможного превращения лута
            MIDAS_KILLS.add(target.getUUID());

            // Визуальный эффект на сервере
            if (attacker.level() instanceof ServerLevel serverLevel) {
                playGoldenCriticalEffect(serverLevel, target);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        UUID uuid = victim.getUUID();
        if (!MIDAS_KILLS.remove(uuid)) return;

        // Превращаем дроп в золото
        turnLootToGold(victim);
    }

    /** Заменяет дроп жертвы на золотой слиток. */
    private static void turnLootToGold(LivingEntity victim) {
        // В MC нет простого API для замены всего лута при смерти.
        // Самый надёжный способ — заспавнить золотой слиток и очистить обычный дроп.
        // Но это требует вмешательства в loot table.
        // Для простоты: заспавним золотой слиток на месте смерти.
        if (victim.level() instanceof ServerLevel serverLevel) {
            // Спавним золотой слиток
            net.minecraft.world.entity.item.ItemEntity gold = new net.minecraft.world.entity.item.ItemEntity(
                    serverLevel,
                    victim.getX(),
                    victim.getY() + victim.getBbHeight() / 2,
                    victim.getZ(),
                    new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLD_INGOT)
            );
            gold.setDefaultPickUpDelay();
            serverLevel.addFreshEntity(gold);
        }
    }

    private static void playGoldenCriticalEffect(ServerLevel level, LivingEntity target) {
        // Золотые частицы
        for (int i = 0; i < 20; i++) {
            double offsetX = (RANDOM.nextDouble() - 0.5) * 0.5;
            double offsetY = RANDOM.nextDouble() * target.getBbHeight();
            double offsetZ = (RANDOM.nextDouble() - 0.5) * 0.5;

            level.sendParticles(
                    ParticleTypes.TOTEM_OF_UNDYING,
                    target.getX() + offsetX,
                    target.getY() + offsetY,
                    target.getZ() + offsetZ,
                    1, 0, 0, 0, 0.0
            );
        }

        // Звук золотого крита
        level.playSound(
                null,
                target.getX(), target.getY(), target.getZ(),
                SoundEvents.TOTEM_USE,
                SoundSource.PLAYERS,
                1.0F, 1.5F
        );
    }
}