package y2k.example.litho

import org.jsoup.Jsoup
import java.net.URL

/**
 * Created by y2k on 07/07/2017.
 **/

typealias Entities = List<Entity>
typealias Subscriptions = List<RssSubscription>
data class Entity(val title: String, val description: String)
data class RssSubscription(val title: String, val url: String, val image: String)

object Parser {

    fun parseEntities(rss: String): Entities =
        Jsoup.parse(rss)
            .select("item")
            .map {
                Entity(
                    it.select("title").text(),
                    it.select("description").text().unescapeHtml())
            }

    fun parserSubscriptions(html: String): Subscriptions =
        Jsoup.parse(html)
            .select("ul#menu-jetbrains-product-blogs a")
            .map {
                RssSubscription(
                    title = it.text(),
                    url = it.absUrl("href"),
                    image = "TODO")
            }
            .map { it.copy(title = it.title.replace(" Blog", "")) }
}

object Loader {

    suspend fun getSubscriptions(): Subscriptions = task {
        URL("https://blog.jetbrains.com/")
            .readText()
            .let(Parser::parserSubscriptions)
    }

    suspend fun getEntities(url: String): Entities = task {
        URL(url)
            .readText()
            .let(Parser::parseEntities)
    }
}