package com.example.hell_yeah_stuff.client;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import com.example.hell_yeah_stuff.event.DashTrailHandler;
import com.example.hell_yeah_stuff.network.DashPayload;
import com.example.hell_yeah_stuff.registry.ModEnchantments;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/** Бинды мода (рывок на поножах). */
@EventBusSubscriber(modid = HellYeahStuffMod.MODID, value = Dist.CLIENT)
public final class ModKeyMappings {

    /** Бинд рывка зачарования «Рывок» на поножах (по умолчанию G). */
    public static final KeyMapping DASH_KEY = new KeyMapping(
            "key.hell_yeah_stuff.dash",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.hell_yeah_stuff");

    /** Сила рывка по горизонтали и небольшой подъём. */
    private static final double DASH_STRENGTH = 1.1D;
    private static final double DASH_UP = 0.18D;

    /** Тик последнего рывка (player.tickCount) для клиентского кулдауна. */
    private static int lastDashTick = -1000;

    @EventBusSubscriber(modid = HellYeahStuffMod.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        @SubscribeEvent
        static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(DASH_KEY);
        }

        private Registration() {}
    }

    /**
     * Рывок зачарования «Рывок» (поножи): движение применяем на клиенте
     * (оно клиент-авторитативно, как рывок крюка-кошки), а серверу шлём
     * DashPayload — он проверит зачарование/кулдаун и включит след частиц.
     */
    @SubscribeEvent
    static void onClientTickDash(ClientTickEvent.Post event) {
        boolean pressed = false;
        while (DASH_KEY.consumeClick()) {
            pressed = true;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (!pressed || player == null || mc.screen != null || player.isSpectator()) {
            return;
        }
        if (ModEnchantments.level(player.getItemBySlot(EquipmentSlot.LEGS), ModEnchantments.DASH) <= 0) {
            return;
        }
        if (player.tickCount - lastDashTick < DashTrailHandler.DASH_COOLDOWN_TICKS) {
            return;
        }
        lastDashTick = player.tickCount;

        // Горизонтальный рывок в направлении взгляда + небольшой подъём.
        Vec3 look = player.getLookAngle();
        Vec3 dir = new Vec3(look.x, 0.0D, look.z);
        if (dir.lengthSqr() < 1.0E-4D) {
            dir = Vec3.directionFromRotation(0.0F, player.getYRot());
        }
        dir = dir.normalize();
        player.setDeltaMovement(player.getDeltaMovement()
                .add(dir.x * DASH_STRENGTH, DASH_UP, dir.z * DASH_STRENGTH));
        player.fallDistance = 0.0F;

        // Сервер: валидация + частицы для всех игроков.
        PacketDistributor.sendToServer(DashPayload.INSTANCE);
    }

    private ModKeyMappings() {}
}
