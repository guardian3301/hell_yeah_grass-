package com.example.hell_yeah_stuff.client;

import com.example.hell_yeah_stuff.item.RailCrossbowItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/**
 * Даёт рельсовому арбалету те же позы рук от первого/третьего лица,
 * что и у ванильного арбалета (удержание + зарядка).
 */
public class RailCrossbowClientExtensions implements IClientItemExtensions {

    @Override
    public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
        if (entity.isUsingItem() && entity.getUseItem() == stack && entity.getUsedItemHand() == hand) {
            return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
        }
        if (RailCrossbowItem.isCharged(stack)) {
            return HumanoidModel.ArmPose.CROSSBOW_HOLD;
        }
        return HumanoidModel.ArmPose.ITEM;
    }
}
