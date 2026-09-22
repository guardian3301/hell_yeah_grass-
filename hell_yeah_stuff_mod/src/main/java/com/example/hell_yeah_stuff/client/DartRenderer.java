package com.example.hell_yeah_stuff.client;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import com.example.hell_yeah_stuff.entity.DartEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** Рендер обычного дротика — стандартный стрелоподобный рендер. */
public class DartRenderer extends ArrowRenderer<DartEntity> {

    public static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "textures/entity/projectiles/dart.png");

    public DartRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(DartEntity entity) {
        return TEXTURE;
    }
}
