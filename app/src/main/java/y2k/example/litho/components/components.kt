package y2k.example.litho.components

import android.graphics.Color
import com.facebook.litho.ComponentLayout.ContainerBuilder
import com.facebook.litho.widget.Progress
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.facebook.yoga.YogaPositionType
import y2k.litho.elmish.experimental.child
import y2k.litho.elmish.experimental.column
import y2k.litho.elmish.experimental.text

fun ContainerBuilder.errorIndicator() =
    column {
        backgroundColor(0xFF303030L.toInt())
        paddingDip(YogaEdge.ALL, 4f)

        text {
            textSizeSp(24f)
            textColor(Color.WHITE)
            text("ERROR")
            alignSelf(YogaAlign.CENTER)
        }
    }

@Suppress("DEPRECATION")
fun ContainerBuilder.preloadIndicator() {
    child { ctx ->
        Progress.create(ctx)
            .apply {
                color(Color.GRAY)

                widthDip(50f)
                heightDip(50f)
                positionType(YogaPositionType.ABSOLUTE)
                alignSelf(YogaAlign.CENTER)
            }
            .withLayout()
    }
}