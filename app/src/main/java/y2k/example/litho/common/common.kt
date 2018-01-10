package y2k.example.litho.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import kotlinx.coroutines.experimental.asCoroutineDispatcher
import kotlinx.coroutines.experimental.withContext
import y2k.example.litho.BuildConfig
import java.io.Serializable
import java.net.URL

/**
 * Created by y2k on 07/07/2017.
 **/

object Log {

    @Suppress("NOTHING_TO_INLINE")
    inline fun <T> log(e: Exception, x: T): T {
        if (BuildConfig.DEBUG) e.printStackTrace()
        return x
    }
}

fun URL.toUri(): Uri = Uri.parse("" + this)

inline fun <reified T : Activity> startActivityWithData(context: Context, item: Serializable) {
    Intent(context, T::class.java)
        .putExtra("data", item)
        .let { context.startActivity(it) }
}

private val htmlRegex = Regex("&#(\\d+);")

fun String.unescapeHtml(): String =
    replace(htmlRegex) {
        it.groupValues[1].toInt().toChar().toString()
    }

suspend inline fun <T> task(crossinline action: () -> T): T =
    withContext(AsyncTask.THREAD_POOL_EXECUTOR.asCoroutineDispatcher()) { action() }