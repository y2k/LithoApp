package y2k.example.litho

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.Serializable
import java.net.URL
import y2k.example.litho.PersistenceStorage as P

/**
 * Created by y2k on 07/07/2017.
 **/

sealed class SubscriptionState {
    object LoadFromCache : SubscriptionState()
    class LoadFromWeb(val preloaded: List<Subscription>) : SubscriptionState()
    class FromWeb(val subscriptions: List<Subscription>) : SubscriptionState()
    class WebError(val preloaded: List<Subscription>) : SubscriptionState()
    class DefaultState(val model: y2k.example.litho.components.Model) : SubscriptionState()
}

class Entities(val value: List<Entity> = emptyList()) : Serializable

class Subscriptions(val value: List<Subscription> = emptyList()) : Serializable
data class Entity(val title: String, val description: String, val url: URL, val image: Image?) : Serializable
data class Subscription(val title: String, val url: URL, val image: String) : Serializable
data class Image(val url: URL, val width: Int, val height: Int) : Serializable

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
            .let(::Entities)

    private fun Element.extractDescription(): String =
        select("description").text()
            .unescapeHtml()
            .let { Jsoup.parse(it).text() }

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
            .let(::Subscriptions)

    private fun Element.extractRssUrl(): URL =
        absUrl("href")
            .replace("http:", "https:")
            .replace("/$".toRegex(), "")
            .plus("/feed/")
            .let(::URL)
}

object Loader {

    suspend fun getSubscriptionsFromCache() =
        getSubscriptionsFromCache__()
            .let { SubscriptionState.LoadFromWeb(it.value) }

    suspend fun getSubscriptionsFromWeb(): SubscriptionState =
        getSubscriptions_()
            .let {
                when (it) {
                    is Ok<Subscriptions> -> SubscriptionState.FromWeb(it.value.value)
                    is Error -> SubscriptionState.WebError(Loader.getSubscriptionsFromCache__().value)
                }
            }

    private suspend fun getSubscriptionsFromCache__(): Subscriptions =
        P.load(Subscriptions())

    private suspend fun getSubscriptions_(): Result<Subscriptions> =
        try {
            Ok(getSubscriptions())
        } catch (e: Exception) {
            Error(e)
        }

    suspend fun getSubscriptions(): Subscriptions =
        Net.readText(URL("https://blog.jetbrains.com/"))
            .let(Parser::parserSubscriptions)
            .also { P.save(it) }

    suspend fun getCachedEntities(url: URL): Entities =
        P.load(Entities(), url.toString())

    suspend fun getEntities_(url: URL): Result<Entities> =
        try {
            Ok(getEntities(url))
        } catch (e: Exception) {
            Error(e)
        }

    private suspend fun getEntities(url: URL): Entities =
        Net.readText(url)
            .let(Parser::parseEntities)
            .also { P.save(it, url.toString()) }
}