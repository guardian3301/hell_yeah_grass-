package com.example.hell_yeah_stuff.client;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import com.example.hell_yeah_stuff.entity.IronBoltEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** Рендер железного болта — стандартный стрелоподобный рендер. */
public class IronBoltRenderer extends ArrowRenderer<IronBoltEntity> {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HellYeahStuffMod.MODID, "textures/entity/projectiles/iron_bolt.png");

    public IronBoltRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(IronBoltEntity entity) {
        return TEXTURE;
    }
}
