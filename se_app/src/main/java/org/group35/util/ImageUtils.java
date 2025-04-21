// src/main/java/org/group35/util/ImageUtils.java
package org.group35.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import javax.imageio.ImageIO;

import net.coobird.thumbnailator.Thumbnails;

public class ImageUtils {

    /**
     * Encode raw bytes into Base64.
     */
    public static String IToBase64(byte[] imageData) {
        return Base64.getEncoder().encodeToString(imageData);
    }

    /**
     * Loads an image from disk, center‑crops it to a square, resizes to 1024×1024,
     * compresses to JPEG at 80% quality, and returns the raw bytes.
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
}
