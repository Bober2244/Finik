# Finik — виртуальный питомец и финансы (8–11 лет)

Многомодульное Android-приложение на Jetpack Compose по макету Claude Design
«Финик 8–11». В приложении есть локальное хранилище, игровая экономика,
подключение к API и трёхмерная сова из Blender.

## Экраны

| Модуль               | Экраны                                                   |
|----------------------|----------------------------------------------------------|
| `feature:onboarding` | Приветствие → Выбери цвет совы → Имя и первый план        |
| `feature:home`       | Вкладка «Питомец»: герой, уход, план недели, входы       |
| `feature:plan`       | Вкладка «План»: статьи со степперами, совет 40/30/10/20  |
| `feature:tasks`      | Вкладка «Задания» + шторка мини-урока (квиз)             |
| `feature:shop`       | Вкладка «Лавка»: товары со скидками в процентах          |
| `feature:goal`       | Вкладка «Мечта»: банка-копилка, прогноз, история         |
| `feature:growth`     | Оверлей «Рост Финика»: стадии, итоги недели              |
| `feature:profile`    | Оверлеи «Профиль» и «Достижения»                         |
| `feature:report`     | Отчёт «Неделя закрыта» (вариант 2c макета)               |

## Структура модулей

```
Finik
├── build-logic/convention   convention-плагины Gradle (finik.android.*)
├── app                      MainActivity, FinikApp (верхняя панель + нижняя навигация), NavHost
├── core
│   ├── model                UI-модели (статьи, питомец, задания, товары…) и SampleData из макета
│   ├── designsystem         токены цветов/шрифтов макета и общие компоненты
│   ├── navigation           NavOptions для вкладок и переходы между экранами
│   └── pet                  PetFigure — модели GLB, анимации, материалы и SceneView
└── feature/*                по модулю на экран/флоу (см. таблицу выше)
```

Правила зависимостей: `app → feature:* → core:*`. Feature-модули друг от друга не зависят;
переходы между ними передаются колбэками и «сшиваются» в
`app/src/main/java/dev/bober/finik/navigation/FinikNavHost.kt`.

## Навигация

- Стартовый граф — онбординг; после «Начать неделю» он снимается со стека, корнем становится «Питомец».
- Пять вкладок переключаются через `topLevelNavOptions<HomeRoute>()` с сохранением состояния.
- «Рост Финика», «Профиль», «Достижения» и «Отчёт» — полноэкранные маршруты поверх вкладок.
- Мини-урок — оверлей внутри вкладки «Задания» с обработкой системной кнопки «назад».

## Convention-плагины

| Плагин                              | Что даёт                                                 |
|-------------------------------------|----------------------------------------------------------|
| `finik.android.application`         | compileSdk/minSdk/targetSdk, Java 17                     |
| `finik.android.application.compose` | + Compose (BOM, tooling)                                 |
| `finik.android.library`             | базовая Android-библиотека                               |
| `finik.android.library.compose`     | + Compose                                                |
| `finik.android.feature`             | + serialization, Navigation Compose, core-модули         |

Версии — в `gradle/libs.versions.toml`.

## Как добавить feature-модуль

1. `feature/<name>/build.gradle.kts`:
   ```kotlin
   plugins { alias(libs.plugins.finik.android.feature) }
   android { namespace = "dev.bober.finik.feature.<name>" }
   ```
2. `include(":feature:<name>")` в `settings.gradle.kts` и
   `implementation(project(":feature:<name>"))` в `app/build.gradle.kts`.
3. Маршрут и регистрация экрана (см. любой `feature/*/navigation/*Navigation.kt`):
   ```kotlin
   @Serializable data object NameRoute
   fun NavController.navigateToName(navOptions: NavOptions? = null) = navigate(NameRoute, navOptions)
   fun NavGraphBuilder.nameScreen(onBack: () -> Unit) { composable<NameRoute> { NameScreen(onBack) } }
   ```
4. Зарегистрировать экран в `FinikNavHost`; если это вкладка — добавить её в `TopLevelDestination`.

## Дизайн-система

- Цвета: `FinikColor` — sRGB-эквиваленты oklch-значений макета, в комментариях оригиналы.
- Шрифты: Nunito (текст) и Unbounded (заголовки), variable TTF в `core/designsystem/src/main/res/font`,
  лицензии OFL — в `core/designsystem/fonts`. Хелперы `nunito(size, weight)` и `unbounded(size)`
  повторяют CSS `font:` из макета.
- Компоненты: `PrimaryButton`, `OutlineButton`, `SquareIconButton`, `FinikCard`, `GradientCard`,
  `CoinChip`, `FinikProgressBar`, `SegmentedBar`, `ShapeDot`, `TagChip`, `BackHeader`,
  `FinikBottomNav`, `FinikShellTopBar`, `FinikToast`.

## 3D-модель совы

`core/pet/src/main/assets/models/owl/finny.glb` — анимированная сова из
`finni-owl-android.zip`. Четыре аксессуара уже привязаны к костям внутри GLB.
Для банданы локальный Z узла `Accessory_bandana` уменьшен до `0.2212763`,
чтобы она прилегала к груди при просмотре сбоку.
Положение шляпы, медали и рюкзака задаёт `tools/owl/adjust_accessories.py`:
он меняет только локальные трансформации узлов GLB, сохраняя геометрию и анимации.
Шесть текстур перьев находятся в `models/owl/textures/`. При знакомстве и в лавке
можно сочетать аксессуары и выбирать цвет; изменения сохраняются на устройстве. Камера
вращается одним пальцем и масштабируется двумя во всех живых 3D-сценах.

Основная стоячая анимация — `Idle_3`; `Idle_4` используется для спокойного
ожидания, `Stand_and_Drink` — при питье, `Running` — во время игры, `Walking` —
при кормлении, `Big_Wave_Hello` — при знакомстве. `restpose` не используется:
это техническая T-поза.

## Данные

Экраны получают состояние через `FinikViewModel` и `FinikRepository`.
`SampleData` используется для начального состояния и Compose Preview.
Старые сохранения `FINIK`, `CACTUS`, `SPARK`, `CAT` и `DOG` показывают сову,
сохраняя имя и прогресс. Для новых профилей используется серверный идентификатор
`CACTUS`. Цвет перьев и набор аксессуаров сохраняются на устройстве: текущий
API не содержит этих полей.
