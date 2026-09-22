package com.example.hell_yeah_stuff.client;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import com.example.hell_yeah_stuff.entity.GrappleDartEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Рендер самого дротика (обычная стрела со своей текстурой).
 *
 * Верёвка НЕ рисуется здесь: рендер сущности не вызывается за фрустумом
 * камеры, а внутри Sable-плота идёт с матрицей позы саб-левела (мировые
 * вектора верёвки искажались бы — трос «худел» и уезжал от игрока).
 * Трос рисует {@link GrappleRopeRenderer} отдельным мировым проходом
 * (RenderLevelStageEvent).
 */
public class GrappleDartRenderer extends ArrowRenderer<GrappleDartEntity> {

    public static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "textures/entity/projectiles/grapple_dart.png");

    public GrappleDartRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(GrappleDartEntity entity) {
        return TEXTURE;
    }
}
