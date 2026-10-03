package com.example.hell_yeah_stuff.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Серверный/общий конфиг пояса.Регистрируется как COMMON.
 */
public final class BeltConfig {
    public static final ModConfigSpec SPEC;
    public static final BeltConfig INSTANCE;

    public final ModConfigSpec.EnumValue<OnBeltRemoved> onBeltRemoved;
    public final ModConfigSpec.BooleanValue shouldDropOnDeath;
    public final ModConfigSpec.DoubleValue amethystShotDamageMultiplier;

    public enum OnBeltRemoved { RETURN, LOCK }

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("belt_slot");
        INSTANCE = new BeltConfig(b);
        b.pop();
        SPEC = b.build();
    }

    private BeltConfig(ModConfigSpec.Builder b) {
        onBeltRemoved = b.comment("Что делать с предметом в слоте оружия при снятии ремня: RETURN = вернуть в инвентарь/выбросить, LOCK = оставить скрытым до возврата ремня")
                .defineEnum("on_belt_removed", OnBeltRemoved.RETURN);
        shouldDropOnDeath = b.comment("Если false, предмет из слота пояса переносится в новое тело при смерти (клонировании), но только если ремень тоже сохранён")
                .define("should_drop_on_death", true);
        amethystShotDamageMultiplier = b.comment("Множитель урона каждой дробинки аметистовой дроби мульти-арбалета (1.0 – 5.0)")
                .defineInRange("amethyst_shot_damage_multiplier", 1.5, 1.0, 5.0);
        b.comment("Тег allowed/blocked и формат отображения настраиваются датапаком и клиентским конфигом").define("note", "see client config and tags");
    }

    private BeltConfig() { throw new AssertionError(); }
}
