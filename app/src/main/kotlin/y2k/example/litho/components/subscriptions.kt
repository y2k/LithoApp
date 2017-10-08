package y2k.example.litho.components

import android.text.Layout
import com.facebook.litho.*
import com.facebook.litho.annotations.*
import com.facebook.litho.widget.*
import com.facebook.yoga.YogaEdge
import y2k.example.litho.*
import y2k.example.litho.R
import y2k.example.litho.components.MainScreen.Msg.*

/**
 * Created by y2k on 06/07/2017.
 **/

class MainScreen(private val context: ComponentContext) {
    sealed class Msg {
        class LoadedFromCache(val value: Subscriptions) : Msg()
        class LoadedFromWeb(val value: Subscriptions) : Msg()
        class LoadedFromWebError : Msg()
    }

    data class Model(
        val error: Boolean,
        val loading: Boolean,
        val recyclerBinder: RecyclerBinder,
        val cached: List<Subscription>)

    fun init(): Pair<Model, Cmd<Msg>> {
        val binder = RecyclerBinder.Builder()
            .layoutInfo(GridLayoutInfo(context, 2))
            .build(context)
        return Model(false, true, binder, emptyList()) to
            Cmd.fromSuspend({ Domain2.loadFromCache() }, ::LoadedFromCache)
    }

    fun update(model: Model, msg: Msg): Pair<Model, Cmd<Msg>> = when (msg) {
        is LoadedFromCache ->
            model.updateItems(msg.value.value) to
                Cmd.fromSuspend({ Domain2.loadFromWeb() }, ::LoadedFromWeb, ::LoadedFromWebError)
        is LoadedFromWeb ->
            model.updateItems(msg.value.value).copy(loading = false) to Cmd.none()
        is LoadedFromWebError ->
            model.copy(error = true, loading = false) to Cmd.none()
    }

    private fun Model.updateItems(newItems: List<Subscription>): Model {
        recyclerBinder.applyDiff(cached, newItems,
            { ItemComponent.create(context).item(it).build() }, { l, r -> l.url == r.url })
        return copy(cached = newItems)
    }

    fun view(model: Model): ComponentLayout = when {
        model.loading -> viewWebLoading(model.recyclerBinder)
        model.error -> viewError(model.recyclerBinder)
        else -> viewLoaded(model.recyclerBinder)
    }

    private fun viewWebLoading(items: RecyclerBinder): ComponentLayout =
        Column.create(context)
            .child(Recycler.create(context).binder(items))
            .child(context.preloadIndicator())
            .build()

    private fun viewLoaded(items: RecyclerBinder): ComponentLayout =
        Recycler.create(context).binder(items).buildWithLayout()

    private fun viewError(items: RecyclerBinder): ComponentLayout =
        Column.create(context)
            .child(Recycler.create(context)
                .binder(items)
                .withLayout().flexGrow(1f))
            .child(context.errorIndicator())
            .build()
}

@LayoutSpec
class MainPageSpec {

    companion object {

        @OnCreateInitialState
        @JvmStatic
        fun createInitialState(c: ComponentContext, state: StateValue<SubscriptionState>) {
            val screen = MainScreen(c)
            state.set(DefaultState(screen.init().first))
            Elmish.handle(screen::init, screen::update, { MainPage.reload(c, DefaultState(it)) })
        }

        @OnCreateLayout
        @JvmStatic
        fun onCreateLayout(c: ComponentContext, @State state: SubscriptionState): ComponentLayout? = when (state) {
            is DefaultState -> MainScreen(c).view(state.model)
        }

        @OnUpdateState
        @JvmStatic
        fun reload(state: StateValue<SubscriptionState>, @Param newState: SubscriptionState) =
            state.set(newState)
    }
}

@LayoutSpec
class ItemComponentSpec {

    companion object {

        @JvmStatic
        @OnCreateLayout
        fun onCreateLayout(c: ComponentContext, @Prop item: Subscription): ComponentLayout =
            Column.create(c)
                .heightDip(200)
                .paddingDip(YogaEdge.ALL, 4)
                .backgroundRes(R.drawable.sub_item_bg)
                .child(Text.create(c)
                    .textAlignment(Layout.Alignment.ALIGN_CENTER)
                    .verticalGravity(VerticalGravity.CENTER)
                    .text(item.title)
                    .textSizeSp(35f)
                    .withLayout()
                    .flexGrow(1f))
                .clickHandler(ItemComponent.onItemClicked(c, item))
                .build()

        @OnEvent(ClickEvent::class)
        @JvmStatic
        fun onItemClicked(c: ComponentContext, @Param item: Subscription) =
            c.startActivity<EntitiesActivity>(item)
    }
}