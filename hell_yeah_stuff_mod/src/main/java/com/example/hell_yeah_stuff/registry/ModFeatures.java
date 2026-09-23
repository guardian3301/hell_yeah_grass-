package com.example.hell_yeah_stuff.registry;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import com.example.hell_yeah_stuff.worldgen.AmethystOutcropFeature;
import com.example.hell_yeah_stuff.worldgen.PlatformFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Регистрация worldgen-фич, которые остаются в моде. */
public final class ModFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, HellYeahStuffMod.MODID);

    /** Ель с платформой; ID совпадает с configured feature datapack. */
    public static final DeferredHolder<Feature<?>, PlatformFeature> PLATFORM_TREE =
            FEATURES.register("platform_tree", PlatformFeature::new);

    /** Наземный аметистовый выход. */
    public static final DeferredHolder<Feature<?>, AmethystOutcropFeature> AMETHYST_OUTCROP =
            FEATURES.register("amethyst_outcrop", AmethystOutcropFeature::new);

    private ModFeatures() {}
}
