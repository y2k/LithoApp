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
        fun reload(state: StateValue<Subscriptions>, @Param newState: Subscriptions) {
            state.set(newState)
        }

        @OnCreateInitialState @JvmStatic
        fun createInitialState(c: ComponentContext, state: StateValue<Subscriptions>) = launch {
            state.set(Subscriptions())

            L.getSubscriptionsCached()
                .let { MainComponent.reload(c, it) }
            L.getSubscriptions()
                .let { MainComponent.reload(c, it) }
        }

        @OnCreateLayout @JvmStatic
        fun onCreateLayout(c: ComponentContext, @State state: Subscriptions): ComponentLayout {
            return when (state.value.isEmpty()) {
                true -> PlaceholderComponent.create(c).buildWithLayout()
                false -> {
                    val recyclerBinder = RecyclerBinder(
                        c, RecyclerBinder.DEFAULT_RANGE_RATIO, GridLayoutInfo(c, 2))

                    state.value.forEachIndexed { i, x ->
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