from __future__ import annotations

from dataclasses import dataclass
from math import sqrt
from typing import Dict, List, Literal, Optional

from fastapi import FastAPI
from pydantic import BaseModel, Field

app = FastAPI(title="VacationVenture Recommender", version="3.0.0")

ALPHA = 0.5  # баланс контентной и collaborative составляющих
BETA = 0.15  # вес контекстной поправки
TOP_N_CANDIDATES = 30
K_NEIGHBORS = 8

RecommendationDomain = Literal["restaurant", "hotel", "event", "flight", "train", "ticket"]


class CandidateItem(BaseModel):
    item_id: str
    title: str

    # Базовые кросс-доменные признаки
    rating: float = 0.0
    tags: List[str] = Field(default_factory=list)
    review_count: int = 0
    price_level: Optional[str] = None

    # ===== Признаки билетов / поездов =====
    price_amount: Optional[float] = None
    duration_minutes: Optional[int] = None
    stops: Optional[int] = None
    departure_hour: Optional[int] = None
    baggage_included: Optional[bool] = None
    carrier: Optional[str] = None
    origin: Optional[str] = None
    destination: Optional[str] = None

    # ===== Признаки отелей =====
    price_per_night: Optional[float] = None
    distance_to_center_km: Optional[float] = None
    stars: Optional[float] = None
    amenities: List[str] = Field(default_factory=list)
    accommodation_type: Optional[str] = None
    cancellation_policy: Optional[str] = None

    # ===== Признаки мероприятий =====
    event_hour: Optional[int] = None
    genre: Optional[str] = None
    location_type: Optional[str] = None  # indoor / outdoor
    age_restriction: Optional[int] = None

    # ===== Признаки ресторанов =====
    cuisine: Optional[str] = None
    district: Optional[str] = None
    open_now: Optional[bool] = None

    # Необязательные признаки для алгоритма из НИР
    content_vector: Optional[List[float]] = None
    interaction_vector: Optional[List[float]] = None


class RankingRequest(BaseModel):
    user_id: Optional[str] = None
    hour: int
    season: str
    domain: RecommendationDomain = "restaurant"

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


class RankingResponse(BaseModel):
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

DOMAIN_VECTOR_SIZE: Dict[RecommendationDomain, int] = {
    "restaurant": 8,
    "hotel": 8,
    "event": 8,
    "flight": 8,
    "train": 8,
    "ticket": 8,
}


@app.post("/api/v1/recommendations/{domain}/rank", response_model=RankingResponse)
def rank_by_domain(domain: RecommendationDomain, payload: RankingRequest) -> RankingResponse:
    payload.domain = domain
    return _rank_items(payload)


@app.post("/api/v1/recommendations/restaurants/rank", response_model=RankingResponse)
def rank_restaurants(payload: RankingRequest) -> RankingResponse:
    # Обратная совместимость со старым endpoint.
    payload.domain = "restaurant"
    return _rank_items(payload)


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


def _rank_items(payload: RankingRequest) -> RankingResponse:
    if not payload.candidates:
        return RankingResponse(ranked_items=[])

    profile = USER_PROFILES.get(payload.user_id or "")
    is_cold_start = _is_cold_start(payload, profile)

    user_vector = _resolve_user_vector(payload, profile)
    content_similarities: Dict[str, float] = {}

    for candidate in payload.candidates:
        item_vec = _build_content_vector(candidate, payload.domain)
        sim_content = _cosine_similarity(user_vector, item_vec)
        content_similarities[candidate.item_id] = sim_content

    selected_candidates = sorted(
        payload.candidates,
        key=lambda item: content_similarities.get(item.item_id, 0.0),
        reverse=True,
    )[: min(TOP_N_CANDIDATES, len(payload.candidates))]

    predicted_ratings: Dict[str, float] = {}
    for item in selected_candidates:
        predicted_ratings[item.item_id] = _predict_item_based_score(
            target_item=item,
            neighbors=selected_candidates,
            user_interactions=payload.user_interactions,
            domain=payload.domain,
        )

    ranked_items: List[ScoredItem] = []
    for item in selected_candidates:
        sim_content = content_similarities.get(item.item_id, 0.0)
        predicted_rating = predicted_ratings.get(item.item_id, 0.0)

        hybrid_score = ALPHA * sim_content + (1 - ALPHA) * predicted_rating
        context_adjustment = _context_relevance(item, payload.season, payload.hour, payload.domain)
        final_score = hybrid_score + BETA * context_adjustment

        ranked_items.append(
            ScoredItem(
                item_id=item.item_id,
                score=round(final_score, 6),
                is_cold_start=is_cold_start,
            )
        )

    ranked_items.sort(key=lambda row: row.score, reverse=True)
    return RankingResponse(ranked_items=ranked_items)


