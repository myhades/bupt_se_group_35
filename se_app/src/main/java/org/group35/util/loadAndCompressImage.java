package org.group35.util;
import net.coobird.thumbnailator.Thumbnails;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class loadAndCompressImage extends IOException{
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
