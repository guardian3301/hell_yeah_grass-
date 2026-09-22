package com.example.hell_yeah_stuff.item;

import com.example.hell_yeah_stuff.entity.IronBoltEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** Железный болт — основной боеприпас рельсового арбалета (стак 64). */
public class IronBoltItem extends ArrowItem {

    public IronBoltItem(Properties properties) {
        super(properties);
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack ammo, LivingEntity shooter, @Nullable ItemStack weapon) {
        return new IronBoltEntity(level, shooter, ammo.copyWithCount(1), weapon);
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        IronBoltEntity bolt = new IronBoltEntity(level, pos.x(), pos.y(), pos.z(), stack.copyWithCount(1));
        bolt.pickup = AbstractArrow.Pickup.ALLOWED;
        return bolt;
    }
}
