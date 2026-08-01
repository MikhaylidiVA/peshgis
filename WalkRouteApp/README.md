# WalkRoute App - MVP

Пешеходный навигатор для Android с планированием маршрутов по времени или количеству шагов.

## Функционал MVP

1. **Определение местоположения** - GPS через Google Play Services Location API
2. **Планирование маршрута** - по шагам или времени прогулки
3. **Навигация** - ведение по маршруту с инструкциями
4. **Подсчет шагов** - через датчик шагов устройства
5. **История прогулок** - сохранение всех маршрутов локально
6. **Профиль пользователя** - статистика и настройки

## Стек технологий

- **Язык**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Архитектура**: MVVM + Clean Architecture
- **DI**: Hilt
- **БД**: Room (локальное хранение)
- **Карты**: OSMdroid (OpenStreetMap - бесплатно, без API ключа)
- **Location**: Google Play Services Location API
- **Шаги**: Android Sensor API (TYPE_STEP_COUNTER, TYPE_STEP_DETECTOR)
- **Голос**: Android Text-to-Speech (системный)

## Структура проекта

```
app/src/main/java/com/walkroute/app/
├── data/
│   ├── local/
│   │   ├── dao/          # DAO интерфейсы
│   │   ├── database/     # Room Database
│   │   ├── entity/       # Entity классы
│   │   └── service/      # Фоновые сервисы
│   └── repository/       # Репозитории
├── domain/
│   ├── model/            # Модели данных
│   └── usecase/          # Use cases / Repositories
├── ui/
│   ├── screens/
│   │   ├── home/         # Главный экран
│   │   ├── routes/       # Экран маршрутов
│   │   ├── navigation/   # Навигация
│   │   ├── history/      # История
│   │   └── profile/      # Профиль
│   └── theme/            # Тема Compose
├── di/                   # Hilt модули
└── WalkRouteApplication.kt
```

## Разрешения

- ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, ACCESS_BACKGROUND_LOCATION
- ACTIVITY_RECOGNITION, HIGH_SAMPLING_RATE_SENSORS
- INTERNET, ACCESS_NETWORK_STATE

## Сборка

1. Откройте проект в Android Studio
2. Sync Gradle файлы
3. Запустите на эмуляторе или устройстве (Android 8.0+)

## Интеграция 2GIS карт

Для полноценной работы с картами 2GIS необходимо:
1. Получить API ключ в [2GIS Dev Center](https://dev.2gis.com/)
2. Добавить SDK зависимость в build.gradle.kts
3. Реализовать маппинг маршрутов на карте

## Следующие шаги

- [x] Реализовать экран выбора параметров маршрута (шаги/время)
- [x] Интегрировать OSMdroid для отображения карт (бесплатно, без API)
- [x] Алгоритм генерации пешеходных маршрутов
- [x] Экран навигации с голосовыми подсказками
- [ ] Полная реализация истории и профиля
- [ ] Тестирование на реальных устройствах
- [ ] Улучшение алгоритма маршрутизации (учёт пешеходных дорожек)
- [ ] Офлайн режим работы
