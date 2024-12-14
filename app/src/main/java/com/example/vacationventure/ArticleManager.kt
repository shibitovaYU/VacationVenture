package com.example.vacationventure

data class VacationArticle(val title: String, val url: String)

class ArticleManager {
    private val articles = listOf(
        VacationArticle("Пляжный отдых", "https://travel.yandex.ru/journal/kuda-poekhat-zimoj-za-granicu/"),
        VacationArticle("Активный отдых", "https://dzen.ru/a/XEgnQDF8GgCudUen"),
        VacationArticle("Культурный туризм (музеи,театры)", "https://travel.yandex.ru/journal/samye-populyarnye-strany-dlya-torizma/"),
        VacationArticle("Кулинарные путешествия", "https://www.colady.ru/top-7-stran-dlya-gastronomicheskix-puteshestvij-gurmana.html"),
        VacationArticle("Ночная жизнь", "https://34travel.me/post/20bestclubs"),
        VacationArticle("Спортивные мероприятия", "https://www.sport-express.ru/"),
        VacationArticle("Концерты и фестивали", "https://dzen.ru/a/ZyCTOh6hFTf1ZXJJ"),
        VacationArticle("Природные экскурсии", "https://bolshayastrana.com/blog/prirodnye-sokrovishcha-rossii-250"),
        VacationArticle("Теплый и солнечный", "https://travel.yandex.ru/journal/kruglyj-god-leto/"),
        VacationArticle("Умеренный", "https://www.atorus.ru/news/press-centre/new/55820.html"),
        VacationArticle("Холодный и снежный", "https://travel.yandex.ru/journal/samye-populyarnye-strany-dlya-torizma/"),
        VacationArticle("Тропический", "https://travelest.ru/marshruty/20-luchshikh-tropicheskikh-ostrovov-dlya-otdykha"),
        VacationArticle("Отель", "https://dzen.ru/a/XHaG0W3HwACusRFV"),
        VacationArticle("Хостел", "https://dzen.ru/a/XGp9zjH-OACuhJZE"),
        VacationArticle("Кемпинг", "https://journal.tinkoff.ru/campings/"),
        VacationArticle("Аренда квартиры", "https://blog.domclick.ru/nedvizhimost/post/kak-pravilno-snyat-kvartiru-na-nebolshoj-srok"),
        VacationArticle("Исследуя новые места", "https://journal.tinkoff.ru/short/top-20-in-2022/"),
        VacationArticle("Расслабляясь на пляже", "https://aviata.kz/media/selection/16-luchshikh-pliazhei-mira-s-lazurnoi-vodoi-i-barkhatnym-peskom/"),
        VacationArticle("Занимаясь спортом", "https://blog.ostrovok.ru/top-10-luchshix-otelej-mira-dlya-aktivnogo-otdyxa/"),
        VacationArticle("Участвуя в культурных мероприятиях", "https://kudago.com/msk/list/luchshie-marshruty-po-moskve-intellektualnyj-i-kul/")
    )

    fun getFilteredArticles(preferences: UserPreferences): List<VacationArticle> {
        return articles.filter { article ->
            article.title.contains(preferences.question1, ignoreCase = true) ||
                    article.title.contains(preferences.question2, ignoreCase = true) ||
                    article.title.contains(preferences.question3, ignoreCase = true) ||
                    article.title.contains(preferences.question4, ignoreCase = true) ||
                    article.title.contains(preferences.question5, ignoreCase = true)
        }
    }
    fun getArticleByIndex(index: Int): VacationArticle {
        return articles[index % articles.size] // Используем % для циклического доступа к статьям
    }
    fun getArticlesCount(): Int {
        return articles.size
    }
}
