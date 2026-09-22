package com.example.hell_yeah_stuff.event;

import com.example.hell_yeah_stuff.item.StealthCloakItem;
import com.example.hell_yeah_stuff.network.CloakNameHidePayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Серверный обработчик скрытной накидки.
 *
 * <p>Следит за тем, кто из игроков носит накидку в Curios-слоте head, и при
 * изменении состояния рассылает {@link CloakNameHidePayload} всем клиентам,
 * чтобы ник владельца скрывался у всех. Сам выбор «скрыт/не скрыт»
 * определяется исключительно сервером — клиент лишь отображает результат.
 */
public final class StealthCloakHandler {

    private StealthCloakHandler() {}

    @EventBusSubscriber(modid = "hell_yeah_stuff")
    public static final class Events {

        /** Кэш последнего известного состояния накидки по UUID игрока. */
        private static final Map<UUID, Boolean> LAST_CLOAK_STATE = new HashMap<>();

        private Events() {}

        /**
         * Проверяет состояние накидки каждый тик и рассылает изменения.
         * Для уменьшения нагрузки используем локальный кэш и отправляем
         * пакет только при изменении состояния.
         */
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
            Boolean last = LAST_CLOAK_STATE.get(player.getUUID());

            if (last == null || last != hasCloak) {
                LAST_CLOAK_STATE.put(player.getUUID(), hasCloak);

                // Обновляем общее состояние
                CloakNameState.setHidden(player.getUUID(), hasCloak);

                // Рассылаем всем клиентам
                CloakNameHidePayload payload = new CloakNameHidePayload(player.getUUID(), hasCloak);
                PacketDistributor.sendToAllPlayers(payload);
            }
        }

        /**
         * Очищает кэш при выходе игрока.
         */
        @SubscribeEvent
        public static void onPlayerLoggedOut(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
            Player player = event.getEntity();
            LAST_CLOAK_STATE.remove(player.getUUID());
            CloakNameState.showName(player.getUUID());

            // Сообщаем всем, что ник больше не скрыт
            CloakNameHidePayload payload = new CloakNameHidePayload(player.getUUID(), false);
            PacketDistributor.sendToAllPlayers(payload);
        }
    }
}
