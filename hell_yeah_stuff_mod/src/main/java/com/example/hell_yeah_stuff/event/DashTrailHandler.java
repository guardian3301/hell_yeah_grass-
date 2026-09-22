package com.example.hell_yeah_stuff.event;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import com.example.hell_yeah_stuff.registry.ModEnchantments;
import com.example.hell_yeah_stuff.registry.ModParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Серверная часть зачарования «Рывок» (hell_yeah_stuff:dash, поножи):
 * проверка пакета рывка и след частиц {@code hell_yeah_stuff:dash_trail}.
 *
 * След: полсекунды (10 тиков) частицы идут из центра хитбокса
 * (торса) игрока и остаются позади — хвост за летящим игроком.
 *
 * Само движение рывка применяет КЛИЕНТ (см. ModKeyMappings) — движение
 * игрока клиент-авторитативно (как рывок крюка-кошки); сервер же
 * валидирует зачарование и кулдаун и спавнит частицы для всех.
 */
@EventBusSubscriber(modid = HellYeahStuffMod.MODID)
public final class DashTrailHandler {

    /** Кулдаун рывка в тиках (2 секунды). Используется и клиентом. */
    public static final int DASH_COOLDOWN_TICKS = 40;
    /** Длительность следа в тиках: 10 = 0.5 секунды. */
    private static final int TRAIL_TICKS = 10;
    /** Частиц за тик. */
    private static final int PARTICLES_PER_TICK = 2;
    /** Люфт кулдауна на рассинхрон клиент/сервер (тиков). */
    private static final int COOLDOWN_SLACK = 3;

    /** Игроки с активным следом -> оставшиеся тики. */
    private static final Map<UUID, Integer> ACTIVE = new HashMap<>();
    /** Последний рывок: игрок -> gameTime. */
    private static final Map<UUID, Long> LAST_DASH = new HashMap<>();

    private DashTrailHandler() {
    }

    /**
     * Серверная валидация рывка из пакета DashPayload: на поножах есть
     * зачарование «Рывок» и кулдаун прошёл — тогда включаем след.
     */
    public static void tryServerDash(ServerPlayer player) {
        if (ModEnchantments.level(player.getItemBySlot(EquipmentSlot.LEGS), ModEnchantments.DASH) <= 0) {
            return;
        }
        long now = player.level().getGameTime();
        Long last = LAST_DASH.get(player.getUUID());
        if (last != null && now - last < DASH_COOLDOWN_TICKS - COOLDOWN_SLACK) {
            return;
        }
        LAST_DASH.put(player.getUUID(), now);
        ACTIVE.put(player.getUUID(), TRAIL_TICKS);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (ACTIVE.isEmpty() || !(player.level() instanceof ServerLevel server)) {
            return;
        }
        Integer left = ACTIVE.get(player.getUUID());
        if (left == null) {
            return;
        }
        if (left <= 0 || !player.isAlive()) {
            ACTIVE.remove(player.getUUID());
            return;
        }
        ACTIVE.put(player.getUUID(), left - 1);

        // Центр хитбокса/торса (getY(0.5)); небольшой разброс, нулевая
        // скорость — частицы «отстают» от игрока и образуют хвост.
        server.sendParticles(ModParticles.DASH_TRAIL.get(),
                player.getX(), player.getY(0.5D), player.getZ(),
                PARTICLES_PER_TICK, 0.12D, 0.12D, 0.12D, 0.0D);
    }
}
