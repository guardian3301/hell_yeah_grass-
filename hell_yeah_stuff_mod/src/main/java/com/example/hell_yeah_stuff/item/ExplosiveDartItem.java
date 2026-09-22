package com.example.hell_yeah_stuff.item;

import com.example.hell_yeah_stuff.entity.ExplosiveDartEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** Взрывной дротик — детонирует через 3 секунды после попадания. */
public class ExplosiveDartItem extends ArrowItem {

    public ExplosiveDartItem(Properties properties) {
        super(properties);
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack ammo, LivingEntity shooter, @Nullable ItemStack weapon) {
        return new ExplosiveDartEntity(level, shooter, ammo.copyWithCount(1), weapon);
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        ExplosiveDartEntity dart = new ExplosiveDartEntity(level, pos.x(), pos.y(), pos.z(), stack.copyWithCount(1));
        dart.pickup = AbstractArrow.Pickup.ALLOWED;
        return dart;
    }
}
