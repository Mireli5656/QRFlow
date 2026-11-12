package qrflow

import android.app.Application
import qrflow.data.local.AppDatabase
import qrflow.data.repository.HistoryRepository

class QRFlowApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.create(this) }

    val historyRepository: HistoryRepository by lazy {
        HistoryRepository(database.historyDao())
    }
}
