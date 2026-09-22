package com.example.hell_yeah_stuff.compat;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Мягкая совместимость с Sable (библиотека «саб-левелов» — движущихся
 * структур, бэкенд Create Aeronautics) через рефлексию: мод собирается
 * и работает без Sable в classpath.
 *
 * Как устроен Sable: блоки саб-левела физически лежат в удалённом
 * «плоте» того же измерения (за миллионы блоков от игрока), а в мир
 * структура проецируется трансформом {@code SubLevel.logicalPose()}.
 * Если цепкий дротик воткнулся в блок плота (движущейся структуры),
 * его координаты — плотовые. Эти хелперы проецируют такие координаты
 * в мировые (и обратно).
 *
 * КРИТИЧНО: если точка лежит в границах плота, а поза саб-левела
 * недоступна (структура ещё не прогрузилась на клиенте после зацепа,
 * только что удалена, или рефлексия не разрешилась), «сырые» плотовые
 * координаты возвращать НЕЛЬЗЯ — физика троса утянет игрока на
 * ~20 млн блоков, лавинная прогрузка чанков обвалит игру. Для физики
 * используйте {@link #projectToWorldOrNull} и пропускайте тик при null.
 */
public final class SableCompat {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Кандидаты класса-входа: разные версии Sable перекладывали API. */
    private static final String[] CONTAINER_CLASSES = {
            "dev.ryanhcode.sable.api.sublevel.SubLevelContainer",
            "dev.ryanhcode.sable.sublevel.SubLevelContainer",
            "dev.ryanhcode.sable.api.SubLevelContainer",
    };

    private static boolean checked;
    private static Method getContainer; // static SubLevelContainer.getContainer(Level)

    /** Кэш инстанс-методов по ФАКТИЧЕСКОМУ классу объекта: устойчивее к
     *  смене иерархии/имён реализаций между версиями Sable, чем жёсткая
     *  привязка к задекларированным именам классов. */
    private static final Map<String, Method> METHODS = new ConcurrentHashMap<>();

    private SableCompat() {}

    public static boolean isLoaded() {
        return resolve();
    }

    /** Лежит ли точка в границах плотов саб-левелов (false без Sable). */
    public static boolean isInPlotBounds(Level level, BlockPos pos) {
        Object container = container(level);
        if (container == null) {
            return false;
        }
        Object in = call(container, "inBounds", new Class<?>[]{BlockPos.class}, pos);
        return in instanceof Boolean b && b;
    }

    /**
     * Мировая позиция точки {@code pos}: если она в плоте саб-левела —
     * проекция через logicalPose, иначе — сама точка.
     *
     * ВНИМАНИЕ: при недоступной позе вернётся исходная (плотовая) точка
     * за миллионы блоков. Годится только там, где большая дистанция
     * безопасна (звук, дистанция для штатного обрыва). Для физики и
     * рендера — {@link #projectToWorldOrNull}.
     */
    public static Vec3 projectToWorld(Level level, BlockPos plotAnchor, Vec3 pos) {
        Vec3 world = projectToWorldOrNull(level, plotAnchor, pos);
        return world != null ? world : pos;
    }

    /**
     * Как {@link #projectToWorld}, но {@code null} вместо «сырых» плотовых
     * координат, когда точка лежит в границах плота, а поза саб-левела
     * недоступна. Вызывающий обязан пропустить тик физики / кадр рендера
     * или отцепиться — но не использовать плотовые координаты как мировые.
     */
    @Nullable
    public static Vec3 projectToWorldOrNull(Level level, BlockPos plotAnchor, Vec3 pos) {
        if (!resolve()) {
            return pos; // Sable нет — плотов не существует, точка мировая
        }
        if (!isInPlotBounds(level, plotAnchor)) {
            return pos; // обычный мир — identity
        }
        Object pose = poseAt(level, plotAnchor);
        if (pose == null) {
            return null; // в плоте, но поза недоступна — координаты опасны
        }
        Object world = call(pose, "transformPosition", new Class<?>[]{Vec3.class}, pos);
        return world instanceof Vec3 v ? v : null;
    }

    /**
     * Обратная проекция: мировая точка -> система координат плота,
     * в котором сидит якорь {@code plotAnchor}. Identity, если якорь
     * не в саб-левеле или поза недоступна.
     */
    public static Vec3 projectFromWorld(Level level, BlockPos plotAnchor, Vec3 worldPos) {
        if (!resolve() || !isInPlotBounds(level, plotAnchor)) {
            return worldPos;
        }
        Object pose = poseAt(level, plotAnchor);
        if (pose == null) {
            return worldPos;
        }
        Object plot = call(pose, "transformPositionInverse", new Class<?>[]{Vec3.class}, worldPos);
        return plot instanceof Vec3 v ? v : worldPos;
    }

    // ------------------------------------------------------------------
    // Внутренности
    // ------------------------------------------------------------------

    @Nullable
    private static Object container(Level level) {
        if (!resolve()) {
            return null;
        }
        try {
            return getContainer.invoke(null, level);
        } catch (Throwable t) {
            return null;
        }
    }

    /** Поза саб-левела, в чьём плоте лежит точка, или null. */
    @Nullable
    private static Object poseAt(Level level, BlockPos pos) {
        Object container = container(level);
        if (container == null) {
            return null;
        }
        Object plot = call(container, "getPlot", new Class<?>[]{ChunkPos.class}, new ChunkPos(pos));
        if (plot == null) {
            return null;
        }
        Object subLevel = call(plot, "getSubLevel", new Class<?>[0]);
        if (subLevel == null) {
            return null;
        }
        Object removed = call(subLevel, "isRemoved", new Class<?>[0]);
        if (removed instanceof Boolean b && b) {
            return null;
        }
        return call(subLevel, "logicalPose", new Class<?>[0]);
    }

    /** Вызов инстанс-метода по имени, метод ищется на фактическом классе
     *  цели (с кэшем). Любой сбой -> null, без исключений наружу. */
    @Nullable
    private static Object call(Object target, String name, Class<?>[] types, Object... args) {
        try {
            String key = target.getClass().getName() + "#" + name;
            Method m = METHODS.get(key);
            if (m == null) {
                m = target.getClass().getMethod(name, types);
                m.trySetAccessible();
                METHODS.put(key, m);
            }
            return m.invoke(target, args);
        } catch (Throwable t) {
            return null;
        }
    }

    private static boolean resolve() {
        if (!checked) {
            checked = true;
            for (String className : CONTAINER_CLASSES) {
                try {
                    Class<?> cls = Class.forName(className);
                    getContainer = cls.getMethod("getContainer", Level.class);
                    LOGGER.info("[hell_yeah_stuff] Sable compat: включена через {}", className);
                    break;
                } catch (Throwable t) {
                    // пробуем следующего кандидата
                }
            }
            if (getContainer == null) {
                LOGGER.info("[hell_yeah_stuff] Sable compat: классы Sable не найдены, проекция плотов отключена");
            }
        }
        return getContainer != null;
    }
}
