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
    // 用来在回调到来时通知另一个线程停止
    private static final AtomicBoolean doneFlag = new AtomicBoolean(false);
    public AudioRecognition (){
    }
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
     * 回调接口，用于返回转写结果或错误
     */
    public interface TranscriptionCallback {
        void onSuccess(String transcription);
        void onFailure(Throwable error);
    }

    /**
     * 异步调用 Whisper 转写接口。
     *
     * @param audioBytes  WAV/其他音频的 byte 数组
     * @param callback    结果回调
     */
    private static void transcribeAudio(byte[] audioBytes, TranscriptionCallback callback) throws IOException {

        RequestBody fileBody = RequestBody.create(audioBytes, MediaType.parse("audio/wav"));

        String model = "whisper-1";

        String prompt = buildAudioPrompt();

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "audio.wav", fileBody)
                .addFormDataPart("model", model)
                .addFormDataPart("language", "en")
                .addFormDataPart("prompt", prompt)
                .addFormDataPart("response_format", "json")
                .build();

        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_TOKEN)
                .post(body)
                .build();

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
                        callback.onSuccess(json.getString("text"));
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
     * 从指定路径读取 WAV 文件并返回其 byte 数组。
     *
     * @param wavFilePath WAV 文件的绝对或相对路径
     * @return 文件内容的 byte 数组
     * @throws IOException           读取文件失败时抛出
     * @throws InvalidPathException  路径字符串非法时抛出
     * @throws NullPointerException  wavFilePath 为 null 时抛出
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

        // 直接读取所有字节到内存
        return Files.readAllBytes(path);
    }

    private static String getMimeType(File file) {
        try {
            String contentType = Files.probeContentType(file.toPath());
            if (contentType != null) {
                return contentType;
            }
        } catch (IOException e) {
            LogUtils.error("读取文件MIME类型时发生IO错误: " + file.getName() + " - " + e.getMessage());
        }

// 基于文件扩展名的回退机制
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".mp3")) return "audio/mpeg";
        if (fileName.endsWith(".wav")) return "audio/wav";
        if (fileName.endsWith(".m4a")) return "audio/mp4"; // audio/mp4 或 audio/x-m4a
        if (fileName.endsWith(".mp4")) return "audio/mp4";
        if (fileName.endsWith(".mpeg")) return "audio/mpeg";
        if (fileName.endsWith(".mpga")) return "audio/mpeg";
        if (fileName.endsWith(".webm")) return "audio/webm";

        return null; // 如果无法通过扩展名确定
    }

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


    public static void main(String[] args) {
        try {
            // 模拟输入（byte[] 类型）
            byte[] wavBytes = AudioRecognition.loadYourWavBytes("F:\\BUPT lessons\\JavaProject\\read.wav");

            // 用于存储数据
            AtomicReference<String> transcriptionResult = new AtomicReference<>();
            // 用线程池同时运行两个任务：（一个用于调用api，一个显示加载中……）
            ExecutorService executor = Executors.newFixedThreadPool(2);

            executor.submit(()->{
                try {
                    AudioRecognition.transcribeAudio(wavBytes, new TranscriptionCallback() {
                        @Override
                        public void onSuccess(String transcription) {
                            // 转写成功，回调到前端或 UI 线程
                            //LogUtils.info("success: " + transcription);
                            transcriptionResult.set(transcription);
                        }

                        @Override
                        public void onFailure(Throwable error) {
                            // 转写失败，回调通知
                            //LogUtils.error("fail: " + error.getMessage());
                            transcriptionResult.set(null);
                        }
                    });
                } catch (IOException e) {
                    LogUtils.error("Error:" + e.getMessage());
                    doneFlag.set(true);
                }
            });

            executor.submit(() -> {
                while (!doneFlag.get()) {
                    //
                    LogUtils.info("加载中...");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {}
                }
                LogUtils.info("gpt生成完成，可以进行渲染");
//                String result = transcriptionResult.get();
//                LogUtils.info(result);
            });

            LogUtils.info("do anything in main thread");
            // 可选，这将会阻塞主线程，这样可以将上述的渲染在主线程上进行
            executor.shutdown();// ExecutorService 停止接受新任务
            executor.awaitTermination(5, TimeUnit.MINUTES);//会等待直到所有任务完成
            LogUtils.info("main thread continue");
            String result = transcriptionResult.get();
            LogUtils.info(result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}