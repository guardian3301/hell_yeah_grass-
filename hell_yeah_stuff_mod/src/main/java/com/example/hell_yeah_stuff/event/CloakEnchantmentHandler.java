package com.example.hell_yeah_stuff.event;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import com.example.hell_yeah_stuff.item.StealthCloakItem;
import com.example.hell_yeah_stuff.registry.ModEnchantments;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Серверная механика зачарований скрытной накидки:
 * <ul>
 *   <li><b>Забвение</b> ({@code hell_yeah_stuff:oblivion}) — при удержании
 *       Shift в течение 1 секунды игрок становится полностью невидимым
 *       (без частиц). Невидимость спадает, как только Shift отпущен.</li>
 *   <li><b>Скрытный шаг</b> ({@code hell_yeah_stuff:shadow_step}) — двойной
 *       тап Shift: телепортация за спину существа, на которое направлен взгляд
 *       (до 20 блоков на I, до 30 на II).</li>
 * </ul>
 */
@EventBusSubscriber(modid = HellYeahStuffMod.MODID)
public final class CloakEnchantmentHandler {

    /** Время удержания Shift до невидимости (1 секунда = 20 тиков). */
    private static final int OBLIVION_HOLD_TICKS = 20;
    /** Максимальный интервал между нажатиями Shift для двойного тапа (0.5 c). */
    private static final int DOUBLE_TAP_WINDOW = 10;
    /** Дальность скрытного шага по уровням (индекс 0 не используется). */
    private static final int[] SHADOW_STEP_RANGE = {0, 20, 30};
    /** Радиус поиска цели по лучу взгляда. */
    private static final double TARGET_SEARCH_RADIUS = 0.5;

    /** Игрок -> сколько тиков уже удерживается Shift. */
    private static final Map<UUID, Integer> SHIFT_HOLD_TICKS = new HashMap<>();
    /** Игрок -> игровой тик последнего нажатия Shift. */
    private static final Map<UUID, Long> LAST_SHIFT_PRESS = new HashMap<>();

    private CloakEnchantmentHandler() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) {
            return;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        boolean hasCloak = StealthCloakItem.isEquipped(player);
        boolean hasOblivion = hasCloak && StealthCloakItem.hasOblivionEnchantment(player);
        boolean hasShadowStep = hasCloak && StealthCloakItem.hasShadowStepEnchantment(player);
        UUID uuid = player.getUUID();

        if (player.isShiftKeyDown()) {
            int held = SHIFT_HOLD_TICKS.getOrDefault(uuid, 0) + 1;
            SHIFT_HOLD_TICKS.put(uuid, held);

            // Первый тик нажатия: проверяем двойной тап для скрытного шага
            if (held == 1) {
                long now = player.level().getGameTime();
                Long last = LAST_SHIFT_PRESS.get(uuid);
                if (hasShadowStep && last != null && now - last > 0 && now - last <= DOUBLE_TAP_WINDOW) {
                    int level = StealthCloakItem.getShadowStepLevel(player);
                    tryShadowStep(serverPlayer, SHADOW_STEP_RANGE[Math.min(level, SHADOW_STEP_RANGE.length - 1)]);
                    // Сбрасываем, чтобы не сработало повторно
                    LAST_SHIFT_PRESS.remove(uuid);
                } else {
                    LAST_SHIFT_PRESS.put(uuid, now);
                }
            }

            // Забвение: полная невидимость после 1 секунды удержания
            if (hasOblivion && held == OBLIVION_HOLD_TICKS) {
                applyOblivionInvisibility(serverPlayer);
            }
        } else {
            SHIFT_HOLD_TICKS.put(uuid, 0);
            // Сбрасываем невидимость, если она была наша (не от зелья)
            if (player.isInvisible() && !player.hasEffect(MobEffects.INVISIBILITY)) {
                player.setInvisible(false);
            }
        }
    }

    /** Применяет полную невидимость (без частиц). */
    private static void applyOblivionInvisibility(ServerPlayer player) {
        if (!player.isInvisible()) {
            player.setInvisible(true);
            if (player.level() instanceof ServerLevel level) {
                // Облачко частиц — эффект активации
                level.sendParticles(ParticleTypes.SMOKE,
                        player.getX(), player.getY() + player.getBbHeight() / 2, player.getZ(),
                        15, 0.2, 0.2, 0.2, 0.02);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 0.5F, 0.5F);
            }
        }
    }

    /** Скрытный шаг: телепортация за спину цели на линии взгляда. */
    private static void tryShadowStep(ServerPlayer player, int maxRange) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();

        // Ищем цель на линии взгляда в пределах дальности
        LivingEntity target = null;
        double bestDistance = maxRange + 1;

        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(maxRange))
                .inflate(TARGET_SEARCH_RADIUS);
        List<Entity> candidates = player.level().getEntities(player, searchBox,
                e -> e instanceof LivingEntity && e.isAlive());

        for (Entity candidate : candidates) {
            LivingEntity living = (LivingEntity) candidate;
            Vec3 toTarget = living.getEyePosition().subtract(eye);
            double distance = toTarget.length();
            if (distance > maxRange) {
                continue;
            }
            // Угол между взглядом и направлением на цель должен быть мал
            Vec3 dir = toTarget.normalize();
            if (look.dot(dir) > 0.985) {
                if (distance < bestDistance) {
                    bestDistance = distance;
                    target = living;
                }
            }
        }

        if (target == null) {
            return;
        }

        // Точка за спиной цели на её направлении взгляда
        Vec3 targetLook = target.getLookAngle();
        Vec3 backOffset = new Vec3(targetLook.x, 0, targetLook.z).normalize().scale(0.8);
        Vec3 teleportPos = target.position().subtract(backOffset).add(0, 0.1, 0);

        if (player.randomTeleport(teleportPos.x, teleportPos.y, teleportPos.z, false)) {
            if (player.level() instanceof ServerLevel level) {
                level.sendParticles(ParticleTypes.PORTAL,
                        player.getX(), player.getY() + 1, player.getZ(),
                        20, 0.3, 0.5, 0.3, 0.1);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }
}
