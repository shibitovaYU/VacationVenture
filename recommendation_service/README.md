# Python recommendation service

Гибридный рекомендательный модуль для VacationVenture по алгоритму из НИР (контент + item-based + контекст + cold start).

## Реализованный алгоритм
1. **Отбор кандидатов (контентная фильтрация)**
   - Строится `sim_content(u, i)` через косинусное сходство между `user_vector` и `content_vector`.
   - Если `content_vector` не передан, собирается из признаков объекта (rating, review_count, price, open_now, tags).
   - Берутся Top-N кандидатов по `sim_content`.

2. **Ранжирование (item-based CF)**
   - Для каждого кандидата `i` считается `sim_item(i, j)` как косинус между векторами взаимодействий объектов.
   - Предсказанный рейтинг считается по формуле
     `r_hat(u,i)=sum(sim(i,j)*r(u,j))/sum(|sim(i,j)|)`.
   - Если взаимодействий нет (cold start), используется fallback-приор на основе качества/популярности объекта.

3. **Гибридизация**
   - `score(u,i) = α * sim_content(u,i) + (1-α) * r_hat(u,i)`
   - По умолчанию `α=0.5`.

4. **Учет контекста**
   - `score_final(u,i) = score(u,i) + β * f(context)`
   - Учитываются сезон, время суток и теги (`open now`, `indoor`, `terrace`, ...).
   - По умолчанию `β=0.15`.

## Cold start
Cold start считается активным, если нет профиля и/или пользовательских взаимодействий.
В этом случае:
- профиль инициализируется prior-вектором,
- collaborative часть переходит на popularity/quality fallback.

## API
### `POST /api/v1/recommendations/restaurants/rank`
Поддерживает расширенный payload:
- `user_vector` (опционально),
- `user_interactions` (опционально),
- `candidates[].content_vector` (опционально),
- `candidates[].interaction_vector` (опционально).

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
