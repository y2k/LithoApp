package y2k.example.litho

import android.text.Html
import org.jsoup.Jsoup
import java.io.Serializable
import java.net.URL

/**
 * Created by y2k on 07/07/2017.
 **/

typealias Entities = List<Entity>
typealias Subscriptions = List<RssSubscription>
data class Entity(val title: String, val description: CharSequence, val url: URL, val image: Image?)
data class RssSubscription(val title: String, val url: URL, val image: String) : Serializable
data class Image(val url: URL, val width: Int, val height: Int)

object Parser {

    fun parseEntities(rss: String): Entities =
        Jsoup.parse(rss)
            .select("item")
            .map {
                val desc = it.select("description").text()
                    .unescapeHtml()
                    .let { Html.fromHtml(it) }
                    .trim()

                val content = it.text()
                val image = "<img src=\"([^\"]+)\" alt=\"[^\"]+\" width=\"(\\d+)\" height=\"(\\d+)".toRegex()
                    .find(content)?.groupValues
                    ?.let { Image(URL(it[1]), it[2].toInt(), it[3].toInt()) }

                Entity(
                    title = it.select("title").text(),
                    description = desc,
                    url = it.select("link").first().nextSibling().toString().let(::URL),
                    image = image)
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