package com.example.hell_yeah_stuff.network;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import com.example.hell_yeah_stuff.event.CloakNameState;
import com.example.hell_yeah_stuff.event.DashTrailHandler;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Регистрация сетевых пакетов мода. */
@EventBusSubscriber(modid = HellYeahStuffMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ModNetwork {

    @SubscribeEvent
    static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(DashPayload.TYPE, DashPayload.STREAM_CODEC, ModNetwork::handleDash);
        registrar.playToClient(CloakNameHidePayload.TYPE, CloakNameHidePayload.STREAM_CODEC, ModNetwork::handleCloakNameHide);
    }

    private static void handleDash(DashPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                DashTrailHandler.tryServerDash(player);
            }
        });
    }

    private static void handleCloakNameHide(CloakNameHidePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Применяем состояние скрытия ника на клиенте.
            // Ссылок на клиентские классы здесь нет — клиент сам читает
            // CloakNameState при отрисовке имён (RenderNameTagEvent).
            CloakNameState.setHidden(payload.targetUuid(), payload.hidden());
        });
    }

    private ModNetwork() {}
}
