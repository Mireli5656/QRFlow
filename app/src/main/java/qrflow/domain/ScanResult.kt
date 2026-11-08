package qrflow.domain

data class ScanResult(
    val content: String,
    val kind: BarcodeKind,
    val type: QrType = QrPayloads.detectType(content)
) {
    /** An http(s) link if the content is one, otherwise null. */
    val link: String? get() = QrPayloads.webLink(content)
}
