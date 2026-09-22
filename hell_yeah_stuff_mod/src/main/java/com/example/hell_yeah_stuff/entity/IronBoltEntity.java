package com.example.hell_yeah_stuff.entity;

import com.example.hell_yeah_stuff.registry.ModEntities;
import com.example.hell_yeah_stuff.registry.ModItems;
import com.example.hell_yeah_stuff.registry.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Железный болт — базовый боеприпас рельсового арбалета.
 *
 * <p>Урон 3 (обычная стрела — 2). После 20 ПРОЙДЕННЫХ блоков пути
 * один раз играет свист в текущей позиции снаряда. Длина пути копится
 * потиково (сумма шагов), а НЕ берётся как расстояние по прямой от точки
 * выстрела — иначе на кривой траектории и рикошетах счёт был бы неверным.
 */
public class IronBoltEntity extends AbstractArrow {

    /** Базовый урон болта. */
    public static final double DAMAGE = 3.0D;
    /** После скольких пройденных блоков срабатывает свист. */
    public static final double WHISTLE_DISTANCE = 20.0D;

    private static final String TAG_TRAVELLED = "HysTravelled";
    private static final String TAG_WHISTLED = "HysWhistled";

    /** Накопленная длина пути в блоках. */
    private double travelled;
    /** Свист уже отыгран (единоразово). */
    private boolean whistled;

    public IronBoltEntity(EntityType<? extends IronBoltEntity> type, Level level) {
        super(type, level);
    }

    protected IronBoltEntity(EntityType<? extends IronBoltEntity> type, Level level, LivingEntity shooter,
                             ItemStack pickupStack, @Nullable ItemStack firedFromWeapon) {
        super(type, shooter, level, pickupStack, firedFromWeapon);
        this.setBaseDamage(DAMAGE);
    }

    protected IronBoltEntity(EntityType<? extends IronBoltEntity> type, Level level,
                             double x, double y, double z, ItemStack pickupStack) {
        super(type, x, y, z, level, pickupStack, null);
        this.setBaseDamage(DAMAGE);
    }

    public IronBoltEntity(Level level, LivingEntity shooter, ItemStack pickupStack,
                          @Nullable ItemStack firedFromWeapon) {
        this(ModEntities.IRON_BOLT.get(), level, shooter, pickupStack, firedFromWeapon);
    }

    public IronBoltEntity(Level level, double x, double y, double z, ItemStack pickupStack) {
        this(ModEntities.IRON_BOLT.get(), level, x, y, z, pickupStack);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItems.IRON_BOLT.get());
    }

    @Override
    public void tick() {
        Vec3 before = this.position();
        super.tick();
        if (this.whistled || this.inGround) {
            return;
        }
        // Суммируем длину шага за тик: sqrt(dx^2 + dy^2 + dz^2).
        this.travelled += this.position().distanceTo(before);
        if (this.travelled < WHISTLE_DISTANCE) {
            return;
        }
        this.whistled = true;
        if (!this.level().isClientSide) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    ModSounds.BOLT_WHISTLE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble(TAG_TRAVELLED, this.travelled);
        tag.putBoolean(TAG_WHISTLED, this.whistled);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.travelled = tag.getDouble(TAG_TRAVELLED);
        this.whistled = tag.getBoolean(TAG_WHISTLED);
    }
}
