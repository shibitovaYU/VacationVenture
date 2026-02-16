from __future__ import annotations

from dataclasses import dataclass
from math import sqrt
from typing import Dict, List, Optional

from fastapi import FastAPI
from pydantic import BaseModel, Field

app = FastAPI(title="VacationVenture Recommender", version="2.0.0")

ALPHA = 0.5  # баланс контентной и collaborative составляющих
BETA = 0.15  # вес контекстной поправки
TOP_N_CANDIDATES = 30
K_NEIGHBORS = 8


class CandidateItem(BaseModel):
    item_id: str
    title: str
    rating: float = 0.0
    tags: List[str] = Field(default_factory=list)
    review_count: int = 0
    price_level: Optional[str] = None

    # Необязательные признаки для алгоритма из НИР
    content_vector: Optional[List[float]] = None
    interaction_vector: Optional[List[float]] = None


class RestaurantRankingRequest(BaseModel):
    user_id: Optional[str] = None
    hour: int
    season: str

    # Кандидаты на рекомендацию
    candidates: List[CandidateItem]

    # Для шага 1 (контент): профиль пользователя из анкеты/истории
    user_vector: Optional[List[float]] = None

    # Для шага 2 (item-based): взаимодействия текущего пользователя с объектами
    user_interactions: Dict[str, float] = Field(default_factory=dict)


class ScoredItem(BaseModel):
    item_id: str
    score: float
    is_cold_start: bool = False


class RestaurantRankingResponse(BaseModel):
    ranked_items: List[ScoredItem]


class HomeRecommendationRequest(BaseModel):
    user_id: Optional[str] = None
    section: str
    hour: int
    season: str


class HomeRecommendationResponse(BaseModel):
    message: str


@dataclass
class UserProfile:
    preferred_tags: List[str]
    preferred_price: Optional[str]
    user_vector: List[float]


# В проде это будет храниться в БД/feature store.
USER_PROFILES: Dict[str, UserProfile] = {
    "demo_family": UserProfile(
        preferred_tags=["family", "open now", "indoor"],
        preferred_price="$$",
        user_vector=[1.0, 0.9, 0.2, 0.8, 0.6],
    ),
    "demo_budget": UserProfile(
        preferred_tags=["cheap", "street", "fast"],
        preferred_price="$",
        user_vector=[0.7, 0.2, 1.0, 0.3, 0.9],
    ),
}

CONTEXT_WEIGHTS: Dict[str, Dict[str, float]] = {
    "winter": {"indoor": 0.35, "warm": 0.3, "open now": 0.2},
    "summer": {"outdoor": 0.35, "terrace": 0.3, "open now": 0.2},
    "autumn": {"cozy": 0.3, "coffee": 0.25, "open now": 0.15},
    "spring": {"fresh": 0.25, "park": 0.25, "open now": 0.15},
}


@app.post("/api/v1/recommendations/restaurants/rank", response_model=RestaurantRankingResponse)
def rank_restaurants(payload: RestaurantRankingRequest) -> RestaurantRankingResponse:
    if not payload.candidates:
        return RestaurantRankingResponse(ranked_items=[])

    profile = USER_PROFILES.get(payload.user_id or "")

    # ===== Шаг 0. Определение cold start =====
    is_cold_start = _is_cold_start(payload, profile)

    # ===== Шаг 1. Отбор кандидатов (контентная фильтрация) =====
    user_vector = _resolve_user_vector(payload, profile)
    content_similarities: Dict[str, float] = {}

    for candidate in payload.candidates:
        item_vec = _build_content_vector(candidate)
        sim_content = _cosine_similarity(user_vector, item_vec)
        content_similarities[candidate.item_id] = sim_content

    selected_candidates = sorted(
        payload.candidates,
        key=lambda item: content_similarities.get(item.item_id, 0.0),
        reverse=True,
    )[: min(TOP_N_CANDIDATES, len(payload.candidates))]

    # ===== Шаг 2. Item-based ранжирование =====
    predicted_ratings: Dict[str, float] = {}
    for item in selected_candidates:
        predicted_ratings[item.item_id] = _predict_item_based_score(
            target_item=item,
            neighbors=selected_candidates,
            user_interactions=payload.user_interactions,
        )

    # ===== Шаг 3 + 4. Гибридизация + контекстная поправка =====
    ranked_items: List[ScoredItem] = []
    for item in selected_candidates:
        sim_content = content_similarities.get(item.item_id, 0.0)
        predicted_rating = predicted_ratings.get(item.item_id, 0.0)

        hybrid_score = ALPHA * sim_content + (1 - ALPHA) * predicted_rating
        context_adjustment = _context_relevance(item, payload.season, payload.hour)
        final_score = hybrid_score + BETA * context_adjustment

        ranked_items.append(
            ScoredItem(
                item_id=item.item_id,
                score=round(final_score, 6),
                is_cold_start=is_cold_start,
            )
        )

    ranked_items.sort(key=lambda row: row.score, reverse=True)
    return RestaurantRankingResponse(ranked_items=ranked_items)


@app.post("/api/v1/recommendations/home", response_model=HomeRecommendationResponse)
def home_recommendation(payload: HomeRecommendationRequest) -> HomeRecommendationResponse:
    section_names = {
        "flight": "авиабилеты",
        "train": "поезда",
        "hotel": "отели",
        "restaurant": "рестораны",
        "event": "мероприятия",
    }

    section = section_names.get(payload.section, payload.section)
    day_phase = "вечер" if payload.hour >= 18 else "день"

    if payload.user_id in USER_PROFILES:
        message = (
            f"Сейчас {day_phase}: рекомендуем открыть «{section}». "
            f"Учли анкету, историю и сезон {payload.season}."
        )
    else:
        message = (
            f"Сейчас {day_phase}: начните с раздела «{section}». "
            f"Для холодного старта используем анкету и контекст ({payload.season})."
        )

    return HomeRecommendationResponse(message=message)


