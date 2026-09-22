package com.example.hell_yeah_stuff.item;

import com.example.hell_yeah_stuff.compat.CuriosCompat;
import com.example.hell_yeah_stuff.entity.AmethystGrenadeEntity;
import com.example.hell_yeah_stuff.entity.AmethystShardEntity;
import com.example.hell_yeah_stuff.entity.GrappleDartEntity;
import com.example.hell_yeah_stuff.registry.ModEnchantments;
import com.example.hell_yeah_stuff.registry.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

/**
 * A multi crossbow that ONLY accepts the mod's darts and the grapple dart.
 * Charged ammo is stored via the modern Data Components API
 * ({@link DataComponents#CHARGED_PROJECTILES}) instead of legacy NBT.
 *
 * Если у стрелка есть активный (воткнутый) цепкий болт, нажатие ПКМ
 * НЕ заряжает/стреляет, а ОТЦЕПЛЯЕТ крюк с РЫВКОМ в направлении
 * взгляда поверх набранной скорости (release-dash как в DL2).
 * Пока крюк воткнут, игрок привязан тросом автоматически
 * (маятниковая физика а-ля Dying Light 2 — см. GrappleDartEntity#tick).
 */
public class MultiCrossbowItem extends CrossbowItem {

    /** Дротики, цепкий дротик и осколок аметиста. Стрелы и фейерверки отклоняются. */
    public static final Predicate<ItemStack> SUPPORTED_AMMO =
            stack -> stack.is(ModItems.DART.get())
                    || stack.is(ModItems.EXPLOSIVE_DART.get())
                    || stack.is(ModItems.GRAPPLE_DART.get())
                    || stack.is(Items.AMETHYST_SHARD);

    /** Осколков в дробовом веере аметистового заряда. */
    private static final int AMETHYST_SHARD_COUNT = 6;
    /** Разброс веера (ванильная неточность выстрела арбалета = 1.0). */
    private static final float AMETHYST_SPREAD = 10.0F;

    public MultiCrossbowItem(Properties properties) {
        super(properties);
    }

