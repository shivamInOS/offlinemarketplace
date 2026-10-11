package com.shivam.offlinemarketplace

import android.app.Application
import android.content.Context
import com.shivam.offlinemarketplace.di.AppContainer
import com.shivam.offlinemarketplace.di.DefaultAppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MarketplaceApplication : Application() {

    lateinit var appContainer: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appContainer = DefaultAppContainer(this)
        applicationScope.launch {
            appContainer.marketplaceRepository.seedListingsIfRequired()
            appContainer.syncScheduler.scheduleSync()
        }
    }

    companion object {
        fun appContainer(context: Context): AppContainer {
            return (context.applicationContext as MarketplaceApplication).appContainer
        }
    }
}
