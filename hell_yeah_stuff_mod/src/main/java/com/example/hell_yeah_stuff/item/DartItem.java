package com.example.hell_yeah_stuff.item;

import com.example.hell_yeah_stuff.entity.DartEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** Дротик — обычный боеприпас для мульти-арбалета. */
public class DartItem extends ArrowItem {

    public DartItem(Properties properties) {
        super(properties);
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack ammo, LivingEntity shooter, @Nullable ItemStack weapon) {
        return new DartEntity(level, shooter, ammo.copyWithCount(1), weapon);
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        DartEntity dart = new DartEntity(level, pos.x(), pos.y(), pos.z(), stack.copyWithCount(1));
        dart.pickup = AbstractArrow.Pickup.ALLOWED;
        return dart;
    }
}
