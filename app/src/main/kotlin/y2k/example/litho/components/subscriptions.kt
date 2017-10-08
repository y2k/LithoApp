package y2k.example.litho.components

import android.text.Layout
import com.facebook.litho.*
import com.facebook.litho.annotations.*
import com.facebook.litho.widget.*
import com.facebook.yoga.YogaEdge
import y2k.example.litho.*
import y2k.example.litho.R
import y2k.example.litho.SubscriptionState.DefaultState
import y2k.example.litho.Loader as L
import y2k.example.litho.PersistenceStorage as P

/**
 * Created by y2k on 06/07/2017.
 **/

object Domain2 {
    suspend fun loadFromCache(): Subscriptions = P.load(Subscriptions())
    suspend fun loadFromWeb(): Subscriptions = L.getSubscriptions()
}

interface Cmd<out T> {

    suspend fun handle(): T?

    companion object {
        fun <T> none(): Cmd<T> =
            object : Cmd<T> {
                suspend override fun handle(): T? = null
            }

        fun <R, T> fromSuspend(f: suspend () -> R, fOk: (R) -> T): Cmd<T> =
            object : Cmd<T> {
                suspend override fun handle(): T = fOk(f())
            }

        fun <R, T> fromSuspend(f: suspend () -> R, fOk: (R) -> T, fError: () -> T): Cmd<T> =
            object : Cmd<T> {
                suspend override fun handle(): T =
                    try {
                        fOk(f())
                    } catch (e: Exception) {
                        fError()
                    }
            }
    }
}

sealed class Msg
class LoadedFromCache(val value: Subscriptions) : Msg()
class LoadedFromWeb(val value: Subscriptions) : Msg()
class LoadedFromWebError : Msg()

data class Model(val items: List<Subscription>, val error: Boolean, val loading: Boolean)

class MainScreen(private val c: ComponentContext) {

    fun init(): Pair<Model, Cmd<Msg>> =
        Model(items = emptyList(), error = false, loading = true) to
            Cmd.fromSuspend({ Domain2.loadFromCache() }, ::LoadedFromCache)

    fun update(model: Model, msg: Msg): Pair<Model, Cmd<Msg>> = when (msg) {
        is LoadedFromCache ->
            model.copy(items = msg.value.value) to
                Cmd.fromSuspend({ Domain2.loadFromWeb() }, ::LoadedFromWeb, ::LoadedFromWebError)
        is LoadedFromWeb ->
            model.copy(items = msg.value.value, loading = false) to Cmd.none()
        is LoadedFromWebError ->
            model.copy(error = true, loading = false) to Cmd.none()
    }

    fun view(model: Model): ComponentLayout =
        when {
            model.loading -> viewWebLoading(model.items)
            model.error -> viewError(model.items)
            else -> viewLoaded(model.items)
        }

    private fun viewWebLoading(items: List<Subscription>) =
        Column.create(c)
            .child(SubscriptionsList.create(c).items(items))
            .child(c.preloadIndicator())
            .build()

    private fun viewLoaded(items: List<Subscription>) =
        SubscriptionsList.create(c).items(items).buildWithLayout()

    private fun viewError(items: List<Subscription>) =
        Column.create(c)
            .child(SubscriptionsList.create(c)
                .items(items)
                .withLayout().flexGrow(1f))
            .child(c.errorIndicator())
            .build()

}

@LayoutSpec
class MainPageSpec {

    companion object {

        @OnCreateInitialState
        @JvmStatic
        fun createInitialState(c: ComponentContext, state: StateValue<SubscriptionState>) = launch {
            //            state.set(SubscriptionState.LoadFromCache)
            //            L.getSubscriptionsFromCache().let { MainPage.reload(c, it) }
            //            L.getSubscriptionsFromWeb().let { MainPage.reload(c, it) }

            val screen = MainScreen(c)
            val (model, cmd) = screen.init()

            state.set(DefaultState(model))

            val msg = cmd.handle() ?: return@launch
            val (model2, cmd2) = screen.update(model, msg)
            MainPage.reload(c, DefaultState(model2))

            val msg2 = cmd2.handle() ?: return@launch
            val (model3, _) = screen.update(model, msg2)
            MainPage.reload(c, DefaultState(model3))
        }

        @OnCreateLayout
        @JvmStatic
        fun onCreateLayout(c: ComponentContext, @State state: SubscriptionState): ComponentLayout? = when (state) {
            is DefaultState -> MainScreen(c).view(state.model)
            else -> TODO("")
//            is SubscriptionState.LoadFromCache -> null
//            is SubscriptionState.LoadFromWeb -> loadFromWeb(c, state)
//            is SubscriptionState.FromWeb -> SubscriptionsList.create(c).items(state.subscriptions).buildWithLayout()
//            is SubscriptionState.WebError -> webError(c, state)
        }

//        private fun loadFromWeb(c: ComponentContext, state: SubscriptionState.LoadFromWeb): ComponentLayout =
//            Column.create(c)
//                .child(SubscriptionsList.create(c).items(state.preloaded))
//                .child(c.preloadIndicator())
//                .build()
//
//        private fun webError(c: ComponentContext, state: SubscriptionState.WebError) =
//            Column.create(c)
//                .child(SubscriptionsList.create(c)
//                    .items(state.preloaded)
//                    .withLayout().flexGrow(1f))
//                .child(c.errorIndicator())
//                .build()

        @OnUpdateState
        @JvmStatic
        fun reload(state: StateValue<SubscriptionState>, @Param newState: SubscriptionState) =
            state.set(newState)
    }
}

@LayoutSpec
class SubscriptionsListSpec {

    companion object {

        @JvmStatic
        @OnCreateLayout
        fun onCreateLayout(c: ComponentContext, @Prop items: List<Subscription>): ComponentLayout {
            val recyclerBinder = RecyclerBinder.Builder()
                .layoutInfo(GridLayoutInfo(c, 2))
                .build(c)

            items.forEachIndexed { i, x ->
                recyclerBinder.insertItemAt(i, ItemComponent.create(c)
                    .item(x)
                    .build())
            }

            return Recycler.create(c)
                .binder(recyclerBinder)
                .buildWithLayout()
        }
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