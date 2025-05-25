package org.group35.service;

import org.group35.model.Transaction;
import org.group35.model.User;
import org.group35.runtime.ApplicationRuntime;
import org.group35.util.TimezoneUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

class BillsRecognitionTest {

    /**
     * Test buildCapturePrompt includes given categories in the output.
     */
    @Test
    void testBuildCapturePrompt() throws Exception {
        var method = BillsRecognition.class.getDeclaredMethod("buildCapturePrompt", String.class);
        method.setAccessible(true);

        String cats = "Food,Transport,Other";
        String prompt = (String) method.invoke(null, cats);

        assertNotNull(prompt);
        assertTrue(prompt.contains(cats), "Prompt should contain the categories list");
        assertTrue(prompt.startsWith("Please analyze the image"), "Prompt should start with instruction");
    }

    /**
     * Test buildTextRequestBody produces valid JSON with correct structure.
     */
    @Test
    void testBuildTextRequestBody() {
        String prompt = "Test prompt with \"quotes\"";
        String body = BillsRecognition.buildTextRequestBody(prompt);

        JSONObject payload = new JSONObject(body);
        assertEquals("gpt-4o", payload.getString("model"));

        JSONArray messages = payload.getJSONArray("messages");
        assertEquals(1, messages.length());

        JSONObject userMessage = messages.getJSONObject(0);
        assertEquals("user", userMessage.getString("role"));

        JSONArray content = userMessage.getJSONArray("content");
        assertEquals(1, content.length(), "Text body should have one content element");

        JSONObject textPart = content.getJSONObject(0);
        assertEquals("text", textPart.getString("type"));
        assertTrue(textPart.getString("text").contains(prompt.replace("\"", "\\\"")));
    }

    /**
     * Test buildImageRequestBody produces valid JSON with text and image parts.
     */
    @Test
    void testBuildImageRequestBody() {
        String fakeBase64 = "ABC123";
        String prompt = "Capture this bill";
        String body = BillsRecognition.buildImageRequestBody(fakeBase64, prompt);

        JSONObject payload = new JSONObject(body);
        assertEquals("gpt-4o", payload.getString("model"));

        JSONArray messages = payload.getJSONArray("messages");
        assertEquals(1, messages.length());
        JSONObject msg = messages.getJSONObject(0);
        assertEquals("user", msg.getString("role"));

        JSONArray content = msg.getJSONArray("content");
        assertEquals(2, content.length(), "Image request should have two content items");

        JSONObject textPart = content.getJSONObject(0);
        assertEquals("text", textPart.getString("type"));
        assertTrue(textPart.getString("text").contains(prompt));

        JSONObject imagePart = content.getJSONObject(1);
        assertEquals("image_url", imagePart.getString("type"));
        JSONObject urlObj = imagePart.getJSONObject("image_url");
        assertTrue(urlObj.getString("url").startsWith("data:image/jpeg;base64,"));
    }

    /**
     * Test removeFirstAndLastLine for various inputs.
     */
    @Test
    void testRemoveFirstAndLastLine() {
        // Less than or equal to two lines
        assertEquals("", BillsRecognition.removeFirstAndLastLine("one line"));
        assertEquals("", BillsRecognition.removeFirstAndLastLine("line1\nline2"));

        // Three lines
        String three = "first\nmiddle\nlast";
        assertEquals("middle", BillsRecognition.removeFirstAndLastLine(three));

        // Multiple lines
        String multi = "A\nB\nC\nD\nE";
        assertEquals("B\nC\nD", BillsRecognition.removeFirstAndLastLine(multi));
    }

    /**
     * Test writeDataToJson writes valid JSON file and validates content.
     */
    @Test
    void testWriteDataToJsonSuccess(@TempDir Path tmp) throws Exception {
        String data = "[\n{\"name\":\"Store\",\"amount\":100,\"time\":\"2025-05-25T10:00:00\",\"location\":\"NYC\",\"category\":\"Other\"}\n]";
        Path file = tmp.resolve("out.json");

        // Should not throw
        BillsRecognition.writeDataToJson(file.toString(), data);

        String written = new String(java.nio.file.Files.readAllBytes(file));
        JSONArray arr = new JSONArray(new JSONTokener(written));
        assertEquals(1, arr.length());
        JSONObject obj = arr.getJSONObject(0);
        assertEquals("Store", obj.getString("name"));
    }

    /**
     * Test writeDataToJson with invalid JSON throws RuntimeException.
     */
    @Test
    void testWriteDataToJsonFailure(@TempDir Path tmp) {
        String bad = "not a json";
        Path file = tmp.resolve("bad.json");
        assertThrows(RuntimeException.class, () -> BillsRecognition.writeDataToJson(file.toString(), bad));
    }
}
