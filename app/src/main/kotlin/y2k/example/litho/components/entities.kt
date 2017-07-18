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
import y2k.example.litho.Loader as L

/**
 * Created by y2k on 07/07/2017.
 **/

@LayoutSpec
class RssListComponentSpec {

    companion object {

        @OnCreateInitialState @JvmStatic
        fun onCreateInitialState(c: ComponentContext, state: StateValue<EntitiesState>, @Prop subscription: Subscription) {
            launch {
                state.set(EntitiesState.LoadFromCache)
                L.getCachedEntities(subscription.url)
                    .let { EntitiesState.LoadFromWeb(it.value) }
                    .let { RssListComponent.updateState(c, it) }
                L.getEntities_(subscription.url)
                    .let {
                        when (it) {
                            is Ok<Entities> -> EntitiesState.FromWeb(it.value.value)
                            is Error -> EntitiesState.WebError(L.getCachedEntities(subscription.url).value)
                        }
                    }
                    .let { RssListComponent.updateState(c, it) }
            }
        }

        @OnCreateLayout @JvmStatic
        fun onCreateLayout(c: ComponentContext, @State state: EntitiesState): ComponentLayout? = when (state) {
            EntitiesState.LoadFromCache -> null
            is EntitiesState.LoadFromWeb ->
                Column.create(c)
                    .child(c.listOfEntities(state.preloaded))
                    .child(c.preloadIndicator())
                    .build()
            is EntitiesState.FromWeb -> c.listOfEntities(state.entities).buildWithLayout()
            is EntitiesState.WebError ->
                Column.create(c)
                    .child(c.listOfEntities(state.preloaded))
                    .child(c.errorIndicator())
                    .build()
        }

        private fun ComponentContext.listOfEntities(items: List<Entity>): Recycler.Builder {
            val recyclerBinder = RecyclerBinder(this)
            items.forEachIndexed { i, x ->
                recyclerBinder.insertItemAt(i,
                    EntityComponent.create(this).item(x).build())
            }
            return Recycler.create(this)
                .binder(recyclerBinder)
        }

        @OnUpdateState @JvmStatic
        fun updateState(state: StateValue<EntitiesState>, @Param newState: EntitiesState) = state.set(newState)
    }
}

sealed class EntitiesState {
    object LoadFromCache : EntitiesState()
    class LoadFromWeb(val preloaded: List<Entity>) : EntitiesState()
    class FromWeb(val entities: List<Entity>) : EntitiesState()
    class WebError(val preloaded: List<Entity>) : EntitiesState()
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