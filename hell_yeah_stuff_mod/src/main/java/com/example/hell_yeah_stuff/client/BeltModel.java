package com.example.hell_yeah_stuff.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import com.example.hell_yeah_stuff.HellYeahStuffMod;

/**
 * Модель ремня — жёстко крепится к торсу, без физики плаща.
 * Развёртка совпадает с нагрудником: тело 16x16 (u=16,v=16) на
 * текстуре 64x32, как у ванильного chestplate. Дополнительный пояс —
 * узкая полоса по талии чуть выступающая за тело, но UV берётся
 * из той же области тела чтобы сохранить совместимость.
 */
public final class BeltModel {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "belt"), "main");

    private BeltModel() {}

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Тело ремня: ровно как ванильный body (8x12x4) но с небольшим inflate
        // чтобы быть видимым поверх кожи/брони. UV — как у chestplate.
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        // полный торс — основа ремня
                        .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.26F))
                        // утолщённый пояс по талии (визуальный акцент)
                        .texOffs(16, 20).addBox(-4.5F, 9.0F, -2.5F, 9.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Небольшие боковые петли для крепления оружия (чисто декор)
        body.addOrReplaceChild("belt_strap_left",
                CubeListBuilder.create().texOffs(16, 28)
                        .addBox(-0.5F, -1.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(4.2F, 7.0F, 2.2F));
        body.addOrReplaceChild("belt_strap_right",
                CubeListBuilder.create().texOffs(16, 28)
                        .addBox(-0.5F, -1.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-4.2F, 7.0F, 2.2F));

        return LayerDefinition.create(mesh, 64, 32);
    }

    /** Создаёт ModelPart из запечённого слоя. */
    public static ModelPart bake(ModelPart root) {
        return root.getChild("body");
    }
}
