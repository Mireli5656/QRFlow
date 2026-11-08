package qrflow.domain

import com.google.mlkit.vision.barcode.common.Barcode
import com.google.zxing.BarcodeFormat

enum class BarcodeKind(val displayName: String, val zxing: BarcodeFormat?) {
    QR_CODE("QR Code", BarcodeFormat.QR_CODE),
    EAN_13("EAN-13", BarcodeFormat.EAN_13),
    EAN_8("EAN-8", BarcodeFormat.EAN_8),
    UPC_A("UPC-A", BarcodeFormat.UPC_A),
    UPC_E("UPC-E", BarcodeFormat.UPC_E),
    CODE_128("Code 128", BarcodeFormat.CODE_128),
    CODE_39("Code 39", BarcodeFormat.CODE_39),
    ITF("ITF", BarcodeFormat.ITF),
    PDF_417("PDF417", BarcodeFormat.PDF_417),
    DATA_MATRIX("Data Matrix", BarcodeFormat.DATA_MATRIX),
    AZTEC("Aztec", BarcodeFormat.AZTEC),
    OTHER("Other", null);

    val isQr: Boolean get() = this == QR_CODE

    companion object {
        fun fromMlKit(format: Int): BarcodeKind = when (format) {
            Barcode.FORMAT_QR_CODE -> QR_CODE
            Barcode.FORMAT_EAN_13 -> EAN_13
            Barcode.FORMAT_EAN_8 -> EAN_8
            Barcode.FORMAT_UPC_A -> UPC_A
            Barcode.FORMAT_UPC_E -> UPC_E
            Barcode.FORMAT_CODE_128 -> CODE_128
            Barcode.FORMAT_CODE_39 -> CODE_39
            Barcode.FORMAT_ITF -> ITF
            Barcode.FORMAT_PDF417 -> PDF_417
            Barcode.FORMAT_DATA_MATRIX -> DATA_MATRIX
            Barcode.FORMAT_AZTEC -> AZTEC
            else -> OTHER
        }

        fun fromName(name: String?): BarcodeKind =
            BarcodeKind.entries.firstOrNull { it.name == name } ?: OTHER
    }
}
