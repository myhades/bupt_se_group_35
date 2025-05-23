package org.group35.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import net.coobird.thumbnailator.Thumbnails;

/**
 * Utility methods for loading, compressing, and converting images.
 */
public class ImageUtils {

    /**
     * Opens a PNG file picker, crops to center square, resizes to the given dimension,
     * and returns the Base64-encoded string of the processed image.
     *
     * @param owner     the JavaFX Window owning the file dialog
     * @param dimension the target width and height in pixels (e.g. 256)
     * @return a Base64 string of the processed PNG image, or null if cancelled
     * @throws IOException if reading or processing fails
     */
    public static String chooseAndProcessImage(Window owner, int dimension) throws IOException {
        File file = FileUtils.chooseFile(
                owner,
                "Select PNG Image",
                null,
                List.of(new FileChooser.ExtensionFilter("PNG Images", "*.png"))
        );
        if (file == null) {
            return null;
        }
        // Crop, resize, and get raw bytes
        byte[] processed = ImageUtils.loadAndResizeImage(file.getAbsolutePath(), dimension);
        // Encode to Base64
        return ImageUtils.toBase64(processed);
    }

    /**
     * Loads an image from disk, center-crops it to a square, resizes to [dimension]×[dimension],
     * outputs as PNG, and returns the raw bytes.
     */
    public static byte[] loadAndResizeImage(String path, int dimension) throws IOException {
        BufferedImage img = ImageIO.read(new File(path));
        int w = img.getWidth(), h = img.getHeight();
        int side = Math.min(w, h);
        int x = (w - side) / 2, y = (h - side) / 2;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Thumbnails.of(img)
                .sourceRegion(x, y, side, side)
                .size(dimension, dimension)
                .outputFormat("png")
                .toOutputStream(out);

        return out.toByteArray();
    }

    /**
     * Loads an image from disk, center‑crops it to a square, resizes to 1024×1024,
     * compresses to JPEG at 80% quality, and returns the raw bytes.
     *
     * @param path file system path to the image
     * @return processed image bytes in JPEG format
     * @throws IOException if file I/O fails
     */
    public static byte[] loadCompressImage(String path) throws IOException {
        BufferedImage img = ImageIO.read(new File(path));
        int width = img.getWidth();
        int height = img.getHeight();
        int square = Math.min(width, height);
        int x = (width - square) / 2;
        int y = (height - square) / 2;

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Thumbnails.of(img)
                .sourceRegion(x, y, square, square)
                .size(1024, 1024)
                .outputFormat("jpg")
                .outputQuality(0.8)
                .toOutputStream(output);

        return output.toByteArray();
    }

    /**
     * Encodes raw image bytes into a Base64 string (no data URI prefix).
     *
     * @param imageData raw image bytes
     * @return Base64 encoded string
     */
    public static String toBase64(byte[] imageData) {
        return Base64.getEncoder().encodeToString(imageData);
    }

    /**
     * Encodes a JavaFX Image into a Base64 string with the given format.
     *
     * @param fxImage the JavaFX Image to encode
     * @param format the image format (e.g., "png" or "jpg")
     * @return Base64 encoded string of the image bytes
     * @throws IOException if encoding fails
     */
    public static String toBase64(Image fxImage, String format) throws IOException {
        BufferedImage bImage = SwingFXUtils.fromFXImage(fxImage, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bImage, format, baos);
        byte[] bytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Decodes a raw Base64 string into a JavaFX Image.
     * Expects no data URI prefix.
     *
     * @param base64Data Base64 encoded image data
     * @return JavaFX Image or null if decoding fails
     */
    public static Image fromBase64(String base64Data) {
        if (base64Data == null || base64Data.isBlank()) {
            return null;
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(base64Data);
            return new Image(new ByteArrayInputStream(bytes));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Converts a JavaFX Image to raw bytes in the specified format (e.g., "png" or "jpg").
     */
    public static byte[] fxImageToBytes(Image fxImage, String format) throws IOException {
        BufferedImage bImage = SwingFXUtils.fromFXImage(fxImage, null);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(bImage, format, baos);
            return baos.toByteArray();
        }
    }
}
