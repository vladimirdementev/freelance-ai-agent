# Freelance AI Agent

MVP backend для поиска фриланс-заказов, их классификации, расчета привлекательности и отправки лучших вариантов в Telegram.

Первая версия специально сделана узкой. Она умеет:

1. получать заказы из FL.ru RSS или другого парсерного слоя;
2. приводить заказ к единому формату;
3. сохранять заказы в PostgreSQL;
4. классифицировать заказ через OpenAI, если задан API-ключ;
5. использовать локальный эвристический классификатор, если OpenAI не настроен;
6. рассчитывать score заказа;
7. отправлять Telegram-уведомление, если заказ проходит заданный порог.

В MVP пока нет автоматической отправки откликов, CRM, мобильного приложения и AI coding agent. Сначала нужно проверить, что система стабильно находит выгодные заказы.

## Архитектура

```text
FL.ru RSS / Kwork feed / parser
  -> /api/projects/ingest
  -> PostgreSQL
  -> AI / heuristic classifier
  -> scoring engine
  -> top projects API
  -> Telegram notification
```

## Технологии

- Java 21
- Spring Boot 4.1
- PostgreSQL
- Flyway
- Maven
- Docker Compose

## Локальный запуск

```bash
docker compose up --build
```

Опциональные переменные окружения:

| Переменная | Назначение | Значение по умолчанию |
| --- | --- | --- |
| `OPENAI_API_KEY` | Включает классификацию через OpenAI | пусто |
| `OPENAI_MODEL` | Модель OpenAI | `gpt-4.1-mini` |
| `TELEGRAM_BOT_TOKEN` | Токен Telegram-бота | пусто |
| `TELEGRAM_CHAT_ID` | ID Telegram-чата для уведомлений | пусто |
| `TARGET_HOURLY_RATE` | Целевая ставка в рублях в час для score | `1500` |
| `MIN_NOTIFICATION_SCORE` | Минимальный score для Telegram-уведомления | `80` |
| `COLLECTORS_ENABLED` | Включает scheduled collectors | `false` |
| `KWORK_FEED_URL` | URL нормализованного JSON-фида Kwork | пусто |
| `FL_RU_FEED_URL` | URL RSS-ленты FL.ru | `https://www.fl.ru/rss/all.xml` |

## API

### Загрузить один нормализованный заказ

```bash
curl -X POST http://localhost:8080/api/projects/ingest \
  -H 'Content-Type: application/json' \
  -d '{
    "platform": "KWORK",
    "externalId": "kwork-123",
    "title": "Telegram bot with payments",
    "description": "Need a Telegram bot with catalog, PostgreSQL, payment API and admin notifications.",
    "price": 18000,
    "publishedAt": "2026-08-14T10:00:00Z"
  }'
```

### Получить топ заказов

```bash
curl 'http://localhost:8080/api/projects/top?limit=10'
```

## FL.ru collector

Для FL.ru используется RSS-лента:

```text
https://www.fl.ru/rss/all.xml
```

Collector читает `title`, `link`, `description`, `pubDate`, извлекает ID проекта из ссылки вида `/projects/{id}/`, парсит бюджет из заголовка и передает заказ в общий pipeline:

```text
RSS item
  -> CollectedProject
  -> ProjectIngestRequest
  -> PostgreSQL
  -> classifier
  -> scoring
  -> Telegram
```

Чтобы включить scheduled collection:

```bash
COLLECTORS_ENABLED=true docker compose up --build
```

## Формат фида для Kwork collector

Kwork collector пока ожидает нормализованный JSON-фид. Это позволяет отдельно развивать реальный парсер площадки, не меняя downstream-часть: базу, AI-анализ, scoring и Telegram.

```json
{
  "items": [
    {
      "externalId": "kwork-123",
      "title": "Telegram bot with payments",
      "description": "Need a bot with payments and PostgreSQL.",
      "price": 18000,
      "publishedAt": "2026-08-14T10:00:00Z"
    }
  ]
}
```

Также поддерживается JSON-массив с такой же структурой элементов.

## Формула scoring

```text
score =
    30% * skill match
  + 25% * automation
  + 20% * price/time
  + 15% * simplicity
  + 10% * win probability
```

`price/time` нормализуется относительно `TARGET_HOURLY_RATE`.

`win probability` — консервативная производная оценка на основе соответствия навыкам, автоматизируемости, простоты и риска.

## Следующие продуктовые шаги

1. Собрать 200-500 реальных заказов с Kwork и Freelance.ru.
2. Проанализировать повторяющиеся типы проектов.
3. Выбрать три основные ниши.
4. Сделать три небольших portfolio projects под выбранные ниши.
5. Заменить normalized feed collector на реальные адаптеры площадок.
6. Добавить генерацию откликов только после проверки качества scoring.
7. Сохранять результаты по каждому заказу: откликнулись или нет, получили заказ или нет, сколько часов заняло выполнение, сколько заработали.
