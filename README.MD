# 🎉Yaypeg🎉
JPEG encoder implementation in Kotlin that converts bitmap images into the JPEG format. It has been developed for fun, and is neither optimized nor particularly capable.

## Overview
The JPEG encoder implements the core steps of the JPEG compression process, which involve:

### 1. Color Space Conversion
Convert the input image from the RGB color space to the YCbCr color space. This step separates luminance (Y) from chrominance (Cb and Cr) information, allowing for more efficient compression.

### 2. Downsampling
Subsample the chrominance components (Cb and Cr) to reduce their resolution. This reduces the color resolution, which our eyes aren't very sensitive to.

### 3. Discrete Cosine Transform (DCT)
Divide the image into small blocks ("block splitting") and apply the DCT to these blocks. This transformation converts spatial image data into frequency components, enabling better compression by concentrating information in fewer coefficients.

### 4. Quantization
Quantize the DCT coefficients by dividing them with a quantization matrix. This step discards high-frequency information that the human eye is less sensitive to, further reducing data while preserving image quality.

### 5. Entropy Coding
Apply lossless compression using run-length encoding (RLE) and Huffman coding to reduce the final data size.

## Sources
- https://www.youtube.com/watch?v=Kv1Hiv3ox8I
- https://en.wikipedia.org/wiki/JPEG#Encoding
