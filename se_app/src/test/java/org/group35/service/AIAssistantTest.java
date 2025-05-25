package org.group35.service;

import okhttp3.*;
import org.group35.controller.TransactionManager;
import org.group35.controller.UserManager;
import org.group35.util.TimezoneUtils;
import org.group35.util.LogUtils;
import org.json.JSONObject;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class AIAssistantTest {

    private AIAssistant assistant;

    @BeforeEach
    void setUp() {
        assistant = new AIAssistant();
    }

    /**
     * Use reflection to call the private buildSavingExpensesSuggestionPrompt method
     * and verify that the goal and transaction JSON appear.
     */
    @Test
    void testBuildSavingExpensesSuggestionPrompt() throws Exception {
        BigDecimal goal = new BigDecimal("1500");
        String usrContent = "[{\"date\":\"2025-05-01\",\"amount\":-200,\"category\":\"Food\"}]";

        Method m = AIAssistant.class.getDeclaredMethod(
                "buildSavingExpensesSuggestionPrompt", BigDecimal.class, String.class);
        m.setAccessible(true);

        String prompt = (String) m.invoke(null, goal, usrContent);

        assertTrue(prompt.contains("Expected budget(expense): 1500"),
                "Should include the goal amount");
        assertTrue(prompt.contains(usrContent),
                "Should include the raw transaction JSON");
        assertTrue(prompt.contains("Savings Timeline: 1 months"),
                "Should include the hard‐coded timeline");
    }

    /**
     * Use reflection to call the private buildAIRecommendationPrompt method
     * and verify that location, date, and JSON appear.
     */
    @Test
    void testBuildAIRecommendationPrompt() throws Exception {
        String location = "Beijing";
        String date = "2025-07-20";
        String txData = "[{\"date\":\"2025-06-15\",\"amount\":5000,\"category\":\"Salary\"}]";

        Method m = AIAssistant.class.getDeclaredMethod(
                "buildAIRecommendationPrompt", String.class, String.class, String.class);
        m.setAccessible(true);

        String prompt = (String) m.invoke(null, location, date, txData);

        assertTrue(prompt.contains("Location: " + location),
                "Should include the location");
        assertTrue(prompt.contains("Current Date: " + date),
                "Should include the date");
        assertTrue(prompt.contains(txData),
                "Should include the transaction history JSON");
        assertTrue(prompt.contains("upcoming holiday"),
                "Should mention holidays");
    }

    /**
     * Use reflection to call the private buildAISummaryPrompt method
     * and verify that the JSON and summary instructions appear.
     */
    @Test
    void testBuildAISummaryPrompt() throws Exception {
        String usrContent = "[{\"date\":\"2025-05-10\",\"amount\":-50,\"category\":\"Coffee\"}]";

        Method m = AIAssistant.class.getDeclaredMethod(
                "buildAISummaryPrompt", String.class);
        m.setAccessible(true);

        String prompt = (String) m.invoke(null, usrContent);

        assertTrue(prompt.contains("one-sentence summary"),
                "Should ask for a one-sentence summary");
        assertTrue(prompt.contains(usrContent),
                "Should include the JSON input");
        assertTrue(prompt.contains("major spending categories"),
                "Should mention categorization");
    }

}
