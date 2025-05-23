package org.group35.service;

import okhttp3.*;
import org.group35.util.LogUtils;
import org.group35.util.TimezoneUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;


public class AIAssistant {
    private static final String API_URL = "https://api.deepseek.com/chat/completions";  // DeepSeek API URL
    private static final String API_TOKEN = "sk-8c5a64ad52574f3e93a27b2d97055aab";  // DeepSeek API token

    private static OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();
    public AIAssistant(){

    }

    private static String buildSavingExpensesSuggestionPrompt( BigDecimal goal, String usrContent){
        //TODO: Customized user input
        String prompt = "Smart Financial Assistant for customizing users' suggestions.\\n" +
                "You are an intelligent financial assistant dedicated to helping users achieve their financial goals through expert analysis and personalized advice.\\n" +
                "Goals:\\n" +
                "1. Analyze the user's financial records.\\n" +
                "2. Provide a categorized list of income and expenses in JSON format.\\n" +
                "3. Offer reliable and feasible suggestions for saving expenses for next month based on monthly income and savings goal.\\n" +
                "## Constraints\\n" +
                "- Adhere to financial ethics and regulations.\\n" +
                "- Provide rational, professional advice.\\n" +
                "## Input Data\\n" +
                "JSON Records :\\n" + usrContent +
                "Do not need to consider the fixed income, only consider the amount in the transaction. The amount is positive means income and negtive means expense\\n" +
                "Expected budget(expense): " + goal + " yuan per month\\n" +
                "Savings Timeline: 1 months\\n" +
                "## Task\\n" +
                "1. Categorize the JSON records into different expense types .\\n" +
                "2. Analize the amount in each categorize.\\n" +
                "3. Analyze the data with the fixed income, and timeline to suggest:\\n" +
                "   - Monthly savings potential.\\n" +
                "   - Areas to reduce spending.\\n" +
                "   - Feasible plan of meeting the expected expenses.\\n" ;
        return prompt;
    }
    private static String buildAIRecommendationPrompt(String location, String Date, String transactionData) {
        return "As a financial advisor specialized in regional expenditure patterns, analyze the user's location and upcoming local holidays.\\n" +
                "User Context:\\n"+
                "- Location: " + location + "\\n" +
                "- Current Date: " + Date + "\\n" +
                "- Transaction History (JSON):\\n" + transactionData + "\\n\\n" +
                "The amount is positive means income and negtive means expense\\n" +
                "Tasks:\\n"+
                "1. Identify ONE upcoming holiday in/after " + Date + " within the next 90 days that typically causes high spending.\\n" +
                "2. Calculate days remaining until this holiday.\\n"+
                "3. Estimate potential extra expenses based on:\\n"+
                "   a) Local holiday traditions\\n"+
                "   b) User's historical spending during similar periods\\n"+
                "4. Generate concise recommendations including:\\n"+
                "   - Specific expense categories at risk\\n"+
                "   - Suggested budget preparation\\n"+
                "   - Practical saving strategies\\n"+
                "Response Requirements:\\n"+
                "- 150 words max\\n"+
                "- Use natural paragraph breaks (no numbering)\\n"+
                "- Mention days remaining, monetary amounts, and concrete action steps\\n" +
                "- Present it in the form of paragraphs and do not use labels for segmentation\\n" +
                "- No other answers should appear except for the content required to be answered";
    }
    private static String buildAISummaryPrompt(String usrContent){
        String prompt = "Smart Financial Assistant for summarizing spending patterns.\\n" +
                "You are an intelligent financial assistant dedicated to helping users understand their spending patterns through concise and insightful summaries.\\n" +
                "Goals:\\n" +
                "1. Analyze the user's financial records.\\n" +
                "2. Generate a brief, one-sentence summary of the user's spending habits.\\n" +
                "## Constraints\\n" +
                "3. Offer reliable and feasible suggestions for saving expenses for next month based on monthly income and savings goal.\\n" +
                "## Constraints\\n" +
                "- Provide a concise, human-readable summary without mentioning specific numbers.\\n" +
                "- Compare essential expenses with discretionary spending .\\n" +
                "- Use natural language that feels personal and helpful.\\n" +
                "- Focus on highlighting the balance or imbalance between different spending categories.\\n" +
                "## Input Data\\n" +
                "JSON Records :\\n" + usrContent +
                "The amount is positive means income and negtive means expense\\n" +
                "## Task\\n" +
                "1. Analyze the data to identify major spending categories.\\n" +
                "2. Determine the balance between essential and discretionary spending.\\n" +
                "3. Return ONLY a single sentence summary that describes the overall spending pattern.\\n" +
                "4. Example format: \\\"Your recent spending shows some essential expenses such as rent and utilities, along with significant discretionary spending on dining out and entertainment.\\\"\\n" +
                "5. Do NOT include any specific monetary values or percentages in your summary.\\n" +
                "6. Explicitly mention the major categories you identified in a way that gives the user insight about their spending priorities.\\n";
        return prompt;
    }

