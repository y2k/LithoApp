package y2k.example.litho

import org.jsoup.Jsoup
import java.io.Serializable
import java.net.URL

/**
 * Created by y2k on 07/07/2017.
 **/

typealias Entities = List<Entity>
typealias Subscriptions = List<RssSubscription>
data class Entity(val title: String, val description: String, val url: URL)
data class RssSubscription(val title: String, val url: URL, val image: String) : Serializable

object Parser {

    fun parseEntities(rss: String): Entities =
        Jsoup.parse(rss)
            .select("item")
            .map {
                Entity(
                    it.select("title").text(),
                    it.select("description").text().unescapeHtml(),
                    it.select("link").text().let(::URL))
            }

    fun parserSubscriptions(html: String): Subscriptions =
        Jsoup.parse(html)
            .select("ul#menu-jetbrains-product-blogs a")
            .map {
                RssSubscription(
                    title = it.text(),
                    url = it.absUrl("href").let(::URL),
                    image = "TODO")
            }
            .map {
                it.copy(
                    url = it.url.toString()
                        .replace("http:", "https:")
                        .replace("/$".toRegex(), "")
                        .let { it + "/feed/" }
                        .let(::URL),
                    title = it.title.replace(" Blog", ""))
            }
}

object Loader {

    suspend fun getSubscriptions(): Subscriptions =
        Net.readText(URL("https://blog.jetbrains.com/"))
            .let(Parser::parserSubscriptions)

    suspend fun getEntities(url: URL): Entities =
        Net.readText(url)
            .let(Parser::parseEntities)
}