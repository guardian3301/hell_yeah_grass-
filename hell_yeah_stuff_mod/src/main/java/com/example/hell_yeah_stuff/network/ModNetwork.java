package com.example.hell_yeah_stuff.network;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import com.example.hell_yeah_stuff.event.BeltHandler;
import com.example.hell_yeah_stuff.event.DashTrailHandler;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Регистрация сетевых пакетов оставшегося контента мода. */
@EventBusSubscriber(modid = HellYeahStuffMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ModNetwork {

    @SubscribeEvent
    static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(DashPayload.TYPE, DashPayload.STREAM_CODEC, ModNetwork::handleDash);
        registrar.playToServer(BeltSwapPayload.TYPE, BeltSwapPayload.STREAM_CODEC, ModNetwork::handleBeltSwap);
        registrar.playToServer(BeltUsePayload.TYPE, BeltUsePayload.STREAM_CODEC, ModNetwork::handleBeltUse);
    }

    private static void handleDash(DashPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                DashTrailHandler.tryServerDash(player);
            }
        });
    }

    private static void handleBeltSwap(BeltSwapPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                BeltHandler.handleSwap(player);
            }
        });
    }

    private static void handleBeltUse(BeltUsePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                BeltHandler.handleUse(player);
            }
        });
    }

    private ModNetwork() {}
}
