package com.example.hell_yeah_stuff.client;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;

/**
 * HUD прицеливания рельсового арбалета:
 *
 * <ul>
 *   <li>ванильный crosshair отключается и заменяется текстурой мода
 *       (отдельные варианты для состояний «от бедра» и «в прицеле»);</li>
 *   <li>по краям экрана рисуется мягкая «виньетка» — без текстуры,
 *       чисто градиентными полосами поверх кадра.</li>
 * </ul>
 *
 * Прозрачность и глубина виньетки зависят от прогресса {@link RailCrossbowAim},
 * поэтому эффект появляется и уходит плавно вместе с зумом.
 */
@EventBusSubscriber(modid = HellYeahStuffMod.MODID, value = Dist.CLIENT)
public final class RailCrossbowAimOverlay {

    /** Прицел в режиме прицеливания (Shift). */
    private static final ResourceLocation CROSSHAIR_AIMED = ResourceLocation.fromNamespaceAndPath(
            HellYeahStuffMod.MODID, "textures/gui/rail_crossbow_crosshair_aimed.png");
    /** Прицел при стрельбе от бедра. */
    private static final ResourceLocation CROSSHAIR_UNAIMED = ResourceLocation.fromNamespaceAndPath(
            HellYeahStuffMod.MODID, "textures/gui/rail_crossbow_crosshair_unaimed.png");

    private static final int SIZE = 15;

    /** Сколько полос рисуем с каждой стороны — чем больше, тем мягче. */
    private static final int VIGNETTE_STEPS = 24;
    /** Доля экрана под затемнение с каждой стороны. */
    private static final float VIGNETTE_SPAN = 0.42F;
    /** Максимальная непрозрачность у самого края экрана. */
    private static final int VIGNETTE_MAX_ALPHA = 200;

    /** Подмена ванильного прицела на кастомный. */
    @SubscribeEvent
    static void onRenderCrosshair(RenderGuiLayerEvent.Pre event) {
        if (!VanillaGuiLayers.CROSSHAIR.equals(event.getName())) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || mc.getDebugOverlay().showDebugScreen()) {
            return;
        }
        if (!mc.options.getCameraType().isFirstPerson()) {
            return;
        }
        boolean mainHand = mc.player.getMainHandItem().getItem()
                instanceof com.example.hell_yeah_stuff.item.RailCrossbowItem;
        boolean offHand = mc.player.getOffhandItem().getItem()
                instanceof com.example.hell_yeah_stuff.item.RailCrossbowItem;
        if (!mainHand && !offHand) {
            return;
        }

        event.setCanceled(true);

        GuiGraphics graphics = event.getGuiGraphics();
        ResourceLocation texture = RailCrossbowAim.progress() > 0.5F ? CROSSHAIR_AIMED : CROSSHAIR_UNAIMED;
        int x = (graphics.guiWidth() - SIZE) / 2;
        int y = (graphics.guiHeight() - SIZE) / 2;

        RenderSystem.enableBlend();
        graphics.blit(texture, x, y, 0.0F, 0.0F, SIZE, SIZE, SIZE, SIZE);
        RenderSystem.disableBlend();
    }

    /**
     * Виньетка поверх всего HUD. Рисуется полосами с растущей альфой
     * к краям — обычный fillGradient умеет только вертикальный градиент,
     * а нам нужны все четыре стороны.
     */
    @SubscribeEvent
    static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || !mc.options.getCameraType().isFirstPerson()) {
            return;
        }
        DeltaTracker delta = event.getPartialTick();
        float progress = RailCrossbowAim.progress(delta.getGameTimeDeltaPartialTick(false));
        if (progress <= 0.001F) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();

        int spanX = Math.max(1, Mth.floor(width * VIGNETTE_SPAN));
        int spanY = Math.max(1, Mth.floor(height * VIGNETTE_SPAN));
        int stepX = Math.max(1, spanX / VIGNETTE_STEPS);
        int stepY = Math.max(1, spanY / VIGNETTE_STEPS);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        for (int i = 0; i < VIGNETTE_STEPS; i++) {
            // Квадратичный набор альфы — так затемнение выглядит мягче в центре.
            float t = (i + 1) / (float) VIGNETTE_STEPS;
            int alpha = Mth.floor(VIGNETTE_MAX_ALPHA * t * t * progress / VIGNETTE_STEPS * 4.0F);
            if (alpha <= 0) {
                continue;
            }
            int color = Math.min(alpha, 255) << 24;

            int left = spanX - (i + 1) * stepX;
            int right = width - spanX + (i + 1) * stepX;
            int top = spanY - (i + 1) * stepY;
            int bottom = height - spanY + (i + 1) * stepY;

            // Левая и правая полосы.
            graphics.fill(Math.max(0, left), 0, Math.max(0, left + stepX), height, color);
            graphics.fill(Math.min(width, right - stepX), 0, Math.min(width, right), height, color);
            // Верхняя и нижняя полосы.
            graphics.fill(0, Math.max(0, top), width, Math.max(0, top + stepY), color);
            graphics.fill(0, Math.min(height, bottom - stepY), width, Math.min(height, bottom), color);
        }

        // Глухие углы/края за пределами градиента.
        int edgeAlpha = Mth.floor(VIGNETTE_MAX_ALPHA * progress) << 24;
        graphics.fill(0, 0, Math.max(0, spanX - VIGNETTE_STEPS * stepX), height, edgeAlpha);
        graphics.fill(Math.min(width, width - spanX + VIGNETTE_STEPS * stepX), 0, width, height, edgeAlpha);
        graphics.fill(0, 0, width, Math.max(0, spanY - VIGNETTE_STEPS * stepY), edgeAlpha);
        graphics.fill(0, Math.min(height, height - spanY + VIGNETTE_STEPS * stepY), width, height, edgeAlpha);

        RenderSystem.disableBlend();
    }

    private RailCrossbowAimOverlay() {}
}
