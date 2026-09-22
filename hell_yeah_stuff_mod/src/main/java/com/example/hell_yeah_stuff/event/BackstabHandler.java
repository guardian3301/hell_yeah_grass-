package com.example.hell_yeah_stuff.event;

import com.example.hell_yeah_stuff.item.DaggerItem;
import com.example.hell_yeah_stuff.registry.ModEnchantments;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.Random;

/**
 * Обработчик механики удара в спину.
 * Вся логика проверяется на сервере.
 *
 * <p>Золотой крит заменён зачарованием «Косание Мидаса» ({@link ModEnchantments#MIDAS_TOUCH}).
 * Логика критического удара и статуи удалена.
 */
public final class BackstabHandler {

    private static final Random RANDOM = new Random();

    // Базовая ширина задней дуги
    public static final float BASE_BACKSTAB_ARC = 80.0F;

    // Добавка зачарования "Коварная рука" за уровень
    public static final float TREACHEROUS_HAND_ARC_BONUS = 20.0F;

    private BackstabHandler() {}

    // ==================== СЕРВЕРНЫЙ ОБРАБОТЧИК УРОНА ====================

    /**
     * Применяет множитель удара в спину при атаке кортиком.
     * Вся проверка выполняется на сервере.
     */
    @EventBusSubscriber(modid = "hell_yeah_stuff")
    public static final class Events {

        private Events() {}

        @SubscribeEvent(priority = EventPriority.HIGH)
        public static void onLivingDamage(LivingDamageEvent.Pre event) {
            // Работаем только на сервере
            if (event.getEntity().level().isClientSide) {
                return;
            }

            DamageSource source = event.getSource();
            Entity directEntity = source.getDirectEntity();
            Entity causingEntity = source.getEntity();

            if (directEntity == null || causingEntity == null) {
                return;
            }

            // Удар в спину только от живого атакующего (без снарядов)
            if (!(directEntity instanceof LivingEntity attacker) || !(causingEntity instanceof LivingEntity)) {
                return;
            }

            LivingEntity target = event.getEntity();
            if (!(target instanceof LivingEntity livingTarget)) {
                return;
            }

            ItemStack weapon = attacker.getMainHandItem();
            if (!(weapon.getItem() instanceof DaggerItem dagger)) {
                return;
            }

            // Проверяем, является ли атака ударом в спину
            if (!isBackstab(attacker, livingTarget, weapon)) {
                return;
            }

            // Базовый множитель от самого кортика
            float baseMultiplier = dagger.getBackstabMultiplier();

            // Финальный множитель (золотой крит заменён зачарованием, здесь просто базовый)
            float finalMultiplier = baseMultiplier;

            // Применяем множитель к финальному урону (после брони и модификаторов)
            float original = event.getNewDamage();
            float modified = original * finalMultiplier;
            event.setNewDamage(modified);

            // Визуальные эффекты на сервере
            if (attacker.level() instanceof ServerLevel serverLevel) {
                playBackstabEffect(serverLevel, livingTarget);
            }
        }
    }

    // ==================== ЛОГИКА ПРОВЕРКИ ====================

    /**
     * Проверяет, является ли атака ударом в спину.
     *
     * @param attacker Атакующий
     * @param target   Цель
     * @param weapon   Оружие атакующего
     * @return true, если атака является ударом в спину
     */
    public static boolean isBackstab(LivingEntity attacker, LivingEntity target, ItemStack weapon) {
        if (!(weapon.getItem() instanceof DaggerItem)) {
            return false;
        }

        // Проверяем дистанцию (обычная досягаемость + небольшой запас)
        double range = attacker.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE) + 0.5;
        double distance = attacker.distanceTo(target);
        if (distance > range) {
            return false;
        }

        // Проверяем прямую видимость (не через стену)
        Vec3 attackerEye = attacker.getEyePosition();
        Vec3 targetPos = target.getEyePosition();
        if (!hasLineOfSight(attacker.level(), attackerEye, targetPos)) {
            return false;
        }

