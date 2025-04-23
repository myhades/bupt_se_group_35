package org.group35.service;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.group35.util.LogUtils;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class DSTest{

    private static final String API_URL = "https://api.deepseek.com/chat/completions";  // DeepSeek API URL
    private static final String API_TOKEN = "sk-8c5a64ad52574f3e93a27b2d97055aab";  // DeepSeek API token

    public static void main(String[] args) {
        try {
            // 用户的财务记录示例
            String stringContent = "2025-04-01,expense,200,Supermarket\n" +
                    "2025-04-03,income,1000,Salary\n" +
                    "2025-04-05,expense,300,Utilities\n"; // 实际的财务记录数据
            int expectedExpenses = 5000;  // 预计支出
            int savingsTimeline = 1;      // 储蓄时间线（以月为单位）

            // 构建请求体，插入动态内容
            String requestBodyString = "{\n" +
                    "  \"messages\": [\n" +
                    "    {\n" +
                    "      \"content\": \"I am a Smart Financial Assistant. I will help you analyze your financial records and provide personalized savings advice.\",\n" +
                    "      \"role\": \"system\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"content\": \"Role: Smart Financial Assistant\\n\",\n" +
                    "      \"role\": \"user\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"model\": \"deepseek-chat\",\n" +
                    "  \"frequency_penalty\": 0,\n" +
                    "  \"max_tokens\": 2048,\n" +
                    "  \"presence_penalty\": 0,\n" +
                    "  \"response_format\": {\n" +
                    "    \"type\": \"text\"\n" +
                    "  },\n" +
                    "  \"stop\": null,\n" +
                    "  \"stream\": false,\n" +
                    "  \"stream_options\": null,\n" +
                    "  \"temperature\": 1,\n" +
                    "  \"top_p\": 1,\n" +
                    "  \"tools\": null,\n" +
                    "  \"tool_choice\": \"none\",\n" +
                    "  \"logprobs\": false,\n" +
                    "  \"top_logprobs\": null\n" +
                    "}";

            // 打印请求体以调试
            System.out.println("Request Body: \n" + requestBodyString);

            // 创建 OkHttpClient
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)  // 设置连接超时为30秒
                    .readTimeout(30, TimeUnit.SECONDS)     // 设置读取超时为30秒
                    .writeTimeout(30, TimeUnit.SECONDS)    // 设置写入超时为30秒
                    .build();
            MediaType mediaType = MediaType.get("application/json");
            RequestBody body = RequestBody.create(mediaType, requestBodyString);

            // 设置请求
            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + API_TOKEN)
                    .post(body)
                    .build();

            // 发送请求并获取响应
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    LogUtils.info("Request successfully, status code: " + response.code());
                    // 打印成功响应内容
                    JSONObject responseJson = new JSONObject(response);
                    JSONArray choices = responseJson.getJSONArray("choices");
                    JSONObject message = choices.getJSONObject(0).getJSONObject("message");
                    String content = message.getString("content");
                    System.out.println("Response: " + response.body().string());
                } else {
                    // 打印失败的响应状态码及响应内容
                    LogUtils.warn("Request failed, status code: " + response.code());
                    LogUtils.debug("Response body: " + response.body().string());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
