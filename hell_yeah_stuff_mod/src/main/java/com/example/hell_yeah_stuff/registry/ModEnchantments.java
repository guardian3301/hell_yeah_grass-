package com.example.hell_yeah_stuff.registry;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * Ключи data-driven зачарований мода (сами определения — в JSON:
 * data/hell_yeah_stuff/enchantment/*.json) и утилиты для их чтения со стека.
 */
public final class ModEnchantments {

    /**
     * «Аметистовые гранаты» — мульти-арбалет стреляет аметистовым боезапасом
     * как контактной гранатой (взрыв при ударе) вместо дробового веера.
     */
    public static final ResourceKey<Enchantment> AMETHYST_GRENADES = ResourceKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "amethyst_grenades"));

    /**
     * «Рывок» — базовое зачарование поножей: по клавише (по умолчанию G)
     * игрок делает рывок в направлении взгляда, а из центра хитбокса
     * (торса) полсекунды идут частицы dash_trail. См. DashTrailHandler.
     */
    public static final ResourceKey<Enchantment> DASH = ResourceKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "dash"));

    /**
     * «Аметистовый конденсатор» для рельсового арбалета (I–III):
     * позволяет натягивать арбалет без болтов в инвентаре — за 15/10/5 секунд
     * в нём синтезируется аметистовый болт. См. RailCrossbowItem.
     */
    public static final ResourceKey<Enchantment> AMETHYST_CONDENSER = ResourceKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "amethyst_condenser"));


    private ModEnchantments() {}
}
