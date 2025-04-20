package org.group35.AIModel;

import okhttp3.*;
import org.group35.util.LoggerHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AIAssistant {
    private static final String API_URL = "https://api.deepseek.com/chat/completions";  // DeepSeek API URL
    private static final String API_TOKEN = "sk-8c5a64ad52574f3e93a27b2d97055aab";  // DeepSeek API token
    private double savingGoal;
    private double monIncome;
    public AIAssistant(double savingGoal,double monIncome){
        this.savingGoal = savingGoal;
        this.monIncome = monIncome;
    }

    private String buildSavingExpensesSuggestionPrompt(String usrContent){
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
                "Fixed Income: " + 5000 + " yuan per month\\n" + //TODO: Customized user input
                "Expected expenses: " + 7000 + " yuan per month\\n" + //TODO: Customized user input
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

    private String buildAISummaryPrompt(String usrContent){
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

    private String DeepSeekCalling(String prompts) throws IOException {
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
        LoggerHelper.debug("Request Body: \n" + requestBodyString);

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
                LoggerHelper.info("Request successfully, status code: " + response.code());
                // 打印成功响应内容
                String responses = responseBody.string();
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
                if (responseBody != null) {
                    LoggerHelper.debug("Response body: " + responseBody.string());
                }

                return "";
            }
        } catch (IOException e) {
            LoggerHelper.error(e.getMessage());
            //e.printStackTrace();

            return "";
        } finally {
            if (responseBody != null) {
                responseBody.close();
            }
        }
    }

    public static void main(String[] args) {
        AIAssistant ass = new AIAssistant(7000,5000);
        String stringContent = "2025-04-01,expense,10000,Supermarket\\n" +
                "2025-04-03,expense,1000,food\\n" +
                "2025-04-05,expense,3000,Utilities\\n"; // data ,test
        try {
            // 调用原有函数
            String response = ass.DeepSeekCalling(ass.buildSavingExpensesSuggestionPrompt(stringContent));
            LoggerHelper.debug(response);

            // 调用AI Summary函数
            String aiSummary = ass.DeepSeekCalling(ass.buildAISummaryPrompt(stringContent));
            LoggerHelper.debug("AI Summary: " + aiSummary);

//            // 添加控制台输出，确保能看到结果
//            System.out.println("==================================");
//            System.out.println("AI Summary: " + aiSummary);
//            System.out.println("==================================");
        } catch (IOException e) {
            LoggerHelper.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}