package y2k.example.litho

import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.URL

/**
 * Created by y2k on 08/07/2017.
 **/

class Tests {

    @Test
    fun `parse subscription is success`() {
        val html = Tests::class.java.getResource("blogs.html").readText()
        val actual = Parser.parserSubscriptions(html)

        assertEquals(
            URL("https://blog.jetbrains.com/idea/feed/"),
            actual.value[0].url)
    }

    @Test
    fun `parser dotnet blog`() {
        val xml = Tests::class.java.getResource("dotnet.xml").readText()
        val actual = Parser.parseEntities(xml)
        assertEquals(
            URL("https://blog.jetbrains.com/dotnet/2017/06/29/rider-eap-24-includes-performance-fixes-f-interactive/"),
            actual.value[0].url)

        assertEquals(
            Image(
                URL("https://d3nmt5vlzunoa1.cloudfront.net/dotnet/files/2017/06/fsharp_interactive_and_editor.png"),
                width = 701,
                height = 495),
            actual.value[0].image)
    }

    @Test
    fun `parser idea blog`() {
        val xml = Tests::class.java.getResource("idea.xml").readText()
        val actual = Parser.parseEntities(xml)
        assertEquals(
            URL("http://feedproxy.google.com/~r/jetbrains_intellijidea/~3/NZSQpe1-ZGU/"),
            actual.value[0].url)
    }
}