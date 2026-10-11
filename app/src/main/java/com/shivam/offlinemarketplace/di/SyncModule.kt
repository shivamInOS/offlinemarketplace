package com.shivam.offlinemarketplace.di

import com.shivam.offlinemarketplace.data.sync.SyncScheduler
import com.shivam.offlinemarketplace.data.sync.WorkManagerSyncScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {

    @Binds
    @Singleton
    abstract fun bindSyncScheduler(
        impl: WorkManagerSyncScheduler
    ): SyncScheduler
}
