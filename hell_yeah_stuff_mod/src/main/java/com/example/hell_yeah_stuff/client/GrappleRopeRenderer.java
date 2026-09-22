package com.example.hell_yeah_stuff.client;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import com.example.hell_yeah_stuff.compat.CuriosCompat;
import com.example.hell_yeah_stuff.entity.GrappleDartEntity;
import com.example.hell_yeah_stuff.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * Верёвка цепкого дротика — ОТДЕЛЬНЫЙ мировой рендер-проход
 * (RenderLevelStageEvent.AFTER_ENTITIES), а не часть рендера сущности:
 *
 * 1. Трос виден ВСЕГДА — даже когда сам дротик за фрустумом камеры
 *    (смотришь вниз на качелях — верёвка над головой не пропадает).
 * 2. Вся геометрия считается в ЧИСТЫХ мировых координатах относительно
 *    камеры. Раньше трос рисовался в PoseStack рендера дротика, а внутри
 *    Sable-плота этот PoseStack уже повёрнут/смещён позой саб-левела —
 *    мировые вектора верёвки искажались: лента истончалась, а конец
 *    у руки уезжал от игрока.
 *
 * Точка старта — кисть подвижной руки из {@link GrappleArmPose}: та же
 * формула, что позирует руку модели (GrappleArmAnimator), включая зажим
 * угла — верёвка «приклеена» к ладони без рассинхрона.
 *
 * Сама лента — как раньше: четыре грани квадратной «трубы», текстура
 * rope_particle (колонки 6–9), узлы-кубики на концах, затухающая
 * стоячая волна после зацепа.
 */
@EventBusSubscriber(modid = HellYeahStuffMod.MODID, value = Dist.CLIENT)
public final class GrappleRopeRenderer {

