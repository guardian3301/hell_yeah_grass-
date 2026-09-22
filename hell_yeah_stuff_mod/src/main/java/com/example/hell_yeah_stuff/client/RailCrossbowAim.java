package com.example.hell_yeah_stuff.client;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import com.example.hell_yeah_stuff.item.RailCrossbowItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Общее состояние «прицеливания» рельсовым арбалетом (удержание Shift).
 *
 * <p>Прогресс 0..1 плавно едет к цели каждый клиентский тик, чтобы и FOV,
 * и виньетка, и прицел менялись синхронно и без рывков. При отпускании
 * Shift прогресс так же плавно возвращается к нулю.</p>
 *
 * <p>По согласованному решению арбалет работает полноценно и во второй
 * руке, поэтому прицеливание включается при арбалете в любой руке.</p>
 */
@EventBusSubscriber(modid = HellYeahStuffMod.MODID, value = Dist.CLIENT)
public final class RailCrossbowAim {

    /** Скорость набора/сброса прогресса за тик (полный ход ≈ 0.25 с). */
    private static final float SPEED = 0.2F;

    private static float progress;
    private static float prevProgress;

    /** Активно ли прицеливание прямо сейчас (хоть чуть-чуть). */
    public static boolean isActive() {
        return progress > 0.001F || prevProgress > 0.001F;
    }

    /** Сглаженный прогресс 0..1 с учётом частичного тика. */
    public static float progress(float partialTick) {
        return Mth.clamp(Mth.lerp(partialTick, prevProgress, progress), 0.0F, 1.0F);
    }

    /** Сглаженный прогресс по текущему кадру. */
    public static float progress() {
        Minecraft mc = Minecraft.getInstance();
        return progress(mc.getTimer().getGameTimeDeltaPartialTick(false));
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        prevProgress = progress;
        float target = shouldAim() ? 1.0F : 0.0F;
        if (progress < target) {
            progress = Math.min(target, progress + SPEED);
        } else if (progress > target) {
            progress = Math.max(target, progress - SPEED);
        }
    }

    /** Держит ли игрок Shift с рельсовым арбалетом в руке. */
    private static boolean shouldAim() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui || mc.screen != null) {
            return false;
        }
        if (!player.isShiftKeyDown() || player.isSpectator()) {
            return false;
        }
        if (!mc.options.getCameraType().isFirstPerson()) {
            return false;
        }
        return holdsRailCrossbow(player.getMainHandItem()) || holdsRailCrossbow(player.getOffhandItem());
    }

    private static boolean holdsRailCrossbow(ItemStack stack) {
        return stack.getItem() instanceof RailCrossbowItem;
    }

    private RailCrossbowAim() {}
}
