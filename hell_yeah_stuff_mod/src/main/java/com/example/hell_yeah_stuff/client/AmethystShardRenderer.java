package com.example.hell_yeah_stuff.client;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import com.example.hell_yeah_stuff.entity.AmethystShardEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** Рендер аметистового осколка — стрелоподобный, со своей текстурой. */
public class AmethystShardRenderer extends ArrowRenderer<AmethystShardEntity> {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HellYeahStuffMod.MODID, "textures/entity/projectiles/amethyst_shard.png");

    public AmethystShardRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(AmethystShardEntity entity) {
        return TEXTURE;
    }
}
