# Finik — виртуальный питомец и финансы (8–11 лет)

Многомодульное Android-приложение на Jetpack Compose по макету Claude Design
«Финик 8–11». Сейчас в проекте дизайн всех экранов и навигация между ними;
бизнес-логика, хранилище и 3D-модель питомца появятся позже.

## Экраны

| Модуль               | Экраны                                                   |
|----------------------|----------------------------------------------------------|
| `feature:onboarding` | Приветствие → Выбери росток → Имя и первый план          |
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
│   └── pet                  PetFigure — фигура питомца, слот для 3D-модели из Blender
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

## 3D-модель питомца

`core/pet` экспортирует `PetFigure(species, spec, mood, animate)` с пресетами размеров из макета
(`Welcome`, `Card`, `Naming`, `hero(stage)`). Сейчас это плоская фигура с idle-анимацией.
Когда модель из Blender будет готова:

1. Экспортировать в glTF/GLB, положить в `core/pet/src/main/assets/`.
2. Подключить рендерер (SceneView или Filament) в `core/pet/build.gradle.kts`.
3. Заменить тело `PetFigure` на рендерер, сохранив сигнатуру — экраны трогать не нужно.

## Данные

Все цифры на экранах — `core/model/.../SampleData.kt`, повторяющий начальное состояние прототипа.
Когда появится логика, экраны получат состояние из ViewModel вместо `SampleData`.
