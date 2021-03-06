package y2k.example.litho.components

import android.support.customtabs.CustomTabsIntent
import com.facebook.litho.ComponentLayout.ContainerBuilder
import com.facebook.yoga.YogaEdge
import y2k.example.litho.*
import y2k.example.litho.Status.*
import y2k.example.litho.common.Log.log
import y2k.example.litho.common.toUri
import y2k.example.litho.components.EntitiesScreen.Model
import y2k.example.litho.components.EntitiesScreen.Msg
import y2k.example.litho.components.EntitiesScreen.Msg.*
import y2k.litho.elmish.experimental.*
import y2k.litho.elmish.experimental.Views.column
import java.net.URL
import y2k.example.litho.Loader as L

/**
 * Created by y2k on 07/07/2017.
 **/

class EntitiesScreen(private val sub: Subscription) : ElmFunctions<Model, Msg> {

    data class Model(
        val status: Status,
        val binder: ContextualRecyclerBinder<Entity>,
        val cached: List<Entity>)

    sealed class Msg {
        class LoadedFromCache(val items: Entities) : Msg()
        class FromWebMsg(val items: Entities) : Msg()
        class ErrorMsg(val e: Exception) : Msg()
        class Open(val url: URL) : Msg()
    }

    override fun init(): Pair<Model, Cmd<Msg>> {
        val binder = ContextualRecyclerBinder(
            ::viewItem, ::fastCompare)

        return Model(InProgress, binder, emptyList()) to
            Cmd.fromSuspend({ L.getCachedEntities(sub.url) }, ::LoadedFromCache, ::ErrorMsg)
    }

    override fun update(model: Model, msg: Msg): Pair<Model, Cmd<Msg>> = when (msg) {
        is LoadedFromCache ->
            model.copy(binder = model.binder.copy(msg.items.value)) to
                Cmd.fromSuspend({ L.getEntities(sub.url) }, ::FromWebMsg, ::ErrorMsg)
        is FromWebMsg ->
            model.copy(
                status = Success,
                binder = model.binder.copy(msg.items.value)) to
                Cmd.none()
        is ErrorMsg ->
            log(msg.e, model.copy(status = Failed)) to Cmd.none()
        is Open ->
            model to Cmd.fromContext { context ->
                CustomTabsIntent.Builder()
                    .build()
                    .launchUrl(context, msg.url.toUri())
            }
    }

    override fun ContainerBuilder.view(model: Model) =
        column {
            when (model.status) {
                InProgress -> viewCached(model)
                Success -> viewFromWeb(model)
                Failed -> viewError(model)
            }
        }

    private fun ContainerBuilder.viewCached(model: Model) =
        column {
            recyclerView {
                binder(model.binder)
            }
            preloadIndicator()
        }

    private fun ContainerBuilder.viewFromWeb(model: Model) =
        recyclerView {
            binder(model.binder)
        }

    private fun ContainerBuilder.viewError(model: Model) =
        column {
            recyclerView {
                binder(model.binder)
            }
            errorIndicator()
        }

    private fun viewItem(item: Entity) =
        column {
            paddingDip(YogaEdge.ALL, 16f)
            backgroundRes(R.drawable.sub_item_bg)

            onClick(Open(item.url))

            text {
                text(item.title)
                textSizeSp(35f)
            }
            if (item.image != null)
                fresco {
                    frescoController {
                        setUri(item.image.url.toString())
                    }
                    aspectRatio(item.image.width.toFloat() / item.image.height)
                }
            text {
                text(item.description)
                textSizeSp(20f)
            }
        }
}