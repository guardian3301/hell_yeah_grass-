package com.example.hell_yeah_stuff.client;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import com.example.hell_yeah_stuff.entity.AmethystGrenadeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Рендер аметистовой гранаты с СОБСТВЕННОЙ UV-развёрткой.
 *
 * Текстура 32x32 поделена на 4 равные части 16x16:
 *  1 (верх-лево)  — вид сбоку, арт рисуется ПО ДИАГОНАЛИ: хвост в левом
 *                   нижнем углу, остриё в правом верхнем (как иконка предмета);
 *  2, 3, 4        — зарезервированы, пока не используются.
 *
 * Геометрия — 4 боковые грани, как у стрел/дротиков (ArrowRenderer), но БЕЗ
 * заднего креста, перпендикулярного стреле. Полоса выборки расширена на
 * 2 текселя во все стороны относительно базовой (толщина 5px -> 9px,
 * концы вынесены на 2px за диагональ) — арт может быть крупнее и толще.
 */
public class AmethystGrenadeRenderer extends EntityRenderer<AmethystGrenadeEntity> {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HellYeahStuffMod.MODID, "textures/entity/projectiles/amethyst_grenade.png");

    // ---- Часть 1: диагональная полоса вида сбоку (текселя, /32 в UV) ----
    // Базовая полоса (толщина 5px по перпендикуляру, вдоль анти-диагонали
    // квадранта) расширена на 2px во все стороны:
    //   OUT = (2.5 - 2 - 4.5)/sqrt(2) со знаком: вынос углов за квадрант;
    //   IN  = 5/sqrt(2): внутренние смещения углов.
    private static final float OUT = 4.0F / (float) Math.sqrt(2.0D); // ~2.83
    private static final float IN  = 5.0F / (float) Math.sqrt(2.0D); // ~3.54
    private static final float SIDE_TAIL_LO_U = -OUT        / 32.0F; // (-8,-2)
    private static final float SIDE_TAIL_LO_V = (16 - IN)   / 32.0F;
    private static final float SIDE_TIP_LO_U  = (16 - IN)   / 32.0F; // (8,-2)
    private static final float SIDE_TIP_LO_V  = -OUT        / 32.0F;
    private static final float SIDE_TIP_HI_U  = (16 + OUT)  / 32.0F; // (8,2)
    private static final float SIDE_TIP_HI_V  = IN          / 32.0F;
    private static final float SIDE_TAIL_HI_U = IN          / 32.0F; // (-8,2)
    private static final float SIDE_TAIL_HI_V = (16 + OUT)  / 32.0F;

    public AmethystGrenadeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(AmethystGrenadeEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(AmethystGrenadeEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(
                Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(
                Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));
        float shake = (float) entity.shakeTime - partialTicks;
        if (shake > 0.0F) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(-Mth.sin(shake * 3.0F) * shake));
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        poseStack.scale(0.05625F, 0.05625F, 0.05625F);
        poseStack.translate(-4.0F, 0.0F, 0.0F);

        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutout(this.getTextureLocation(entity)));

        // Четыре боковые грани (часть 1): диагональная полоса,
        // хвост (x=-8) — левый нижний угол, остриё (x=8) — правый верхний.
        // Заднего креста нет.
        PoseStack.Pose pose = poseStack.last();
        for (int j = 0; j < 4; j++) {
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            vertex(pose, vc, -8, -2, 0, SIDE_TAIL_LO_U, SIDE_TAIL_LO_V, packedLight);
            vertex(pose, vc,  8, -2, 0, SIDE_TIP_LO_U, SIDE_TIP_LO_V, packedLight);
            vertex(pose, vc,  8,  2, 0, SIDE_TIP_HI_U, SIDE_TIP_HI_V, packedLight);
            vertex(pose, vc, -8,  2, 0, SIDE_TAIL_HI_U, SIDE_TAIL_HI_V, packedLight);
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer vc,
                               int x, int y, int z, float u, float v, int light) {
        vc.addVertex(pose, (float) x, (float) y, (float) z)
                .setColor(-1)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
    }
}
