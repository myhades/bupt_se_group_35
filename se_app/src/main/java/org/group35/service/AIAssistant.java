package org.group35.service;

import okhttp3.*;
import org.group35.controller.TransactionManager;
import org.group35.model.Transaction;
import org.group35.util.LogUtils;
import org.group35.util.TimezoneUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;


public class AIAssistant {
    private static final String API_URL = "https://api.deepseek.com/chat/completions";  // DeepSeek API URL
    private static final String API_TOKEN = "sk-8c5a64ad52574f3e93a27b2d97055aab";  // DeepSeek API token

    private static final AtomicBoolean doneFlag = new AtomicBoolean(false);
    private static OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();
    public AIAssistant(){

    }
    public interface RecognitionCallback {
        void onSuccess(String content);
        void onFailure(Throwable e);
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

    private static void DeepSeekCalling(String prompts, RecognitionCallback callback) throws IOException {
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
                LogUtils.error("API calling error:" + e.getMessage());
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

                    callback.onSuccess(content);
                } catch (Exception ex) {
                    LogUtils.error("处理响应时出错" + ex.getMessage());
                    callback.onFailure(ex);
                }finally {
                    doneFlag.set(true);
                }
            }
        });

    }
    //api
    public static CompletableFuture<String> AISuggestionAsyn(BigDecimal userSavingGoal, String stringContent) {
        CompletableFuture<String> response = new CompletableFuture<>();
        try{
            String prompt = buildSavingExpensesSuggestionPrompt(userSavingGoal, stringContent);
            DeepSeekCalling(prompt, new RecognitionCallback() {
                @Override
                public void onSuccess(String content) {
                    response.complete(content);
                }

                @Override
                public void onFailure(Throwable e) {
                    response.completeExceptionally(e);
                }
            });
            // LogUtils.debug(response);
            return response;
        } catch (IOException e) {
            LogUtils.error(e.getMessage());
            response.completeExceptionally(e);
        }
        return response;
    }
    public static CompletableFuture<String> AISummaryAsyn(String stringContent) {
        CompletableFuture<String> response = new CompletableFuture<>();
        try{
            String prompt = buildAISummaryPrompt(stringContent);
            DeepSeekCalling(prompt, new RecognitionCallback() {
                @Override
                public void onSuccess(String content) {
                    response.complete(content);
                }

                @Override
                public void onFailure(Throwable e) {
                    response.completeExceptionally(e);
                }
            });
            // LogUtils.debug(response);
            return response;
        } catch (IOException e) {
            LogUtils.error(e.getMessage());
            response.completeExceptionally(e);
        }
        return response;
    }
    public static CompletableFuture<String> AIRecommendationAsyn(String location, String stringContent) throws IOException {
        CompletableFuture<String> response = new CompletableFuture<>();
        try{
            String localTime = TimezoneUtils.getLocalTime(location);
            String prompt = buildAIRecommendationPrompt(location, localTime, stringContent);
            DeepSeekCalling(prompt, new RecognitionCallback() {
                @Override
                public void onSuccess(String content) {
                    response.complete(content);
                }

                @Override
                public void onFailure(Throwable e) {
                    response.completeExceptionally(e);
                }
            });
            // LogUtils.debug(response);
            return response;
        } catch (IOException e) {
            LogUtils.error(e.getMessage());
            response.completeExceptionally(e);
        }
        return response;

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

        CompletableFuture<String> suggFuture = AISuggestionAsyn(budget, stringContent);
        CompletableFuture<String> summFuture = AISummaryAsyn(stringContent);
        CompletableFuture<String> recFuture = AIRecommendationAsyn(location, stringContent);

        // Wait for all futures to complete (for demonstration)
        CompletableFuture.allOf(suggFuture, summFuture, recFuture).join();

        LogUtils.info(suggFuture.join());
        LogUtils.info(summFuture.join());
        LogUtils.info(recFuture.join());

    }
}