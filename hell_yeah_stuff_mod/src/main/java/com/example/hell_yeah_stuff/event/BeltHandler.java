package com.example.hell_yeah_stuff.event;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import com.example.hell_yeah_stuff.config.BeltConfig;
import com.example.hell_yeah_stuff.registry.ModDataAttachments;
import com.example.hell_yeah_stuff.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = HellYeahStuffMod.MODID)
public final class BeltHandler {
    private BeltHandler() {}
    public static void handleSwap(Player player) {
        if (!ModDataAttachments.isBeltEquipped(player)) return;
        ItemStack hand = player.getMainHandItem().copy();
        ItemStack belt = ModDataAttachments.getBeltWeapon(player).copy();
        ModDataAttachments.setBeltWeapon(player, hand);
        player.setItemInHand(InteractionHand.MAIN_HAND, belt);
        if (player instanceof ServerPlayer sp) sp.inventoryMenu.broadcastChanges();
    }
    public static void handleUse(Player player) {
        if (!ModDataAttachments.isBeltEquipped(player)) return;
        ItemStack belt = ModDataAttachments.getBeltWeapon(player);
        if (belt.isEmpty()) return;
        ItemStack hand = player.getMainHandItem().copy();
        player.setItemInHand(InteractionHand.MAIN_HAND, belt.copy());
        belt.use(player.level(), player, InteractionHand.MAIN_HAND);
        ModDataAttachments.setBeltWeapon(player, player.getMainHandItem().copy());
        player.setItemInHand(InteractionHand.MAIN_HAND, hand);
        if (player instanceof ServerPlayer sp) sp.inventoryMenu.broadcastChanges();
    }
    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getSlot() != EquipmentSlot.CHEST) return;
        boolean wasBelt = event.getFrom().is(ModItems.BELT.get());
        boolean nowBelt = event.getTo().is(ModItems.BELT.get());
        if (wasBelt && !nowBelt) {
            ItemStack beltWeapon = ModDataAttachments.getBeltWeapon(player);
            if (beltWeapon.isEmpty()) return;
            BeltConfig.OnBeltRemoved mode = BeltConfig.INSTANCE.onBeltRemoved.get();
            if (mode == BeltConfig.OnBeltRemoved.RETURN) {
                ModDataAttachments.setBeltWeapon(player, ItemStack.EMPTY);
                if (!player.getInventory().add(beltWeapon)) player.drop(beltWeapon, false);
                if (player instanceof ServerPlayer sp) sp.inventoryMenu.broadcastChanges();
            }
        }
    }
    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) {
            if (event.getOriginal().hasData(ModDataAttachments.BELT_WEAPON)) {
                event.getEntity().setData(ModDataAttachments.BELT_WEAPON, event.getOriginal().getData(ModDataAttachments.BELT_WEAPON).copy());
            }
            return;
        }
        if (BeltConfig.INSTANCE.shouldDropOnDeath.get()) return;
        if (event.getOriginal().hasData(ModDataAttachments.BELT_WEAPON)) {
            ItemStack belt = event.getOriginal().getData(ModDataAttachments.BELT_WEAPON);
            if (!belt.isEmpty()) event.getEntity().setData(ModDataAttachments.BELT_WEAPON, belt.copy());
        }
    }
}
