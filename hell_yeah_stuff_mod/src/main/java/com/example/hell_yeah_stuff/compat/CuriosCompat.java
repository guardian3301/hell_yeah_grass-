package com.example.hell_yeah_stuff.compat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Мягкая совместимость с Curios через рефлексию — мод собирается и работает
 * без Curios в classpath. Если Curios не установлен, всё просто возвращает false.
 */
public final class CuriosCompat {

    private static boolean checked;
    private static Method getCuriosInventory; // CuriosApi.getCuriosInventory(LivingEntity) -> Optional<ICuriosItemHandler>
    private static Method findFirstCurio;     // ICuriosItemHandler.findFirstCurio(Item) -> Optional<SlotResult>

    private CuriosCompat() {}

    /** Есть ли у сущности данный предмет в любом слоте Curios (у нас магазин помечен только тегом belt). */
    public static boolean hasCurio(LivingEntity entity, Item item) {
        if (!resolve()) return false;
        try {
            Optional<?> handlerOpt = (Optional<?>) getCuriosInventory.invoke(null, entity);
            if (handlerOpt.isEmpty()) return false;
            Optional<?> result = (Optional<?>) findFirstCurio.invoke(handlerOpt.get(), item);
            return result.isPresent();
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean resolve() {
        if (!checked) {
            checked = true;
            try {
                Class<?> api = Class.forName("top.theillusivec4.curios.api.CuriosApi");
                Class<?> handler = Class.forName("top.theillusivec4.curios.api.type.capability.ICuriosItemHandler");
                getCuriosInventory = api.getMethod("getCuriosInventory", LivingEntity.class);
                findFirstCurio = handler.getMethod("findFirstCurio", Item.class);
            } catch (Throwable t) {
                getCuriosInventory = null;
            }
        }
        return getCuriosInventory != null;
    }
}
