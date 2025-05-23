package org.group35.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.IOException;

public class CurrencyUtils {
    private static final OkHttpClient httpClient = new OkHttpClient();
    private static final String EXCHANGE_API_URL = "https://api.exchangerate-api.com/v4/latest/USD";
    private static final String DEFAULT_CURRENCY = "USD";

    /**
     * 根据国家代码推断货币
     */
    public static String getCurrencyByCountryCode(String countryCode) {
        switch (countryCode.toUpperCase()) {
            case "US": case "USA": return "USD";
            case "CN": case "CHN": return "CNY";
            case "JP": case "JPN": return "JPY";
            case "GB": case "UK": return "GBP";
            case "CA": return "CAD";
            case "AU": return "AUD";
            case "IN": return "INR";
            case "KR": return "KRW";
            case "SG": return "SGD";
            case "HK": return "HKD";
            case "CH": return "CHF";
            case "RU": return "RUB";
            case "BR": return "BRL";
            case "MX": return "MXN";
            case "DE": case "FR": case "IT": case "ES": case "NL": case "AT": case "BE":
            case "PT": case "GR": case "FI": case "IE": case "LU": return "EUR";
            default: return DEFAULT_CURRENCY;
        }
    }

    /**
     * 获取汇率信息
     */
    public static String getExchangeRate(String currency) {
        if ("USD".equals(currency)) {
            return "1.00 per USD";
        }

        try {
            Request request = new Request.Builder()
                    .url(EXCHANGE_API_URL)
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject jsonResponse = JsonParser.parseString(response.body().string())
                            .getAsJsonObject();

                    if (jsonResponse.has("rates") &&
                            jsonResponse.getAsJsonObject("rates").has(currency)) {
                        double rate = jsonResponse.getAsJsonObject("rates")
                                .get(currency).getAsDouble();
                        return String.format("%.2f per USD", rate);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting exchange rate: " + e.getMessage());
        }

        return "1.00 per USD";
    }

    /**
     * 格式化货币显示名称
     */
    public static String formatCurrencyName(String currency) {
        switch (currency) {
            case "CNY": return "RMB/Yuan";
            case "USD": return "US Dollar";
            case "JPY": return "Japanese Yen";
            case "EUR": return "Euro";
            case "GBP": return "British Pound";
            case "CAD": return "Canadian Dollar";
            case "AUD": return "Australian Dollar";
            case "CHF": return "Swiss Franc";
            case "INR": return "Indian Rupee";
            case "KRW": return "South Korean Won";
            case "SGD": return "Singapore Dollar";
            case "HKD": return "Hong Kong Dollar";
            case "RUB": return "Russian Ruble";
            case "BRL": return "Brazilian Real";
            case "MXN": return "Mexican Peso";
            case "RMB": return "RMB/Yuan";
            default: return currency;
        }
    }

    /**
     * 获取默认货币
     */
    public static String getDefaultCurrency() {
        return DEFAULT_CURRENCY;
    }
}