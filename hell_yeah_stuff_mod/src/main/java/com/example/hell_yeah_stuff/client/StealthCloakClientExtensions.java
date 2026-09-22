package com.example.hell_yeah_stuff.client;

import com.example.hell_yeah_stuff.client.model.CowlArmorModel;
import com.example.hell_yeah_stuff.item.StealthCloakItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Клиентские расширения для скрытной накидки.
 * Регистрирует модель брони cowl для рендера на игроке.
 */
public class StealthCloakClientExtensions implements IClientItemExtensions {

    private static CowlArmorModel<?> armorModel = null;

    @Override
    @OnlyIn(Dist.CLIENT)
    public @Nullable HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
        if (slot == EquipmentSlot.HEAD && stack.getItem() instanceof StealthCloakItem) {
            if (armorModel == null) {
                armorModel = new CowlArmorModel<>(
                        Minecraft.getInstance().getEntityModels().bakeLayer(CowlArmorModel.LAYER)
                );
            }
            return armorModel;
        }
        return original;
    }

    public static void register(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new StealthCloakClientExtensions());
    }
}