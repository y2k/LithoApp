package y2k.example.litho.components

import android.text.Layout
import com.facebook.litho.widget.GridLayoutInfo
import com.facebook.litho.widget.VerticalGravity
import com.facebook.yoga.YogaEdge
import y2k.example.litho.*
import y2k.example.litho.Status.*
import y2k.example.litho.common.Error
import y2k.example.litho.common.Ok
import y2k.example.litho.common.Result
import y2k.example.litho.common.startActivityWithData
import y2k.example.litho.components.SubscriptionsScreen.Model
import y2k.example.litho.components.SubscriptionsScreen.Msg
import y2k.example.litho.components.SubscriptionsScreen.Msg.*
import y2k.litho.elmish.experimental.*
import y2k.litho.elmish.experimental.Views.column
import y2k.example.litho.Loader as L

object SubscriptionsScreen : ElmFunctions<Model, Msg> {
    data class Model(
        val status: Status,
        val binder: ContextualRecyclerBinder<Subscription>)

    sealed class Msg {
        class FromCacheMsg(val value: Subscriptions) : Msg()
        class FromWebMsg(val value: Result<Subscriptions, Exception>) : Msg()
        class OpenMsg(val item: Subscription) : Msg()
    }

    override fun init(): Pair<Model, Cmd<Msg>> {
        val binder = ContextualRecyclerBinder(::viewItem, ::fastCompare) {
            layoutInfo(GridLayoutInfo(null, 2))
        }

        return Model(InProgress, binder) to
            Cmd.fromSuspend({ L.getCachedSubscriptions() }, ::FromCacheMsg)
    }

    override fun update(model: Model, msg: Msg): Pair<Model, Cmd<Msg>> = when (msg) {
        is OpenMsg -> model to Cmd.fromContext {
            startActivityWithData<EntitiesActivity>(msg.item)
        }
        is FromCacheMsg -> model to
            Cmd.fromSuspend({ L.getSubscriptionsResult() }, ::FromWebMsg)
        is FromWebMsg -> when (msg.value) {
            is Ok -> model.copy(
                status = Success,
                binder = model.binder.copy(msg.value.value.value)) to Cmd.none()
            is Error -> model.copy(status = Failed) to Cmd.none()
        }
    }

    override fun view(model: Model) =
        column {
            recyclerView {
                binder(model.binder)
            }

            if (model.status == InProgress)
                preloadIndicator()

            if (model.status == Failed)
                errorIndicator()
        }

    private fun viewItem(item: Subscription) =
        column {
            heightDip(200f)
            paddingDip(YogaEdge.ALL, 4f)
            backgroundRes(R.drawable.sub_item_bg)

            onClick(OpenMsg(item))

            text {
                textAlignment(Layout.Alignment.ALIGN_CENTER)
                verticalGravity(VerticalGravity.CENTER)
                text(item.title)
                textSizeSp(35f)
                flexGrow(1f)
            }
        }
}