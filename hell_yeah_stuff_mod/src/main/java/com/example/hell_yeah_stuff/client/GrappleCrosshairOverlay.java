package com.example.hell_yeah_stuff.client;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import com.example.hell_yeah_stuff.entity.GrappleDartEntity;
import com.example.hell_yeah_stuff.registry.ModItems;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Прицел крюка-кошки в стиле Apex Legends: пока в руке мульти-арбалет,
 * заряженный крюковым дротиком, и точка под перекрестием находится в
 * пределах дальности троса ({@link GrappleDartEntity#MAX_ROPE_LENGTH}),
 * вместо ванильного прицела рисуется специальный —
 * {@code textures/gui/grapple_crosshair.png} (15x15, как ванильный).
 *
 * Вне дальности (луч не достал до блока) остаётся обычный прицел, так что
 * игрок мгновенно видит момент входа в зону зацепа.
 */
@EventBusSubscriber(modid = HellYeahStuffMod.MODID, value = Dist.CLIENT)
public final class GrappleCrosshairOverlay {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HellYeahStuffMod.MODID, "textures/gui/grapple_crosshair.png");

    /** Размер текстуры прицела (совпадает с ванильным перекрестием). */
    private static final int SIZE = 15;

    @SubscribeEvent
    static void onRenderCrosshair(RenderGuiLayerEvent.Pre event) {
        if (!VanillaGuiLayers.CROSSHAIR.equals(event.getName())) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null
                || mc.options.hideGui
                || !mc.options.getCameraType().isFirstPerson()
                || mc.getDebugOverlay().showDebugScreen()) {
            return;
        }

        if (!holdsGrappleLoadedCrossbow(player)) {
            return;
        }

        // Луч из камеры на длину троса: попали в блок — зацеп дотянется.
        HitResult hit = player.pick(GrappleDartEntity.maxRopeLength(player), 1.0F, false);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return; // вне дальности — оставляем ванильный прицел
        }

        // В зоне зацепа: гасим ванильный прицел и рисуем свой по центру.
        event.setCanceled(true);
        GuiGraphics graphics = event.getGuiGraphics();
        int x = (graphics.guiWidth() - SIZE) / 2;
        int y = (graphics.guiHeight() - SIZE) / 2;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(TEXTURE, x, y, 0.0F, 0.0F, SIZE, SIZE, SIZE, SIZE);
        RenderSystem.disableBlend();
    }

    /** Мульти-арбалет с заряженным крюковым дротиком в любой из рук. */
    private static boolean holdsGrappleLoadedCrossbow(LocalPlayer player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.is(ModItems.MULTI_CROSSBOW.get())) {
                continue;
            }
            ChargedProjectiles charged = stack.get(DataComponents.CHARGED_PROJECTILES);
            if (charged != null && charged.contains(ModItems.GRAPPLE_DART.get())) {
                return true;
            }
        }
        return false;
    }

    private GrappleCrosshairOverlay() {}
}
