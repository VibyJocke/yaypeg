import java.awt.image.BufferedImage
import kotlin.math.cos
import kotlin.math.sqrt


fun main(args: Array<String>) {
    require(args.size == 2) { "Must contain file name and quality" }
    println("Program arguments: ${args.joinToString()}")

    val imageFile = args[0]
    val quality = args[1].toInt()

    val image = ImageReader.loadRasterImage(imageFile)

    // 1. Color Space Conversion
    val rgb = imageToRgb(image)
    val yCbCrImage = rgb.map { rgbToYCbCr(it) }

    // 2. Chrominance Downsampling
    val downsampledYCbCr = YCbCrChannels(
        yCbCrImage.yChannel(),
        yCbCrImage.cbChannel().downsample(image.width),
        yCbCrImage.crChannel().downsample(image.width)
    )
    val downsampledImageWidth = image.width / 2

    // 3. Block splitting
    val yCbCrImageYBlocks = downsampledYCbCr.y.toBlocks(image.width)
    val yCbCrImageCbBlocks = downsampledYCbCr.cb.toBlocks(downsampledImageWidth)
    val yCbCrImageCrBlocks = downsampledYCbCr.cr.toBlocks(downsampledImageWidth)

    // 4. Discrete Cosine Transformation
    val yDctBlocks = yCbCrImageYBlocks.map { dct(it) }
    val cbDctBlocks = yCbCrImageCbBlocks.map { dct(it) }
    val crDctBlocks = yCbCrImageCrBlocks.map { dct(it) }
    yDctBlocks.first().printBlock()

    // 5. Quantization
    val yQuantizedBlocks = quantization(yDctBlocks, quality)
    yQuantizedBlocks.first().printBlock()

    // 6. Entropy coding (run-length and hoffman)

    // Debug renderers
    ImageViewer.viewImage("original", image)
    ImageViewer.viewRawImage("ycc (y)", downsampledYCbCr.y, image.width)
    ImageViewer.viewRawImage("ycc (cb)", downsampledYCbCr.cb, image.width / 2)
    ImageViewer.viewRawImage("ycc (cr)", downsampledYCbCr.cr, image.width / 2)
    ImageViewer.viewRawImage("rgb (r)", rgb.map { it.r }, image.width)
    ImageViewer.viewRawImage("rgb (g)", rgb.map { it.g }, image.width)
    ImageViewer.viewRawImage("rgb (b)", rgb.map { it.b }, image.width)
}

private fun imageToRgb(image: BufferedImage): List<RGBPixel> {
    val newImage = ArrayList<RGBPixel>()
    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            newImage.add(rgbIntToRgbPixel(image.getRGB(x, y)))
        }
    }
    return newImage
}

private fun rgbToYCbCr(pixel: RGBPixel): YCbCrPixel {
    val y = 0 + (0.299 * pixel.r.toUByte().toInt()) + (0.587 * pixel.g.toUByte()
        .toInt()) + (0.114 * pixel.b.toUByte().toInt())
    val cb = 128 - (0.168736 * pixel.r.toUByte().toInt()) - (0.331264 * pixel.g.toUByte()
        .toInt()) + (0.5 * pixel.b.toUByte().toInt())
    val cr = 128 + (0.5 * pixel.r.toUByte().toInt()) - (0.418688 * pixel.g.toUByte()
        .toInt()) - (0.081312 * pixel.b.toUByte().toInt())
    return YCbCrPixel(y.toInt().toByte(), cb.toInt().toByte(), cr.toInt().toByte())
}

private fun List<Byte>.downsample(width: Int, factor: Int = 2): List<Byte> {
    println("Downsampling image by factor of $factor")
    return this.chunked(width)
        .filterIndexed { index, _ -> index % factor == 0 }
        .flatten()
        .filterIndexed { index, _ -> index % factor == 0 }
}

fun List<Byte>.toBlocks(width: Int, blockSize: Int = 8): List<List<ByteArray>> {
    val height = size / width
    println("Creating ${blockSize}x${blockSize} blocks from ${width}x${height} image")
    val blocks = mutableListOf<List<ByteArray>>()
    for (i in 0 until height step blockSize) {
        for (j in 0 until width step blockSize) {
            val block = mutableListOf<ByteArray>()
            for (x in i until i + blockSize) {
                val rowBlock = mutableListOf<Byte>()
                for (y in j until j + blockSize) {
                    val calculatedPosition = x * width + y
                    if (calculatedPosition < size) {
                        rowBlock.add(this[calculatedPosition])
                    } else {
                        // If the image doesn't cleanly divide into blocksizes,
                        // fill with black (for now, but preferably use nearby color)
                        rowBlock.add(0)
                    }
                }
                block.add(rowBlock.toByteArray())
            }
            blocks.add(block)
        }
    }
    return blocks
}

fun dct(input: List<ByteArray>, blockSize: Int = 8): List<DoubleArray> {
    val result = Array(blockSize) { DoubleArray(blockSize) { 0.0 } }
    for (u in 0 until blockSize) {
        for (v in 0 until blockSize) {
            var sum = 0.0
            for (x in 0 until blockSize) {
                for (y in 0 until blockSize) {
                    val cu = if (u == 0) 1.0 / sqrt(2.0) else 1.0
                    val cv = if (v == 0) 1.0 / sqrt(2.0) else 1.0
                    sum += input[x][y] * cos(((2 * x + 1) * u * Math.PI) / (2.0 * blockSize)) *
                            cos(((2 * y + 1) * v * Math.PI) / (2.0 * blockSize)) * cu * cv
                }
            }
            val alphaU = if (u == 0) 1.0 / sqrt(2.0) else 1.0
            val alphaV = if (v == 0) 1.0 / sqrt(2.0) else 1.0
            result[u][v] = 0.25 * alphaU * alphaV * sum
        }
    }
    return result.toList()
}

private val quantizationMatrix = arrayOf(
    byteArrayOf(16, 11, 10, 16, 24, 40, 51, 61),
    byteArrayOf(12, 12, 14, 19, 26, 58, 60, 55),
    byteArrayOf(14, 13, 16, 24, 40, 57, 69, 56),
    byteArrayOf(14, 17, 22, 29, 51, 87, 80, 62),
    byteArrayOf(18, 22, 37, 56, 68, 109, 103, 77),
    byteArrayOf(24, 35, 55, 64, 81, 104, 113, 92),
    byteArrayOf(49, 64, 78, 87, 103, 121, 120, 101),
    byteArrayOf(72, 92, 95, 98, 112, 100, 103, 99)
)

fun quantization(dctBlocks: List<List<DoubleArray>>, quality: Int): Array<Array<DoubleArray>> {
    println("Quantizing ${dctBlocks.size} blocks with quality $quality")
    val quantizedBlocks = Array(dctBlocks.size) { Array(dctBlocks[0].size) { DoubleArray(dctBlocks[0][0].size) } }
    for (i in dctBlocks.indices) {
        for (j in dctBlocks[i].indices) {
            for (k in dctBlocks[i][j].indices) {
                quantizedBlocks[i][j][k] = (dctBlocks[i][j][k] / (quantizationMatrix[j][k] * (quality.toFloat() / 50.0))).toInt().toDouble()
            }
        }
    }
    return quantizedBlocks
}

fun List<DoubleArray>.printBlock() {
    for (row in this) {
        for (element in row) {
            print("$element\t")
        }
        println()
    }
}

fun Array<DoubleArray>.printBlock() {
    for (row in this) {
        for (element in row) {
            print("$element\t")
        }
        println()
    }
}