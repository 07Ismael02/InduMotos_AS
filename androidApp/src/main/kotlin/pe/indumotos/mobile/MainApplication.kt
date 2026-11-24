package pe.indumotos.mobile

import android.app.Application
import pe.indumotos.mobile.di.initKoin
import org.koin.android.ext.koin.androidContext

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate();
        initKoin { androidContext(this@MainApplication) }
    }
}
