package org.group35.util;
import java.util.Base64;

public class imageToBase64 {
    public static String IToBase64(byte[] imageData) {
        return Base64.getEncoder().encodeToString(imageData);
    }
}
