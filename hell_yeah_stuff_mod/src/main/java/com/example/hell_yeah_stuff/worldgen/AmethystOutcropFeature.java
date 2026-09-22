package com.example.hell_yeah_stuff.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Worldgen-фича: наземный аметистовый выход — небольшая горка аметистовых
 * блоков с прорастающим аметистом внутри и кристаллами сверху. Источник
 * осколков для аметистовых дротиков без спуска к жеодам.
 * Подключается через placed_feature + neoforge biome modifier
 * (data/hell_yeah_stuff/neoforge/biome_modifier/add_amethyst_outcrop.json).
 */
public class AmethystOutcropFeature extends Feature<NoneFeatureConfiguration> {

    public AmethystOutcropFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        RandomSource random = ctx.random();
        BlockPos origin = ctx.origin();

        BlockPos below = origin.below();
        if (!level.getBlockState(below).isSolidRender(level, below)) {
            return false;
        }
        if (!level.getBlockState(origin).isAir()) {
            // Точка внутри листвы/воды — пропускаем, выход должен торчать наружу.
            return false;
        }

        List<BlockPos> tops = new ArrayList<>();
        boolean placedAny = false;

        // Горка 3x3 (углы — с шансом), высота столбика 1-3 к центру.
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                boolean corner = dx != 0 && dz != 0;
                if (corner && random.nextFloat() < 0.5F) {
                    continue;
                }
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                BlockPos base = new BlockPos(x, surfaceY, z);
                BlockPos under = base.below();
                if (!level.getBlockState(under).isSolidRender(level, under)) {
                    continue;
                }
                // Перепад больше 2 блоков от центра — обрыв, не тянем колонны.
                if (Math.abs(surfaceY - origin.getY()) > 2) {
                    continue;
                }
                int height = (dx == 0 && dz == 0) ? 2 + random.nextInt(2)
                        : 1 + random.nextInt(corner ? 1 : 2);
                BlockPos top = base;
                for (int dy = 0; dy < height; dy++) {
                    BlockPos pos = base.above(dy);
                    if (!level.getBlockState(pos).isAir()
                            && !level.getBlockState(pos).canBeReplaced()) {
                        break;
                    }
                    level.setBlock(pos, Blocks.AMETHYST_BLOCK.defaultBlockState(), 2);
                    top = pos;
                    placedAny = true;
                }
                if (placedAny) {
                    tops.add(top);
                }
            }
        }

        if (!placedAny) {
            return false;
        }

        // Ядро из прорастающего аметиста — выход можно «фармить» как жеоду.
        level.setBlock(new BlockPos(origin.getX(), origin.getY(), origin.getZ()),
                Blocks.BUDDING_AMETHYST.defaultBlockState(), 2);

        // Кристаллы сверху: на части вершин — целые друзы, на части — бутоны.
        for (BlockPos top : tops) {
            BlockPos above = top.above();
            if (!level.getBlockState(above).isAir()) {
                continue;
            }
            float roll = random.nextFloat();
            if (roll < 0.35F) {
                level.setBlock(above, Blocks.AMETHYST_CLUSTER.defaultBlockState(), 2);
            } else if (roll < 0.55F) {
                level.setBlock(above, Blocks.LARGE_AMETHYST_BUD.defaultBlockState(), 2);
            } else if (roll < 0.7F) {
                level.setBlock(above, Blocks.MEDIUM_AMETHYST_BUD.defaultBlockState(), 2);
            }
        }
        return true;
    }
}
