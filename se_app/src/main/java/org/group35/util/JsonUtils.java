package org.group35.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import org.group35.util.LoggerHelper;

/**
 * JSON utility class.
 */
public class JsonUtils {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Convert object to JSON string.
     */
    public static String toJson(Object obj) {
        LoggerHelper.trace("toJson() called for object of type: " + (obj != null ? obj.getClass().getName() : "null"));
        String json = gson.toJson(obj);
        LoggerHelper.trace("toJson() completed, result length: " + (json != null ? json.length() : 0));
        return json;
    }

    /**
     * Convert JSON string to designated object.
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        LoggerHelper.trace("fromJson() called for target class: " + classOfT.getName());
        T obj = gson.fromJson(json, classOfT);
        LoggerHelper.trace("fromJson() completed, created instance of: " + (obj != null ? obj.getClass().getName() : "null"));
        return obj;
    }

    /**
     * Read JSON from file.
     */
    public static <T> T readJsonFromFile(File file, Class<T> classOfT) throws IOException {
        LoggerHelper.trace("readJsonFromFile() called for file: " + file.getAbsolutePath());
        try (Reader reader = new FileReader(file)) {
            T obj = gson.fromJson(reader, classOfT);
            LoggerHelper.trace("readJsonFromFile() completed, created instance of: " + (obj != null ? obj.getClass().getName() : "null"));
            return obj;
        }
    }

    /**
     * Write JSON to file.
     */
    public static void writeJsonToFile(File file, Object obj) throws IOException {
        LoggerHelper.trace("writeJsonToFile() called for file: " + file.getAbsolutePath());
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(obj, writer);
        }
        LoggerHelper.trace("writeJsonToFile() completed for file: " + file.getAbsolutePath());
    }
}