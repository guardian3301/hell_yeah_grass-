package com.example.hell_yeah_stuff.item;

import net.minecraft.util.StringRepresentable;

/**
 * Вариант кортика.
 * Используется для хранения состояния предмета через Data Components.
 */
public enum DaggerVariant implements StringRepresentable {
    NORMAL("normal"),
    GILDED("gilded");

    private final String name;

    DaggerVariant(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    /**
     * Получает безопасный вариант по умолчанию.
     */
    public static DaggerVariant safeDefault() {
        return NORMAL;
    }

    /**
     * Безопасно получает вариант по имени.
     * Возвращает NORMAL для неизвестных значений.
     */
    public static DaggerVariant fromStringSafe(String name) {
        for (DaggerVariant variant : values()) {
            if (variant.name.equals(name)) {
                return variant;
            }
        }
        return NORMAL;
    }
}