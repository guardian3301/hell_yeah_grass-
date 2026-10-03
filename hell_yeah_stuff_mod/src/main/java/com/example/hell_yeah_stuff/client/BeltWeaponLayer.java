package com.example.hell_yeah_stuff.client;

import com.example.hell_yeah_stuff.registry.ModDataAttachments;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Предмет за спиной: рендерится ТОЛЬКО если надет ремень и есть BELT_WEAPON.
 * Жёсткая привязка к спине (без физики плаща): трансформ берётся из body.
 */
public class BeltWeaponLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {

    public BeltWeaponLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       T entity, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(entity instanceof Player player)) return;
        if (!ModDataAttachments.isBeltEquipped(player)) return;
        ItemStack beltWeapon = ModDataAttachments.getBeltWeapon(player);
        if (beltWeapon.isEmpty()) return;
        // Не дублировать если предмет уже в руках
        // (всё равно рисуем за спиной — это отдельный слот)

        M parent = this.getParentModel();
        poseStack.pushPose();
        // Позиционируем относительно торса
        // Скопируем трансформ тела: поворот туловища уже учтён в parent.body
        // Для простоты используем абсолютный трансформ: смещаем к спине
        // Наследуем поворот торса через parent.body
        // PoseStack уже содержит трансформ тела из HumanoidMobRenderer
        // Делаем: смещение к центру спины + поворот

        // Привязываем к кости body: копируем её поворот
        // В RenderLayer стек уже после трансляций модели, поэтому достаточно
        // сместить назад по Z тела
        poseStack.translate(0.0F, 0.35F, 0.22F);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(0.0F));
        poseStack.scale(0.85F, 0.85F, 0.85F);

        Minecraft.getInstance().getItemRenderer().renderStatic(
                beltWeapon, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY,
                poseStack, buffer, entity.level(), 0);

        poseStack.popPose();
    }
}
