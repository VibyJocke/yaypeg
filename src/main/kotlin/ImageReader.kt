import java.awt.image.BufferedImage
import javax.imageio.ImageIO

class ImageReader {
    fun loadRasterImage(filePath: String): BufferedImage {
        println("Loading image $filePath")
        return ImageIO.read(javaClass.getResource(filePath))
    }
}
