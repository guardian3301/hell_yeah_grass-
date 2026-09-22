package com.example.hell_yeah_stuff.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/**
 * Даёт мульти-арбалету те же позы рук от первого/третьего лица,
 * что и у ванильного арбалета (удержание + зарядка).
 */
public class MultiCrossbowClientExtensions implements IClientItemExtensions {

    @Override
    public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
        if (entity.isUsingItem() && entity.getUseItem() == stack) {
            return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
        }
        return HumanoidModel.ArmPose.CROSSBOW_HOLD;
    }
}
