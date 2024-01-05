data class RGBPixel(val r: Byte, val g: Byte, val b: Byte)
data class RGBChannels(val r: List<Byte>, val g: List<Byte>, val b: List<Byte>)

fun List<RGBPixel>.rChannel(): List<Byte> = this.map { it.r }
fun List<RGBPixel>.gChannel(): List<Byte> = this.map { it.g }
fun List<RGBPixel>.bChannel(): List<Byte> = this.map { it.b }
fun List<RGBPixel>.rgbChannels() = RGBChannels(rChannel(), gChannel(), bChannel())

fun rgbIntToRgbPixel(color: Int): RGBPixel {
    val blue: Int = color and 0xff
    val green: Int = color and 0xff00 shr 8
    val red: Int = color and 0xff0000 shr 16
    return RGBPixel(red.toByte(), green.toByte(), blue.toByte())
}

fun rgbPixelToRgbInt(pixel: RGBPixel): Int {
    var rgb = 0 + pixel.r
    rgb = (rgb shl 8) + pixel.g
    rgb = (rgb shl 8) + pixel.b
    return rgb
}
