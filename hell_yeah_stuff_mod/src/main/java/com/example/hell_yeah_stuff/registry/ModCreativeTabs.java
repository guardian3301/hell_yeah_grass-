package com.example.hell_yeah_stuff.registry;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@EventBusSubscriber(modid = HellYeahStuffMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ModCreativeTabs {
    @SubscribeEvent
    static void addToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.RAIL_CROSSBOW.get());
            event.accept(ModItems.MULTI_CROSSBOW.get());
            event.accept(ModItems.DART.get());
            event.accept(ModItems.GRAPPLE_DART.get());
            event.accept(ModItems.EXPLOSIVE_DART.get());
            event.accept(ModItems.BLOCK_MAGAZINE.get());
            event.accept(ModItems.REINFORCED_ROPE.get());
            event.accept(ModItems.BELT.get());
            return;
        }
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.insertAfter(new net.minecraft.world.item.ItemStack(Items.SKELETON_SPAWN_EGG),
                    new net.minecraft.world.item.ItemStack(ModItems.GEODE_SKELETON_SPAWN_EGG.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }
    private ModCreativeTabs() {}
}
