package com.example.hell_yeah_stuff.item;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Базовый класс для всех кортиков.
 *
 * <p>Позволяет блокировать удары при удержании ПКМ (по правилам щита).
 * Удар в спину обрабатывается сервером в {@code BackstabHandler}.
 * Зачарование «Косание Мидаса» ({@link com.example.hell_yeah_stuff.registry.ModEnchantments#MIDAS_TOUCH})
 * даёт шанс золотого крита при ударе в спину.
 */
public class DaggerItem extends Item {

    private final Tier tier;
    private final float baseAttackDamage;
    private final int maxDurability;
    private final float backstabMultiplier;

    private static final ResourceLocation ATTACK_DAMAGE_ID =
            ResourceLocation.fromNamespaceAndPath("hell_yeah_stuff", "dagger_damage");
    private static final ResourceLocation ATTACK_SPEED_ID =
            ResourceLocation.fromNamespaceAndPath("hell_yeah_stuff", "dagger_speed");

    /**
     * Создаёт кортик с указанными параметрами.
     * Атрибуты атаки задаются через {@link ItemAttributeModifiers} в конструкторе Properties,
     * как у ванильного SwordItem в MC 1.21.1.
     */
    public DaggerItem(Tier tier, float baseAttackDamage, float attackSpeed,
                      int maxDurability, float backstabMultiplier, Properties properties) {
        super(properties
                .durability(maxDurability)
                .attributes(createAttributes(baseAttackDamage, attackSpeed)));
        this.tier = tier;
        this.baseAttackDamage = baseAttackDamage;
        this.maxDurability = maxDurability;
        this.backstabMultiplier = backstabMultiplier;
    }

    // ==================== АТРИБУТЫ ====================

    /**
     * Создаёт атрибуты для кортика (1.21.1 паттерн: через builder + EquipmentSlotGroup).
     */
    private static ItemAttributeModifiers createAttributes(float damage, float attackSpeed) {
        return ItemAttributeModifiers.builder()
                .add(
                        Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(ATTACK_DAMAGE_ID, damage,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(
                        Attributes.ATTACK_SPEED,
                        new AttributeModifier(ATTACK_SPEED_ID, attackSpeed,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();
    }

    // ==================== БЛОКИРОВАНИЕ (ПКМ) ====================

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        // Стандартное завершение использования.
    }

    // ==================== ПРОЧНОСТЬ ====================

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        if (state.getDestroySpeed(level, pos) != 0.0F) {
            stack.hurtAndBreak(2, miner, EquipmentSlot.MAINHAND);
        }
        return true;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return tier.getRepairIngredient().test(repair) || super.isValidRepairItem(toRepair, repair);
    }

    // ==================== УДАР В СПИНУ ====================

    public float getBackstabMultiplier() {
        return this.backstabMultiplier;
    }
}