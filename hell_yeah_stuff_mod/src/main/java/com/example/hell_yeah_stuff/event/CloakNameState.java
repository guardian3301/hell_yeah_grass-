package com.example.hell_yeah_stuff.event;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Общее (сервер+клиент) состояние скрытия ников игроков со скрытной накидкой.
 *
 * <p>На сервере этот набор — источник истины: сервер следит за экипировкой
 * накидки и рассылает {@code CloakNameHidePayload} всем клиентам при изменении.
 * На клиенте набор заполняется из полученных пакетов и используется при
 * отрисовке имён через {@code RenderNameTagEvent}.
 *
 * <p>Использование общих статических данных безопасно, потому что логика
 * «кто скрыт» определяется сервером, а клиент только отражает её.
 */
public final class CloakNameState {

    /** UUID игроков, чьи ники должны быть скрыты. */
    private static final Set<UUID> HIDDEN_NAMES = Collections.synchronizedSet(new HashSet<>());

    private CloakNameState() {}

    public static boolean isNameHidden(UUID uuid) {
        return HIDDEN_NAMES.contains(uuid);
    }

    public static void hideName(UUID uuid) {
        HIDDEN_NAMES.add(uuid);
    }

    public static void showName(UUID uuid) {
        HIDDEN_NAMES.remove(uuid);
    }

    public static void setHidden(UUID uuid, boolean hidden) {
        if (hidden) {
            hideName(uuid);
        } else {
            showName(uuid);
        }
    }

    /** Очищает состояние (для переподключения клиента). */
    public static void clear() {
        HIDDEN_NAMES.clear();
    }
}
