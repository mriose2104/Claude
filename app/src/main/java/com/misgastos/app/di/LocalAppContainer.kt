package com.misgastos.app.di

import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer not provided. Wrap your content in CompositionLocalProvider(LocalAppContainer provides container).")
}
