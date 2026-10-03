package com.example.hell_yeah_stuff.registry;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * Data Attachments для пояса.
 * BELT_WEAPON — предмет, лежащий за спиной (на ремне).
 * Сервер-авторитативно, хранится на Player.
 */
public final class ModDataAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, HellYeahStuffMod.MODID);

    public static final Supplier<AttachmentType<ItemStack>> BELT_WEAPON =
            ATTACHMENT_TYPES.register("belt_weapon",
                    () -> AttachmentType.builder(() -> ItemStack.EMPTY)
                            .serialize(ItemStack.CODEC)
                            .copyOnDeath()
                            .build());

    private ModDataAttachments() {}

    /** Проверка: надет ли ремень (лежит в CHEST слоте). Сервер и клиент одинаково. */
    public static boolean isBeltEquipped(Player player) {
        ItemStack chest = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
        return !chest.isEmpty() && chest.is(ModItems.BELT.get());
    }

    public static ItemStack getBeltWeapon(Player player) {
        if (!player.hasData(BELT_WEAPON)) return ItemStack.EMPTY;
        ItemStack s = player.getData(BELT_WEAPON);
        return s == null ? ItemStack.EMPTY : s;
    }

    public static void setBeltWeapon(Player player, ItemStack stack) {
        player.setData(BELT_WEAPON, stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
    }

    public static boolean hasBeltWeapon(Player player) {
        ItemStack s = getBeltWeapon(player);
        return !s.isEmpty();
    }
}
