package y2k.example.litho.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.support.v7.util.DiffUtil
import com.facebook.litho.Component
import com.facebook.litho.widget.ComponentRenderInfo
import com.facebook.litho.widget.RecyclerBinder
import com.facebook.litho.widget.RecyclerBinderUpdateCallback
import kotlinx.coroutines.experimental.asCoroutineDispatcher
import kotlinx.coroutines.experimental.run
import java.io.Serializable

/**
 * Created by y2k on 07/07/2017.
 **/

fun <T> RecyclerBinder.applyDiff(
    old: List<T>, newItems: List<T>,
    func: (T) -> Component<*>,
    compareIds: (T, T) -> Boolean) {
    val renderer = RecyclerBinderUpdateCallback.ComponentRenderer<T> { x, _ ->
        ComponentRenderInfo.create().component(func(x)).build()
    }
    val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
        override fun getOldListSize(): Int = old.size
        override fun getNewListSize(): Int = newItems.size
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            compareIds(old[oldItemPosition], newItems[newItemPosition])

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            old[oldItemPosition] == newItems[newItemPosition]
    })
    val callback = RecyclerBinderUpdateCallback.acquire(
        old.size, newItems, renderer, this)
    diffResult.dispatchUpdatesTo(callback)
    callback.applyChangeset()
    RecyclerBinderUpdateCallback.release(callback)
}

inline fun <reified T : Activity> Context.startActivityWithData(item: Serializable) {
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