def _is_cold_start(payload: RankingRequest, profile: Optional[UserProfile]) -> bool:
    no_profile = profile is None and not payload.user_vector
    no_interactions = len(payload.user_interactions) == 0
    return no_profile or no_interactions


def _resolve_user_vector(payload: RankingRequest, profile: Optional[UserProfile]) -> List[float]:
    target_size = DOMAIN_VECTOR_SIZE[payload.domain]

    if payload.user_vector:
        return _normalize_vector(_fit_vector(payload.user_vector, target_size))

    if profile is not None:
        return _normalize_vector(_fit_vector(profile.user_vector, target_size))

    # Cold start fallback: усреднённый prior-вектор
    fallback = [0.55, 0.45, 0.5, 0.4, 0.5, 0.5, 0.5, 0.5]
    return _normalize_vector(_fit_vector(fallback, target_size))


def _build_content_vector(item: CandidateItem, domain: RecommendationDomain) -> List[float]:
    if item.content_vector:
        return _normalize_vector(_fit_vector(item.content_vector, DOMAIN_VECTOR_SIZE[domain]))

    if domain == "restaurant":
        # [rating, reviews, price, open_now, tag_density, cuisine, district, evening_fit]
        rating_norm = _clip(item.rating / 5.0)
        review_norm = _clip(item.review_count / 500.0)
        price_bucket = _price_to_float(item.price_level)
        open_now = _to_bool_feature(item.open_now) or _tag_flag(item.tags, "open now")
        tag_density = _clip(len(item.tags) / 8.0)
        cuisine_flag = _is_present(item.cuisine)
        district_flag = _is_present(item.district)
        evening_fit = _tag_flag(item.tags, "dinner")
        return _normalize_vector([
            rating_norm,
            review_norm,
            price_bucket,
            open_now,
            tag_density,
            cuisine_flag,
            district_flag,
            evening_fit,
        ])

    if domain == "hotel":
        # [rating, price/night, distance_center, stars, amenities, breakfast, flexible_cancel, accommodation]
        rating_norm = _clip(item.rating / 5.0)
        price_norm = _clip((item.price_per_night or 0.0) / 500.0)
        distance_norm = _clip((item.distance_to_center_km or 20.0) / 20.0)
        stars_norm = _clip((item.stars or 0.0) / 5.0)
        amenities_norm = _clip(len(item.amenities) / 10.0)
        breakfast = _contains_any(item.amenities, ["breakfast", "завтрак"])
        flexible_cancel = _contains_any([item.cancellation_policy or ""], ["free", "flex", "бесплат", "гибк"])
        accommodation = _is_present(item.accommodation_type)
        return _normalize_vector([
            rating_norm,
            1.0 - price_norm,
            1.0 - distance_norm,
            stars_norm,
            amenities_norm,
            breakfast,
            flexible_cancel,
            accommodation,
        ])

    if domain == "event":
        # [rating, popularity, date_fit, genre, location_type, duration_fit, age_friendly, tag_density]
        rating_norm = _clip(item.rating / 5.0)
        popularity = _clip(item.review_count / 1000.0)
        date_fit = _is_present(item.event_hour)
        genre_flag = _is_present(item.genre)
        location_flag = _contains_any([item.location_type or ""], ["indoor", "outdoor", "внутри", "улиц"])
        duration_fit = _clip((item.duration_minutes or 90) / 240.0)
        age_friendly = 1.0 - _clip((item.age_restriction or 0) / 21.0)
        tag_density = _clip(len(item.tags) / 8.0)
        return _normalize_vector([
            rating_norm,
            popularity,
            date_fit,
            genre_flag,
            location_flag,
            duration_fit,
            age_friendly,
            tag_density,
        ])

    # flight / train / ticket
    # [price, duration, stops, departure_fit, baggage, carrier, route, popularity]
    price_norm = _clip((item.price_amount or 0.0) / 2000.0)
    duration_norm = _clip((item.duration_minutes or 0) / 1440.0)
    stops_norm = _clip((item.stops or 0) / 3.0)
    departure_fit = _clip(((item.departure_hour or 12) - 6) / 18.0)
    baggage = _to_bool_feature(item.baggage_included)
    carrier = _is_present(item.carrier)
    route = _is_present(item.origin) * _is_present(item.destination)
    popularity = _clip(item.review_count / 1000.0)

    return _normalize_vector([
        1.0 - price_norm,
        1.0 - duration_norm,
        1.0 - stops_norm,
        departure_fit,
        baggage,
        carrier,
        route,
        popularity,
    ])


