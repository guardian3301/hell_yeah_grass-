package com.example.hell_yeah_stuff.worldgen;

import com.example.hell_yeah_stuff.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Процедурная фича: разодранное дерево (дуб/берёза).
 *
 * <p>Генерирует повреждённое дерево — ствол с «вырванным» участком,
 * редкие листья. Дерево должно выглядеть так, словно его разорвало или повредили.
 *
 * <p>Частота: ~1 на 256 чанков (настраивается через placed_feature/reduction).
 */
public class TornTreeFeature extends Feature<NoneFeatureConfiguration> {

    public TornTreeFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        BlockPos origin = ctx.origin();
        RandomSource random = ctx.random();

        // Проверяем грунт — нужна трава/земля/подзол
        BlockPos groundPos = origin.below();
        BlockState groundState = level.getBlockState(groundPos);
        if (!groundState.is(BlockTags.DIRT)) {
            return false;
        }

        // Проверяем, что нет других блоков над местом генерации
        if (!level.getBlockState(origin).isAir()) {
            return false;
        }

        // Выбираем тип дерева: дуб (50%) или берёза (50%)
        boolean isOak = random.nextBoolean();
        BlockState log = isOak ? Blocks.OAK_LOG.defaultBlockState() : Blocks.BIRCH_LOG.defaultBlockState();
        BlockState leaves = isOak ? Blocks.OAK_LEAVES.defaultBlockState() : Blocks.BIRCH_LEAVES.defaultBlockState();

        // Высота: 5-8 блоков
        int height = 5 + random.nextInt(4);

        // Ствол
        List<BlockPos> trunkPositions = new ArrayList<>();
        for (int dy = 0; dy < height; dy++) {
            BlockPos pos = origin.above(dy);
            level.setBlock(pos, log, 2);
            trunkPositions.add(pos);
        }

        // "Вырванный" участок: убираем 1-2 бревна из ствола и заменяем листьями
        int tearStart = 2 + random.nextInt(2); // где начинается повреждение
        for (int dy = tearStart; dy < Math.min(tearStart + 1 + random.nextInt(2), height - 1); dy++) {
            BlockPos pos = origin.above(dy);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        }

        // В месте повреждения оставляем листьевые остатки
        for (int dy = tearStart; dy < Math.min(tearStart + 1 + random.nextInt(2), height - 1); dy++) {
            int radius = 1;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) == radius && Math.abs(dz) == radius) continue;
                    if (random.nextFloat() < 0.3f) continue;
                    BlockPos leafPos = origin.offset(dx, dy, dz);
                    if (level.getBlockState(leafPos).isAir()) {
                        level.setBlock(leafPos, leaves, 2);
                    }
                }
            }
        }

        // Крона: разреженная и повреждённая
        int crownStart = tearStart + 1;
        int crownEnd = height - 1;
        for (int dy = crownStart; dy <= crownEnd; dy++) {
            int radius = (dy == crownEnd) ? 1 : 2;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    // Пропускаем углы (крестообразная форма)
                    if (Math.abs(dx) == radius && Math.abs(dz) == radius) continue;
                    // Случайные пропуски для "повреждённого" вида
                    if (random.nextFloat() < 0.4f) continue;

                    BlockPos leafPos = origin.offset(dx, dy, dz);
                    if (level.getBlockState(leafPos).isAir()) {
                        level.setBlock(leafPos, leaves, 2);
                    }
                }
            }
        }

        return true;
    }
}