    private static final ResourceLocation ROPE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "textures/entity/rope.png");

    /** Полуширина ленты верёвки в блоках (~1.5 пикселя). */
    private static final float ROPE_RADIUS = 0.045F;
    /** UV-полоса верёвки: колонки 6..9 текстуры 16x16 (как в rope.json). */
    private static final float U0 = 6.0F / 16.0F;
    private static final float U1 = 9.0F / 16.0F;

    // ---- Колебания струны: y = A·sin(2π/l·x)·cos(ωt)·e^{-t/τ} ----
    /** A = WAVE_AMPLITUDE_REF / l — чем длиннее верёвка, тем меньше амплитуда. */
    private static final float WAVE_AMPLITUDE_REF = 1.5F;
    /** Потолок амплитуды на коротком тросе (блоков). */
    private static final float WAVE_AMPLITUDE_MAX = 0.2F;
    /** Полуразмер узла на концах: куб со стороной в 1.5 раза больше
     *  толщины верёвки (2·ROPE_RADIUS·1.5 / 2). */
    private static final float KNOT_HALF = ROPE_RADIUS * 1.5F;
    /** UV узла: квадрат 3x3 пикселя (колонки 6..9, строки 0..3) —
     *  пиксельно-выровненный, 1:1 на квадратную грань куба, без растяжения. */
    private static final float KNOT_V0 = 0.0F / 16.0F;
    private static final float KNOT_V1 = 3.0F / 16.0F;
    /** ω = WAVE_OMEGA_REF / l (рад/тик) — длинный трос колеблется медленнее. */
    private static final float WAVE_OMEGA_REF = 8.0F;
    /** τ — время затухания в e раз (тиков): колебания гаснут за ~1.5-2 с. */
    private static final float WAVE_DECAY_TICKS = 12.0F;

    /** Цвет верёвки текущего вызова рендера: белый по умолчанию, зелёный
     *  при «Усиленной верёвке» на ремне владельца. Рендер однопоточный,
     *  поэтому статические поля безопасны. */
    private static int tintR = 255;
    private static int tintG = 255;
    private static int tintB = 255;

    private GrappleRopeRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }
        float pt = mc.getTimer().getGameTimeDeltaPartialTick(true);
        Vec3 cam = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

        // Все живые дротики (летящие и воткнутые) — верёвки всех игроков.
        for (GrappleDartEntity dart : GrappleDartEntity.clientDarts()) {
            if (dart.level() != level) {
                continue;
            }
            if (!(dart.getOwner() instanceof Player player)
                    || !player.isAlive() || player.level() != level) {
                continue;
            }
            renderRope(dart, player, pt, cam, event.getPoseStack(), buffer, level);
        }
    }

    private static void renderRope(GrappleDartEntity dart, Player player, float pt,
                                   Vec3 cam, PoseStack poseStack,
                                   MultiBufferSource buffer, ClientLevel level) {
        // Единая геометрия с позой руки: кисть подвижной руки + мировая
        // (Sable-проецированная) позиция дротика.
        GrappleArmPose.Result armPose = GrappleArmPose.compute(player, dart, pt);
        if (armPose == null) {
            return; // саб-левел Sable ещё не прогрузился — кадр без верёвки
        }
        Vec3 hand = armPose.hand();
        Vec3 dartPos = armPose.target();

        // «Усиленная верёвка» на ремне Curios — трос владельца зелёный.
        boolean reinforced = CuriosCompat.hasCurio(player, ModItems.REINFORCED_ROPE.get());
        tintR = reinforced ? 80 : 255;
        tintG = reinforced ? 230 : 255;
        tintB = reinforced ? 80 : 255;

        Vec3 delta = hand.subtract(dartPos);
        float length = (float) delta.length();
        if (length < 0.1F || length > 512.0F) {
            return; // страховка: не тянем ленту на «сырые» плотовые координаты
        }

        // Ортонормальный базис вокруг оси верёвки — квадратное сечение 1:1.
        Vec3 dir = delta.scale(1.0D / length);
        Vec3 ref = Math.abs(dir.y) > 0.99D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 side1 = dir.cross(ref).normalize().scale(ROPE_RADIUS);
        Vec3 side2 = dir.cross(side1).normalize().scale(ROPE_RADIUS);

        // Четыре угла квадратного сечения (полуширина ROPE_RADIUS по обеим осям).
        Vec3 c1 = side1.add(side2);
        Vec3 c2 = side1.subtract(side2);
        Vec3 c3 = side1.scale(-1.0D).subtract(side2);
        Vec3 c4 = side2.subtract(side1);

        // Затухающая стоячая волна (см. формулу «Колебания струны»):
        // концы (рука и дротик) закреплены: sin = 0 на краях.
        Vec3 waveDir = side2.normalize();
        float waveScale = 0.0F;
        float waveAge = dart.getRopeWaveAge(pt);
        if (waveAge >= 0.0F) {
            float amp = Math.min(WAVE_AMPLITUDE_MAX, WAVE_AMPLITUDE_REF / length);
            float omega = WAVE_OMEGA_REF / length;
            float decay = (float) Math.exp(-waveAge / WAVE_DECAY_TICKS);
            waveScale = amp * Mth.cos(omega * waveAge) * decay;
            if (Math.abs(waveScale) < 1.0E-3F) {
                waveScale = 0.0F;
            }
        }

        // Освещение берём у руки игрока (большая часть троса рядом с ним).
        int light = LevelRenderer.getLightColor(level, BlockPos.containing(hand));

        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(ROPE_TEXTURE));
        poseStack.pushPose();
        // Мировые координаты относительно камеры: начало — у дротика.
        poseStack.translate(dartPos.x - cam.x, dartPos.y - cam.y, dartPos.z - cam.z);
        PoseStack.Pose pose = poseStack.last();
        // Четыре грани прямоугольной «трубы» верёвки.
        ribbon(vc, pose, delta, c1, c2, length, waveDir, waveScale, light);
        ribbon(vc, pose, delta, c2, c3, length, waveDir, waveScale, light);
        ribbon(vc, pose, delta, c3, c4, length, waveDir, waveScale, light);
        ribbon(vc, pose, delta, c4, c1, length, waveDir, waveScale, light);
        // Узлы на закреплённых концах.
        knot(vc, pose, Vec3.ZERO, dir, side1, side2, light); // у дротика
        knot(vc, pose, delta, dir, side1, side2, light);     // у руки
        poseStack.popPose();
    }

    /**
     * Узел — куб с центром в точке end, ориентированный по осям верёвки
     * (dir вдоль, side1/side2 поперёк). Сторона куба = 2·KNOT_HALF.
     */
    private static void knot(VertexConsumer vc, PoseStack.Pose pose, Vec3 end,
                             Vec3 dir, Vec3 side1, Vec3 side2, int light) {
        Vec3 ax = dir.scale(KNOT_HALF);
        Vec3 s1 = side1.normalize().scale(KNOT_HALF);
        Vec3 s2 = side2.normalize().scale(KNOT_HALF);

        quad(vc, pose, end.add(s2), ax, s1, light);
        quad(vc, pose, end.subtract(s2), ax, s1, light);
        quad(vc, pose, end.add(s1), ax, s2, light);
        quad(vc, pose, end.subtract(s1), ax, s2, light);
        quad(vc, pose, end.add(ax), s1, s2, light);
        quad(vc, pose, end.subtract(ax), s1, s2, light);
    }

    /** Квадратная грань куба: центр center, полуоси ua и va.
     *  UV — пиксельный квадрат 3x3 (U0..U1 x KNOT_V0..KNOT_V1), 1:1 на грань. */
    private static void quad(VertexConsumer vc, PoseStack.Pose pose, Vec3 center,
                             Vec3 ua, Vec3 va, int light) {
        vertex(vc, pose, center.subtract(ua).subtract(va), U0, KNOT_V0, light);
        vertex(vc, pose, center.add(ua).subtract(va), U1, KNOT_V0, light);
        vertex(vc, pose, center.add(ua).add(va), U1, KNOT_V1, light);
        vertex(vc, pose, center.subtract(ua).add(va), U0, KNOT_V1, light);
    }

    /**
     * Одна грань между двумя углами сечения: сегменты по ~1 блоку
     * (при активной волне — мельче), текстура тайлится по длине.
     */
    private static void ribbon(VertexConsumer vc, PoseStack.Pose pose,
                               Vec3 delta, Vec3 edgeA, Vec3 edgeB, float length,
                               Vec3 waveDir, float waveScale, int light) {
        int segments = waveScale != 0.0F
                ? Math.max(12, Mth.ceil(length * 2.0F))
                : Math.max(1, Mth.ceil(length));
        for (int i = 0; i < segments; i++) {
            float t0 = (float) i / segments;
            float t1 = (float) (i + 1) / segments;
            Vec3 a = delta.scale(t0).add(waveOffset(waveDir, waveScale, t0));
            Vec3 b = delta.scale(t1).add(waveOffset(waveDir, waveScale, t1));
            float v0 = length * t0;
            float v1 = length * t1;
            vertex(vc, pose, a.add(edgeA), U0, v0, light);
            vertex(vc, pose, a.add(edgeB), U1, v0, light);
            vertex(vc, pose, b.add(edgeB), U1, v1, light);
            vertex(vc, pose, b.add(edgeA), U0, v1, light);
        }
    }

    /** Смещение точки верёвки стоячей волной: sin(2π·t), узлы на концах. */
    private static Vec3 waveOffset(Vec3 waveDir, float waveScale, float t) {
        if (waveScale == 0.0F) {
            return Vec3.ZERO;
        }
        return waveDir.scale(waveScale * Mth.sin((float) (Math.PI * 2.0D) * t));
    }

    private static void vertex(VertexConsumer vc, PoseStack.Pose pose,
                               Vec3 p, float u, float v, int light) {
        vc.addVertex(pose, (float) p.x, (float) p.y, (float) p.z)
                .setColor(tintR, tintG, tintB, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
