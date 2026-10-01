package com.example.hell_yeah_stuff.registry;
import com.example.hell_yeah_stuff.HellYeahStuffMod;
import com.example.hell_yeah_stuff.entity.GeodeSkeletonEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
@EventBusSubscriber(modid = HellYeahStuffMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ModEntityAttributes {
    @SubscribeEvent
    public static void onCreate(EntityAttributeCreationEvent e){
        e.put(ModEntities.GEODE_SKELETON.get(), GeodeSkeletonEntity.createAttributes().build());
    }
    private ModEntityAttributes(){}
}
