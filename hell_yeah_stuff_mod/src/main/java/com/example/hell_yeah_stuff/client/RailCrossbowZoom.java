package com.example.hell_yeah_stuff.client;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;

/**
 * Зум ×3 при прицеливании рельсовым арбалетом (эффект «подзорной трубы»).
 *
 * <p>Приближение в 3 раза == множитель FOV 1/3. Переход туда и обратно
 * плавный — идёт по общему прогрессу из {@link RailCrossbowAim}. Рамки/текстуры
 * окуляра нет — только изменение угла обзора.</p>
 */
@EventBusSubscriber(modid = HellYeahStuffMod.MODID, value = Dist.CLIENT)
public final class RailCrossbowZoom {

    /** Множитель FOV при полном зуме: 1/3 == приближение ×3. */
    private static final float ZOOMED_FOV_MULTIPLIER = 1.0F / 3.0F;

    @SubscribeEvent
    static void onComputeFov(ComputeFovModifierEvent event) {
        float progress = RailCrossbowAim.progress();
        if (progress <= 0.0F) {
            return;
        }
        float target = Mth.lerp(progress, 1.0F, ZOOMED_FOV_MULTIPLIER);
        event.setNewFovModifier(event.getNewFovModifier() * target);
    }

    private RailCrossbowZoom() {}
}