    @Override
    public Predicate<ItemStack> getSupportedHeldProjectiles() {
        return SUPPORTED_AMMO;
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return SUPPORTED_AMMO;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        // Активный крюк? Тогда ПКМ отцепляет с рывком — без зарядки/выстрела.
        // findActive работает на обеих сторонах (состояние синхронизировано),
        // поэтому клиент не начнёт ложную анимацию зарядки.
        GrappleDartEntity dart = GrappleDartEntity.findActive(level, player);
        if (dart != null) {
            if (level.isClientSide) {
                // Рывок — на клиенте: добавляется к РЕАЛЬНОЙ текущей скорости
                // качания (движение игрока клиент-авторитативно).
                dart.applyDetachDash(player);
            } else {
                // Сервер только убирает дротик и играет звук — скорость не трогает.
                dart.detach();
            }
            return InteractionResultHolder.consume(player.getItemInHand(hand));
        }

        // Блочный магазин в поясе (Curios): зарядка -> выстрел -> авто-дозарядка
        // (мгновенно, из инвентаря) -> выстрел -> обычная ручная перезарядка.
        // Выполняется на ОБЕИХ сторонах, чтобы клиент сразу знал, что арбалет
        // заряжен, и второй ПКМ стрелял мгновенно, а не начинал натяжку.
        ItemStack crossbow = player.getItemInHand(hand);
        boolean wasCharged = isCharged(crossbow);
        CompoundTag preTag = tag(crossbow);
        boolean wasAutoLoaded = preTag.getBoolean(TAG_AUTO);
        // Наличие магазина берём из СИНХРОНИЗИРУЕМОГО флага TAG_MAG (его пишет
        // сервер в inventoryTick), а НЕ из прямого запроса Curios: на клиенте
        // Curios-инвентарь синхронизируется с задержкой, из-за чего клиент
        // иногда "не видел" магазин и вместо дозарядки начинал новую натяжку —
        // заряженный арбалет как будто требовал перезарядки.
        boolean hasMag = preTag.getBoolean(TAG_MAG);
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (wasCharged && !isCharged(crossbow)) {
            if (wasAutoLoaded) {
                // Второй (авто-заряженный) выстрел — дальше только ручная зарядка.
                setMagState(crossbow, hasMag, 0, false);
            } else if (hasMag) {
                ItemStack ammo = player.getProjectile(crossbow);
                if (!ammo.isEmpty()) {
                    ItemStack single = ammo.copyWithCount(1);
                    if (!level.isClientSide && !player.hasInfiniteMaterials()) {
                        ammo.shrink(1);
                    }
                    crossbow.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(single));
                    setMagState(crossbow, true, 1, true);
                    if (!level.isClientSide) {
                        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.CROSSBOW_LOADING_END.value(), SoundSource.PLAYERS, 1.0F, 1.2F);
                    }
                }
            }
        }
        return result;
    }

    // ---------------------------------------------------------------
    // Аметистовый боезапас: обычный осколок аметиста = дробовой веер;
    // с зачарованием «Аметистовые гранаты» тот же осколок летит
    // контактной гранатой. Остальные дротики стреляют штатным путём
    // CrossbowItem.
    // ---------------------------------------------------------------

    @Override
    protected void shoot(ServerLevel level, LivingEntity shooter, InteractionHand hand, ItemStack weapon,
                         List<ItemStack> projectileItems, float velocity, float inaccuracy, boolean isCrit,
                         @Nullable LivingEntity target) {
        boolean amethyst = false;
        for (ItemStack projectile : projectileItems) {
            if (projectile.is(Items.AMETHYST_SHARD)) {
                amethyst = true;
                break;
            }
        }
        if (!amethyst) {
            // Передаём дротику руку выстрела: верёвка цепкого дротика
            // будет исходить из ПРОТИВОПОЛОЖНОЙ руки (см. GrappleDartEntity).
            GrappleDartEntity.beginShot(hand);
            try {
                super.shoot(level, shooter, hand, weapon, projectileItems, velocity, inaccuracy, isCrit, target);
            } finally {
                GrappleDartEntity.endShot();
            }
            return;
        }

        boolean grenades = ModEnchantments.level(weapon, ModEnchantments.AMETHYST_GRENADES) > 0;
        // Угловые смещения между снарядами при Multishot — как в ванильном shoot().
        float spreadStep = projectileItems.size() == 1 ? 0.0F
                : 20.0F / (float) (projectileItems.size() - 1);
        float base = (float) ((projectileItems.size() - 1) % 2) * spreadStep / 2.0F;
        float sign = 1.0F;
        for (int i = 0; i < projectileItems.size(); i++) {
            ItemStack ammo = projectileItems.get(i);
            if (ammo.isEmpty()) {
                continue;
            }
            float yawOffset = base + sign * (float) ((i + 1) / 2) * spreadStep;
            sign = -sign;
            if (grenades) {
                AmethystGrenadeEntity grenade = new AmethystGrenadeEntity(level, shooter, weapon);
                grenade.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot() + yawOffset,
                        0.0F, velocity, 1.0F);
                level.addFreshEntity(grenade);
            } else {
                for (int shard = 0; shard < AMETHYST_SHARD_COUNT; shard++) {
                    AmethystShardEntity entity = new AmethystShardEntity(level, shooter, weapon);
                    float shardVelocity = velocity * (0.8F + shooter.getRandom().nextFloat() * 0.3F);
                    entity.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot() + yawOffset,
                            0.0F, shardVelocity, AMETHYST_SPREAD);
                    level.addFreshEntity(entity);
                }
            }
            weapon.hurtAndBreak(this.getDurabilityUse(ammo), shooter, LivingEntity.getSlotForHand(hand));
            if (weapon.isEmpty()) {
                break;
            }
        }
        level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                SoundEvents.CROSSBOW_SHOOT, shooter.getSoundSource(), 1.0F,
                grenades ? 0.8F : 1.1F);
        level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, shooter.getSoundSource(), 1.2F,
                0.9F + shooter.getRandom().nextFloat() * 0.2F);
    }

    // ---------------------------------------------------------------
    // Жёлтая плашка (рисуется НАД шкалой прочности — см. MagazineBarDecorator):
    // полная = заряжен + доступен авто-второй выстрел, половина = остался
    // авто-заряженный выстрел, пустая = нужно заряжать вручную.
    // ---------------------------------------------------------------

    public static final String TAG_MAG = "hys_mag";
    public static final String TAG_CHARGES = "hys_charges";
    public static final String TAG_AUTO = "hys_auto";

    private static boolean magReady(Player player) {
        return CuriosCompat.hasCurio(player, ModItems.BLOCK_MAGAZINE.get());
    }

    private static void setMagState(ItemStack stack, boolean mag, int charges, boolean auto) {
        CompoundTag t = tag(stack);
        t.putBoolean(TAG_MAG, mag);
        t.putInt(TAG_CHARGES, charges);
        t.putBoolean(TAG_AUTO, auto);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide || !(entity instanceof Player player) || level.getGameTime() % 5 != 0) {
            return;
        }
        boolean mag = CuriosCompat.hasCurio(player, ModItems.BLOCK_MAGAZINE.get());
        CompoundTag t = tag(stack);
        boolean auto = t.getBoolean(TAG_AUTO);
        int charges;
        if (!isCharged(stack)) {
            charges = 0;
            auto = false;
        } else if (auto) {
            charges = 1; // авто-заряженный второй выстрел
        } else {
            // Заряжен вручную: полный, если магазин готов и есть дротик про запас.
            charges = magReady(player) && !player.getProjectile(stack).isEmpty() ? 2 : 1;
        }
        if (t.getBoolean(TAG_MAG) != mag || t.getInt(TAG_CHARGES) != charges || t.getBoolean(TAG_AUTO) != auto) {
            setMagState(stack, mag, charges, auto);
        }
    }

    private static CompoundTag tag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null ? data.copyTag() : new CompoundTag();
    }
}
