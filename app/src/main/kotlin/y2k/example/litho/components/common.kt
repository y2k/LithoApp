package y2k.example.litho.components

import android.graphics.Color
import com.facebook.litho.Column
import com.facebook.litho.ComponentContext
import com.facebook.litho.ComponentLayout
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.widget.Progress
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaJustify

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