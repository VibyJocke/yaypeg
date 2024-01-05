import java.awt.image.BufferedImage
import javax.imageio.ImageIO

object ImageReader {
    fun loadRasterImage(filePath: String): BufferedImage {
        println("Loading image $filePath")
        return ImageIO.read(javaClass.getResource(filePath))
    }
}
