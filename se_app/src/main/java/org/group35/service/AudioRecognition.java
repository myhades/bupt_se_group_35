package org.group35.service;

import okhttp3.*;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.*;
import java.util.Objects;
import java.util.concurrent.atomic.*;

import org.group35.util.LogUtils;
import org.slf4j.helpers.SubstituteLogger;

public class AudioRecognition {

    private static final String API_URL = "https://api.gptsapi.net/v1/audio/transcriptions";
    private static final String API_TOKEN = "sk-id8a932449e17e32258e1565c2ab579825ad061b479cbtQr";  // model API token
    // Used to notify another thread when the callback arrives
    private static final AtomicBoolean doneFlag = new AtomicBoolean(false);
    public AudioRecognition (){
    }

    /**
     * Builds the audio transcription prompt.
     *
     * @return The prompt text.
     */
    private static String buildAudioPrompt(){
        String promptText = "This audio is about transactions";
        return promptText;
    }

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    /**
     * Callback interface for transcription result or error.
     */
    public interface TranscriptionCallback {
        void onSuccess(String transcription);
        void onFailure(Throwable error);
    }

    /**
     * Asynchronously calls the Whisper transcription API.
     *
     * @param audioBytes  The byte array of the WAV/audio file.
     * @param callback    The result callback.
     */
    private static void transcribeAudio(byte[] audioBytes, TranscriptionCallback callback) throws IOException {

        // Create the file body for the audio file to be uploaded
        RequestBody fileBody = RequestBody.create(audioBytes, MediaType.parse("audio/wav"));

        String model = "whisper-1";// Transcription model

        // Prepare the transcription prompt
        String prompt = buildAudioPrompt();

        // Build the multipart form for the request
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "audio.wav", fileBody)
                .addFormDataPart("model", model)
                .addFormDataPart("language", "en")
                .addFormDataPart("prompt", prompt)
                .addFormDataPart("response_format", "json")
                .build();

        // Create the request to the API
        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_TOKEN)// Include the API token in the header
                .post(body)
                .build();

        // Execute the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtils.error("Transcription request failed" + e.getMessage());
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

                    String jsonString = respBody != null ? respBody.string() : "";
                    JSONObject json = new JSONObject(jsonString);
                    if (json.has("text")) {
                        callback.onSuccess(json.getString("text"));// Return the transcription text
                    } else {
                        String errMsg = "No 'text' in response JSON";
                        LogUtils.error(errMsg);
                        callback.onFailure(new IllegalStateException(errMsg));
                    }
                } catch (Exception ex) {
                    LogUtils.error("Error processing transcription response" + ex.getMessage());
                    callback.onFailure(ex);
                }finally {
                    doneFlag.set(true);
                }
            }
        });
    }


    /**
     * Reads the WAV file from the specified path and returns its byte array.
     *
     * @param wavFilePath The absolute or relative path to the WAV file.
     * @return The byte array of the file content.
     * @throws IOException           If reading the file fails.
     * @throws InvalidPathException  If the path string is invalid.
     * @throws NullPointerException  If wavFilePath is null.
     */
    public static byte[] loadYourWavBytes(String wavFilePath) throws IOException {
        Objects.requireNonNull(wavFilePath, "wavFilePath must not be null");

        Path path = Paths.get(wavFilePath);
        if (!Files.exists(path)) {
            throw new IOException("WAV file not found: " + path.toAbsolutePath());
        }
        if (!Files.isReadable(path)) {
            throw new IOException("WAV file is not readable: " + path.toAbsolutePath());
        }

        // Read all bytes into memory
        return Files.readAllBytes(path);
    }


    /**
     * Gets the MIME type of the file.
     *
     * @param file The file to check.
     * @return The MIME type of the file.
     */
    private static String getMimeType(File file) {
        try {
            String contentType = Files.probeContentType(file.toPath());
            if (contentType != null) {
                return contentType;
            }
        } catch (IOException e) {
            LogUtils.error("Error reading MIME type of file: " + file.getName() + " - " + e.getMessage());
        }

        // Fallback based on file extension
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".mp3")) return "audio/mpeg";
        if (fileName.endsWith(".wav")) return "audio/wav";
        if (fileName.endsWith(".m4a")) return "audio/mp4"; // audio/mp4 or audio/x-m4a
        if (fileName.endsWith(".mp4")) return "audio/mp4";
        if (fileName.endsWith(".mpeg")) return "audio/mpeg";
        if (fileName.endsWith(".mpga")) return "audio/mpeg";
        if (fileName.endsWith(".webm")) return "audio/webm";

        return null; // If MIME type cannot be determined from the extension
    }

    /**
     * Asynchronously transcribes audio bytes and returns a CompletableFuture.
     *
     * @param audioBytes The byte array of the audio.
     * @return A CompletableFuture that will hold the transcription result.
     */
    public static CompletableFuture<String> transcribeAsync(byte[] audioBytes) {
        CompletableFuture<String> cf = new CompletableFuture<>();
        try {
            transcribeAudio(audioBytes, new TranscriptionCallback() {
                @Override
                public void onSuccess(String transcription) {
                    cf.complete(transcription);
                }
                @Override
                public void onFailure(Throwable error) {
                    cf.completeExceptionally(error);
                }
            });
        } catch (IOException e) {
            cf.completeExceptionally(e);
        }
        return cf;
    }


    /**
     * Main method to test transcription.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        try {
            // Simulate input (byte[] type)
            byte[] wavBytes = AudioRecognition.loadYourWavBytes("F:\\BUPT lessons\\JavaProject\\read.wav");

            // Used to store transcription result
            AtomicReference<String> transcriptionResult = new AtomicReference<>();
            // Create a thread pool to run two tasks simultaneously (one for the API call, one for loading message)
            ExecutorService executor = Executors.newFixedThreadPool(2);

            executor.submit(() -> {
                try {
                    AudioRecognition.transcribeAudio(wavBytes, new TranscriptionCallback() {
                        @Override
                        public void onSuccess(String transcription) {
                            // Transcription success, callback to the front end or UI thread
                            transcriptionResult.set(transcription);
                        }

                        @Override
                        public void onFailure(Throwable error) {
                            // Transcription failed, callback notification
                            transcriptionResult.set(null);
                        }
                    });
                } catch (IOException e) {
                    LogUtils.error("Error: " + e.getMessage());
                    doneFlag.set(true);
                }
            });

            executor.submit(() -> {
                while (!doneFlag.get()) {
                    // Show "Loading..." while waiting
                    LogUtils.info("Loading...");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {}
                }
                LogUtils.info("GPT transcription complete, can render the result");
            });

            LogUtils.info("Do anything in the main thread");
            // Optionally, block the main thread so rendering can be done in the main thread
            executor.shutdown(); // ExecutorService stops accepting new tasks
            executor.awaitTermination(5, TimeUnit.MINUTES); // Wait for all tasks to complete
            LogUtils.info("Main thread continues");

            String result = transcriptionResult.get();
            LogUtils.info(result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}