def _predict_item_based_score(
    target_item: CandidateItem,
    neighbors: List[CandidateItem],
    user_interactions: Dict[str, float],
    domain: RecommendationDomain,
) -> float:
    scored_neighbors: List[tuple[float, CandidateItem]] = []

    for other_item in neighbors:
        if other_item.item_id == target_item.item_id:
            continue
        similarity = _item_similarity(target_item, other_item, domain)
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

    return _cold_start_item_prior(target_item, domain)


def _item_similarity(item_i: CandidateItem, item_j: CandidateItem, domain: RecommendationDomain) -> float:
    vector_i = _build_interaction_vector(item_i, domain)
    vector_j = _build_interaction_vector(item_j, domain)
    return _cosine_similarity(vector_i, vector_j)


def _build_interaction_vector(item: CandidateItem, domain: RecommendationDomain) -> List[float]:
    if item.interaction_vector:
        return _normalize_vector(_fit_vector(item.interaction_vector, DOMAIN_VECTOR_SIZE[domain]))

    if domain in ("flight", "train", "ticket"):
        return _normalize_vector([
            _clip((item.price_amount or 0.0) / 2000.0),
            _clip((item.duration_minutes or 0) / 1440.0),
            _clip((item.stops or 0) / 3.0),
            _to_bool_feature(item.baggage_included),
            _is_present(item.carrier),
            _clip(item.review_count / 1000.0),
            _is_present(item.origin),
            _is_present(item.destination),
        ])

    if domain == "hotel":
        return _normalize_vector([
            _clip(item.rating / 5.0),
            _clip((item.price_per_night or 0.0) / 500.0),
            _clip((item.distance_to_center_km or 20.0) / 20.0),
            _clip((item.stars or 0.0) / 5.0),
            _clip(len(item.amenities) / 10.0),
            _contains_any(item.amenities, ["spa", "breakfast", "pool"]),
            _is_present(item.accommodation_type),
            _contains_any([item.cancellation_policy or ""], ["free", "flex", "бесплат", "гибк"]),
        ])

    if domain == "event":
        return _normalize_vector([
            _clip(item.rating / 5.0),
            _clip(item.review_count / 1000.0),
            _is_present(item.genre),
            _contains_any([item.location_type or ""], ["indoor", "outdoor", "внутри", "улиц"]),
            _clip((item.duration_minutes or 90) / 240.0),
            1.0 - _clip((item.age_restriction or 0) / 21.0),
            _clip(len(item.tags) / 10.0),
            _is_present(item.event_hour),
        ])

    # restaurant
    return _normalize_vector([
        _clip(item.rating / 5.0),
        _clip(item.review_count / 1000.0),
        _clip(len(item.tags) / 10.0),
        _price_to_float(item.price_level),
        _tag_flag(item.tags, "open now"),
        _is_present(item.cuisine),
        _is_present(item.district),
        _is_present(item.open_now),
    ])