    private static CompletableFuture<String> DeepSeekCalling(String prompts) throws IOException {
        CompletableFuture<String> future = new CompletableFuture<>();
        // 修正JSON格式，特别是转义字符
        String requestBodyString = "{\n" +
                "  \"messages\": [\n" +
                "    {\n" +
                "      \"content\": \"I am a Smart Financial Assistant. I will help you analyze your financial records and provide personalized savings advice.\",\n" +
                "      \"role\": \"system\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"content\": \"Role: Smart Financial Assistant\\n" + prompts.replace("\\", "\\\\").replace("\"", "\\\"") + "\",\n" +
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
        LogUtils.debug("Request Body: \n" + requestBodyString);

        MediaType mediaType = MediaType.get("application/json");
        RequestBody body = RequestBody.create(mediaType, requestBodyString);

        // 设置请求
        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + API_TOKEN)
                .post(body)
                .build();
        // new
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                LogUtils.error("DeepSeek API call failed: " + e.getMessage());
                future.completeExceptionally(e);
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody rb = response.body()) {
                    if (!response.isSuccessful() || rb == null) {
                        String msg = "DeepSeek API returned " + response.code();
                        LogUtils.warn(msg);
                        future.completeExceptionally(new IOException(msg));
                        return;
                    }
                    String resp = rb.string();
                    JSONObject json = new JSONObject(resp);
                    JSONArray choices = json.getJSONArray("choices");
                    String content = choices
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");
                    future.complete(content);
                }
            }
        });

        return future;
//        // 发送请求并获取响应
//        ResponseBody responseBody = null;
//        try {
//            Response response = client.newCall(request).execute();
//            responseBody = response.body();
//
//            if (response.isSuccessful()) {
//                LogUtils.info("Request successfully, status code: " + response.code());
//                // 打印成功响应内容
//                String responses = responseBody.string();
//                LogUtils.debug(responses);
//                JSONObject responseJson = new JSONObject(responses);
//                JSONArray choices = responseJson.getJSONArray("choices");
//                JSONObject message = choices.getJSONObject(0).getJSONObject("message");
//                String content = message.getString("content");
//                LogUtils.debug("Response: " + content);
//                return content;
//            } else {
//                // 打印失败的响应状态码及响应内容
//                LogUtils.warn("Request failed, status code: " + response.code());
//                if (responseBody != null) {
//                    LogUtils.debug("Response body: " + responseBody.string());
//                }
//
//                return "";
//            }
//        } catch (IOException e) {
//            LogUtils.error(e.getMessage());
//            //e.printStackTrace();
//
//            return "";
//        } finally {
//            if (responseBody != null) {
//                responseBody.close();
//            }
//        }
    }
    //api
    public static CompletableFuture<String> AISuggestion(BigDecimal userSavingGoal, String stringContent) {
        try{
            CompletableFuture<String> response = DeepSeekCalling(buildSavingExpensesSuggestionPrompt(userSavingGoal, stringContent));
            // LogUtils.debug(response);
            return response;
        } catch (IOException e) {
            LogUtils.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public static CompletableFuture<String> AISummary(String stringContent) {
        try{
            CompletableFuture<String> response = DeepSeekCalling(buildAISummaryPrompt(stringContent));
            // LogUtils.debug(response);
            return response;
        } catch (IOException e) {
            LogUtils.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public static CompletableFuture<String> AIRecommendation(String location, String stringContent) throws IOException {
        String localTime = TimezoneUtils.getLocalTime(location);
        try{
            CompletableFuture<String> response = DeepSeekCalling(buildAIRecommendationPrompt(location, localTime, stringContent));
            // LogUtils.debug(response);
            return response;
        } catch (IOException e) {
            LogUtils.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) throws IOException {
        //api using example
//        User usr = UserManager.getCurrentUser();
//        BigDecimal budget = usr.getMonthlyBudget();
        BigDecimal budget = BigDecimal.valueOf(5000);
        String location = "Tokyo, Japan";
        //String stringContent = TransactionUtils.transferTransaction();
        String stringContent = "2025-04-01,expense,10000,Supermarket\\n" +
                "2025-04-03,expense,1000,food\\n" +
                "2025-04-05,expense,3000,Utilities\\n"; // data in any format

        CompletableFuture<String> suggFuture = AISuggestion(budget, stringContent);
        CompletableFuture<String> summFuture = AISummary(stringContent);
        CompletableFuture<String> recFuture = AIRecommendation(location, stringContent);

        // Wait for all futures to complete (for demonstration)
        CompletableFuture.allOf(suggFuture, summFuture, recFuture).join();

        LogUtils.info(suggFuture.join());
        LogUtils.info(summFuture.join());
        LogUtils.info(recFuture.join());

    }
}