package com.example.hell_yeah_stuff.mixin.client;

import com.example.hell_yeah_stuff.effect.StealthEffect;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Миксин для скрытия брони при эффекте скрытности (зачарование "Забвение").
 * Перехватывает рендер слоёв брони в методе render.
 */
@Mixin(net.minecraft.client.renderer.entity.player.PlayerRenderer.class)
public abstract class PlayerRendererMixin {

    /**
     * Пропускаем рендер слоёв брони (layers), если у игрока есть эффект скрытности.
     * Инъекция в начало метода render перед рендером слоёв.
     */
    @Inject(
            method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFFFFLnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;render(Lnet/minecraft/world/entity/LivingEntity;FFFFFLnet/minecraft/client/renderer/MultiBufferSource;I)V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void hys$skipArmorLayers(AbstractClientPlayer player, float entityYaw, float partialTicks, float poseProgress, float ageInTicks, float netHeadYaw, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        // Проверяем эффект скрытности
        if (StealthEffect.shouldHideArmor(player)) {
            // Отменяем дальнейший рендер (включая слои)
            ci.cancel();
        }
    }
}