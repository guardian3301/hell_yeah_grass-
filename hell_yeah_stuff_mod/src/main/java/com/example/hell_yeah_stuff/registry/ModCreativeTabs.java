package com.example.hell_yeah_stuff.registry;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import com.example.hell_yeah_stuff.item.DaggerItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

/**
 * Предметы мода встают в креативе РЯДОМ СО СВОИМИ АНАЛОГАМИ (вкладка Combat):
 *  - арбалеты: ванильный -> мульти-арбалет -> рельсовый;
 *  - боеприпасы перед фейерверками;
 *  - кортики и скрытная накидка — рядом с мечами/оружием;
 *  - блочный магазин и верёвка — рядом с лошадиной бронёй.
 *
 * Аметистовый болт в вкладки НЕ добавляется: он существует только как
 * содержимое заряженного кортика/арбалета.
 */
@EventBusSubscriber(modid = HellYeahStuffMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ModCreativeTabs {

    private static final CreativeModeTab.TabVisibility VISIBLE =
            CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;

    @SubscribeEvent
    static void addToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() != CreativeModeTabs.COMBAT) {
            return;
        }
        event.accept(ModItems.RAIL_CROSSBOW.get());
        event.accept(ModItems.MULTI_CROSSBOW.get());
        event.accept(ModItems.DART.get());
        event.accept(ModItems.GRAPPLE_DART.get());
        event.accept(ModItems.EXPLOSIVE_DART.get());
        event.accept(ModItems.BLOCK_MAGAZINE.get());
        event.accept(ModItems.REINFORCED_ROPE.get());
    }

    private ModCreativeTabs() {}
}