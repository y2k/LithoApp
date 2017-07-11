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

/**
 * Created by y2k on 07/07/2017.
 **/

@LayoutSpec
class RssListComponentSpec {

    companion object {

        @OnCreateInitialState @JvmStatic
        fun onCreateInitialState(c: ComponentContext, state: StateValue<Entities>, @Prop subscription: Subscription) {
            launch {
                state.set(emptyList())
                Loader.getEntities(subscription.url)
                    .let { RssListComponent.updateState(c, it) }
            }
        }

        @OnUpdateState @JvmStatic
        fun updateState(state: StateValue<Entities>, @Param newState: Entities) = state.set(newState)

        @OnCreateLayout @JvmStatic
        fun onCreateLayout(c: ComponentContext, @State state: Entities): ComponentLayout {
            return when (state.isEmpty()) {
                true -> PlaceholderComponent.create(c).buildWithLayout()
                false -> {
                    val recyclerBinder = RecyclerBinder(c)
                    state.forEachIndexed { i, x ->
                        recyclerBinder.insertItemAt(i,
                            EntityComponent.create(c).item(x).build())
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
class EntityComponentSpec {

    companion object {

        @OnCreateLayout @JvmStatic
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

        @OnEvent(ClickEvent::class) @JvmStatic
        fun onItemClicked(c: ComponentContext, @Param item: Entity) {
            CustomTabsIntent.Builder()
                .build()
                .launchUrl(c, Uri.parse("" + item.url))
        }
    }
}