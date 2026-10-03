package com.example.hell_yeah_stuff.client;

import com.example.hell_yeah_stuff.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import com.example.hell_yeah_stuff.HellYeahStuffMod;

/**
 * Слой брони для ремня: рендерит BeltModel поверх chestplate-слота.
 * Копирует позу тела из родительской HumanoidModel, крепится намертво
 * (без качания как у плаща — просто copyFrom body).
 */
public class BeltArmorLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "textures/models/armor/belt.png");

    private final net.minecraft.client.model.geom.ModelPart beltBody;

    public BeltArmorLayer(RenderLayerParent<T, M> renderer, EntityModelSet modelSet) {
        super(renderer);
        this.beltBody = modelSet.bakeLayer(BeltModel.LAYER).getChild("body");
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       T entity, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.isEmpty() || !chest.is(ModItems.BELT.get())) return;

        M parentModel = this.getParentModel();
        // Копируем трансформ торса родительской модели
        this.beltBody.copyFrom(parentModel.body);

        poseStack.pushPose();
        // Без дополнительных смещений — жёсткая привязка к торсу
        beltBody.render(poseStack,
                buffer.getBuffer(net.minecraft.client.renderer.RenderType.armorCutoutNoCull(TEXTURE)),
                packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
