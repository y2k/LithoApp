package y2k.example.litho

import android.text.Html
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.Serializable
import java.net.URL

/**
 * Created by y2k on 07/07/2017.
 **/

typealias Entities = List<Entity>
typealias Subscriptions = List<Subscription>
data class Entity(val title: String, val description: CharSequence, val url: URL, val image: Image?)
data class Subscription(val title: String, val url: URL, val image: String) : Serializable
data class Image(val url: URL, val width: Int, val height: Int)

object Parser {

    fun parseEntities(rss: String): Entities =
        Jsoup.parse(rss)
            .select("item")
            .map { node ->
                Entity(
                    title = node.select("title").text(),
                    description = node.extractDescription(),
                    url = node.select("link").first().nextSibling().toString().let(::URL),
                    image = node.extractImage())
            }

    private fun Element.extractDescription(): CharSequence =
        select("description").text()
            .unescapeHtml()
            .let(Html::fromHtml)
            .trim()

    private fun Element.extractImage(): Image? =
        "<img src=\"([^\"]+)\" alt=\"[^\"]+\" width=\"(\\d+)\" height=\"(\\d+)".toRegex()
            .find(text())?.groupValues
            ?.let { Image(url = URL(it[1]), width = it[2].toInt(), height = it[3].toInt()) }

    fun parserSubscriptions(html: String): Subscriptions =
        Jsoup.parse(html)
            .select("ul#menu-jetbrains-product-blogs a")
            .map { node ->
                Subscription(
                    title = node.text().replace(" Blog", ""),
                    url = node.extractRssUrl(),
                    image = "TODO")
            }

    private fun Element.extractRssUrl(): URL =
        absUrl("href")
            .replace("http:", "https:")
            .replace("/$".toRegex(), "")
            .let { it + "/feed/" }
            .let(::URL)
}

object Loader {

    suspend fun getSubscriptions(): Subscriptions =
        Net.readText(URL("https://blog.jetbrains.com/"))
            .let(Parser::parserSubscriptions)

    suspend fun getEntities(url: URL): Entities =
        Net.readText(url)
            .let(Parser::parseEntities)
}