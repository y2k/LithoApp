package y2k.example.litho

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.litho.ComponentContext
import com.facebook.litho.LithoView
import com.facebook.soloader.SoLoader
import y2k.example.litho.components.MainComponent

/**
 * Created by y2k on 07/07/2017.
 **/

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = ComponentContext(this)
        val component = MainComponent.create(context).build()
        //        final Component component = RssListComponent.create(context)
        //                .subscription(new RssSubscription(
        //                        "Kotlin blog",
        //                        "http://feeds.feedburner.com/kotlin",
        //                        "https://d3nmt5vlzunoa1.cloudfront.net/dotnet/files/2017/06/logo.png"))
        //                .build();
        setContentView(LithoView.create(context, component))
    }

    class App : Application() {

        override fun onCreate() {
            super.onCreate()

            Fresco.initialize(this)
            SoLoader.init(this, false)
        }
    }
}