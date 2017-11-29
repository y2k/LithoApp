package y2k.example.litho

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.facebook.drawee.backends.pipeline.Fresco
import y2k.example.litho.components.EntitiesScreen
import y2k.example.litho.components.SubscriptionsScreen
import y2k.litho.elmish.experimental.program

/**
 * Created by y2k on 07/07/2017.
 **/

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        program(SubscriptionsScreen)
//        program(EntitiesScreen(Subscription(
//            "", URL("http://feeds.feedburner.com/kotlin"), ""))) // FIXME:
    }

    class App : Application() {

        override fun onCreate() {
            super.onCreate()
            _app = this
            Fresco.initialize(this)
            com.facebook.soloader.SoLoader.init(this, false)
        }

        companion object {
            val app: Application get() = _app
            private lateinit var _app: Application
        }
    }
}

class EntitiesActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        program(EntitiesScreen(intent.getSerializableExtra("data") as Subscription))
    }
}