def _is_cold_start(payload: RestaurantRankingRequest, profile: Optional[UserProfile]) -> bool:
    no_profile = profile is None and not payload.user_vector
    no_interactions = len(payload.user_interactions) == 0
    return no_profile or no_interactions


def _resolve_user_vector(payload: RestaurantRankingRequest, profile: Optional[UserProfile]) -> List[float]:
    if payload.user_vector:
        return _normalize_vector(payload.user_vector)

    if profile is not None:
        return _normalize_vector(profile.user_vector)

    # Cold start fallback: усреднённый prior-вектор для инициализации предпочтений
    return _normalize_vector([0.55, 0.45, 0.5, 0.4, 0.5])


def _build_content_vector(item: CandidateItem) -> List[float]:
    if item.content_vector:
        return _normalize_vector(item.content_vector)

    # Базовый фиче-вектор объекта при отсутствии явных эмбеддингов:
    # [rating_norm, review_norm, price_bucket, open_now, tag_density]
    rating_norm = _clip(item.rating / 5.0)
    review_norm = _clip(item.review_count / 500.0)
    price_bucket = _price_to_float(item.price_level)
    open_now = 1.0 if any(tag.lower() == "open now" for tag in item.tags) else 0.0
    tag_density = _clip(len(item.tags) / 8.0)

    return _normalize_vector([rating_norm, review_norm, price_bucket, open_now, tag_density])


def _predict_item_based_score(
    target_item: CandidateItem,
    neighbors: List[CandidateItem],
    user_interactions: Dict[str, float],
) -> float:
    # Формула (3): r_hat(u, i) = sum(sim(i,j) * r(u,j)) / sum(|sim(i,j)|)
    scored_neighbors: List[tuple[float, CandidateItem]] = []

    for other_item in neighbors:
        if other_item.item_id == target_item.item_id:
            continue
        similarity = _item_similarity(target_item, other_item)
        if similarity > 0:
            scored_neighbors.append((similarity, other_item))

    scored_neighbors.sort(key=lambda pair: pair[0], reverse=True)
    top_neighbors = scored_neighbors[:K_NEIGHBORS]

    numerator = 0.0
    denominator = 0.0

    for similarity, neighbor in top_neighbors:
        interaction_signal = user_interactions.get(neighbor.item_id, 0.0)
        numerator += similarity * interaction_signal
        denominator += abs(similarity)

    if denominator > 0:
        return _clip(numerator / denominator)

    # Cold start для CF-компоненты: популярность/качество объекта
    return _cold_start_item_prior(target_item)


def _item_similarity(item_i: CandidateItem, item_j: CandidateItem) -> float:
    # Формула (2): косинус похожести между векторами взаимодействий объектов i и j
    vector_i = _build_interaction_vector(item_i)
    vector_j = _build_interaction_vector(item_j)
    return _cosine_similarity(vector_i, vector_j)


def _build_interaction_vector(item: CandidateItem) -> List[float]:
    if item.interaction_vector:
        return _normalize_vector(item.interaction_vector)

    # При отсутствии полной матрицы R используем прокси-поведенческие признаки.
    return _normalize_vector([
        _clip(item.rating / 5.0),
        _clip(item.review_count / 1000.0),
        _clip(len(item.tags) / 10.0),
    ])


def _context_relevance(item: CandidateItem, season: str, hour: int) -> float:
    season_weights = CONTEXT_WEIGHTS.get(season.lower(), {})
    tags_lower = {tag.lower() for tag in item.tags}

    seasonal_score = sum(weight for tag, weight in season_weights.items() if tag in tags_lower)
    evening_bonus = 0.2 if hour >= 18 and "open now" in tags_lower else 0.0

    # Нормируем к [0, 1]
    return _clip(seasonal_score + evening_bonus)


def _cold_start_item_prior(item: CandidateItem) -> float:
    rating_norm = _clip(item.rating / 5.0)
    review_norm = _clip(item.review_count / 800.0)
    return _clip(0.65 * rating_norm + 0.35 * review_norm)


def _cosine_similarity(vec_a: List[float], vec_b: List[float]) -> float:
    if not vec_a or not vec_b:
        return 0.0

    size = min(len(vec_a), len(vec_b))
    if size == 0:
        return 0.0

    a = vec_a[:size]
    b = vec_b[:size]

    numerator = sum(x * y for x, y in zip(a, b))
    norm_a = sqrt(sum(x * x for x in a))
    norm_b = sqrt(sum(y * y for y in b))

    if norm_a == 0 or norm_b == 0:
        return 0.0

    return _clip(numerator / (norm_a * norm_b))


def _normalize_vector(vector: List[float]) -> List[float]:
    if not vector:
        return []
    norm = sqrt(sum(x * x for x in vector))
    if norm == 0:
        return [0.0 for _ in vector]
    return [x / norm for x in vector]


def _price_to_float(price_level: Optional[str]) -> float:
    if not price_level:
        return 0.5
    bucket = price_level.count("$")
    return _clip(bucket / 4.0)


def _clip(value: float) -> float:
    return max(0.0, min(1.0, value))
