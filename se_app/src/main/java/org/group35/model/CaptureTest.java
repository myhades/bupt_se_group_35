package org.group35.model;

import okhttp3.*;
import org.group35.util.LoggerHelper;
import org.group35.util.imageToBase64;
import org.group35.util.loadAndCompressImage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class CaptureTest {
    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";  // DeepSeek API URL
    private static final String API_TOKEN = "sk-8c5a64ad52574f3e93a27b2d97055aab";  // DeepSeek API token
    private double savingGoal;
    private double monIncome;
    public CaptureTest(double savingGoal,double monIncome){
        this.savingGoal = savingGoal;
        this.monIncome = monIncome;
    }

    private String buildCapturePrompt(){
        //TODO: Customized user input
        // 文本参数
        String promptText = "识别图片中内容，并返回一个json文件，以类别，金额，时间的格式总结每个输入输出";
        return promptText;
    }


    public String buildRequestBody(String base64Image, String prompt) {
        JSONObject payload = new JSONObject();
        payload.put("model", "deepseek-ai/Janus-Pro-7B");
        payload.put("temperature", 1);
        payload.put("max_tokens", 2048);
        payload.put("top_p", 1);

        // 构建消息数组
        JSONArray messages = new JSONArray();

        // 用户消息
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");

        JSONArray contentArray = new JSONArray();

        // 文本部分 (安全转义)
        JSONObject textContent = new JSONObject();
        textContent.put("type", "text");
        textContent.put("text", prompt.replace("\"", "\\\"")); // 转义双引号
        contentArray.put(textContent);

        // 图片部分
        JSONObject imageContent = new JSONObject();
        imageContent.put("type", "image_url");
        JSONObject imageUrl = new JSONObject();
        imageUrl.put("url", "data:image/jpeg;base64," + base64Image);
        imageContent.put("image_url", imageUrl);
        //contentArray.put(imageContent);

        userMessage.put("content", contentArray);
        messages.put(userMessage);

        payload.put("messages", messages);
//        JSONObject payload = new JSONObject();
//
//        // 基础参数
//        payload.put("model", "deepseek-chat");
//        payload.put("temperature", 0.7);
//        payload.put("max_tokens", 1024);
//
//        // 构建消息结构
//        JSONArray messages = new JSONArray();
//        JSONObject message = new JSONObject();
//        message.put("role", "user");
//
//        JSONArray content = new JSONArray();
//
//        // 文本部分
//        content.put(new JSONObject()
//                .put("type", "text")
//                .put("text", prompt));
//
//        // 图片部分
//        content.put(new JSONObject()
//                .put("type", "image_url")
//                .put("image_url", new JSONObject()
//                        .put("url", "data:image/jpeg;base64," + base64Image)));
//
//        message.put("content", content);
//        messages.put(message);
//
//        payload.put("messages", messages);


        // 构建请求体，插入动态内容
        String requestBodyString = "{\n" +
                "  \"messages\": [\n" +
                "    {\n" +
                "      \"content\": [\n" +
                "           {\n" +
                "              \"type\" : \"text\" ,\n" +
                "              \"text\": \"" + prompt + "\"\n" +
                "            },\n" +
                "           {\n" +
                "               \"type\": \"image_url\",\n" +
                "               \"image_url\": {\n" +
                "                   \"url\": \"data:image/jpeg;base64," + base64Image + "\"\n" +
                "                }\n" +
                "           }\n" +
                "      ],\n" +
                "      \"role\": \"user\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"model\": \"deepseek-multimodal\",\n" +
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
        LoggerHelper.debug("request body: " + payload.toString());
        return payload.toString();
    }

    private String DeepSeekCalling(String requestBody) throws IOException {

        // 创建 OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)  // 设置连接超时为30秒
                .readTimeout(60, TimeUnit.SECONDS)     // 设置读取超时为60秒
                .writeTimeout(30, TimeUnit.SECONDS)    // 设置写入超时为30秒
                .build();

        RequestBody body = RequestBody.create(
                requestBody,
                MediaType.get("application/json")
        );

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
                LoggerHelper.info("Request successfully, status code: " + response.code());
                // 打印成功响应内容
                String responses = response.body().string();
                LoggerHelper.debug(responses);
                JSONObject responseJson = new JSONObject(responses);
                JSONArray choices = responseJson.getJSONArray("choices");
                JSONObject message = choices.getJSONObject(0).getJSONObject("message");
                String content = message.getString("content");
                LoggerHelper.debug("Response: " + content);
                return content;
            } else {
                // 打印失败的响应状态码及响应内容
                LoggerHelper.warn("Request failed, status code: " + response.code());
                LoggerHelper.debug("Response body: " + response.body().string());
                return null;
            }
        } catch (IOException e) {
            LoggerHelper.error(e.getMessage());
            //e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        CaptureTest ass = new CaptureTest(7000,5000);

        try {
            byte[] imageData = loadAndCompressImage.loadCompressImage("C:\\Users\\29772\\OneDrive - Queen Mary, University of London\\Documents\\GitHub\\bupt_se_group_35\\se_app\\src\\main\\resources\\org\\group35\\model\\img.png");
            String base64Image = imageToBase64.IToBase64(imageData);
            String requestBody = ass.buildRequestBody(base64Image, ass.buildCapturePrompt());
            String response = ass.DeepSeekCalling(requestBody);
            LoggerHelper.debug(response);
        } catch (IOException e) {
            LoggerHelper.error(e.getMessage());
            throw new RuntimeException(e);
        }

    }
}
