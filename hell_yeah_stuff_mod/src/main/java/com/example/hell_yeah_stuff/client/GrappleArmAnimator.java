package com.example.hell_yeah_stuff.client;

import com.example.hell_yeah_stuff.entity.GrappleDartEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Поворот руки с тросом: пока цепкий дротик летит или воткнут, рука,
 * из которой исходит верёвка (противоположная руке с арбалетом — см.
 * {@link GrappleDartEntity#ropeArm()}), вытягивается точно в сторону
 * сущности дротика — на тот угол, который образует линия «игрок → дротик».
 *
 * Углы и кисть считает {@link GrappleArmPose} — та же геометрия, что у
 * рендера верёвки, поэтому трос «приклеен» к ладони.
 *
 * Вызывается из PlayerModelMixin в ХВОСТЕ PlayerModel#setupAnim —
 * после всей ванильной анимации (иначе ванилла перезаписала бы углы).
 * Работает для локального игрока и для чужих игроков (третье лицо);
 * вид от первого лица не трогает: там PlayerRenderer#renderHand
 * выставляет углы руки заново уже после setupAnim.
 */
public final class GrappleArmAnimator {

    private GrappleArmAnimator() {
    }

    public static void pose(PlayerModel<?> model, LivingEntity entity) {
        if (!(entity instanceof Player player) || !player.isAlive()) {
            return;
        }
        GrappleDartEntity dart = GrappleDartEntity.clientDartFor(player);
        if (dart == null) {
            return;
        }

        float pt = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        GrappleArmPose.Result armPose = GrappleArmPose.compute(player, dart, pt);
        if (armPose == null) {
            return;
        }

        // Рука модели по умолчанию висит вниз (xRot = 0), горизонтально
        // вперёд = -90°: направление на дротик даёт xRot = -90° + pitch.
        ModelPart armPart = armPose.arm() == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;
        armPart.xRot = armPose.xRot();
        armPart.yRot = armPose.yRot();
        armPart.zRot = 0.0F;

        // Рукав скина копируется с руки в КОНЦЕ ванильного setupAnim —
        // то есть ДО нашей правки; синхронизируем его заново.
        ModelPart sleeve = armPose.arm() == HumanoidArm.RIGHT ? model.rightSleeve : model.leftSleeve;
        sleeve.copyFrom(armPart);
    }
}
