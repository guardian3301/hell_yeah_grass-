package com.example.hell_yeah_stuff.registry;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import com.example.hell_yeah_stuff.worldgen.AmethystOutcropFeature;
import com.example.hell_yeah_stuff.worldgen.PlatformFeature;
import com.example.hell_yeah_stuff.worldgen.TornTreeFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Регистрация worldgen-фич мода. */
public final class ModFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, HellYeahStuffMod.MODID);

    /** Ель с платформой — спавнится вместо большой ели с шансом 5%. */
    public static final DeferredHolder<Feature<?>, PlatformFeature> PLATFORM =
            FEATURES.register("platform", PlatformFeature::new);

    /** Наземный аметистовый выход — горка аметиста с кристаллами. */
    public static final DeferredHolder<Feature<?>, AmethystOutcropFeature> AMETHYST_OUTCROP =
            FEATURES.register("amethyst_outcrop", AmethystOutcropFeature::new);

    /** Разодранное дерево с застрявшим ржавым кортиком. */
    public static final DeferredHolder<Feature<?>, TornTreeFeature> TORN_TREE =
            FEATURES.register("torn_tree", TornTreeFeature::new);

    private ModFeatures() {
    }
}
