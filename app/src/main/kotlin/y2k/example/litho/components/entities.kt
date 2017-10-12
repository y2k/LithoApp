package y2k.example.litho.components

import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.yoga.YogaEdge
import y2k.example.litho.*
import y2k.example.litho.R
import y2k.example.litho.Status.*
import y2k.example.litho.components.EntitiesScreen.Model
import y2k.example.litho.components.EntitiesScreen.Msg
import y2k.example.litho.components.EntitiesScreen.Msg.*
import y2k.litho.elmish.*
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
        class ErrorMsg : Msg()
        class Open : Msg()
    }

    override fun init(): Pair<Model, Cmd<Msg>> {
        val binder = ContextualRecyclerBinder(
            {}, this::viewItem, ::fastCompare)

        return Model(InProgress, binder, emptyList()) to
            Cmd.fromSuspend({ L.getCachedEntities(sub.url) }, ::LoadedFromCache)
    }

    override fun update(model: Model, msg: Msg): Pair<Model, Cmd<Msg>> = when (msg) {
        is LoadedFromCache ->
            model.copy(
                binder = model.binder.copy(msg.items.value)) to
                Cmd.fromSuspend({ L.getEntities(sub.url) }, ::FromWebMsg, ::ErrorMsg)
        is FromWebMsg ->
            model.copy(
                status = Success,
                binder = model.binder.copy(msg.items.value)) to
                Cmd.none()
        is ErrorMsg ->
            model.copy(status = Failed) to Cmd.none()
        is Open -> TODO()
    }

    override fun view(model: Model) = when (model.status) {
        InProgress -> viewCached(model)
        Success -> viewFromWeb(model)
        Failed -> viewError(model)
    }

    private fun viewCached(model: Model) =
        column {
            children(
                recyclerView_ {
                    binder_(model.binder)
                },
                preloadIndicator())
        }

    private fun viewFromWeb(model: Model) =
        recyclerView_ {
            binder_(model.binder)
        }

    private fun viewError(model: Model) =
        column {
            children(
                recyclerView_ {
                    binder_(model.binder)
                },
                errorIndicator())
        }

    private fun viewItem(item: Entity) =
        column {
            paddingDip(YogaEdge.ALL, 16)
            backgroundRes(R.drawable.sub_item_bg)

            child(text {
                text(item.title)
                textSizeSp(35f)
            })

            if (item.image != null) {
                child(
                    fresco {
                        controller(Fresco.newDraweeControllerBuilder()
                            .setUri(item.image.url.toString())
                            .build())
                        aspectRatio(item.image.width.toFloat() / item.image.height)
                    })
            }

            child(text {
                text(item.description)
                textSizeSp(20f)
            })
//                .clickHandler(EntityComponent.onItemClicked(c, item))
//fun onItemClicked(c: ComponentContext, @Param item: Entity) {
//    CustomTabsIntent.Builder()
//        .build()
//        .launchUrl(c, Uri.parse("" + item.url))
//}
        }
}