        // Проверяем угол
        float requiredArc = getBackstabArc(weapon);
        return isWithinBackArc(attacker, target, requiredArc);
    }

    /**
     * Проверяет прямую видимость между двумя точками.
     */
    private static boolean hasLineOfSight(net.minecraft.world.level.Level level, Vec3 from, Vec3 to) {
        Vec3 delta = to.subtract(from);
        double maxDistance = delta.length();
        Vec3 normalized = delta.normalize();

        // Шагаем по лучу
        double step = 0.5;
        for (double dist = 0; dist < maxDistance; dist += step) {
            Vec3 point = from.add(normalized.scale(dist));
            if (!level.getBlockState(new net.minecraft.core.BlockPos((int) Math.floor(point.x),
                    (int) Math.floor(point.y), (int) Math.floor(point.z))).isAir()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Проверяет, находится ли атакующий в задней дуге цели.
     *
     * @param attacker Атакующий
     * @param target   Цель
     * @param arcDegrees Ширина дуги в градусах
     * @return true, если атакующий находится в задней дуге
     */
    private static boolean isWithinBackArc(LivingEntity attacker, LivingEntity target, float arcDegrees) {
        // Получаем позицию глаз цели
        Vec3 targetEyePos = target.getEyePosition();

        // Получаем горизонтальное направление взгляда цели
        Vec3 targetLookVec = target.getLookAngle();
        targetLookVec = new Vec3(targetLookVec.x, 0, targetLookVec.z).normalize();

        // Вектор от цели к атакующему (горизонтальный)
        Vec3 toAttacker = attacker.getEyePosition().subtract(targetEyePos);
        toAttacker = new Vec3(toAttacker.x, 0, toAttacker.z).normalize();

        // Вычисляем угол между векторами через dot product
        double dot = targetLookVec.dot(toAttacker);

        // Если атакующий спереди (dot > 0), это не удар в спину
        if (dot > 0) {
            return false;
        }

        // Преобразуем dot product в угол
        // dot = cos(angle), но нам нужен угол от заднего направления
        // Угол от заднего направления = 180° - angle от переднего
        // cos(180° - angle) = -cos(angle) = -dot
        double angleFromBack = Math.acos(-dot);
        double angleDegrees = Math.toDegrees(angleFromBack);

        // Проверяем, находится ли атакующий в пределах задней дуги
        return angleDegrees <= arcDegrees / 2.0;
    }

    /**
     * Получает ширину задней дуги с учётом зачарования "Коварная рука".
     *
     * @param weapon Оружие
     * @return Ширина дуги в градусах
     */
    private static float getBackstabArc(ItemStack weapon) {
        float arc = BASE_BACKSTAB_ARC;

        // Проверяем зачарование "Коварная рука" (ResourceKey -> уровень через ItemEnchantments)
        ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(weapon);
        for (var entry : enchantments.entrySet()) {
            if (entry.getKey().is(ModEnchantments.TREACHEROUS_HAND)) {
                int level = entry.getIntValue();
                if (level > 0) {
                    arc += TREACHEROUS_HAND_ARC_BONUS * level;
                }
                break;
            }
        }

        return arc;
    }

    // ==================== ЭФФЕКТЫ ====================

    /**
     * Воспроизводит эффект обычного удара в спину.
     */
    public static void playBackstabEffect(ServerLevel level, LivingEntity target) {
        // Красные частицы
        for (int i = 0; i < 10; i++) {
            double offsetX = (RANDOM.nextDouble() - 0.5) * 0.3;
            double offsetY = RANDOM.nextDouble() * target.getBbHeight();
            double offsetZ = (RANDOM.nextDouble() - 0.5) * 0.3;

            level.sendParticles(
                    ParticleTypes.CRIT,
                    target.getX() + offsetX,
                    target.getY() + offsetY,
                    target.getZ() + offsetZ,
                    1, 0, 0, 0, 0.0
            );
        }

        // Звук удара в спину — особый, узнаваемый
        level.playSound(
                null,
                target.getX(), target.getY(), target.getZ(),
                SoundEvents.PLAYER_ATTACK_CRIT,
                SoundSource.PLAYERS,
                1.0F, 1.2F
        );

        // Дополнительный звук "прокола" для ясности
        level.playSound(
                null,
                target.getX(), target.getY(), target.getZ(),
                SoundEvents.SHIELD_BREAK,
                SoundSource.PLAYERS,
                0.8F, 1.5F
        );
    }
}