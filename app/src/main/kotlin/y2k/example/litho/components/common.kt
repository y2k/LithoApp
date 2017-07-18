package y2k.example.litho.components

import android.graphics.Color
import com.facebook.litho.Column
import com.facebook.litho.ComponentContext
import com.facebook.litho.ComponentLayout
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.widget.Progress
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.facebook.yoga.YogaJustify
import com.facebook.yoga.YogaPositionType

/**
 * Created by y2k on 11/07/2017.
 **/

@LayoutSpec
class PlaceholderComponentSpec {

    companion object {

        @OnCreateLayout @JvmStatic
        fun onCreateLayout(c: ComponentContext): ComponentLayout =
            Column.create(c)
                .alignItems(YogaAlign.CENTER)
                .justifyContent(YogaJustify.CENTER)
                .child(Progress.create(c)
                    .color(Color.GRAY)
                    .withLayout()
                    .widthDip(50).heightDip(50))
                .build()
    }
}

fun ComponentContext.errorIndicator(): ComponentLayout.ContainerBuilder =
    Column.create(this)
        .backgroundColor(0xFF303030L.toInt())
        .paddingDip(YogaEdge.ALL, 4)
        .child(Text.create(this)
            .textSizeSp(24f)
            .textColor(Color.WHITE)
            .text("ERROR")
            .withLayout().alignSelf(YogaAlign.FLEX_END))

fun ComponentContext.preloadIndicator(): ComponentLayout.Builder =
    Progress.create(this)
        .color(Color.GRAY)
        .withLayout()
        .positionType(YogaPositionType.ABSOLUTE)
        .alignSelf(YogaAlign.CENTER)
        .widthDip(50).heightDip(50)