package com.example.hell_yeah_stuff;

import com.example.hell_yeah_stuff.registry.ModBlockEntities;
import com.example.hell_yeah_stuff.registry.ModBlocks;
import com.example.hell_yeah_stuff.registry.ModDataComponents;
import com.example.hell_yeah_stuff.registry.ModEnchantmentEffects;
import com.example.hell_yeah_stuff.registry.ModEntities;
import com.example.hell_yeah_stuff.registry.ModFeatures;
import com.example.hell_yeah_stuff.registry.ModItems;
import com.example.hell_yeah_stuff.registry.ModMobEffects;
import com.example.hell_yeah_stuff.registry.ModParticles;
import com.example.hell_yeah_stuff.registry.ModSounds;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(HellYeahStuffMod.MODID)
public class HellYeahStuffMod {
    public static final String MODID = "hell_yeah_stuff";

    public HellYeahStuffMod(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.BLOCKS.register(modEventBus); // >>> NEW: острая аметистовая пыль
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        // ModCreativeTabs регистрируется сам через @EventBusSubscriber —
        // он добавляет предметы в ванильную вкладку Combat, отдельного
        // DeferredRegister у него нет.
        ModSounds.SOUND_EVENTS.register(modEventBus); // >>> NEW: кастомные звуки
        ModFeatures.FEATURES.register(modEventBus); // ель с платформой (worldgen)
        ModParticles.PARTICLE_TYPES.register(modEventBus); // взмах аметистовой сабли
        ModMobEffects.MOB_EFFECTS.register(modEventBus); // «Взрывоопасность» от взрывного дротика
        ModEnchantmentEffects.ENCHANTMENT_ENTITY_EFFECTS.register(modEventBus); // зачарование «Забвение»
        ModDataComponents.DATA_COMPONENTS.register(modEventBus); // варианты кортиков
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus); // пустой реестр (block entities удалены)
    }
}
