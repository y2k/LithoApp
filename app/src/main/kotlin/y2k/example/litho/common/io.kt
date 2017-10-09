package y2k.example.litho.common

import android.content.SharedPreferences
import android.util.Base64
import okhttp3.OkHttpClient
import okhttp3.Request
import y2k.example.litho.MainActivity
import java.io.*
import java.net.URL

object PersistenceStorage {

    fun toKey(url: URL) = url.toString()

    @Suppress("UNCHECKED_CAST")
    suspend fun <T : Serializable> load(def: T, key: String? = null): T = task {
        getPrefs()
            .getString(key ?: def.javaClass.name, null)
            ?.let { Base64.decode(it, 0) }
            ?.let(::ByteArrayInputStream)
            ?.let(::ObjectInputStream)
            ?.let { it.readObject() as T } ?: def
    }

    suspend fun <T : Serializable> save(value: T, key: String? = null) = task {
        val memStream = ByteArrayOutputStream()
        val objStream = ObjectOutputStream(memStream)
        objStream.writeObject(value)
        objStream.close()

        memStream.toByteArray()
            .let { Base64.encodeToString(it, 0) }
            .let { getPrefs().edit().putString(key ?: value.javaClass.name, it).apply() }
    }

    private fun getPrefs(): SharedPreferences =
        MainActivity.App.app.getSharedPreferences("prefs", 0)
}

object Net {

    private val client = OkHttpClient()

    suspend fun readText(url: URL): String = task {
        Request.Builder()
            .url(url).build()
            .let(client::newCall)
            .execute()
            .also { if (!it.isSuccessful) throw IOException("Unexpected code " + it) }
            .body()!!.string()
    }
}