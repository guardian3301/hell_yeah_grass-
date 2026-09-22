# Миграция: minicrossbow → hell_yeah_stuff + цепкий болт

## Что в архиве

Полное дерево `src/main/java` + `src/main/resources` с новым пакетом
`com.example.hell_yeah_stuff` и MODID `hell_yeah_stuff`.

Новое:
- `item/GrappleBoltItem.java` — предмет «Цепкий болт» (боеприпас мини-арбалета)
- `entity/GrappleBoltEntity.java` — логика крюка (якорь → подтягивание → отцепление)
- `client/GrappleBoltRenderer.java`
- Предикат модели `hell_yeah_stuff:grapple` + модель `mini_crossbow_grapple_bolt`
- Рецепт: дротик + крючок-ловушка + нить → 1 цепкий болт

## Механика болта (как договорились)

1. Заряжается в мини-арбалет как обычный дротик.
2. Выстрел → болт втыкается в блок (якорь). Попадание по мобу = обычный
   малый урон стрелы, без зацепа.
3. Нажатие ПКМ с арбалетом в руке (НЕ удержание) — подтягивание к якорю,
   без урона от падения во время полёта.
4. Повторное нажатие ПКМ или прибытие — отцепление, болт уничтожается
   (одноразовый, pickup = DISALLOWED).
5. Один крюк за раз: новый выстрел удаляет старый болт.
6. Трос рвётся на 48 блоках; если блок под болтом сломали — крюк слетает.
   Как у любой стрелы, воткнутый болт деспаунится через 60 секунд.

Настройки в `GrappleBoltEntity`: `PULL_SPEED` (0.85), `ARRIVE_DISTANCE` (1.6),
`MAX_ROPE_LENGTH` (48).

## Что нужно сделать вручную

1. **DartEntity.java и ExplosiveDartEntity.java** не были приложены — в своих
   файлах поменяйте только первую строку пакета и импорты:
   `com.example.minicrossbow.*` → `com.example.hell_yeah_stuff.*`
   (в IDE: Refactor → Rename Package на корневом пакете сделает всё сразу).
2. **ModItems / ModEntities / ModCreativeTabs** восстановлены по структуре jar.
   Если ваши оригиналы отличаются (durability, размеры сущностей, вкладки) —
   перенесите из моих файлов только блоки `>>> NEW`.
3. **Текстуры**: перенесите папку `assets/minicrossbow/textures` в
   `assets/hell_yeah_stuff/textures` без изменений. Добавьте новые:
   - `textures/item/grapple_bolt.png`
   - `textures/item/mini_crossbow_grapple_bolt.png`
   - `textures/entity/projectiles/grapple_bolt.png`
   Пока их нет — будет чёрно-фиолетовая заглушка (не краш),
   можно временно указать текстуры дротика в json/рендере.
4. **Удалите старые папки** `assets/minicrossbow` и `data/minicrossbow`
   и старый пакет `com/example/minicrossbow`.
5. **gradle.properties / build.gradle**: если там задан `mod_id=minicrossbow` или
   `group=com.example.minicrossbow` — обновите на `hell_yeah_stuff`.
6. Старые миры: предметы/сущности со старым id `minicrossbow:*` исчезнут
   при загрузке мира (смена mod id ломает совместимость сохранений).
