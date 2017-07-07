package y2k.example.litho

import android.graphics.Color
import android.widget.Toast
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.litho.*
import com.facebook.litho.annotations.*
import com.facebook.litho.fresco.FrescoImage
import com.facebook.litho.widget.Progress
import com.facebook.litho.widget.Recycler
import com.facebook.litho.widget.RecyclerBinder
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaEdge

/**
 * Created by y2k on 07/07/2017.
 **/

@LayoutSpec
class RssListComponentSpec {

    companion object {

        @OnCreateInitialState @JvmStatic
        fun onCreateInitialState(c: ComponentContext, state: StateValue<Entities>, @Prop subscription: RssSubscription) {
            launch {
                state.set(emptyList())
                Loader
                    .getEntities(subscription.url)
                    .let { RssListComponent.updateState(c, it) }
            }
        }

        @OnUpdateState @JvmStatic
        fun updateState(state: StateValue<Entities>, @Param newState: Entities) = state.set(newState)

        @OnCreateLayout @JvmStatic
        fun onCreateLayout(c: ComponentContext, @State state: Entities): ComponentLayout {
            return when (state.isEmpty()) {
                true ->
                    Column.create(c)
                        .paddingDip(YogaEdge.ALL, 16)
                        .backgroundColor(Color.WHITE)
                        .child(Progress.create(c)
                            .color(Color.GRAY))
                        .build()
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
            return Column.create(c)
                .paddingDip(YogaEdge.ALL, 16)
                .backgroundColor(Color.WHITE)
                .child(Text.create(c)
                    .text(item.title)
                    .textSizeSp(35f))
                .child(FrescoImage.create(c)
                    .controller(Fresco.newDraweeControllerBuilder()
                        .setUri("http://img0.joyreactor.cc/pics/post/-3838584.jpeg")
                        .build())
                    .aspectRatio(1f)
                    .buildWithLayout())
                .child(Text.create(c)
                    .text(item.description)
                    .textSizeSp(20f))
//                .clickHandler(ItemComponent.onItemClicked(c, item))
                .build()
        }

        @OnEvent(ClickEvent::class) @JvmStatic
        fun onItemClicked(c: ComponentContext, @Param item: Entity) {
            Toast.makeText(c, "Clicked ($item)", Toast.LENGTH_SHORT).show()
        }
    }
}