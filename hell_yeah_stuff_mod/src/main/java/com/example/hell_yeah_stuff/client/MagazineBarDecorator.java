package com.example.hell_yeah_stuff.client;

import com.example.hell_yeah_stuff.item.MultiCrossbowItem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.client.IItemDecorator;

/**
 * Жёлтая плашка зарядов магазина (как у мешочка), разделённая на две секции.
 * Рисуется НАД ванильной шкалой прочности, поэтому обе видны одновременно.
 * Полная (2 секции) — заряжен + доступен мгновенный второй выстрел,
 * половина (1 секция) — остался авто-заряженный выстрел, пустая — скрыта.
 */
public final class MagazineBarDecorator implements IItemDecorator {

    private static final int YELLOW = 0xFFFFD830;
    private static final int BG = 0xFF000000;

    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return false;
        }
        CompoundTag tag = data.copyTag();
        if (!tag.getBoolean(MultiCrossbowItem.TAG_MAG)) {
            return false;
        }
        int charges = tag.getInt(MultiCrossbowItem.TAG_CHARGES);
        if (charges <= 0) {
            return false;
        }
        // Шкала прочности занимает y+13..y+15 — рисуем на 2px выше неё.
        int x = xOffset + 2;
        int y = yOffset + 10;
        // Поверх предмета (иначе полоска прячется за спрайтом).
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 200);
        // Чёрная подложка 13x2 — как у ванильной шкалы прочности.
        guiGraphics.fill(x, y, x + 13, y + 2, BG);
        // Одна жёлтая полоска, разделённая на две секции по 6px с разрывом 1px.
        if (charges >= 1) {
            guiGraphics.fill(x, y, x + 6, y + 1, YELLOW);
        }
        if (charges >= 2) {
            guiGraphics.fill(x + 7, y, x + 13, y + 1, YELLOW);
        }
        guiGraphics.pose().popPose();
        return true;
    }
}
