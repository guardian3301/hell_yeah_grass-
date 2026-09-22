package com.example.hell_yeah_stuff.registry;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Кастомные частицы мода. */
public final class ModParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, HellYeahStuffMod.MODID);

    /** След рывка при отцеплении крюка-кошки (particle/dash_trail). */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> DASH_TRAIL =
            PARTICLE_TYPES.register("dash_trail", () -> new SimpleParticleType(false));

    private ModParticles() {}
}
