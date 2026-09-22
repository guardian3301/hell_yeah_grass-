package com.example.hell_yeah_stuff.registry;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import com.example.hell_yeah_stuff.block.SharpAmethystDustBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Блоки мода. */
public final class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, HellYeahStuffMod.MODID);

    /**
     * «Острая аметистовая пыль» — временное облако от аметистового болта.
     * Без коллизии, без лут-таблицы (не выпадает), ломается поршнями,
     * принимает отложенные тики для самоуничтожения через 5 секунд.
     */
    public static final DeferredHolder<Block, SharpAmethystDustBlock> SHARP_AMETHYST_DUST =
            BLOCKS.register("sharp_amethyst_dust",
                    () -> new SharpAmethystDustBlock(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .noCollission()
                            .noOcclusion()
                            .instabreak()
                            .noLootTable()
                            .replaceable()
                            .pushReaction(PushReaction.DESTROY)
                            .sound(SoundType.AMETHYST_CLUSTER)));

    private ModBlocks() {}
}