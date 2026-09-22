package com.example.hell_yeah_stuff.item;

import com.example.hell_yeah_stuff.compat.CuriosCloakSupport;
import com.example.hell_yeah_stuff.registry.ModEnchantments;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * Скрытная накидка.
 * Надевается в слот Curios (head).
 * Скрывает ник игрока и позволяет использовать зачарования «Забвение»
 * и «Скрытный шаг».
 */
public class StealthCloakItem extends Item {

    public StealthCloakItem(Properties properties) {
        super(properties);
    }

    public static boolean isEquipped(Player player) {
        return CuriosCloakSupport.isCloakEquipped(player);
    }

    public static ItemStack getCloakStack(Player player) {
        return CuriosCloakSupport.getCloakStack(player);
    }



    /** Получает уровень зачарования по ResourceKey. */
    private static int getEnchantmentLevel(ItemStack stack, ResourceKey<Enchantment> key) {
        if (stack.isEmpty()) {
            return 0;
        }
        ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
        for (var entry : enchantments.entrySet()) {
            if (entry.getKey().is(key)) {
                return entry.getIntValue();
            }
        }
        return 0;
    }
}
