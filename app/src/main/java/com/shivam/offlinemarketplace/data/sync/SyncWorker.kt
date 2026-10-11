package com.shivam.offlinemarketplace.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.shivam.offlinemarketplace.MarketplaceApplication
import java.io.IOException

class SyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "SyncWorker triggered (Run attempt: $runAttemptCount)")
        return try {
            val repository = MarketplaceApplication.appContainer(applicationContext).marketplaceRepository
            val syncResult = repository.syncPendingListings()

            if (syncResult.isSuccess) {
                Log.d(TAG, "SyncWorker: All pending listings successfully synchronized.")
                Result.success()
            } else {
                val exception = syncResult.exceptionOrNull()
                if (exception is IOException) {
                    Log.w(TAG, "SyncWorker: Network issue encountered (${exception.message}). WorkManager will retry with exponential backoff (Next attempt #${runAttemptCount + 1}).")
                    Result.retry()
                } else {
                    Log.e(TAG, "SyncWorker: Non-retryable error (${exception?.message}). Terminating work.")
                    Result.failure()
                }
            }
        } catch (e: IOException) {
            Log.w(TAG, "SyncWorker: Network failure during sync (${e.message}). Scheduling WorkManager retry (Next attempt #${runAttemptCount + 1}).")
            Result.retry()
        } catch (e: Exception) {
            Log.e(TAG, "SyncWorker: Permanent failure during sync execution (${e.message}).")
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "MarketplaceSync"
    }
}
