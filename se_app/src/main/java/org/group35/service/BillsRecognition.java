package org.group35.service;

import okhttp3.*;
import org.group35.util.ImageUtils;
import org.group35.util.LogUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class BillsRecognition {
    private static final String API_URL = "https://api.gptsapi.net";  // model API URL
    private static final String API_TOKEN = "sk-id8a932449e17e32258e1565c2ab579825ad061b479cbtQr";  // model API token

    public BillsRecognition(double savingGoal, double monIncome){
    }

    private String buildCapturePrompt(){

        //TODO: Customized user input
        // 文本参数
        String promptText = "Please analyze the image containing bill details and generate a JSON-formatted output. Each entry in the JSON should include the following fields: Description, Amount, Merchant, and Date.\n" +
                "\n" +
                "If the bill does not contain Merchant information, this field can be left empty.\n" +
                "If the bill does not contain a Description, please generate a brief description based on the bill content (no more than 20 words).\n" +
                "Output the result as a JSON structure that can be directly saved to a file. Ensure the output strictly follows this JSON format:\n" +
                "[\n" +
                "{\n" +
                "\"Description\": \"...\",\n" +
                "\"Amount\": \"...\",\n" +
                "\"Merchant\": \"...\",\n" +
                "\"Date\": \"...\"\n" +
                "},\n" +
                "{\n" +
                "\"Description\": \"...\",\n" +
                "\"Amount\": \"...\",\n" +
                "\"Merchant\": \"...\",\n" +
                "\"Date\": \"...\"\n" +
                "}\n" +
                "]\n" +
                "Please ensure the output is in a valid JSON file format and exclude any additional information or explanations. Return only the JSON data structured as specified.";
        return promptText;
    }


    public String buildRequestBody(String base64Image, String prompt) {
        JSONObject payload = new JSONObject();
        payload.put("model", "gpt-4o");
//        payload.put("temperature", 1);
//        payload.put("max_tokens", 2048);
//        payload.put("top_p", 1);

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
        contentArray.put(imageContent);

        userMessage.put("content", contentArray);
        //userMessage.put("content", "Hello!");
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


        LogUtils.debug("request body: " + payload.toString());
        return payload.toString();
    }

    private String multimodelAPICalling(String requestBody) throws IOException {

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
                .url(API_URL + "/v1/chat/completions")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + API_TOKEN)
                .post(body)
                .build();

        // 发送请求并获取响应
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                LogUtils.info("Request successfully, status code: " + response.code());
                // 打印成功响应内容
                String responses = response.body().string();
                LogUtils.debug(response.message());
                JSONObject responseJson = new JSONObject(responses);
                JSONArray choices = responseJson.getJSONArray("choices");
                JSONObject message = choices.getJSONObject(0).getJSONObject("message");
                String content = message.getString("content");
                // LogUtils.debug("Response: " + content);
                return content;
            } else {
                // 打印失败的响应状态码及响应内容
                LogUtils.warn("Request failed, status code: " + response.code());
                LogUtils.debug("Response body: " + response.body().string());
                return null;
            }
        } catch (IOException e) {
            LogUtils.error(e.getMessage());
            //e.printStackTrace();
            return null;
        }
    }
    public void writeDataToJson(String path, String data){
        try {

            String[] lines = data.trim().split("\n");
            StringBuilder filteredDataBuilder = new StringBuilder();
            if(lines[0].trim().equals("[") )filteredDataBuilder.append(lines[0].trim());
            for (int i = 1; i < lines.length - 1; i++) {
                filteredDataBuilder.append(lines[i].trim());
            }
            if(lines[lines.length - 1].trim().equals("]")) filteredDataBuilder.append(lines[lines.length - 1].trim());

            // 验证 JSON 格式
            JSONArray jsonArray = new JSONArray(new JSONTokener(filteredDataBuilder.toString()));

            //TODO: 使用JsonUtil
            try (FileWriter fileWriter = new FileWriter(path)){
                fileWriter.write(jsonArray.toString());
                LogUtils.debug("Write successfully");
            }
//            File file = new File(path);
//            JsonUtils.writeJsonToFile(file, jsonArray);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        BillsRecognition ass = new BillsRecognition(7000,5000);

        try {
            byte[] imageData = ImageUtils.loadCompressImage("C:\\Users\\29772\\OneDrive - Queen Mary, University of London\\Documents\\GitHub\\bupt_se_group_35\\se_app\\src\\main\\resources\\org\\group35\\model\\img.png");
            String jsonDataPath = "./data.json";
            String base64Image = ImageUtils.toBase64(imageData);
            String requestBody = ass.buildRequestBody(base64Image, ass.buildCapturePrompt());
            String response = ass.multimodelAPICalling(requestBody);
            LogUtils.debug(response);
            ass.writeDataToJson(jsonDataPath, response);
        } catch (IOException e) {
            LogUtils.error(e.getMessage());
            throw new RuntimeException(e);
        }

    }
}
