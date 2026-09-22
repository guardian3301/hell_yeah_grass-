package com.example.hell_yeah_stuff.entity;

import com.example.hell_yeah_stuff.registry.ModBlocks;
import com.example.hell_yeah_stuff.registry.ModEntities;
import com.example.hell_yeah_stuff.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Аметистовый болт — синтезируется зачарованием «Аметистовый конденсатор».
 *
 * <p>Урон и свист — как у железного болта. При попадании в сущность под её
 * ногами вырастает временный блок «Острая аметистовая пыль» (аналог
 * порошкового снега). Подобрать болт нельзя — он не существует как крафт.
 */
public class AmethystBoltEntity extends IronBoltEntity {

    public AmethystBoltEntity(EntityType<? extends AmethystBoltEntity> type, Level level) {
        super(type, level);
    }

    public AmethystBoltEntity(Level level, LivingEntity shooter, ItemStack pickupStack,
                              @Nullable ItemStack firedFromWeapon) {
        super(ModEntities.AMETHYST_BOLT.get(), level, shooter, pickupStack, firedFromWeapon);
        this.pickup = Pickup.DISALLOWED;
    }

    public AmethystBoltEntity(Level level, double x, double y, double z, ItemStack pickupStack) {
        super(ModEntities.AMETHYST_BOLT.get(), level, x, y, z, pickupStack);
        this.pickup = Pickup.DISALLOWED;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItems.AMETHYST_BOLT.get());
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide || !(result.getEntity() instanceof LivingEntity target)) {
            return;
        }
        spawnDust(this.level(), target);
    }

    /**
     * Ставит временный блок под ноги цели. Если там нет места — пробует
     * клетку выше; если и там занято — вешает Замедление II напрямую.
     */
    private static void spawnDust(Level level, LivingEntity target) {
        BlockPos at = target.blockPosition();
        if (tryPlace(level, at) || tryPlace(level, at.above())) {
            return;
        }
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, false, true, true));
    }

    private static boolean tryPlace(Level level, BlockPos pos) {
        BlockState current = level.getBlockState(pos);
        if (!current.isAir() && !current.canBeReplaced()) {
            return false;
        }
        if (current.is(Blocks.WATER) || current.is(Blocks.LAVA)) {
            return false;
        }
        level.setBlockAndUpdate(pos, ModBlocks.SHARP_AMETHYST_DUST.get().defaultBlockState());
        return true;
    }
}
