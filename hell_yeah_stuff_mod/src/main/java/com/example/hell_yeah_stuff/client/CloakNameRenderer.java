package com.example.hell_yeah_stuff.client;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import com.example.hell_yeah_stuff.event.CloakNameState;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;

/**
 * Клиентский рендер ников: скрывает ник игрока, если сервер сообщил,
 * что на нём надета скрытная накидка (см. CloakNameState).
 *
 * <p>Клиент не принимает самостоятельных решений — он лишь отражает
 * состояние, полученное от сервера через CloakNameHidePayload.
 */
@EventBusSubscriber(modid = HellYeahStuffMod.MODID, value = Dist.CLIENT)
public final class CloakNameRenderer {

    private CloakNameRenderer() {}

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        Entity entity = event.getEntity();
        if (entity == null) {
            return;
        }
        if (CloakNameState.isNameHidden(entity.getUUID())) {
            event.setCanRender(TriState.FALSE);
        }
    }
}
