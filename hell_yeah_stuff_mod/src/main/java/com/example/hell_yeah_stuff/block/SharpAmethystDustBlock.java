package com.example.hell_yeah_stuff.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * «Острая аметистовая пыль» — временный блок от аметистового болта,
 * по поведению — аналог порошкового снега.
 *
 * <ul>
 *   <li>Не имеет коллизии — сквозь него можно ходить.</li>
 *   <li>Любое живое внутри каждый тик получает Замедление II на 3 секунды
 *       (эффект постоянно обновляется, пока цель стоит в облаке).</li>
 *   <li>Сам блок исчезает через {@link #LIFETIME_TICKS} (5 секунд) по
 *       отложенному тику, запланированному при установке.</li>
 * </ul>
 */
public class SharpAmethystDustBlock extends Block {

    /** Время жизни блока — 5 секунд. */
    public static final int LIFETIME_TICKS = 100;
    /** Длительность замедления, навешиваемого каждый тик — 3 секунды. */
    private static final int SLOWNESS_DURATION = 60;
    /** Амплитуда 1 == Замедление II. */
    private static final int SLOWNESS_AMPLIFIER = 1;

    public SharpAmethystDustBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        // Самоуничтожение через 5 секунд.
        level.scheduleTick(pos, this, LIFETIME_TICKS);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.removeBlock(pos, false);
        level.levelEvent(2001, pos, Block.getId(state));
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || !(entity instanceof LivingEntity living)) {
            return;
        }
        living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                SLOWNESS_DURATION, SLOWNESS_AMPLIFIER, false, true, true));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        for (int i = 0; i < 3; i++) {
            level.addParticle(ParticleTypes.WITCH,
                    pos.getX() + random.nextDouble(),
                    pos.getY() + random.nextDouble() * 0.6D,
                    pos.getZ() + random.nextDouble(),
                    0.0D, 0.01D, 0.0D);
        }
    }
}
