package com.example.hell_yeah_stuff.entity;

import com.example.hell_yeah_stuff.registry.ModEntities;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Осколок аметиста — «дробина» аметистового дротика. При выстреле из
 * мульти-арбалета один дротик дробится на несколько осколков с разбросом,
 * как у дробовика. Осколки одноразовые: разбиваются о блок/цель со звоном
 * аметиста и не поднимаются обратно.
 */
public class AmethystShardEntity extends AbstractArrow {

    /** Урон одного осколка (полный веер вблизи складывается в большой урон). */
    private static final double DAMAGE = 2.0D;
    /** Дальность жизни: через 40 тиков осколок рассыпается в воздухе. */
    private static final int MAX_LIFE_TICKS = 40;

    public AmethystShardEntity(EntityType<? extends AmethystShardEntity> type, Level level) {
        super(type, level);
        this.setBaseDamage(DAMAGE);
        this.pickup = Pickup.DISALLOWED;
    }

    public AmethystShardEntity(Level level, LivingEntity shooter, @Nullable ItemStack firedFromWeapon) {
        super(ModEntities.AMETHYST_SHARD.get(), shooter, level,
                new ItemStack(Items.AMETHYST_SHARD), firedFromWeapon);
        this.setBaseDamage(DAMAGE);
        this.pickup = Pickup.DISALLOWED;
        this.setSoundEvent(SoundEvents.AMETHYST_BLOCK_HIT);
    }

    public AmethystShardEntity(Level level, double x, double y, double z) {
        super(ModEntities.AMETHYST_SHARD.get(), x, y, z, level,
                new ItemStack(Items.AMETHYST_SHARD), null);
        this.setBaseDamage(DAMAGE);
        this.pickup = Pickup.DISALLOWED;
        this.setSoundEvent(SoundEvents.AMETHYST_BLOCK_HIT);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.tickCount > MAX_LIFE_TICKS) {
            this.shatter();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        this.shatter();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        this.shatter();
    }

    /** Рассыпается с осколками и звоном аметиста, сущность удаляется. */
    private void shatter() {
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(
                    new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.AMETHYST_SHARD)),
                    this.getX(), this.getY(), this.getZ(),
                    6, 0.08D, 0.08D, 0.08D, 0.05D);
            server.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.NEUTRAL,
                    0.7F, 0.9F + this.random.nextFloat() * 0.4F);
            this.discard();
        }
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.AMETHYST_SHARD);
    }
}
