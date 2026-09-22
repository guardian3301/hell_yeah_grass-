package com.example.hell_yeah_stuff.client;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import com.example.hell_yeah_stuff.entity.AmethystBoltEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** Рендер аметистового болта. */
public class AmethystBoltRenderer extends ArrowRenderer<AmethystBoltEntity> {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HellYeahStuffMod.MODID, "textures/entity/projectiles/amethyst_bolt.png");

    public AmethystBoltRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(AmethystBoltEntity entity) {
        return TEXTURE;
    }
}
