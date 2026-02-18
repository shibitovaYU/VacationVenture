# Python recommendation service

Сервис поддерживает гибридное ранжирование (content-based + item-based CF + контекст) для нескольких доменов:
- рестораны
- отели
- мероприятия
- авиабилеты/поезда (ticket)

## Что изменилось
Теперь фичи зависят от домена, а не только от ресторанов.

### Доменные признаки
- **Билеты (flight/train/ticket):** `price_amount`, `duration_minutes`, `stops`, `departure_hour`, `baggage_included`, `carrier`, `origin`, `destination`.
- **Отели:** `price_per_night`, `distance_to_center_km`, `stars`, `amenities`, `accommodation_type`, `cancellation_policy`.
- **Мероприятия:** `event_hour`, `genre`, `location_type`, `duration_minutes`, `age_restriction`.
- **Рестораны:** прежние признаки + `cuisine`, `district`, `open_now`.

## API

### `POST /api/v1/recommendations/{domain}/rank`
Универсальный endpoint ранжирования кандидатов.

`domain`:
- `restaurant`
- `hotel`
- `event`
- `flight`
- `train`
- `ticket`

Тело запроса:
```json
{
  "user_id": "demo_family",
  "hour": 19,
  "season": "summer",
  "domain": "hotel",
  "candidates": [
    {
      "item_id": "h_1",
      "title": "Hotel A",
      "rating": 4.6,
      "price_per_night": 130,
      "distance_to_center_km": 1.2,
      "stars": 4,
      "amenities": ["spa", "breakfast"],
      "accommodation_type": "apartment",
      "cancellation_policy": "free cancellation"
    }
  ],
  "user_interactions": {
    "h_0": 0.9
  }
}
```

Ответ:
```json
{
  "ranked_items": [
    {"item_id": "h_1", "score": 0.812345, "is_cold_start": false}
  ]
}
```

### `POST /api/v1/recommendations/restaurants/rank`
Обратная совместимость со старым endpoint (внутри перенаправляется на домен `restaurant`).

### `POST /api/v1/recommendations/home`
Возвращает персональную подсказку для стартового экрана.

## Запуск
```bash
cd recommendation_service
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8000
```

Android-клиент использует URL `http://10.0.2.2:8000` (эмулятор Android).
