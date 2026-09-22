package com.example.hell_yeah_stuff.entity;

import com.example.hell_yeah_stuff.registry.ModEntities;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Аметистовая контактная граната. Стреляется из мульти-арбалета аметистовым
 * дротиком, когда на арбалете стоит зачарование «Аметистовые гранаты»:
 * вместо дробового веера летит один снаряд, взрывающийся при ЛЮБОМ
 * контакте (без таймера, в отличие от взрывного дротика).
 *
 * При контакте с ПОВЕРХНОСТЬЮ (блоком) — осколочный эффект: помимо взрыва
 * от точки удара во все стороны разлетаются {@value #SHRAPNEL_COUNT}
 * аметистовых дробинок (полусфера от грани блока).
 */
public class AmethystGrenadeEntity extends AbstractArrow {

    /** Мощность взрыва (у ТНТ = 4.0, у взрывного дротика = 2.5). */
    private static final float EXPLOSION_POWER = 2.0F;
    /** Сколько дробинок разлетается при ударе о поверхность. */
    private static final int SHRAPNEL_COUNT = 12;
    /** Скорость разлёта дробинок (блоков/тик), минимум и разброс. */
    private static final float SHRAPNEL_SPEED_MIN = 0.55F;
    private static final float SHRAPNEL_SPEED_VAR = 0.5F;
    /** Отступ точки спавна дробинок от поверхности вдоль нормали. */
    private static final double SHRAPNEL_SURFACE_OFFSET = 0.3D;

    public AmethystGrenadeEntity(EntityType<? extends AmethystGrenadeEntity> type, Level level) {
        super(type, level);
        this.pickup = Pickup.DISALLOWED;
    }

    public AmethystGrenadeEntity(Level level, LivingEntity shooter, @Nullable ItemStack firedFromWeapon) {
        super(ModEntities.AMETHYST_GRENADE.get(), shooter, level,
                new ItemStack(Items.AMETHYST_SHARD), firedFromWeapon);
        this.pickup = Pickup.DISALLOWED;
    }

    @Override
    public void tick() {
        super.tick();
        // Хрустальный след, чтобы гранату было видно в полёте.
        if (this.level().isClientSide && !this.inGround) {
            this.level().addParticle(net.minecraft.core.particles.ParticleTypes.END_ROD,
                    this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.level().isClientSide) {
            Vec3 normal = Vec3.atLowerCornerOf(result.getDirection().getNormal());
            Vec3 origin = result.getLocation().add(normal.scale(SHRAPNEL_SURFACE_OFFSET));
            this.explode();
            // Осколки — ПОСЛЕ взрыва, чтобы он их не разметал/не повредил.
            this.scatterShrapnel(origin, normal);
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!this.level().isClientSide) {
            // Полусфера осколков — навстречу полёту (от «поверхности» цели).
            Vec3 motion = this.getDeltaMovement();
            Vec3 normal = motion.lengthSqr() > 1.0E-6D
                    ? motion.normalize().scale(-1.0D)
                    : new Vec3(0.0D, 1.0D, 0.0D);
            Vec3 origin = this.position().add(normal.scale(SHRAPNEL_SURFACE_OFFSET));
            this.explode();
            this.scatterShrapnel(origin, normal);
            this.discard();
        }
    }

    private void explode() {
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.NEUTRAL, 1.2F, 0.7F);
        this.level().explode(this, this.getX(), this.getY(), this.getZ(),
                EXPLOSION_POWER, Level.ExplosionInteraction.TNT);
    }

    /**
     * Разлёт дробинок от точки удара: случайные направления по полусфере
     * вокруг нормали {@code normal} (грань блока или «навстречу полёту»
     * при попадании в сущность), чтобы осколки не улетали сразу в
     * поверхность. Владелец дробинок — стрелок гранаты, так что убийства
     * засчитываются ему.
     */
    private void scatterShrapnel(Vec3 origin, Vec3 normal) {
        RandomSource random = this.random;
        LivingEntity shooter = this.getOwner() instanceof LivingEntity living ? living : null;

        for (int i = 0; i < SHRAPNEL_COUNT; i++) {
            // Случайный единичный вектор; зеркалим в полусферу нормали
            // и слегка «отжимаем» от поверхности.
            Vec3 dir = new Vec3(
                    random.nextGaussian(),
                    random.nextGaussian(),
                    random.nextGaussian());
            if (dir.lengthSqr() < 1.0E-4D) {
                dir = normal;
            }
            dir = dir.normalize();
            if (dir.dot(normal) < 0.0D) {
                dir = dir.subtract(normal.scale(2.0D * dir.dot(normal))); // отражение
            }
            dir = dir.add(normal.scale(0.35D)).normalize();

            AmethystShardEntity shard = shooter != null
                    ? new AmethystShardEntity(this.level(), shooter, this.getWeaponItem())
                    : new AmethystShardEntity(this.level(), origin.x, origin.y, origin.z);
            shard.setPos(origin.x, origin.y, origin.z);
            float speed = SHRAPNEL_SPEED_MIN + random.nextFloat() * SHRAPNEL_SPEED_VAR;
            shard.setDeltaMovement(dir.scale(speed));
            // Ориентация по вектору полёта, чтобы не мигала в первый тик.
            shard.setYRot((float) (Math.atan2(dir.x, dir.z) * (180.0D / Math.PI)));
            shard.setXRot((float) (Math.atan2(dir.y, dir.horizontalDistance()) * (180.0D / Math.PI)));
            shard.yRotO = shard.getYRot();
            shard.xRotO = shard.getXRot();
            this.level().addFreshEntity(shard);
        }
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.AMETHYST_SHARD);
    }
}
