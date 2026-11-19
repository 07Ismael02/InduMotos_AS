package pe.indumotos.mobile

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settings = getSharedPreferences("appearance", MODE_PRIVATE)
        val stored = settings.getString("theme", null)
        val appearance = Appearance.entries.firstOrNull { it.name == stored } ?: Appearance.SYSTEM

        setContent {
            App(
                backendUrl = BuildConfig.BACKEND_URL,
                initialTheme = appearance,
                onAppearanceChanged = { settings.edit().putString("theme", it.name).apply() },
                onDarkTheme = { dark -> window.decorView.post { updateSystemBars(dark) } }
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun updateSystemBars(dark: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val lightBars = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            window.insetsController?.setSystemBarsAppearance(if (dark) 0 else lightBars, lightBars)
        } else {
            var lightBars = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                lightBars = lightBars or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
            val decor = window.decorView
            decor.systemUiVisibility = (decor.systemUiVisibility and lightBars.inv()) or
                (if (dark) 0 else lightBars)
        }
    }
}
