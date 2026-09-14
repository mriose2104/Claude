package com.misgastos.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misgastos.app.data.prefs.ThemeMode
import com.misgastos.app.di.LocalAppContainer
import com.misgastos.app.ui.navigation.MisGastosApp
import com.misgastos.app.ui.theme.AppThemeMode
import com.misgastos.app.ui.theme.MisGastosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as MisGastosApplication).container

        setContent {
            val themeMode by container.userPreferences.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
            val appThemeMode = when (themeMode) {
                ThemeMode.LIGHT -> AppThemeMode.LIGHT
                ThemeMode.DARK -> AppThemeMode.DARK
                ThemeMode.SYSTEM -> AppThemeMode.SYSTEM
            }

            CompositionLocalProvider(LocalAppContainer provides container) {
                MisGastosTheme(themeMode = appThemeMode) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        MisGastosApp()
                    }
                }
            }
        }
    }
}
