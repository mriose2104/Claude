package com.misgastos.app

import android.app.Application
import com.misgastos.app.data.notification.NotificationChannels
import com.misgastos.app.data.seed.SeedData
import com.misgastos.app.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MisGastosApplication : Application() {

    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationChannels.ensureCreated(this)
        applicationScope.launch {
            SeedData.seedIfEmpty(container.database)
        }
    }
}
