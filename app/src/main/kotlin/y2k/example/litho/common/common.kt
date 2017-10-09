package y2k.example.litho.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import kotlinx.coroutines.experimental.asCoroutineDispatcher
import kotlinx.coroutines.experimental.run
import java.io.Serializable

/**
 * Created by y2k on 07/07/2017.
 **/

inline fun <reified T : Activity> Context.startActivity(item: Serializable) {
    Intent(this, T::class.java)
        .putExtra("data", item)
        .let { startActivity(it) }
}

private val htmlRegex = Regex("&#(\\d+);")

fun String.unescapeHtml(): String =
    replace(htmlRegex) {
        it.groupValues[1].toInt().toChar().toString()
    }

suspend inline fun <T> task(crossinline action: () -> T): T =
    run(AsyncTask.THREAD_POOL_EXECUTOR.asCoroutineDispatcher()) { action() }