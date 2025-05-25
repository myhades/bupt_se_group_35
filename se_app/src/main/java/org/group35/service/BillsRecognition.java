package org.group35.service;

import okhttp3.*;
import org.group35.controller.TransactionManager;
import org.group35.model.Transaction;
import org.group35.model.User;
import org.group35.runtime.ApplicationRuntime;
import org.group35.util.ImageUtils;
import org.group35.util.JsonUtils;
import org.group35.util.LogUtils;
import org.group35.util.TimezoneUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class BillsRecognition {
    private static final String API_URL = "https://api.gptsapi.net";  // model API URL
    private static final String API_TOKEN = "sk-id8a932449e17e32258e1565c2ab579825ad061b479cbtQr";  // model API token

    private static final AtomicBoolean doneFlag = new AtomicBoolean(false);
    // Create OkHttpClient
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();
    public BillsRecognition(double savingGoal, double monIncome){
    }

    /**
     * Callback interface for recognition result.
     */
    public interface RecognitionCallback {
        void onSuccess(Transaction billcontent);
        void onFailure(Throwable e);
    }

    /**
     * Builds the prompt for capturing the bill details in an image.
     *
     * @param categories List of categories that can be selected for the bill.
     * @return The generated prompt string.
     */
    private static String buildCapturePrompt(String categories){

        String promptText = "Please analyze the image containing bill details and generate a JSON-formatted output. Each entry in the JSON should include the following fields: name, amount, date, location, category\n" +
                "\n" +
                "If the bill does not contain information, this field can be left empty. But category cannot be empty\n" +
                "Name is merchant(supplier) name\n" +
                "Amount is the amount of income or expenditure. The amount is positive means income and negtive means expense\n" +
                "Time indicate the time described by the user. Time must conform the LocalDateTime format \nlocation indicate the location described by the user\n" +
                "The category must be one of the following categories. Please select the most suitable category (if it does not match any of these declared categories, set it as the \"other\" category).The categories are"+ categories +"\n" +
                "Output the result as a JSON structure that can be directly saved to a file. Ensure the output strictly follows this JSON format:\n" +
                "[\n" +
                "{\n" +
                "\"name\": \"...\",\n" +
                "\"amount\": \"...\",\n" +
                "\"time\": \"...\",\n" +
                "\"location\": \"...\"\n" +
                "\"category\": \"...\",\n" +
                "}" +
                "]\n" +
                "The category must be in [" + categories + "]" +
                "There is only one bill, so you must generate only one json body\n" +
                "Please ensure the output is in a valid JSON file format and exclude any additional information or explanations. Return only the JSON data structured as specified.";
        return promptText;
    }

    /**
     * Builds the prompt for analyzing bill content from a given text.
     *
     * @param content   The bill content text.
     * @param categories List of available categories for the bill.
     * @return The generated prompt string.
     */
    private static String buildTextPrompt(String content, String categories){

        User user = ApplicationRuntime.getInstance().getCurrentUser();
        String time = TimezoneUtils.getCurrentTimeByZone(user.getTimezone());
        String promptText = "Please analyze the text containing bill details and generate a JSON-formatted output. Each entry in the JSON should include the following fields: name, amount, date, location, category\n" +
                "\n" +
                "The bill content is:" + content + "\n" +
                "If the bill does not contain information, this field can be left empty. But category cannot be empty\n" +
                "name is merchant(supplier) name\n" +
                "amount is the amount of income or expenditure. The amount is positive means income and negtive means expense. Please convert the currency amount to the default US dollar unit according to the text description\n" +
                "time indicate the time described by the user. time must conform the LocalDateTime format. If the time provided by the user is not a specific time but an adverb of time, then infer based on the current time("+ time +") to obtain the most accurate time possible. The number of time digits that cannot be inferred is replaced by 0.\n" +
                "location indicate the location described by the user\n" +
                "The category must be one of the following categories. Please select the most suitable category (if it does not match any of these declared categories, set it as the \"other\" category).The categories you can select are:"+ categories +"\n" +
                "Output the result as a JSON structure that can be directly saved to a file. Ensure the output strictly follows this JSON format:\n" +
                "[\n" +
                "{\n" +
                "\"name\": \"...\",\n" +
                "\"amount\": \"...\",\n" +
                "\"time\": \"...\",\n" +
                "\"location\": \"...\"\n" +
                "\"category\": \"...\",\n" +
                "}" +
                "]\n" +
                "The category must be in [" + categories + "]" +
                "There is only one bill, so you must generate only one json body\n" +
                "Please ensure the output is in a valid JSON file format and exclude any additional information or explanations. Return only the JSON data structured as specified.";
        return promptText;
    }

    /**
     * Builds the request body for image-based recognition.
     *
     * @param base64Image The base64 encoded image string.
     * @param prompt The prompt that instructs the model how to interpret the image.
     * @return The request body string in JSON format.
     */
    public static String buildImageRequestBody(String base64Image, String prompt) {
        JSONObject payload = new JSONObject();
        payload.put("model", "gpt-4o");

        // Build message array
        JSONArray messages = new JSONArray();

        // User message
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");

        JSONArray contentArray = new JSONArray();

        // Text part (safely escape quotes)
        JSONObject textContent = new JSONObject();
        textContent.put("type", "text");
        textContent.put("text", prompt.replace("\"", "\\\"")); // Escape double quotes
        contentArray.put(textContent);

        // Image part
        JSONObject imageContent = new JSONObject();
        imageContent.put("type", "image_url");
        JSONObject imageUrl = new JSONObject();
        imageUrl.put("url", "data:image/jpeg;base64," + base64Image);
        imageContent.put("image_url", imageUrl);
        contentArray.put(imageContent);

        userMessage.put("content", contentArray);
        //userMessage.put("content", "Hello!");
        messages.put(userMessage);

        payload.put("messages", messages);

        LogUtils.debug("request body: " + payload.toString());
        return payload.toString();
    }


    /**
     * Builds the request body for text-based recognition.
     *
     * @param prompt The prompt to analyze the text and extract bill details.
     * @return The request body string in JSON format.
     */
    public static String buildTextRequestBody(String prompt) {
        JSONObject payload = new JSONObject();
        payload.put("model", "gpt-4o");

        // Build message array
        JSONArray messages = new JSONArray();

        // User message
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");

        JSONArray contentArray = new JSONArray();

        // Text part (safely escape quotes)
        JSONObject textContent = new JSONObject();
        textContent.put("type", "text");
        textContent.put("text", prompt.replace("\"", "\\\"")); // Escape double quotes
        contentArray.put(textContent);

        userMessage.put("content", contentArray);
        messages.put(userMessage);

        payload.put("messages", messages);

        LogUtils.debug("request body: " + payload.toString());
        return payload.toString();
    }

    /**
     * Makes an API call to the multi-model service for bill recognition.
     *
     * @param requestBody The request body in JSON format.
     * @param callback The callback to handle success or failure.
     * @throws IOException If an I/O error occurs during the API call.
     */
    private static void multimodelAPICalling(String requestBody, RecognitionCallback callback) throws IOException {

        RequestBody body = RequestBody.create(
                requestBody,
                MediaType.get("application/json")
        );

        // Set up the request
        Request request = new Request.Builder()
                .url(API_URL + "/v1/chat/completions")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + API_TOKEN)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtils.error("API calling error: " + e.getMessage());
                callback.onFailure(e);
                doneFlag.set(true);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try (ResponseBody respBody = response.body()) {
                    if (!response.isSuccessful()) {
                        String msg = respBody != null ? respBody.string() : "empty body";
                        IOException err = new IOException("HTTP " + response.code() + ": " + msg);
                        LogUtils.warn("Transcription failed: " + err.getMessage());
                        callback.onFailure(err);
                        return;
                    }
                    String text = respBody.string();
                    JSONObject json = new JSONObject(text);
                    JSONArray choices = json.getJSONArray("choices");
                    String content = choices
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");
                    content = removeFirstAndLastLine(content);
                    LogUtils.info(content);
                    Object contents = JsonUtils.parseJsonValidation(content);
                    Transaction transaction = TransactionManager.getByJSON(contents);

                    callback.onSuccess(transaction);
                } catch (Exception ex) {
                    LogUtils.error("Error processing response: " + ex.getMessage());
                    callback.onFailure(ex);
                } finally {
                    doneFlag.set(true);
                }
            }
        });
    }

    /**
     * Removes the first and last line from a given string (usually for cleaning up the response content).
     *
     * @param inputString The input string to be processed.
     * @return The cleaned-up string without the first and last line.
     */
    public static String removeFirstAndLastLine(String inputString) {

        String[] lines = inputString.split("\n");

        if (lines.length <= 2) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        for (int i = 1; i < lines.length - 1; i++) {
            result.append(lines[i]).append("\n");
        }

        return result.toString().trim();
    }

    /**
     * Writes the recognized data to a JSON file.
     *
     * @param path The file path where the JSON data should be saved.
     * @param data The JSON data as a string.
     */
    public static void writeDataToJson(String path, String data) {
        try {

            String[] lines = data.trim().split("\n");
            StringBuilder filteredDataBuilder = new StringBuilder();
            if (lines[0].trim().equals("[")) filteredDataBuilder.append(lines[0].trim());
            for (int i = 1; i < lines.length - 1; i++) {
                filteredDataBuilder.append(lines[i].trim());
            }
            if (lines[lines.length - 1].trim().equals("]")) filteredDataBuilder.append(lines[lines.length - 1].trim());

            // Validate the JSON format
            JSONArray jsonArray = new JSONArray(new JSONTokener(filteredDataBuilder.toString()));

            // Write to file
            try (FileWriter fileWriter = new FileWriter(path)) {
                fileWriter.write(jsonArray.toString());
                LogUtils.debug("Write successfully");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Asynchronously recognizes the content of an image and returns a transaction object.
     *
     * @param base64Image The base64 encoded image string.
     * @return A CompletableFuture that will contain the recognized transaction details.
     */
    public static CompletableFuture<Transaction> imageRecognitionAsync(String base64Image) {
        CompletableFuture<Transaction> cf = new CompletableFuture<>();
        try {
            User currentUser = ApplicationRuntime.getInstance().getCurrentUser();
            List<String> categoryList = currentUser.getCategory();
            String categories = String.join("", categoryList);
            categories = categories.replaceAll("\\\\(.)", "$1");

            String prompt = buildCapturePrompt(categories);
            String body = buildImageRequestBody(base64Image, prompt);

            multimodelAPICalling(body, new RecognitionCallback() {
                @Override
                public void onSuccess(Transaction transactions) {
                    cf.complete(transactions);
                }

                @Override
                public void onFailure(Throwable e) {
                    cf.completeExceptionally(e);
                }
            });
        } catch (Exception e) {
            LogUtils.error("Image recognition error: " + e.getMessage());
            cf.completeExceptionally(e);
        }
        return cf;
    }

    /**
     * Asynchronously recognizes the content of a text and returns a transaction object.
     *
     * @param text The text to be analyzed.
     * @return A CompletableFuture that will contain the recognized transaction details.
     */
    public static CompletableFuture<Transaction> textRecognitionAsync(String text) {
        CompletableFuture<Transaction> cf = new CompletableFuture<>();
        try {
            User currentUser = ApplicationRuntime.getInstance().getCurrentUser();
            List<String> categoryList = currentUser.getCategory();
            String categories = String.join("", categoryList);
            categories = categories.replaceAll("\\\\(.)", "$1");

            text = TransactionManager.escapeString(text);
            String prompt = buildTextPrompt(text, categories);
            String body = buildTextRequestBody(prompt);

            multimodelAPICalling(body, new RecognitionCallback() {
                @Override
                public void onSuccess(Transaction transactions) {
                    cf.complete(transactions);
                }

                @Override
                public void onFailure(Throwable e) {
                    cf.completeExceptionally(e);
                }
            });
        } catch (Exception e) {
            LogUtils.error("Text recognition error: " + e.getMessage());
            cf.completeExceptionally(e);
        }
        return cf;
    }


    public static void main(String[] args) {

//        try {
//            byte[] imageData = ImageUtils.loadCompressImage("C:\\Users\\29772\\OneDrive - Queen Mary, University of London\\Pictures\\Screenshots\\屏幕截图 2025-05-23 173712.png");
//            String jsonDataPath = "./data.json";
//            String base64Image = ImageUtils.toBase64(imageData);
////            User currentUser = ApplicationRuntime.getInstance().getCurrentUser();
////            List<String> categoryList = currentUser.getCategory();
//            List<String> categoryList = Arrays.asList("Electronics\\nSmartphones", "Home\\tAppliances", "Books\\\"Fiction");
//            String category = String.join("",categoryList);
//            category = category.replaceAll("\\\\(.)", "$1");
//
//            String requestBody = buildTextRequestBody(buildTextPrompt(category));
//            String response = multimodelAPICalling(requestBody);
//            LogUtils.debug(response);
//            writeDataToJson(jsonDataPath, response);
//        } catch (IOException e) {
//            LogUtils.error(e.getMessage());
//            throw new RuntimeException(e);
//        }

    }
}
