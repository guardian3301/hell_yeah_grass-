package com.example.hell_yeah_stuff.item;

import com.example.hell_yeah_stuff.entity.AmethystShardEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Аметистовый дротик — дробовой боезапас мульти-арбалета.
 * При выстреле дробится на веер осколков (см. MultiCrossbowItem#shoot);
 * с зачарованием «Аметистовые гранаты» летит контактной гранатой.
 * Одиночный снаряд здесь — запасной путь для раздатчика и т. п.
 */
public class AmethystDartItem extends ArrowItem {

    public AmethystDartItem(Properties properties) {
        super(properties);
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack ammo, LivingEntity shooter, @Nullable ItemStack weapon) {
        return new AmethystShardEntity(level, shooter, weapon);
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        return new AmethystShardEntity(level, pos.x(), pos.y(), pos.z());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.hell_yeah_stuff.amethyst_dart")
                .withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
