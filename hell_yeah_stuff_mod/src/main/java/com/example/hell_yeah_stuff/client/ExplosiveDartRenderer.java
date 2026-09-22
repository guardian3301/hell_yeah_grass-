package com.example.hell_yeah_stuff.client;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import com.example.hell_yeah_stuff.entity.ExplosiveDartEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** Рендер взрывного дротика. */
public class ExplosiveDartRenderer extends ArrowRenderer<ExplosiveDartEntity> {

    public static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "textures/entity/projectiles/explosive_dart.png");

    public ExplosiveDartRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(ExplosiveDartEntity entity) {
        return TEXTURE;
    }
}
