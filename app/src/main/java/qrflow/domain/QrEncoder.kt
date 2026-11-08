package qrflow.domain

import android.graphics.Bitmap
import androidx.core.graphics.ColorUtils
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QrEncoder {

    const val DEFAULT_SIZE = 1024

    val DEFAULT_FOREGROUND: Int = 0xFF000000.toInt()
    val DEFAULT_BACKGROUND: Int = 0xFFFFFFFF.toInt()

    fun encode(
        content: String,
        kind: BarcodeKind = BarcodeKind.QR_CODE,
        sizePx: Int = DEFAULT_SIZE,
        foreground: Int = DEFAULT_FOREGROUND,
        background: Int = DEFAULT_BACKGROUND
    ): Result<Bitmap> = runCatching {
        require(content.isNotEmpty()) { "Content is empty" }
        val format = requireNotNull(kind.zxing) { "This format cannot be generated" }
        val (width, height) = dimensions(kind, sizePx)

        val hints = HashMap<EncodeHintType, Any>()
        if (kind == BarcodeKind.QR_CODE) {
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
            hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.M
            hints[EncodeHintType.MARGIN] = 2
        }

        val matrix = MultiFormatWriter().encode(content, format, width, height, hints)
        val w = matrix.width
        val h = matrix.height
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            val offset = y * w
            for (x in 0 until w) {
                pixels[offset + x] = if (matrix.get(x, y)) foreground else background
            }
        }
        Bitmap.createBitmap(pixels, w, h, Bitmap.Config.ARGB_8888)
    }

    fun contrastRatio(foreground: Int, background: Int): Double =
        ColorUtils.calculateContrast(opaque(foreground), opaque(background))

    /** Returns a user-facing warning if the colour pair may be hard to scan, otherwise null. */
    fun colorWarning(foreground: Int, background: Int): String? {
        val fg = opaque(foreground)
        val bg = opaque(background)
        return when {
            ColorUtils.calculateContrast(fg, bg) < 3.0 ->
                "Low contrast: this code may be hard to scan."
            ColorUtils.calculateLuminance(fg) > ColorUtils.calculateLuminance(bg) ->
                "Light-on-dark codes are not supported by every scanner."
            else -> null
        }
    }

    private fun dimensions(kind: BarcodeKind, size: Int): Pair<Int, Int> = when (kind) {
        BarcodeKind.QR_CODE, BarcodeKind.DATA_MATRIX, BarcodeKind.AZTEC -> size to size
        BarcodeKind.PDF_417 -> size to size / 2
        else -> size to size / 3
    }

    private fun opaque(color: Int): Int = color or (0xFF shl 24)
}
