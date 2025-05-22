package org.group35.service;

import okhttp3.*;
import org.group35.util.LogUtils;
import org.group35.util.TimeZoneUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class AIAssistant {
    private static final String API_URL = "https://api.deepseek.com/chat/completions";  // DeepSeek API URL
    private static final String API_TOKEN = "sk-8c5a64ad52574f3e93a27b2d97055aab";  // DeepSeek API token

    public AIAssistant(){

    }

    private static String buildSavingExpensesSuggestionPrompt(int income, int goal, String usrContent){
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
                "JSON Records (date,type,amount,merchant):\\n" + usrContent +
                "Fixed Income: " + income + " yuan per month\\n" + //TODO: Customized user input
                "Expected expenses: " + goal + " yuan per month\\n" + //TODO: Customized user input
                "Savings Timeline: 1 months\\n" +
                "## Task\\n" +
                "1. Categorize the JSON records into different expense types (e.g., food, utilities, salary ...).\\n" +
                "2. Return a JSON object with the categorized list.\\n" +
                "3. Analyze the data with the fixed income, savings goal, and timeline to suggest:\\n" +
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
                "- Compare essential expenses (housing, utilities, groceries) with discretionary spending (entertainment, dining, shopping).\\n" +
                "- Use natural language that feels personal and helpful.\\n" +
                "- Focus on highlighting the balance or imbalance between different spending categories.\\n" +
                "## Input Data\\n" +
                "JSON Records (date,type,amount,merchant):\\n" + usrContent +
                "## Task\\n" +
                "1. Analyze the data to identify major spending categories.\\n" +
                "2. Determine the balance between essential and discretionary spending.\\n" +
                "3. Return ONLY a single sentence summary that describes the overall spending pattern.\\n" +
                "4. Example format: \\\"Your recent spending shows some essential expenses such as rent and utilities, along with significant discretionary spending on dining out and entertainment.\\\"\\n" +
                "5. Do NOT include any specific monetary values or percentages in your summary.\\n" +
                "6. Explicitly mention the major categories you identified in a way that gives the user insight about their spending priorities.\\n";
        return prompt;
    }

    private static String DeepSeekCalling(String prompts) throws IOException {
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

        // 创建 OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)  // 设置连接超时为30秒
                .readTimeout(60, TimeUnit.SECONDS)     // 设置读取超时为30秒
                .writeTimeout(60, TimeUnit.SECONDS)    // 设置写入超时为30秒
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
        ResponseBody responseBody = null;
        try {
            Response response = client.newCall(request).execute();
            responseBody = response.body();

            if (response.isSuccessful()) {
                LogUtils.info("Request successfully, status code: " + response.code());
                // 打印成功响应内容
                String responses = responseBody.string();
                LogUtils.debug(responses);
                JSONObject responseJson = new JSONObject(responses);
                JSONArray choices = responseJson.getJSONArray("choices");
                JSONObject message = choices.getJSONObject(0).getJSONObject("message");
                String content = message.getString("content");
                LogUtils.debug("Response: " + content);
                return content;
            } else {
                // 打印失败的响应状态码及响应内容
                LogUtils.warn("Request failed, status code: " + response.code());
                if (responseBody != null) {
                    LogUtils.debug("Response body: " + responseBody.string());
                }

                return "";
            }
        } catch (IOException e) {
            LogUtils.error(e.getMessage());
            //e.printStackTrace();

            return "";
        } finally {
            if (responseBody != null) {
                responseBody.close();
            }
        }
    }
    //api
    public static String AISuggestion(int userSavingGoal, int userMonIncome, String stringContent) {
        try{
            String response = DeepSeekCalling(buildSavingExpensesSuggestionPrompt(userMonIncome, userSavingGoal, stringContent));
            LogUtils.debug(response);
            return response;
        } catch (IOException e) {
            LogUtils.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public static String AISummary(String stringContent) {
        try{
            String response = DeepSeekCalling(buildAISummaryPrompt(stringContent));
            LogUtils.debug(response);
            return response;
        } catch (IOException e) {
            LogUtils.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public static String AIRecommendation(String location, String stringContent) throws IOException {
        String localTime = TimeZoneUtil.getLocalTime(location);
        try{
            String response = DeepSeekCalling(buildAIRecommendationPrompt(location, localTime, stringContent));
            LogUtils.debug(response);
            return response;
        } catch (IOException e) {
            LogUtils.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        //api using example
//        int income = 5000, goal = 5000; // fixed income in a month, saving goal
        String stringContent = "2025-04-01,expense,10000,Supermarket\\n" +
                "2025-04-03,expense,1000,food\\n" +
                "2025-04-05,expense,3000,Utilities\\n"; // data in any format
//        String sugg = AISuggestion(income,goal,stringContent);
//        String summ = AISummary(stringContent);
//        LogUtils.info(sugg);
//        LogUtils.info(summ);
        String location = "Tokyo, Japan";
        String response = AIRecommendation(location, stringContent);
        LogUtils.info(response);

    }
}