def _context_relevance(item: CandidateItem, season: str, hour: int, domain: RecommendationDomain) -> float:
    tags_lower = {tag.lower() for tag in item.tags}

    if domain in ("flight", "train", "ticket"):
        departure_hour = item.departure_hour if item.departure_hour is not None else 12
        departure_fit = 1.0 - _clip(abs(departure_hour - hour) / 12.0)
        direct_bonus = 0.2 if (item.stops or 0) == 0 else 0.0
        return _clip(0.7 * departure_fit + direct_bonus)

    if domain == "hotel":
        season_weights = CONTEXT_WEIGHTS.get(season.lower(), {})
        seasonal_score = sum(weight for tag, weight in season_weights.items() if tag in tags_lower)
        if season.lower() == "winter" and _contains_any(item.amenities, ["spa", "pool"]):
            seasonal_score += 0.2
        if season.lower() == "summer" and (item.distance_to_center_km or 10.0) <= 3.0:
            seasonal_score += 0.2
        return _clip(seasonal_score)

    if domain == "event":
        event_hour = item.event_hour if item.event_hour is not None else hour
        time_fit = 1.0 - _clip(abs(event_hour - hour) / 12.0)
        location_bonus = 0.15 if (item.location_type or "").lower() in {"indoor", "outdoor"} else 0.0
        return _clip(time_fit + location_bonus)

    # restaurant
    season_weights = CONTEXT_WEIGHTS.get(season.lower(), {})
    seasonal_score = sum(weight for tag, weight in season_weights.items() if tag in tags_lower)
    evening_bonus = 0.2 if hour >= 18 and (item.open_now or "open now" in tags_lower) else 0.0
    return _clip(seasonal_score + evening_bonus)


def _cold_start_item_prior(item: CandidateItem, domain: RecommendationDomain) -> float:
    if domain in ("flight", "train", "ticket"):
        price_score = 1.0 - _clip((item.price_amount or 0.0) / 2000.0)
        duration_score = 1.0 - _clip((item.duration_minutes or 0) / 1440.0)
        stop_score = 1.0 - _clip((item.stops or 0) / 3.0)
        return _clip(0.45 * price_score + 0.35 * duration_score + 0.2 * stop_score)

    if domain == "hotel":
        rating_norm = _clip(item.rating / 5.0)
        stars_norm = _clip((item.stars or 0.0) / 5.0)
        center_norm = 1.0 - _clip((item.distance_to_center_km or 20.0) / 20.0)
        return _clip(0.5 * rating_norm + 0.3 * stars_norm + 0.2 * center_norm)

    if domain == "event":
        rating_norm = _clip(item.rating / 5.0)
        review_norm = _clip(item.review_count / 800.0)
        age_norm = 1.0 - _clip((item.age_restriction or 0) / 21.0)
        return _clip(0.5 * rating_norm + 0.3 * review_norm + 0.2 * age_norm)

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


def _fit_vector(vector: List[float], size: int) -> List[float]:
    if len(vector) == size:
        return vector
    if len(vector) > size:
        return vector[:size]
    return vector + [0.0] * (size - len(vector))


def _price_to_float(price_level: Optional[str]) -> float:
    if not price_level:
        return 0.5
    bucket = price_level.count("$")
    return _clip(bucket / 4.0)


def _clip(value: float) -> float:
    return max(0.0, min(1.0, value))


def _contains_any(values: List[str], needles: List[str]) -> float:
    lowered = " ".join(values).lower()
    return 1.0 if any(needle.lower() in lowered for needle in needles) else 0.0


def _to_bool_feature(value: Optional[bool]) -> float:
    if value is None:
        return 0.0
    return 1.0 if value else 0.0


def _is_present(value: object) -> float:
    if value is None:
        return 0.0
    if isinstance(value, str):
        return 1.0 if value.strip() else 0.0
    return 1.0


def _tag_flag(tags: List[str], expected: str) -> float:
    expected_l = expected.lower()
    return 1.0 if any(tag.lower() == expected_l for tag in tags) else 0.0
