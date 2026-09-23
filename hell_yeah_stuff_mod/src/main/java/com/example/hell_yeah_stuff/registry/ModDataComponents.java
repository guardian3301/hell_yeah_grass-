package com.example.hell_yeah_stuff.registry;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Реестр data components мода.
 *
 * <p>Компоненты удалённых кортиков не восстанавливаются. Реестр оставлен
 * пустым, поскольку он корректно регистрируется и позволяет добавлять новые
 * компоненты без изменения bootstrap-класса.</p>
 */
public final class ModDataComponents {

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, HellYeahStuffMod.MODID);

    private ModDataComponents() {}
}
