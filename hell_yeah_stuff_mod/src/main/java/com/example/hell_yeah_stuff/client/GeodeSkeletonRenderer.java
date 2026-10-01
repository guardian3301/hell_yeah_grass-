package com.example.hell_yeah_stuff.client;
import com.example.hell_yeah_stuff.entity.GeodeSkeletonEntity;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
public class GeodeSkeletonRenderer extends HumanoidMobRenderer<GeodeSkeletonEntity, SkeletonModel<GeodeSkeletonEntity>> {
    private static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath("hell_yeah_stuff","textures/entity/geode_skeleton.png");
    public GeodeSkeletonRenderer(EntityRendererProvider.Context ctx){
        super(ctx, new SkeletonModel<>(ctx.bakeLayer(ModelLayers.SKELETON)), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this, new SkeletonModel<>(ctx.bakeLayer(ModelLayers.SKELETON_INNER_ARMOR)), new SkeletonModel<>(ctx.bakeLayer(ModelLayers.SKELETON_OUTER_ARMOR)), ctx.getModelManager()));
    }
    @Override public ResourceLocation getTextureLocation(GeodeSkeletonEntity e){ return TEX; }
}
