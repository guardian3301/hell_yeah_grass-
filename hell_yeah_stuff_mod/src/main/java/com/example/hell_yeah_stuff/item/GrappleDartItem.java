package com.example.hell_yeah_stuff.item;

import com.example.hell_yeah_stuff.entity.GrappleDartEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Цепкий дротик — боеприпас для мульти-арбалета.
 * Одноразовый; один крюк за раз — новый выстрел снимает предыдущий.
 */
public class GrappleDartItem extends ArrowItem {

    public GrappleDartItem(Properties properties) {
        super(properties);
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack ammo, LivingEntity shooter, @Nullable ItemStack weapon) {
        // «Один крюк за раз»: старый дротик исчезает при новом выстреле.
        // Заякоренный берём из реестра (он может висеть в Sable-плоте за
        // тысячи блоков), летящие рядом добираем локальным поиском.
        if (shooter instanceof net.minecraft.world.entity.player.Player player) {
            GrappleDartEntity active = GrappleDartEntity.findActive(level, player);
            if (active != null) {
                active.discard();
            }
        }
        for (GrappleDartEntity old : level.getEntitiesOfClass(GrappleDartEntity.class,
                shooter.getBoundingBox().inflate(128.0D), dart -> dart.getOwner() == shooter)) {
            old.discard();
        }
        return new GrappleDartEntity(level, shooter, ammo.copyWithCount(1), weapon);
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        GrappleDartEntity dart = new GrappleDartEntity(level, pos.x(), pos.y(), pos.z(), stack.copyWithCount(1));
        dart.pickup = AbstractArrow.Pickup.DISALLOWED;
        return dart;
    }
}
