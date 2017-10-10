package y2k.example.litho.components

import android.net.Uri
import android.support.customtabs.CustomTabsIntent
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.litho.*
import com.facebook.litho.annotations.*
import com.facebook.litho.fresco.FrescoImage
import com.facebook.litho.widget.Recycler
import com.facebook.litho.widget.RecyclerBinder
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaEdge
import y2k.example.litho.*
import y2k.example.litho.R
import y2k.example.litho.Status.*
import y2k.example.litho.common.Cmd
import y2k.example.litho.common.Elmish
import y2k.example.litho.common.applyDiff
import y2k.example.litho.components.RssScreen.Msg.*
import java.net.URL
import y2k.example.litho.Loader as L

/**
 * Created by y2k on 07/07/2017.
 **/

class RssScreen(private val context: ComponentContext) {
    data class Model(
        val url: URL,
        val status: Status,
        val binder: RecyclerBinder,
        val cached: List<Entity>)

    sealed class Msg {
        class LoadedFromCache(val items: Entities) : Msg()
        class FromWebMsg(val items: Entities) : Msg()
        class ErrorMsg : Msg()
    }

    fun init(url: URL): Pair<Model, Cmd<Msg>> {
        val binder = RecyclerBinder.Builder().build(context)
        return Model(url, InProgress, binder, emptyList()) to
            Cmd.fromSuspend({ L.getCachedEntities(url) }, ::LoadedFromCache)
    }

    fun update(model: Model, msg: Msg): Pair<Model, Cmd<Msg>> = when (msg) {
        is LoadedFromCache ->
            model.update(msg.items.value) to
                Cmd.fromSuspend({ L.getEntities(model.url) }, ::FromWebMsg, ::ErrorMsg)
        is RssScreen.Msg.FromWebMsg ->
            model.update(msg.items.value).copy(status = Success) to Cmd.none()
        is RssScreen.Msg.ErrorMsg ->
            model.copy(status = Failed) to Cmd.none()
    }

    private fun RssScreen.Model.update(newItems: List<Entity>): RssScreen.Model {
        binder.applyDiff(cached, newItems,
            { EntityComponent.create(context).item(it).build() }, { l, r -> l.url == r.url })
        return copy(cached = newItems)
    }

    fun view(model: Model): ComponentLayout = when (model.status) {
        InProgress -> viewCached(model.binder)
        Success -> viewFromWeb(model.binder)
        Failed -> viewError(model.binder)
    }

    private fun viewCached(items: RecyclerBinder): ComponentLayout =
        Column.create(context)
            .child(Recycler.create(context).binder(items))
            .child(context.preloadIndicator())
            .build()

    private fun viewFromWeb(items: RecyclerBinder): ComponentLayout =
        Recycler.create(context).binder(items).buildWithLayout()

    private fun viewError(items: RecyclerBinder): ComponentLayout =
        Column.create(context)
            .child(Recycler.create(context).binder(items))
            .child(context.errorIndicator())
            .build()
}

@LayoutSpec
class RssListComponentSpec {
    companion object {

        @OnCreateInitialState
        @JvmStatic
        fun onCreateInitialState(c: ComponentContext, state: StateValue<RssScreen.Model>, @Prop subscription: Subscription) {
            val screen = RssScreen(c)
            state.set(screen.init(subscription.url).first)
            Elmish.handle(
                { screen.init(subscription.url) }, screen::update,
                { RssListComponent.updateState(c, it) })
        }

        @OnCreateLayout
        @JvmStatic
        fun onCreateLayout(c: ComponentContext, @State state: RssScreen.Model): ComponentLayout =
            RssScreen(c).view(state)

        @OnUpdateState
        @JvmStatic
        fun updateState(state: StateValue<RssScreen.Model>, @Param newState: RssScreen.Model) =
            state.set(newState)
    }
}

@LayoutSpec
class EntityComponentSpec {

    companion object {

        @OnCreateLayout
        @JvmStatic
        fun onCreateLayout(c: ComponentContext, @Prop item: Entity): ComponentLayout {
            val column = Column.create(c)
                .paddingDip(YogaEdge.ALL, 16)
                .backgroundRes(R.drawable.sub_item_bg)
                .child(Text.create(c)
                    .text(item.title)
                    .textSizeSp(35f))
            if (item.image != null) {
                column
                    .child(FrescoImage.create(c)
                        .controller(Fresco.newDraweeControllerBuilder()
                            .setUri(item.image.url.toString())
                            .build())
                        .aspectRatio(item.image.width.toFloat() / item.image.height)
                        .buildWithLayout())
            }
            column
                .child(Text.create(c)
                    .text(item.description)
                    .textSizeSp(20f))
                .clickHandler(EntityComponent.onItemClicked(c, item))

            return column.build()
        }

        @OnEvent(ClickEvent::class)
        @JvmStatic
        fun onItemClicked(c: ComponentContext, @Param item: Entity) {
            CustomTabsIntent.Builder()
                .build()
                .launchUrl(c, Uri.parse("" + item.url))
        }
    }
}