# Фон «Лесная поляна»

Создан 29 сентября 2026 года встроенным инструментом image_gen (навык imagegen). Используется как декоративный фон Android-приложения Finik. Иллюстрация не содержит текста, персонажей и интерфейса; сова отображается отдельно.

Файл: `core/designsystem/src/main/res/drawable-nodpi/finik_forest_day.png`.

Фон статичен: не перехватывает касания и не добавляет анимацию. В тёмной теме применяется затемнение через Compose; отдельная копия изображения не нужна. Карточки с заданиями, бюджетом и кнопками сохраняют собственные поверхности для читаемости.

Блок совы на главном экране полностью прозрачный: у него нет собственной заливки, градиента или рамки, поэтому за питомцем видна лесная поляна.

## Проверка оформления

Проверено на Android-эмуляторе: приветствие, домашний экран, бюджет и задания в светлой теме; домашний экран и профиль в тёмной теме. Для прозрачных экранов согласованы переходы вперёд и назад. Цвет текста оболочки задан явно, чтобы баннер деморежима оставался читаемым в тёмной теме.

- [Приветствие](screenshots/forest-welcome.png)
- [Главный экран — светлая тема](screenshots/forest-home-light.png)
- [Главный экран — тёмная тема](screenshots/forest-home-dark.png)

Итоговый PNG: 887 × 1774 пикселя, около 1,8 МБ. APK собирается задачей `:app:assembleDebug`.

## Исходный запрос генерации

Use case: illustration-story. Asset type: full-screen portrait background illustration for an Android children's financial learning game with a friendly blue owl mascot. Generate ONLY the background artwork, no phone mockup, no interface. Portrait 1024 by 2048 composition. A sunny, inviting storybook woodland clearing, rounded mint and sage treetops and soft leafy branches framing the far left and right edges, gentle pale turquoise sky fading into warm cream, a small buttery sun glow and a few fluffy ivory clouds near the upper edge, low rolling meadow hills, tiny daisies and a few soft coral mushrooms nestled at the bottom corners. Polished contemporary children's book gouache / softly sculpted paper-cut illustration with subtle tactile grain, soft rounded organic shapes, joyful and calm, designed for children age 7–11. Existing app palette is warm ivory #FCFAF4, fresh leaf green, golden yellow, and a blue owl. Main composition requirement: keep the middle 65% of width and the middle 70% of height extremely spacious, very pale warm ivory with only the faintest atmosphere so dark UI text and a separate 3D owl remain readable. Put visual richness along the narrow outer borders and lower corners; the scene should still feel like a beautiful cohesive forest world behind app cards. Keep edges light enough for dark text. No owl or other characters in this image, no text, no lettering, no numbers, no logos, no coins, no watermark, no hard dark outlines, no scary shapes.
