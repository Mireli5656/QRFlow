package qrflow.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import qrflow.data.local.HistoryDao
import qrflow.data.local.HistoryEntity
import qrflow.data.local.HistorySource
import qrflow.data.local.barcodeKind
import qrflow.data.local.qrType
import qrflow.domain.BarcodeKind
import qrflow.domain.QrEncoder
import qrflow.domain.QrPayloads
import qrflow.domain.QrType
import qrflow.domain.ScanResult

class HistoryRepository(private val dao: HistoryDao) {

    val all: Flow<List<HistoryEntity>> = dao.observeAll()

    fun recent(limit: Int): Flow<List<HistoryEntity>> = dao.observeRecent(limit)

    fun search(query: String): Flow<List<HistoryEntity>> {
        val q = query.trim()
        if (q.isEmpty()) return dao.observeAll()
        return dao.observeAll().map { list ->
            list.filter { item ->
                item.content.contains(q, ignoreCase = true) ||
                    item.title.contains(q, ignoreCase = true) ||
                    item.barcodeKind.displayName.contains(q, ignoreCase = true) ||
                    item.qrType.label.contains(q, ignoreCase = true)
            }
        }
    }

    suspend fun get(id: Long): HistoryEntity? = dao.getById(id)

    suspend fun addGenerated(
        type: QrType,
        content: String,
        foreground: Int = QrEncoder.DEFAULT_FOREGROUND,
        background: Int = QrEncoder.DEFAULT_BACKGROUND
    ): Long = dao.insert(
        HistoryEntity(
            source = HistorySource.GENERATED,
            type = type.name,
            format = BarcodeKind.QR_CODE.name,
            content = content,
            title = QrPayloads.title(type, content),
            foregroundColor = foreground,
            backgroundColor = background,
            createdAt = System.currentTimeMillis()
        )
    )

    suspend fun addScanned(result: ScanResult): Long = dao.insert(
        HistoryEntity(
            source = HistorySource.SCANNED,
            type = result.type.name,
            format = result.kind.name,
            content = result.content,
            title = QrPayloads.title(result.type, result.content),
            foregroundColor = QrEncoder.DEFAULT_FOREGROUND,
            backgroundColor = QrEncoder.DEFAULT_BACKGROUND,
            createdAt = System.currentTimeMillis()
        )
    )

    /** Re-inserts a previously deleted entry (used by the Undo action). */
    suspend fun restore(entity: HistoryEntity) {
        dao.insert(entity)
    }

    suspend fun delete(id: Long) = dao.deleteById(id)

    suspend fun clear() = dao.clear()
}
