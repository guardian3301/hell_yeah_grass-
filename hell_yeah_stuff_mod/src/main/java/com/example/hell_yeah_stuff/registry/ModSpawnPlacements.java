package com.example.hell_yeah_stuff.registry;
import com.example.hell_yeah_stuff.HellYeahStuffMod;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
@EventBusSubscriber(modid = HellYeahStuffMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ModSpawnPlacements {
    @SubscribeEvent
    public static void register(RegisterSpawnPlacementsEvent e){
        e.register(ModEntities.GEODE_SKELETON.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);
    }
    private ModSpawnPlacements(){}
}


