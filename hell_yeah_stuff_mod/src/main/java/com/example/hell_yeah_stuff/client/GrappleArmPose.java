package com.example.hell_yeah_stuff.client;

import com.example.hell_yeah_stuff.compat.SableCompat;
import com.example.hell_yeah_stuff.entity.GrappleDartEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Единая геометрия «руки с тросом»: одни и те же углы и точка кисти
 * используются и для позы модели игрока (GrappleArmAnimator), и для
 * точки старта верёвки (GrappleRopeRenderer). Благодаря этому верёвка
 * исходит РОВНО из подвижной (анимированной) руки — кисть считается из
 * итоговых, уже зажатых углов руки, а не из «идеального» направления
 * на дротик.
 */
public final class GrappleArmPose {

    /** Длина руки игрока от пивота плеча до кисти (~9 пикселей). */
    public static final double ARM_LENGTH = 0.55D;
    /** Максимальный горизонтальный разворот руки от корпуса (рад, ~109°):
     *  дальше рука неанатомично уходит за спину — угол зажимается,
     *  корпус игрока и так доворачивается за камерой. */
    public static final float MAX_REL_YAW = 1.9F;
    /** Страховка: к «сырым» плотовым координатам Sable руку не тянем. */
    private static final double MAX_TARGET_DISTANCE = 512.0D;
    /** В упор руку не выворачиваем (дротик в первый тик у глаз). */
    private static final double MIN_TARGET_DISTANCE = 0.25D;

    /**
     * arm — рука с тросом; shoulder/hand — мировые координаты плеча и
     * кисти; xRot/yRot — итоговые (зажатые) углы руки для модели;
     * target — мировая позиция дротика (Sable-проекция).
     */
    public record Result(HumanoidArm arm, Vec3 shoulder, Vec3 hand,
                         float xRot, float yRot, Vec3 target) {
    }

    private GrappleArmPose() {
    }

    @Nullable
    public static Result compute(Player player, GrappleDartEntity dart, float pt) {
        // Позиция дротика в МИРОВЫХ координатах: для якоря в Sable-плоте —
        // проекция позой саб-левела (null = структура ещё не прогрузилась),
        // для обычного якоря projectToWorld — тождество.
        Vec3 target = SableCompat.projectToWorldOrNull(
                dart.level(), dart.blockPosition(), dart.getPosition(pt));
        if (target == null) {
            return null;
        }
        target = target.add(0.0D, 0.05D, 0.0D);

        // Плечо руки с тросом: пивот руки на ±5 пикселей от оси корпуса,
        // высота ~78% хитбокса; right = (cos, 0, sin).
        HumanoidArm arm = dart.ropeArm();
        // ПРАВАЯ сторона игрока в мировых координатах = (-cos, 0, -sin) от yaw
        // корпуса (как в ванильном рендере лески удочки): при yaw=0 игрок
        // смотрит на +Z, его правая рука — на -X. Знак был перевёрнут —
        // трос выходил из зеркальной (арбалетной) руки.
        double armSign = arm == HumanoidArm.RIGHT ? -1.0D : 1.0D;
        float bodyYaw = Mth.rotLerp(pt, player.yBodyRotO, player.yBodyRot) * Mth.DEG_TO_RAD;
        double px = Mth.lerp(pt, player.xo, player.getX());
        double py = Mth.lerp(pt, player.yo, player.getY());
        double pz = Mth.lerp(pt, player.zo, player.getZ());
        Vec3 shoulder = new Vec3(
                px + Math.cos(bodyYaw) * 0.3125D * armSign,
                py + player.getBbHeight() * 0.78D,
                pz + Math.sin(bodyYaw) * 0.3125D * armSign);

        Vec3 d = target.subtract(shoulder);
        double len = d.length();
        if (len < MIN_TARGET_DISTANCE || len > MAX_TARGET_DISTANCE) {
            return null;
        }

        // Углы линии «плечо → дротик» в конвенции Minecraft:
        // yaw = atan2(z, x) - 90°, pitch отрицательный вверх.
        double horiz = Math.sqrt(d.x * d.x + d.z * d.z);
        float pitch = (float) -Math.atan2(d.y, horiz);
        float yawWorld = (float) Math.atan2(d.z, d.x) - Mth.HALF_PI;
        float relYaw = Mth.wrapDegrees((yawWorld - bodyYaw) * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
        relYaw = Mth.clamp(relYaw, -MAX_REL_YAW, MAX_REL_YAW);

        // Кисть — конец руки, повёрнутой на ИТОГОВЫЕ (зажатые) углы:
        // если clamp сработал, верёвка всё равно выйдет из реальной кисти.
        float effYaw = bodyYaw + relYaw;
        Vec3 dir = new Vec3(
                -Math.sin(effYaw) * Math.cos(pitch),
                -Math.sin(pitch),
                Math.cos(effYaw) * Math.cos(pitch));
        Vec3 hand = shoulder.add(dir.scale(ARM_LENGTH));

        return new Result(arm, shoulder, hand, -Mth.HALF_PI + pitch, relYaw, target);
    }
}
