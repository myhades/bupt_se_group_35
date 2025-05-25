package org.group35.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.time.LocalDateTime;

/**
 * JSON utility class with support for Java 8 date/time types.
 */
public class JsonUtils {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new DateTimeAdapter())
            .setPrettyPrinting()
            .create();

    /**
     * Convert object to JSON string.
     */
    public static String toJson(Object obj) {
        LogUtils.trace("Trying to convert to JSON from object type: " +
                (obj != null ? obj.getClass().getName() : "null"));
        String json = gson.toJson(obj);
        LogUtils.trace("Conversion completed. Result length: " +
                (json != null ? json.length() : 0));
        return json;
    }
    /**
     * Parses a string and attempts to convert it into a valid JSON object or array.
     *
     * <p>This method first tries to parse the string as a {@link JSONObject}. If that fails,
     * it tries to parse the string as a {@link JSONArray}. If both attempts fail, it logs the
     * error and returns {@code null}.</p>
     *
     * @param str The string to be parsed as JSON. It can represent either a JSON object or an array.
     * @return An {@link Object} representing the parsed JSON data, which can be either a
     *         {@link JSONObject} or a {@link JSONArray}. Returns {@code null} if the string
     *         is not valid JSON.
     */
    public static Object parseJsonValidation(String str) {

        try {
            return new JSONObject(str);
        } catch (Exception e) {
            try {
                return new JSONArray(str);
            } catch (Exception ex) {
                LogUtils.trace("Not a JSON format");
                return null;
            }
        }
    }

    /**
     * Convert JSON string to designated object.
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        LogUtils.trace("Starting to convert JSON into object type: " + classOfT.getName());
        T obj = gson.fromJson(json, classOfT);
        LogUtils.trace("Conversion completed. Created an instance of: " +
                (obj != null ? obj.getClass().getName() : "null"));
        return obj;
    }

    /**
     * Read JSON from file.
     */
    public static <T> T readJsonFromFile(File file, Class<T> classOfT) throws IOException {
        LogUtils.trace("Reading JSON from file: " + file.getAbsolutePath());
        try (Reader reader = new FileReader(file)) {
            T obj = gson.fromJson(reader, classOfT);
            LogUtils.trace("Parsing completed. Created an instance of: " +
                    (obj != null ? obj.getClass().getName() : "null"));
            return obj;
        }
    }

    /**
     * Write JSON to file.
     */
    public static void writeJsonToFile(File file, Object obj) throws IOException {
        LogUtils.trace("Writing JSON to file: " + file.getAbsolutePath());
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(obj, writer);
        }
        LogUtils.trace("Writing completed for file: " + file.getAbsolutePath());
    }
}