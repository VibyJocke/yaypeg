/**
 * @param y Luminance
 * @param cb Blue Luminance
 * @param cr Red Luminance
 */
data class YCbCrPixel(val y: Byte, val cb: Byte, val cr: Byte)
data class YCbCrChannels(val y: List<Byte>, val cb: List<Byte>, val cr: List<Byte>)

fun List<YCbCrPixel>.yChannel(): List<Byte> = this.map { it.y }
fun List<YCbCrPixel>.cbChannel(): List<Byte> = this.map { it.cb }
fun List<YCbCrPixel>.crChannel(): List<Byte> = this.map { it.cr }
fun List<YCbCrPixel>.yCbCrChannels() = YCbCrChannels(yChannel(), cbChannel(), crChannel())
