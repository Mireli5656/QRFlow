package qrflow.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import qrflow.domain.BarcodeKind
import qrflow.domain.QrType

object HistorySource {
    const val GENERATED = "GENERATED"
    const val SCANNED = "SCANNED"
}

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val source: String,
    val type: String,
    val format: String,
    val content: String,
    val title: String,
    val foregroundColor: Int,
    val backgroundColor: Int,
    val createdAt: Long
)

val HistoryEntity.barcodeKind: BarcodeKind get() = BarcodeKind.fromName(format)
val HistoryEntity.qrType: QrType get() = QrType.fromName(type)
val HistoryEntity.isScanned: Boolean get() = source == HistorySource.SCANNED
val HistoryEntity.canRegenerate: Boolean get() = barcodeKind.zxing != null
