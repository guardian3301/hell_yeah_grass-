package com.example.hell_yeah_stuff.mixin.client;

import com.example.hell_yeah_stuff.client.GrappleArmAnimator;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Хвост PlayerModel#setupAnim: после всей ванильной анимации рука,
 * держащая трос цепкого дротика, вытягивается в сторону дротика
 * (угол «хитбокс игрока → сущность верёвки») — см. GrappleArmAnimator.
 * Инъекция в хвост обязательна: раньше ванилла перезаписала бы углы.
 */
@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin {

    @Inject(
            method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
            at = @At("TAIL")
    )
    private void hys$grappleRopeArm(LivingEntity entity, float limbSwing, float limbSwingAmount,
                                    float ageInTicks, float netHeadYaw, float headPitch,
                                    CallbackInfo ci) {
        GrappleArmAnimator.pose((PlayerModel<?>) (Object) this, entity);
    }
}
