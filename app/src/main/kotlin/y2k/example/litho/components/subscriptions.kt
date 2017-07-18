package y2k.example.litho.components

import android.content.Intent
import android.text.Layout
import com.facebook.litho.*
import com.facebook.litho.annotations.*
import com.facebook.litho.widget.*
import com.facebook.yoga.YogaEdge
import y2k.example.litho.*
import y2k.example.litho.R
import y2k.example.litho.Loader as L

/**
 * Created by y2k on 06/07/2017.
 **/

@LayoutSpec
class MainComponentSpec {

    companion object {

        @OnUpdateState @JvmStatic
        fun reload(state: StateValue<SubscriptionState>, @Param newState: SubscriptionState) = state.set(newState)

        @OnCreateInitialState @JvmStatic
        fun createInitialState(c: ComponentContext, state: StateValue<SubscriptionState>) = launch {
            state.set(SubscriptionState.LoadFromCache)
            L.getSubscriptionsCached()
                .let { MainComponent.reload(c, SubscriptionState.LoadFromWeb(it.value)) }
            L.getSubscriptions_()
                .let {
                    when (it) {
                        is Ok<Subscriptions> -> SubscriptionState.FromWeb(it.value.value)
                        is Error -> SubscriptionState.WebError(L.getSubscriptionsCached().value)
                    }
                }
                .let { MainComponent.reload(c, it) }
        }

        @OnCreateLayout @JvmStatic
        fun onCreateLayout(c: ComponentContext, @State state: SubscriptionState): ComponentLayout? = when (state) {
            is SubscriptionState.LoadFromCache -> null
            is SubscriptionState.LoadFromWeb -> loadFromWeb(c, state)
            is SubscriptionState.FromWeb -> SubscriptionsList.create(c).items(state.subscriptions).buildWithLayout()
            is SubscriptionState.WebError -> webError(c, state)
        }

        private fun loadFromWeb(c: ComponentContext, state: SubscriptionState.LoadFromWeb): ComponentLayout =
            Column.create(c)
                .child(SubscriptionsList.create(c).items(state.preloaded))
                .child(c.preloadIndicator())
                .build()

        private fun webError(c: ComponentContext, state: SubscriptionState.WebError) =
            Column.create(c)
                .child(SubscriptionsList.create(c)
                    .items(state.preloaded)
                    .withLayout().flexGrow(1f))
                .child(c.errorIndicator())
                .build()
    }
}

sealed class SubscriptionState {
    object LoadFromCache : SubscriptionState()
    class LoadFromWeb(val preloaded: List<Subscription>) : SubscriptionState()
    class FromWeb(val subscriptions: List<Subscription>) : SubscriptionState()
    class WebError(val preloaded: List<Subscription>) : SubscriptionState()
}

@LayoutSpec
class SubscriptionsListSpec {

    companion object {

        @JvmStatic @OnCreateLayout
        fun onCreateLayout(c: ComponentContext, @Prop items: List<Subscription>): ComponentLayout {
            val recyclerBinder = RecyclerBinder(
                c, RecyclerBinder.DEFAULT_RANGE_RATIO, GridLayoutInfo(c, 2))

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

        @JvmStatic @OnCreateLayout
        fun onCreateLayout(c: ComponentContext, @Prop item: Subscription): ComponentLayout {
            return Column.create(c)
                .heightDip(200)
                .paddingDip(YogaEdge.ALL, 4)
                .backgroundRes(R.drawable.sub_item_bg)
                .child(
                    Text.create(c)
                        .textAlignment(Layout.Alignment.ALIGN_CENTER)
                        .verticalGravity(VerticalGravity.CENTER)
                        .text(item.title)
                        .textSizeSp(35f)
                        .withLayout()
                        .flexGrow(1f))
                .clickHandler(ItemComponent.onItemClicked(c, item))
                .build()
        }

        @OnEvent(ClickEvent::class) @JvmStatic
        fun onItemClicked(c: ComponentContext, @Param item: Subscription) {
            Intent(c, EntitiesActivity::class.java)
                .putExtra("data", item)
                .let { c.startActivity(it) }
        }
    }
}