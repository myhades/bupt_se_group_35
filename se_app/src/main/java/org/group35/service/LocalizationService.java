package org.group35.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.group35.util.TimezoneUtils;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class LocalizationService {
    private static final OkHttpClient httpClient = new OkHttpClient();
    private static Properties config;

    // IP定位API
    private static final String IP_LOCATION_API = "https://ipapi.co/json/";
    private static final String IP_LOCATION_API_BACKUP = "http://ip-api.com/json/";

    // 缓存当前本地化信息
    private static LocalInfo cachedLocalInfo;
    private static LocalDateTime lastUpdateTime;

    static {
        loadConfig();
    }

    public static class LocalInfo {
        private String timezone;
        private String currency;
        private String exchangeRate;
        private String location;
        private LocalDateTime updateTime;

        public LocalInfo(String timezone, String currency, String exchangeRate, String location) {
            this.timezone = timezone;
            this.currency = currency;
            this.exchangeRate = exchangeRate;
            this.location = location;
            this.updateTime = LocalDateTime.now();
        }

        // Getters
        public String getTimezone() { return timezone; }
        public String getCurrency() { return currency; }
        public String getExchangeRate() { return exchangeRate; }
        public String getLocation() { return location; }
        public LocalDateTime getUpdateTime() { return updateTime; }
    }

    private static void loadConfig() {
        config = new Properties();
        try (InputStream input = LocalizationService.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input != null) {
                config.load(input);
            } else {
                setDefaultConfig();
            }
        } catch (IOException e) {
            System.err.println("Error loading config: " + e.getMessage());
            setDefaultConfig();
        }
    }

    private static void setDefaultConfig() {
        config.setProperty("default.location", "Unknown Location");
        config.setProperty("default.currency", "USD");
        config.setProperty("default.timezone", "UTC");
        config.setProperty("exchange.api.url", "https://api.exchangerate-api.com/v4/latest/USD");
    }

    /**
     * 通过IP地址自动检测用户位置
     */
    public static LocalInfo detectUserLocation() throws IOException {
        try {
            return detectLocationFromPrimaryAPI();
        } catch (Exception e) {
            System.err.println("Primary location API failed: " + e.getMessage());
            try {
                return detectLocationFromBackupAPI();
            } catch (Exception e2) {
                System.err.println("Backup location API failed: " + e2.getMessage());
                throw new IOException("All location detection APIs failed");
            }
        }
    }

    /**
     * 通用位置检测方法
     */
    private static LocalInfo detectLocationFromAPI(String apiUrl, boolean isPrimary) throws IOException {
        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP " + response.code());
            }

            JsonObject json = JsonParser.parseString(response.body().string()).getAsJsonObject();

            // 检查API错误
            if (isPrimary && json.has("error") && json.get("error").getAsBoolean()) {
                String reason = json.has("reason") ? json.get("reason").getAsString() : "Unknown error";
                throw new IOException("API Error: " + reason);
            }
            if (!isPrimary && json.has("status") && "fail".equals(json.get("status").getAsString())) {
                String message = json.has("message") ? json.get("message").getAsString() : "Unknown error";
                throw new IOException("API returned failure status: " + message);
            }

            // 解析数据
            String city = json.has("city") ? json.get("city").getAsString() : "Unknown";
            String region = json.has(isPrimary ? "region" : "regionName") ?
                    json.get(isPrimary ? "region" : "regionName").getAsString() : "";
            String country = json.has(isPrimary ? "country_name" : "country") ?
                    json.get(isPrimary ? "country_name" : "country").getAsString() : "Unknown";
            String currency = isPrimary ? (json.has("currency") ? json.get("currency").getAsString() : "USD") :
                    getCurrencyByCountryCode(json.has("countryCode") ? json.get("countryCode").getAsString() : "US");
            String timezone = json.has("timezone") ? json.get("timezone").getAsString() : "UTC";

            double lat = json.has(isPrimary ? "latitude" : "lat") ?
                    json.get(isPrimary ? "latitude" : "lat").getAsDouble() : 0.0;
            double lng = json.has(isPrimary ? "longitude" : "lon") ?
                    json.get(isPrimary ? "longitude" : "lon").getAsDouble() : 0.0;

            // 优化位置显示
            String displayLocation = !region.isEmpty() && !region.equals(city) ? region : city;
            if (!displayLocation.equals(country)) {
                displayLocation = displayLocation + ", " + country;
            }

            // 获取精确时区
            String displayTimezone = timezone;
            if (!timezone.contains("/") && lat != 0.0 && lng != 0.0) {
                try {
                    String preciseTimezone = TimezoneUtils.getTimeZoneId(lat, lng);
                    if (preciseTimezone != null && !preciseTimezone.isEmpty()) {
                        displayTimezone = preciseTimezone;
                    }
                } catch (Exception e) {
                    // 如果获取失败，保持原始时区
                }
            }

            // 获取汇率
            String exchangeRate = getExchangeRate(currency);

            // 输出调试信息
            System.out.println("Precise timezone: " + displayTimezone);
            System.out.println("Final processed data:");
            System.out.println("  Location: " + displayLocation);
            System.out.println("  Currency: " + currency);
            System.out.println("  Timezone: " + displayTimezone);
            System.out.println("  Exchange Rate: " + exchangeRate);

            return new LocalInfo(displayLocation, currency, exchangeRate, displayLocation);
        }
    }

    /**
     * 使用主API检测位置
     */
    private static LocalInfo detectLocationFromPrimaryAPI() throws IOException {
        return detectLocationFromAPI(IP_LOCATION_API, true);
    }

    /**
     * 使用备用API检测位置
     */
    private static LocalInfo detectLocationFromBackupAPI() throws IOException {
        return detectLocationFromAPI(IP_LOCATION_API_BACKUP, false);
    }

    /**
     * 根据国家代码推断货币
     */
    private static String getCurrencyByCountryCode(String countryCode) {
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
            default: return "USD";
        }
    }

    /**
     * 获取当前本地化信息
     */
    public static LocalInfo getCurrentLocalInfo() {
        return getLocalInfoSync();
    }

    /**
     * 异步获取本地化信息
     */
    public static CompletableFuture<LocalInfo> getCurrentLocalInfoAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return detectUserLocation();
            } catch (Exception e) {
                System.err.println("Error getting local info: " + e.getMessage());
                return getDefaultLocalInfo();
            }
        });
    }

    /**
     * 同步获取本地化信息
     */
    private static LocalInfo getLocalInfoSync() {
        try {
            LocalInfo info = detectUserLocation();
            cachedLocalInfo = info;
            lastUpdateTime = LocalDateTime.now();
            System.out.println("Local info updated - Timezone: " + info.getTimezone() + ", Currency: " + info.getCurrency());
            return info;
        } catch (Exception e) {
            System.err.println("Location detection failed: " + e.getMessage());
            return getDefaultLocalInfo();
        }
    }

    /**
     * 获取汇率信息
     */
    private static String getExchangeRate(String currency) {
        if ("USD".equals(currency)) {
            return "1.00 per USD";
        }

        try {
            String apiUrl = config.getProperty("exchange.api.url",
                    "https://api.exchangerate-api.com/v4/latest/USD");

            Request request = new Request.Builder()
                    .url(apiUrl)
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
     * 获取默认本地化信息
     */
    private static LocalInfo getDefaultLocalInfo() {
        String timezone = config.getProperty("default.timezone", "UTC");
        String currency = config.getProperty("default.currency", "USD");
        String location = config.getProperty("default.location", "Unknown Location");
        String exchangeRate = getExchangeRate(currency);

        return new LocalInfo(timezone, currency, exchangeRate, location);
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
     * 强制刷新缓存
     */
    public static void refreshCache() {
        cachedLocalInfo = null;
        lastUpdateTime = null;
    }

    /**
     * 测试位置检测功能
     */
    public static void testLocationDetection() {
        try {
            LocalInfo info = detectUserLocation();
            System.out.println("=== Location Detection Test ===");
            System.out.println("Location: " + info.getLocation());
            System.out.println("Currency: " + info.getCurrency() + " (" + formatCurrencyName(info.getCurrency()) + ")");
            System.out.println("Timezone: " + info.getTimezone());
            System.out.println("Exchange Rate: " + info.getExchangeRate());
            System.out.println("==============================");
        } catch (Exception e) {
            System.err.println("Location detection test failed: " + e.getMessage());
        }
    }
}
