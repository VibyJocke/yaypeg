import java.awt.image.BufferedImage


fun main(args: Array<String>) {
    require(args.size == 1) { "Must contain a file name" }
    println("Program arguments: ${args.joinToString()}")

    val image = ImageReader().loadRasterImage(args.first())

    // 1. Color Space Conversion
    val rgb = imageToRgb(image)
    val yCbCrImage = rgb.map { rgbToYCbCr(it) }

    // 2. Chrominance Downsampling
    val downsampledYCbCr = YCbCrChannels(
        yCbCrImage.yChannel(),
        yCbCrImage.cbChannel().downsample(image.width),
        yCbCrImage.crChannel().downsample(image.width)
    )

    // 3. Block splitting

    // 4. Discrete Cosine Transformation

    // 5. Quantization

    // 6. Entropy coding (run-length and hoffman)

    // Debug renderers
    ImageViewer.viewImage("original", image)
    ImageViewer.viewRawImage("ycc (y)", downsampledYCbCr.y, image.width)
    ImageViewer.viewRawImage("ycc (cb)", downsampledYCbCr.cb, image.width/2)
    ImageViewer.viewRawImage("ycc (cr)", downsampledYCbCr.cr, image.width/2)
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

// https://en.wikipedia.org/wiki/YCbCr#JPEG_conversion
private fun rgbToYCbCr(pixel: RGBPixel): YCbCrPixel {
    val y = 0 + (0.299 * pixel.r.toUByte().toInt()) + (0.587 * pixel.g.toUByte()
        .toInt()) + (0.114 * pixel.b.toUByte().toInt())
    val cb = 128 - (0.168736 * pixel.r.toUByte().toInt()) - (0.331264 * pixel.g.toUByte()
        .toInt()) + (0.5 * pixel.b.toUByte().toInt())
    val cr = 128 + (0.5 * pixel.r.toUByte().toInt()) - (0.418688 * pixel.g.toUByte()
        .toInt()) - (0.081312 * pixel.b.toUByte().toInt())
    //println("y: $y, cb: $cb, cr: $cr")
    require(y in 0.0..255.0) { println("failed $y") }
    require(cb in 0.0..255.0) { println("failed $cb") }
    require(cr in 0.0..255.0) { println("failed $cr") }
    return YCbCrPixel(y.toInt().toByte(), cb.toInt().toByte(), cr.toInt().toByte())
}

private fun List<Byte>.downsample(width: Int): List<Byte> {
    return this.chunked(width)
        .filterIndexed { index, _ -> index % 2 == 0 } // Remove every second row
        .flatten()
        .filterIndexed { index, _ -> index % 2 == 0 } // Remove every second column
}
