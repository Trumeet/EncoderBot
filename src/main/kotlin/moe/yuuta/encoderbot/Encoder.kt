package moe.yuuta.encoderbot

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URLEncoder
import java.util.*


object Encoder {
    fun b6(origin: String): String = Base64.getEncoder().encodeToString(origin.toByteArray())
    fun qr(origin: String): InputStream {
        val qrCodeWriter = QRCodeWriter()
        val hints = mutableMapOf<EncodeHintType, Any>()
        hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
        val bitMatrix = qrCodeWriter.encode(origin, BarcodeFormat.QR_CODE, 512, 512, hints)
        val stream = ByteArrayOutputStream()
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", stream)
        val inputStream = ByteArrayInputStream(stream.toByteArray())
        stream.close()
        return inputStream
    }
    fun url(origin: String): String = URLEncoder.encode(origin, "UTF-8")
}