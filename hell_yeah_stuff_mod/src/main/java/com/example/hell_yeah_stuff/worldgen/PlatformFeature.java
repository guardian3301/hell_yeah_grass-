package com.example.hell_yeah_stuff.worldgen;

import com.example.hell_yeah_stuff.HellYeahStuffMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/**
 * Worldgen-фича: ставит случайный NBT-шаблон (ель с платформой/балкой/якорем)
 * в точке, где иначе выросла бы большая ель. Вызывается из random_selector'а,
 * подменяющего ванильные mega_spruce/mega_pine с шансом 5%.
 */
public class PlatformFeature extends Feature<NoneFeatureConfiguration> {

    /** Варианты построек — выбирается случайный при каждой генерации. */
    private static final ResourceLocation[] TEMPLATES = {
            ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "platform_1"),
            ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "platform_2"),
            ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "beam_1"),
            ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "beam_2"),
            ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "beam_3"),
            ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "beam_4"),
            ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "beam_5"),
            ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "broken_beam_1"),
            ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "angle_anchor_point"),
            ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "target_1"),
    };

    /** Наземный обломок балки: проверяем твёрдую землю под всей площадью 3x5. */
    private static final ResourceLocation BROKEN_BEAM =
            ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "broken_beam_1");

    /** Только балки — для повышенного шанса на возвышенностях и полянах. */
    private static final ResourceLocation[] BEAM_TEMPLATES = {
            ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "beam_1"),
            ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "beam_2"),
            ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "beam_3"),
            ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "beam_4"),
            ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "beam_5"),
            ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "broken_beam_1"),
    };

    /** Спутники балки у обрыва: якорная точка, балка или обломок (шанс 30%). */
    private static final ResourceLocation[] COMPANION_TEMPLATES = {
            ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "angle_anchor_point"),
            ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "beam_1"),
            ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "beam_2"),
            ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "beam_3"),
            ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "beam_4"),
            ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "beam_5"),
            ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "broken_beam_1"),
    };

    /** Лут-таблица для бочек в постройках платформ. */
    private static final ResourceKey<LootTable> BARREL_LOOT = ResourceKey.create(
            Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath(HellYeahStuffMod.MODID, "chests/platform_barrel"));

    /** Мин. дистанция между платформами (блоков по горизонтали). */
    private static final int MIN_SPACING = 96;
    /** Последние размещённые платформы (для дедупликации кластеров).
     *  Worldgen-декорация чанка идёт последовательно, но подстрахуемся синхронизацией. */
    private static final java.util.ArrayDeque<BlockPos> RECENT = new java.util.ArrayDeque<>();
    private static final int RECENT_LIMIT = 64;

    public PlatformFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    /** true, если рядом уже стоит платформа — тогда эту не ставим. */
    private static boolean tooClose(BlockPos origin) {
        synchronized (RECENT) {
            for (BlockPos p : RECENT) {
                long dx = p.getX() - origin.getX();
                long dz = p.getZ() - origin.getZ();
                if (dx * dx + dz * dz < (long) MIN_SPACING * MIN_SPACING) {
                    return true;
                }
            }
            RECENT.addFirst(origin.immutable());
            if (RECENT.size() > RECENT_LIMIT) {
                RECENT.removeLast();
            }
            return false;
        }
    }

    /**
     * true, если точка — самый возвышенный участок в радиусе 50 блоков.
     * Высоты берём из шума генератора (getBaseHeight) — он не требует
     * загрузки соседних чанков, поэтому радиус 50 безопасен.
     */
    private static boolean isLocalPeak(FeaturePlaceContext<NoneFeatureConfiguration> ctx, BlockPos origin) {
        WorldGenLevel level = ctx.level();
        RandomState randomState = level.getLevel().getChunkSource().randomState();
        int own = ctx.chunkGenerator().getBaseHeight(
                origin.getX(), origin.getZ(),
                Heightmap.Types.WORLD_SURFACE_WG, level, randomState);
        // 16 точек по двум окружностям (радиусы 25 и 50)
        for (int radius : new int[]{25, 50}) {
            for (int i = 0; i < 8; i++) {
                double angle = Math.PI * 2 * i / 8;
                int x = origin.getX() + (int) Math.round(Math.cos(angle) * radius);
                int z = origin.getZ() + (int) Math.round(Math.sin(angle) * radius);
                int h = ctx.chunkGenerator().getBaseHeight(
                        x, z, Heightmap.Types.WORLD_SURFACE_WG, level, randomState);
                if (h > own + 1) { // допуск 1 блок
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Ищет обрыв/скалу рядом: в радиусе 15 блоков точка с перепадом высоты
     * более 20 блоков относительно точки генерации (в любую сторону).
     * Возвращает направление на САМЫЙ большой перепад (с шагом 90 градусов,
     * north/south/east/west) или null, если обрыва нет.
     * Высоты — из шума генератора, соседние чанки не грузятся.
     */
    private static Direction cliffDirection(FeaturePlaceContext<NoneFeatureConfiguration> ctx, BlockPos origin) {
        WorldGenLevel level = ctx.level();
        RandomState randomState = level.getLevel().getChunkSource().randomState();
        int own = ctx.chunkGenerator().getBaseHeight(
                origin.getX(), origin.getZ(),
                Heightmap.Types.WORLD_SURFACE_WG, level, randomState);
        int bestDiff = 20; // порог: интересует перепад > 20
        double bestAngle = Double.NaN;
        // 8 точек по окружности радиуса 15 + 4 точки на радиусе 8
        for (int radius : new int[]{8, 15}) {
            int samples = radius == 15 ? 8 : 4;
            for (int i = 0; i < samples; i++) {
                double angle = Math.PI * 2 * i / samples;
                int x = origin.getX() + (int) Math.round(Math.cos(angle) * radius);
                int z = origin.getZ() + (int) Math.round(Math.sin(angle) * radius);
                int h = ctx.chunkGenerator().getBaseHeight(
                        x, z, Heightmap.Types.WORLD_SURFACE_WG, level, randomState);
                int diff = Math.abs(h - own);
                if (diff > bestDiff) {
                    bestDiff = diff;
                    bestAngle = angle;
                }
            }
        }
        if (Double.isNaN(bestAngle)) {
            return null;
        }
        // Квантуем угол к ближайшей стороне света (шаг 90 градусов).
        // angle: 0 -> +X (east), PI/2 -> +Z (south), PI -> -X (west), 3PI/2 -> -Z (north)
        int quadrant = (int) Math.round(bestAngle / (Math.PI / 2)) % 4;
        return switch (quadrant) {
            case 0 -> Direction.EAST;
            case 1 -> Direction.SOUTH;
            case 2 -> Direction.WEST;
            default -> Direction.NORTH;
        };
    }

    /**
     * true, если вокруг точки мало деревьев: считаем еловые брёвна в радиусе
     * 10 блоков (по горизонтали, высота +20 от origin). Один взрослый ствол —
     * ~6-30 брёвен, порог 12 означает "меньше пары деревьев рядом".
     */
    private static boolean fewTreesAround(WorldGenLevel level, BlockPos origin) {
        int logs = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -10; dx <= 10; dx++) {
            for (int dz = -10; dz <= 10; dz++) {
                if (dx * dx + dz * dz > 100) continue; // круг радиуса 10
                for (int dy = 0; dy <= 20; dy++) {
                    pos.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    if (level.getBlockState(pos).is(BlockTags.SPRUCE_LOGS)) {
                        logs++;
                        if (logs >= 12) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Пытается поставить рядом с балкой у обрыва спутник — якорную точку,
     * другую балку или обломок. Кандидаты ищутся в 10-14 блоках от основной
     * постройки в "чуть худших" местах: вбок или прочь от обрыва (не к краю).
     * Требование — твёрдая земля под стволом 2x2 и перепад высоты не больше
     * 3 блоков от основной точки.
     */
    private static void tryPlaceCompanion(FeaturePlaceContext<NoneFeatureConfiguration> ctx,
                                          BlockPos origin, Direction cliffDir,
                                          StructureTemplateManager manager) {
        WorldGenLevel level = ctx.level();
        // Направления-кандидаты: перпендикулярно обрыву и от него (не к обрыву).
        Direction[] candidates = {
                cliffDir.getClockWise(),
                cliffDir.getCounterClockWise(),
                cliffDir.getOpposite(),
        };
        // Перемешанный порядок, чтобы спутник появлялся с разных сторон.
        int shift = ctx.random().nextInt(candidates.length);
        for (int i = 0; i < candidates.length; i++) {
            Direction dir = candidates[(i + shift) % candidates.length];
            int dist = 10 + ctx.random().nextInt(5); // 10-14 блоков
            int x = origin.getX() + dir.getStepX() * dist;
            int z = origin.getZ() + dir.getStepZ() * dist;
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
            BlockPos spot = new BlockPos(x, y, z);
            // "Немного хуже" — но не обрыв: перепад от основной точки до 3 блоков.
            if (Math.abs(y - origin.getY()) > 3) {
                continue;
            }
            // Твёрдая земля под стволом 2x2.
            boolean solid = true;
            for (int dx = 0; dx <= 1 && solid; dx++) {
                for (int dz = 0; dz <= 1 && solid; dz++) {
                    BlockPos below = spot.offset(dx, -1, dz);
                    if (!level.getBlockState(below).isSolidRender(level, below)) {
                        solid = false;
                    }
                }
            }
            if (!solid) {
                continue;
            }
            ResourceLocation pick = COMPANION_TEMPLATES[ctx.random().nextInt(COMPANION_TEMPLATES.length)];
            StructureTemplate template = manager.getOrCreate(pick);
            if (template.getSize().equals(Vec3i.ZERO)) {
                continue;
            }
            Vec3i size = template.getSize();
            BlockPos pivot = new BlockPos(size.getX() / 2, 0, size.getZ() / 2);
            StructurePlaceSettings settings = new StructurePlaceSettings()
                    .setRotation(Rotation.getRandom(ctx.random()))
                    .setRotationPivot(pivot)
                    .setIgnoreEntities(false)
                    .addProcessor(BlockIgnoreProcessor.AIR);
            BlockPos placePos = spot.offset(-pivot.getX(), 0, -pivot.getZ());
            template.placeInWorld(level, placePos, placePos, settings, ctx.random(), 2);
            assignBarrelLoot(level, template, settings, placePos, ctx.random());
            return; // один спутник за раз
        }
    }

    /**
     * После установки шаблона находит все бочки внутри его габаритов
     * и вешает на пустые лут-таблицу platform_barrel (заполнится при открытии).
     */
    private static void assignBarrelLoot(WorldGenLevel level, StructureTemplate template,
                                         StructurePlaceSettings settings, BlockPos placePos,
                                         net.minecraft.util.RandomSource random) {
        BoundingBox box = template.getBoundingBox(settings, placePos);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = box.minX(); x <= box.maxX(); x++) {
            for (int y = box.minY(); y <= box.maxY(); y++) {
                for (int z = box.minZ(); z <= box.maxZ(); z++) {
                    pos.set(x, y, z);
                    if (level.getBlockEntity(pos) instanceof BarrelBlockEntity barrel) {
                        barrel.setLootTable(BARREL_LOOT, random.nextLong());
                    }
                }
            }
        }
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        BlockPos origin = ctx.origin(); // позиция "саженца" большой ели

        // Одна платформа за раз: если поблизости (96 блоков) уже поставили —
        // выращиваем на этом месте ничего (селектор уже "потратил" дерево,
        // но лес густой, соседние ели прикроют).
        if (tooClose(origin)) {
            return false;
        }

        // Селектор в JSON срабатывает с шансом 70%. Ярусы мест:
        //  - у обрыва (перепад >20 блоков в радиусе 15): балка, полный шанс 70%;
        //  - "хорошее" место (пик в радиусе 50 или мало деревьев в радиусе 10):
        //    балка, полный шанс 70%;
        //  - обычное место: любой вариант, шанс 2.5% (пропускаем оставшиеся попытки).
        ResourceLocation chosen;
        Direction cliffDir = cliffDirection(ctx, origin);
        if (cliffDir != null) {
            chosen = BEAM_TEMPLATES[ctx.random().nextInt(BEAM_TEMPLATES.length)];
        } else if (isLocalPeak(ctx, origin) || fewTreesAround(level, origin)) {
            chosen = BEAM_TEMPLATES[ctx.random().nextInt(BEAM_TEMPLATES.length)];
        } else {
            if (ctx.random().nextFloat() >= (0.025f / 0.70f)) {
                return false;
            }
            chosen = TEMPLATES[ctx.random().nextInt(TEMPLATES.length)];
        }

        // Проверка грунта под постройкой. Для наземного обломка балки
        // (broken_beam_1, 3x5) — строго твёрдая земля под всей площадью.
        // Для деревьев (ствол 2x2) допускаем воздушные ямы: считаем воздух
        // под каждой колонной ствола (до 4 вглубь). Вода/листва — отказ,
        // слишком глубокая пустота (4+) — тоже отказ.
        int totalAirBelow = 0;
        if (chosen.equals(BROKEN_BEAM)) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos below = origin.offset(dx, -1, dz);
                    if (!level.getBlockState(below).isSolidRender(level, below)) {
                        return false;
                    }
                }
            }
        } else {
            for (int dx = 0; dx <= 1; dx++) {
                for (int dz = 0; dz <= 1; dz++) {
                    int airDepth = 0;
                    while (airDepth < 4) {
                        BlockPos below = origin.offset(dx, -1 - airDepth, dz);
                        if (level.getBlockState(below).isAir()) {
                            airDepth++;
                        } else if (level.getBlockState(below).isSolidRender(level, below)) {
                            break;
                        } else {
                            return false; // вода/листва — не ставим
                        }
                    }
                    if (airDepth >= 4) {
                        return false; // висит слишком высоко
                    }
                    totalAirBelow += airDepth;
                }
            }
        }

        StructureTemplateManager manager = level.getLevel().getServer()
                .getStructureManager();
        StructureTemplate template = manager.getOrCreate(chosen);
        if (template.getSize().equals(Vec3i.ZERO)) {
            return false;
        }

        // Поворот вокруг центра ствола: у обрыва балка разворачивается в его
        // сторону (шаг 90 градусов); иначе — случайный поворот для разнообразия.
        // Шаблоны балок построены "рабочей стороной" на север (-Z).
        Rotation rotation;
        if (cliffDir != null) {
            rotation = switch (cliffDir) {
                case EAST -> Rotation.CLOCKWISE_90;
                case SOUTH -> Rotation.CLOCKWISE_180;
                case WEST -> Rotation.COUNTERCLOCKWISE_90;
                default -> Rotation.NONE; // NORTH
            };
        } else {
            rotation = Rotation.getRandom(ctx.random());
        }
        Vec3i size = template.getSize();
        BlockPos pivot = new BlockPos(size.getX() / 2, 0, size.getZ() / 2);

        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(rotation)
                .setRotationPivot(pivot)
                .setIgnoreEntities(false)
                // Воздух шаблона не переносим в мир: там, где в NBT нет
                // structure_void, воздух больше не срезает соседние деревья —
                // "сломанные" обрезкой ели не появляются.
                .addProcessor(BlockIgnoreProcessor.AIR);

        // Центрируем шаблон на точке дерева (ствол 2x2 в середине куска 8x8).
        BlockPos placePos = origin.offset(-pivot.getX(), 0, -pivot.getZ());
        template.placeInWorld(level, placePos, placePos, settings, ctx.random(), 2);
        assignBarrelLoot(level, template, settings, placePos, ctx.random());

        // Если под фичей было больше 2 блоков воздуха — продлеваем ствол 2x2
        // на два блока вниз еловыми брёвнами, чтобы дерево не висело.
        if (totalAirBelow > 2) {
            BlockState log = Blocks.SPRUCE_LOG.defaultBlockState();
            for (int dx = 0; dx <= 1; dx++) {
                for (int dz = 0; dz <= 1; dz++) {
                    for (int dy = 1; dy <= 2; dy++) {
                        BlockPos below = origin.offset(dx, -dy, dz);
                        if (level.getBlockState(below).isAir()) {
                            level.setBlock(below, log, 2);
                        }
                    }
                }
            }
        }

        // Рядом с балкой у обрыва — 30% шанс спутника (якорная точка,
        // другая балка или обломок) в соседнем "чуть худшем" месте.
        if (cliffDir != null && ctx.random().nextFloat() < 0.45f) {
            tryPlaceCompanion(ctx, origin, cliffDir, manager);
        }
        return true;
    }
}
