package org.group35.service;

import okhttp3.*;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import org.group35.util.LogUtils;

public class AudioRecognition {

    private static final String API_URL = "https://api.gptsapi.net/v1/audio/transcriptions";
    private static final String API_TOKEN = "sk-id8a932449e17e32258e1565c2ab579825ad061b479cbtQr";  // model API token

    public AudioRecognition (){
    }
    private static String buildAudioPrompt(){
        String promptText = "";
        return promptText;
    }

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // 连接超时
            .writeTimeout(60, TimeUnit.SECONDS) // 写入超时（对于上传大文件很重要）
            .readTimeout(60, TimeUnit.SECONDS) // 读取超时（转录可能需要一些时间）
            .build();

    public static void main(String[] args) {

        String filePath = "path/to/your/audiofile.mp3"; // 支持的格式: mp3, mp4, mpeg, mpga, m4a, wav, webm

        File audioFile = new File(filePath);
        if (!audioFile.exists()) {
            System.err.println("错误：在 '" + filePath + "' 未找到音频文件。");
            return;
        }

        // OpenAI API 对音频文件大小限制为 25MB
        if (audioFile.length() > 25 * 1024 * 1024) {
            System.err.println("错误：音频文件大小超过 25MB 限制。");
            return;
        }

        try {
            String transcribedText = transcribeAudio(audioFile, buildAudioPrompt());
            System.out.println("转录文本:");
            System.out.println(transcribedText);
        } catch (Exception e) {
            System.err.println("转录过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String transcribeAudio(File audioFile, String prompt) throws IOException {
        String mimeType = getMimeType(audioFile);

        if (mimeType == null) {
            System.err.println("警告: 无法确定文件 " + audioFile.getName() + " 的 MIME 类型。将尝试使用 'application/octet-stream'。");
            mimeType = "application/octet-stream"; // OpenAI 应该仍然可以处理它，但最好提供正确的类型
        }

        RequestBody fileBody = RequestBody.create(audioFile, MediaType.parse(mimeType));

        String model = "whisper-1";

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", audioFile.getName(), fileBody)
                .addFormDataPart("model", model)
                .addFormDataPart("language", "en")
                .addFormDataPart("prompt", prompt)
                .build();

        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_TOKEN)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                LogUtils.info("Request successfully, status code: " + response.code());
                String responseBodyString = response.body() != null ? response.body().string() : null;
                if (responseBodyString == null || responseBodyString.isEmpty()) {
                    LogUtils.warn("Return an empty response body");
                }

                JSONObject jsonResponse = new JSONObject(responseBodyString);

                if (jsonResponse.has("text")) {
                    return jsonResponse.getString("text");
                } else {
                    LogUtils.error("RuntimeException: cannot find \"text\" body");
                    return null;
                }

            } else {
                // 打印失败的响应状态码及响应内容
                LogUtils.warn("Request failed, status code: " + response.code());
                LogUtils.debug("Response body: " + response.body().string());
                return null;
            }


        } catch (Exception e) {
            LogUtils.error(e.getMessage());
            return null;
        }
    }

    /**
     * 根据文件扩展名尝试获取 MIME 类型。
     * @param file 要检查的文件
     * @return 文件的 MIME 类型字符串，如果无法确定则返回 null
     */
    private static String getMimeType(File file) {
        try {
            String contentType = Files.probeContentType(file.toPath());
            if (contentType != null) {
                return contentType;
            }
        } catch (IOException e) {
            System.err.println("读取文件MIME类型时发生IO错误: " + file.getName() + " - " + e.getMessage());
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
}