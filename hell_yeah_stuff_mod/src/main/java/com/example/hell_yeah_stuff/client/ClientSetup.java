package com.example.hell_yeah_stuff.client;

import com.example.hell_yeah_stuff.client.StealthCloakClientExtensions;
import com.example.hell_yeah_stuff.client.model.CowlArmorModel;
import com.example.hell_yeah_stuff.HellYeahStuffMod;
import com.example.hell_yeah_stuff.item.RailCrossbowItem;
import com.example.hell_yeah_stuff.registry.ModEnchantments;
import com.example.hell_yeah_stuff.registry.ModEntities;
import com.example.hell_yeah_stuff.registry.ModItems;
import com.example.hell_yeah_stuff.registry.ModParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = HellYeahStuffMod.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ClientSetup {

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Same model predicates as the vanilla crossbow so the item
            // animates identically in hand (pulling frames + charged states).
            ItemProperties.register(ModItems.MULTI_CROSSBOW.get(),
                    ResourceLocation.withDefaultNamespace("pull"),
                    (stack, level, entity, seed) -> {
                        if (entity == null) {
                            return 0.0F;
                        }
                        return CrossbowItem.isCharged(stack)
                                ? 0.0F
                                : (float) (stack.getUseDuration(entity) - entity.getUseItemRemainingTicks())
                                        / (float) CrossbowItem.getChargeDuration(stack, entity);
                    });

            ItemProperties.register(ModItems.MULTI_CROSSBOW.get(),
                    ResourceLocation.withDefaultNamespace("pulling"),
                    (stack, level, entity, seed) ->
                            entity != null
                                    && entity.isUsingItem()
                                    && entity.getUseItem() == stack
                                    && !CrossbowItem.isCharged(stack) ? 1.0F : 0.0F);

            ItemProperties.register(ModItems.MULTI_CROSSBOW.get(),
                    ResourceLocation.withDefaultNamespace("charged"),
                    (stack, level, entity, seed) -> CrossbowItem.isCharged(stack) ? 1.0F : 0.0F);

            // Custom predicate: 1.0 when an explosive dart is loaded.
            ItemProperties.register(ModItems.MULTI_CROSSBOW.get(),
                    ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "explosive"),
                    (stack, level, entity, seed) -> {
                        ChargedProjectiles charged = stack.get(DataComponents.CHARGED_PROJECTILES);
                        return charged != null && charged.contains(ModItems.EXPLOSIVE_DART.get()) ? 1.0F : 0.0F;
                    });

            // 1.0 when a grapple dart is loaded.
            ItemProperties.register(ModItems.MULTI_CROSSBOW.get(),
                    ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "grapple"),
                    (stack, level, entity, seed) -> {
                        ChargedProjectiles charged = stack.get(DataComponents.CHARGED_PROJECTILES);
                        return charged != null && charged.contains(ModItems.GRAPPLE_DART.get()) ? 1.0F : 0.0F;
                    });

            // 1.0 when an amethyst shard is loaded.
            ItemProperties.register(ModItems.MULTI_CROSSBOW.get(),
                    ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "amethyst"),
                    (stack, level, entity, seed) -> {
                        ChargedProjectiles charged = stack.get(DataComponents.CHARGED_PROJECTILES);
                        return charged != null && charged.contains(Items.AMETHYST_SHARD) ? 1.0F : 0.0F;
                    });

            // 1.0 когда на арбалете стоит зачарование «Аметистовые гранаты».
            ItemProperties.register(ModItems.MULTI_CROSSBOW.get(),
                    ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "grenades"),
                    (stack, level, entity, seed) ->
                            ModEnchantments.level(stack, ModEnchantments.AMETHYST_GRENADES) > 0 ? 1.0F : 0.0F);

            // >>> NEW: рельсовый арбалет.
            registerRailCrossbowProperties(ModItems.RAIL_CROSSBOW.get());
        });
    }

    /**
     * Предикаты модели рельсового арбалета:
     * <ul>
     *   <li>{@code pull} / {@code pulling} / {@code charged} — как у ванильного арбалета;</li>
     *   <li>{@code amethyst} — заряжен аметистовый болт;</li>
     *   <li>{@code regenerating} — идёт синтез болта конденсатором;</li>
     *   <li>{@code condenser} — уровень конденсатора / 3 (0.333 / 0.666 / 1.0),
     *       чтобы кадры анимации rail_crossbow_regenerating_1..3 соответствовали уровню;</li>
     *   <li>{@code regen} — прогресс синтеза 0..1 (на будущее: плавные кадры).</li>
     * </ul>
     */
    private static void registerRailCrossbowProperties(Item crossbow) {
        ItemProperties.register(crossbow,
                ResourceLocation.withDefaultNamespace("pull"),
                (stack, level, entity, seed) -> {
                    if (entity == null || entity.getUseItem() != stack || RailCrossbowItem.isCharged(stack)) {
                        return 0.0F;
                    }
                    float charge = CrossbowItem.getChargeDuration(stack, entity);
                    if (charge <= 0.0F) {
                        return 0.0F;
                    }
                    float used = stack.getUseDuration(entity) - entity.getUseItemRemainingTicks();
                    return Math.min(used / charge, 1.0F);
                });

        ItemProperties.register(crossbow,
                ResourceLocation.withDefaultNamespace("pulling"),
                (stack, level, entity, seed) ->
                        entity != null
                                && entity.isUsingItem()
                                && entity.getUseItem() == stack
                                && !RailCrossbowItem.isCharged(stack) ? 1.0F : 0.0F);

        ItemProperties.register(crossbow,
                ResourceLocation.withDefaultNamespace("charged"),
                (stack, level, entity, seed) -> RailCrossbowItem.isCharged(stack) ? 1.0F : 0.0F);

        ItemProperties.register(crossbow,
                ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "amethyst"),
                (stack, level, entity, seed) -> RailCrossbowItem.isAmethystLoaded(stack) ? 1.0F : 0.0F);

        ItemProperties.register(crossbow,
                ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "regenerating"),
                (stack, level, entity, seed) ->
                        RailCrossbowItem.isRegenerating(stack, gameTime(level, entity)) ? 1.0F : 0.0F);

        // 0.0 — синтеза нет; во время синтеза — строго больше нуля (0.01..1.0),
        // чтобы override’ы кадров rail_crossbow_regenerating_1..3 не срабатывали вхолостую.
        ItemProperties.register(crossbow,
                ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "regen"),
                (stack, level, entity, seed) -> {
                    long now = gameTime(level, entity);
                    if (!RailCrossbowItem.isRegenerating(stack, now)) {
                        return 0.0F;
                    }
                    return 0.01F + RailCrossbowItem.regenProgress(stack, now) * 0.99F;
                });

        ItemProperties.register(crossbow,
                ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "condenser"),
                (stack, level, entity, seed) ->
                        ModEnchantments.level(stack, ModEnchantments.AMETHYST_CONDENSER) / 3.0F);
    }

    /** Игровое время для предикатов (level в них может быть null). */
    private static long gameTime(@Nullable ClientLevel level, @Nullable LivingEntity entity) {
        if (level != null) {
            return level.getGameTime();
        }
        return entity != null ? entity.level().getGameTime() : 0L;
    }

    @SubscribeEvent
    static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.DART.get(), DartRenderer::new);
        event.registerEntityRenderer(ModEntities.EXPLOSIVE_DART.get(), ExplosiveDartRenderer::new);
        event.registerEntityRenderer(ModEntities.GRAPPLE_DART.get(), GrappleDartRenderer::new);
        event.registerEntityRenderer(ModEntities.AMETHYST_SHARD.get(), AmethystShardRenderer::new);
        event.registerEntityRenderer(ModEntities.AMETHYST_GRENADE.get(), AmethystGrenadeRenderer::new);
        // >>> NEW: болты рельсового арбалета.
        event.registerEntityRenderer(ModEntities.IRON_BOLT.get(), IronBoltRenderer::new);
        event.registerEntityRenderer(ModEntities.AMETHYST_BOLT.get(), AmethystBoltRenderer::new);
    }

    @SubscribeEvent
    static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // Регистрируем слой модели накидки (cowl) для брони
        event.registerLayerDefinition(CowlArmorModel.LAYER, CowlArmorModel::createBodyLayer);
    }

    @SubscribeEvent
    static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        // След рывка зачарования «Рывок» на поножах (particle/dash_trail).
        event.registerSpriteSet(ModParticles.DASH_TRAIL.get(),
                sprites -> new DashTrailParticle.Provider(sprites));
    }

    @SubscribeEvent
    static void onRegisterItemDecorations(RegisterItemDecorationsEvent event) {
        // Жёлтая плашка зарядов магазина над шкалой прочности мульти-арбалета.
        event.register(ModItems.MULTI_CROSSBOW.get(), new MagazineBarDecorator());
    }

    @SubscribeEvent
    static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        // Gives the multi crossbow the exact same first/third person hold and
        // charge animations as the vanilla crossbow.
        event.registerItem(new MultiCrossbowClientExtensions(), ModItems.MULTI_CROSSBOW.get());
        // >>> NEW: рельсовый арбалет — позы ванильного арбалета.
        event.registerItem(new RailCrossbowClientExtensions(), ModItems.RAIL_CROSSBOW.get());
        // Скрытная накидка
        event.registerItem(new StealthCloakClientExtensions(), ModItems.STEALTH_CLOAK.get());
    }

    private ClientSetup() {}
}
