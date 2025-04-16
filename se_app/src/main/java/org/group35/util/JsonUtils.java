package org.group35.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;

/**
 * JSON utility class.
 */
public class JsonUtils {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Convert object to JSON string.
     */
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    /**
     * Convert JSON string to designated object.
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    /**
     * Read JSON from file.
     */
    public static <T> T readJsonFromFile(File file, Class<T> classOfT) throws IOException {
        try (Reader reader = new FileReader(file)) {
            return gson.fromJson(reader, classOfT);
        }
    }

    /**
     * Write JSON to file.
     */
    public static void writeJsonToFile(File file, Object obj) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(obj, writer);
        }
    }
}
