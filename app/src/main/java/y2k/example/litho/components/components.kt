package y2k.example.litho.components

import android.graphics.Color
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.facebook.yoga.YogaPositionType
import y2k.litho.elmish.experimental.childWithLayout
import y2k.litho.elmish.experimental.column
import y2k.litho.elmish.experimental.progressL
import y2k.litho.elmish.experimental.text

/**
 * Created by y2k on 11/07/2017.
 **/

fun errorIndicator() =
    column {
        backgroundColor(0xFF303030L.toInt())
        paddingDip(YogaEdge.ALL, 4f)

        childWithLayout(text {
            textSizeSp(24f)
            textColor(Color.WHITE)
            text("ERROR")
        }, {
            alignSelf(YogaAlign.CENTER)
        })
    }

fun preloadIndicator() =
    progressL { layout ->
        color(Color.GRAY)
        layout {
            widthDip(50f)
            heightDip(50f)
            positionType(YogaPositionType.ABSOLUTE)
            alignSelf(YogaAlign.CENTER)
        }
    }