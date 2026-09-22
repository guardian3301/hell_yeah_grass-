package com.example.hell_yeah_stuff.entity;

import com.example.hell_yeah_stuff.registry.ModEnchantments;
import com.example.hell_yeah_stuff.registry.ModEntities;
import com.example.hell_yeah_stuff.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Обычный дротик — базовый боеприпас мульти-арбалета.
 *
 * Прочность: дротик — предмет с прочностью (4 выстрела), поэтому
 * при выстреле копия предмета внутри снаряда получает 1 единицу
 * износа; сломанный дротик не подбирается обратно.
 *
 * «Верность» (Loyalty) на мульти-арбалете: после попадания в блок
 * или моба дротик разворачивается и летит обратно к стрелку
 * (как трезубец). Работает только для дротиков — аметистовые
 * осколки и гранаты не возвращаются.
 */
public class DartEntity extends AbstractArrow {

    private static final double DAMAGE = 4.0D;

    private static final String TAG_LOYALTY = "HysLoyalty";
    private static final String TAG_RETURNING = "HysReturning";

    /** Уровень «Верности» оружия, из которого выстрелили (0 — нет). */
    private int loyalty;
    /** Летит обратно к владельцу. */
    private boolean returning;
    public DartEntity(EntityType<? extends DartEntity> type, Level level) {
        super(type, level);
    }

    public DartEntity(Level level, LivingEntity shooter, ItemStack pickupStack, @Nullable ItemStack firedFromWeapon) {
        super(ModEntities.DART.get(), shooter, level, pickupStack, firedFromWeapon);
        this.setBaseDamage(DAMAGE);
        this.initFromWeapon(firedFromWeapon);
    }

    public DartEntity(Level level, double x, double y, double z, ItemStack pickupStack) {
        super(ModEntities.DART.get(), x, y, z, level, pickupStack, null);
        this.setBaseDamage(DAMAGE);
        this.initFromWeapon(null);
    }

    /** Считывает «Верность» с оружия (на мульти-арбалет чара не ставится). */
    protected void initFromWeapon(@Nullable ItemStack firedFromWeapon) {
        this.loyalty = firedFromWeapon != null
                ? ModEnchantments.level(firedFromWeapon, Enchantments.LOYALTY)
                : 0;
    }

    /** Можно ли возвращать дротик владельцу. */
    protected boolean canReturn() {
        if (this.level().isClientSide || this.loyalty <= 0) {
            return false;
        }
        Entity owner = this.getOwner();
        if (owner == null || !owner.isAlive() || owner.level() != this.level()) {
            return false;
        }
        return !(owner instanceof ServerPlayer player) || !player.isSpectator();
    }

    /** Переводит дротик в режим полёта к владельцу. */
    protected void startReturn() {
        if (!this.returning) {
            this.returning = true;
            this.playSound(SoundEvents.TRIDENT_RETURN, 0.7F, 1.7F);
        }
        this.inGround = false;
        this.setNoPhysics(true);
        this.pickup = Pickup.ALLOWED;
    }

    /** Один тик полёта к владельцу (логика трезубца с «Верностью»). */
    protected void tickReturn(Entity owner) {
        Vec3 toOwner = owner.getEyePosition().subtract(this.position());
        this.setPosRaw(this.getX(), this.getY() + toOwner.y * 0.015D * this.loyalty, this.getZ());
        double speed = 0.08D + 0.05D * this.loyalty;
        this.setDeltaMovement(this.getDeltaMovement().scale(0.95D)
                .add(toOwner.normalize().scale(speed)));
    }

    @Override
    public void tick() {
        if ((this.returning || this.inGround) && this.canReturn()) {
            this.startReturn();
            this.tickReturn(this.getOwner());
        } else if (this.returning && !this.level().isClientSide) {
            // Владелец пропал — дротик просто падает как обычная стрела.
            this.returning = false;
            this.setNoPhysics(false);
        }
        super.tick();
    }

    /**
     * Попадание по сущности.
     *
     * <p>Entity#discard() в 1.21.1 объявлен final, перекрыть его нельзя.
     * Поэтому дротик с «Верностью» на момент удара считается
     * пронзающим ({@link #getPierceLevel()}): в этой ветке AbstractArrow
     * не уничтожает снаряд, и тот живым уходит в полёт к владельцу.
     * Повторные попадания отсекает {@link #findHitEntity}.
     */
    @Override
    protected void onHitEntity(EntityHitResult result) {
        boolean back = this.canReturn();
        super.onHitEntity(result);
        if (back && !this.isRemoved()) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.1D));
            this.startReturn();
        }
    }

    /**
     * Серверная хитрость: пока дротик с «Верностью» летит вперёд,
     * он считается пронзающим — именно это не даёт ванили удалить
     * снаряд сразу после удара. На клиенте и без чары — ванильное 0.
     */
    @Override
    public byte getPierceLevel() {
        byte base = super.getPierceLevel();
        if (base <= 0 && this.loyalty > 0 && !this.returning
                && !this.level().isClientSide) {
            return (byte) 1;
        }
        return base;
    }

    /** На обратном пути дротик никого не бьёт. */
    @Nullable
    @Override
    protected EntityHitResult findHitEntity(Vec3 start, Vec3 end) {
        return this.returning ? null : super.findHitEntity(start, end);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(TAG_LOYALTY, this.loyalty);
        tag.putBoolean(TAG_RETURNING, this.returning);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.loyalty = tag.getInt(TAG_LOYALTY);
        this.returning = tag.getBoolean(TAG_RETURNING);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItems.DART.get());
    }
}
