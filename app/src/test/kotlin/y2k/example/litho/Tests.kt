package y2k.example.litho

import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.URL

/**
 * Created by y2k on 08/07/2017.
 **/

class Tests {

    @Test
    fun parser_dotnet_blog() {
        val xml = Tests::class.java.getResource("dotnet.xml").readText()
        val actual = Parser.parseEntities(xml)
        assertEquals(
            URL("https://blog.jetbrains.com/dotnet/2017/06/29/rider-eap-24-includes-performance-fixes-f-interactive/"),
            actual[0].url)
    }

    @Test
    fun parser_idea_blog() {
        val xml = Tests::class.java.getResource("idea.xml").readText()
        val actual = Parser.parseEntities(xml)
        assertEquals(
            URL("http://feedproxy.google.com/~r/jetbrains_intellijidea/~3/NZSQpe1-ZGU/"),
            actual[0].url)
    }
}