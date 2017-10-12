package y2k.example.litho.components

import android.graphics.Color
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.facebook.yoga.YogaPositionType
import y2k.litho.elmish.childWithLayout
import y2k.litho.elmish.column
import y2k.litho.elmish.progressL
import y2k.litho.elmish.text

/**
 * Created by y2k on 11/07/2017.
 **/

fun errorIndicator() =
    column {
        backgroundColor(0xFF303030L.toInt())
        paddingDip(YogaEdge.ALL, 4)

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
            widthDip(50)
            heightDip(50)
            positionType(YogaPositionType.ABSOLUTE)
            alignSelf(YogaAlign.CENTER)
        }
    }