package com.example.hell_yeah_stuff.compat;

import com.example.hell_yeah_stuff.item.StealthCloakItem;
import com.example.hell_yeah_stuff.registry.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Поддержка скрытной накидки в Curios через рефлексию (мягкая совместимость).
 *
 * <p>Позволяет получать ItemStack накидки из Curios-слота head, не имея Curios
 * в classpath. Если Curios не установлен — все методы безопасно возвращают
 * пустой стек / false. Клиентские классы Curios не загружаются на dedicated
 * server (только рефлексия по именам).
 */
public final class CuriosCloakSupport {

    private static boolean checked;
    private static Method getCuriosInventory;   // CuriosApi.getCuriosInventory(LivingEntity)
    private static Method findFirstCurio;        // ICuriosItemHandler.findFirstCurio(Item)
    private static Method slotResultStack;       // SlotResult.stack()
    private static Method slotResultContext;     // SlotResult.slotContext()
    private static Method slotContextIdentifier; // SlotContext.identifier()

    private CuriosCloakSupport() {}

    /**
     * Возвращает ItemStack накидки, если она надета в Curios-слот head.
     * Пустой стек, если накидки нет или Curios не установлен.
     */
    public static ItemStack getCloakStack(LivingEntity entity) {
        if (!resolve()) {
            return ItemStack.EMPTY;
        }
        try {
            Optional<?> handlerOpt = (Optional<?>) getCuriosInventory.invoke(null, entity);
            if (handlerOpt.isEmpty()) {
                return ItemStack.EMPTY;
            }

            Item cloakItem = ModItems.STEALTH_CLOAK.get();
            Optional<?> result = (Optional<?>) findFirstCurio.invoke(handlerOpt.get(), cloakItem);
            if (result.isEmpty()) {
                return ItemStack.EMPTY;
            }

            Object slotResult = result.get();

            // Проверяем, что предмет надет в слот head
            Object slotContext = slotResultContext.invoke(slotResult);
            String identifier = (String) slotContextIdentifier.invoke(slotContext);
            if (!"head".equals(identifier)) {
                return ItemStack.EMPTY;
            }

            return (ItemStack) slotResultStack.invoke(slotResult);
        } catch (Throwable t) {
            return ItemStack.EMPTY;
        }
    }

    /**
     * Проверяет, надета ли накидка в слот head.
     */
    public static boolean isCloakEquipped(LivingEntity entity) {
        return !getCloakStack(entity).isEmpty();
    }

    private static boolean resolve() {
        if (!checked) {
            checked = true;
            try {
                Class<?> api = Class.forName("top.theillusivec4.curios.api.CuriosApi");
                Class<?> handler = Class.forName("top.theillusivec4.curios.api.type.capability.ICuriosItemHandler");
                Class<?> slotResult = Class.forName("top.theillusivec4.curios.api.SlotResult");
                Class<?> slotContext = Class.forName("top.theillusivec4.curios.api.SlotContext");

                getCuriosInventory = api.getMethod("getCuriosInventory", LivingEntity.class);
                findFirstCurio = handler.getMethod("findFirstCurio", Item.class);
                slotResultStack = slotResult.getMethod("stack");
                slotResultContext = slotResult.getMethod("slotContext");
                slotContextIdentifier = slotContext.getMethod("identifier");
            } catch (Throwable t) {
                getCuriosInventory = null;
                findFirstCurio = null;
                slotResultStack = null;
                slotResultContext = null;
                slotContextIdentifier = null;
            }
        }
        return getCuriosInventory != null && findFirstCurio != null
                && slotResultStack != null && slotResultContext != null
                && slotContextIdentifier != null;
    }
}
