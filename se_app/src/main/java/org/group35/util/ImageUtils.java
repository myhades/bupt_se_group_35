package org.group35.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import net.coobird.thumbnailator.Thumbnails;

public class ImageUtils {

    public static String IToBase64(byte[] imageData) {
        return Base64.getEncoder().encodeToString(imageData);
    }

    public static byte[] loadCompressImage(String path) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Thumbnails.of(new File(path))
                .size(1024, 1024)
                .outputFormat("jpg")
                .outputQuality(0.8)
                .toOutputStream(outputStream); // 关键修改

        return outputStream.toByteArray();
    }
}
