import java.awt.Color
import java.awt.FlowLayout
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_RGB
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.SwingConstants.CENTER

object ImageViewer {

    fun viewImage(title: String, image: BufferedImage) {
        val frame = JFrame()
        frame.title = title
        frame.contentPane.layout = FlowLayout(CENTER, 0, 0)
        frame.contentPane.add(JLabel(ImageIcon(image)))
        frame.pack()
        frame.isVisible = true
        frame.isResizable = false
    }

    fun viewRawImage(title: String, rgbImage: List<Byte>, width: Int) {
        try {
            val imageArray = rgbImage.chunked(width).map { it.toByteArray() }.toTypedArray()
            val bufferedImage = BufferedImage(imageArray[0].size, imageArray.size, TYPE_INT_RGB)
            imageArray.forEachIndexed { x, row ->
                row.forEachIndexed { y, col ->
                    val shade = col.toUByte().toInt()
                    bufferedImage.setRGB(y, x, Color(shade, shade, shade).rgb)
                }
            }
            viewImage(title, bufferedImage)
        } catch (ex: Exception) {
            println("Failed to view $title")
            ex.printStackTrace()
        }
    }

}
