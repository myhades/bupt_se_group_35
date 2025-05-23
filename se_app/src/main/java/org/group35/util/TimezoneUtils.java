package org.group35.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

public class TimezoneUtils {

    // GeoNames API配置
    private static final String GEONAMES_SEARCH_URL = "http://api.geonames.org/searchJSON";
    private static final String GEONAMES_TIMEZONE_URL = "http://api.geonames.org/timezoneJSON";
    private static final String GEONAMES_USERNAME = "Dusk_0027"; // GeoNames 免费账户用户名

    // IP定位API
    private static final String IP_LOCATION_API = "https://ipapi.co/json/";
    private static final String IP_LOCATION_API_BACKUP = "http://ip-api.com/json/";

    // 默认设置
    private static final String DEFAULT_LOCATION = "Unknown";
    private static final String DEFAULT_TIMEZONE = "UTC";

    private static final OkHttpClient httpClient = new OkHttpClient();

    // 缓存当前本地化信息
    private static LocalInfo cachedLocalInfo;
    private static LocalDateTime lastUpdateTime;

    /**
     * 本地化信息类
     */
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

    // ========== 时区相关方法 ==========

    public static String getLocalTime(String location) throws IOException {
        // 1. 获取地理坐标
        double[] coordinates = getCoordinates(location);
        double lat = coordinates[0];
        double lng = coordinates[1];

        // 2. 获取时区信息
        String timeZoneId = getTimeZoneId(lat, lng);

        // 3. 获取并格式化当地时间
        return getCurrentTimeByZone(timeZoneId);
    }

    public static double[] getCoordinates(String location) throws IOException {
        HttpUrl url = HttpUrl.parse(GEONAMES_SEARCH_URL).newBuilder()
                .addQueryParameter("q", location)
                .addQueryParameter("maxRows", "1")    // 限制返回1条结果
                .addQueryParameter("username", GEONAMES_USERNAME)
                .build();

        Request request = new Request.Builder().url(url).build();
        LogUtils.info("send coordinates request: " + url);

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                LogUtils.error("fail to get coordinates: HTTP" + response.code());
                throw new RuntimeException("fail: HTTP " + response.code());
            }
            LogUtils.info("get response");
            JsonObject jsonResponse = JsonParser.parseString(response.body().string()).getAsJsonObject();
            if (jsonResponse.getAsJsonArray("geonames").size() == 0) {
                throw new RuntimeException("cannot find the coordinate in " + location);
            }

            JsonObject locationData = jsonResponse.getAsJsonArray("geonames")
                    .get(0).getAsJsonObject();

            return new double[]{
                    locationData.get("lat").getAsDouble(),
                    locationData.get("lng").getAsDouble()
            };
        }
    }

    public static String getTimeZoneId(double lat, double lng) throws IOException {
        HttpUrl url = HttpUrl.parse(GEONAMES_TIMEZONE_URL).newBuilder()
                .addQueryParameter("lat", String.valueOf(lat))
                .addQueryParameter("lng", String.valueOf(lng))
                .addQueryParameter("username", GEONAMES_USERNAME)
                .build();

        Request request = new Request.Builder().url(url).build();
        LogUtils.info("send TimeZone request: " + url);

        try (Response response = httpClient.newCall(request).execute()) {
            JsonObject jsonResponse = JsonParser.parseString(response.body().string()).getAsJsonObject();

            // 错误处理（如南极坐标返回无数据）
            if (jsonResponse.has("status")) {
                String errorMsg = jsonResponse.getAsJsonObject("status").get("message").getAsString();
                LogUtils.warn("finding error:" + errorMsg);
                throw new RuntimeException("fail to find TimeZone: " + errorMsg);
            }
            LogUtils.info("timezoneid:" + jsonResponse.get("timezoneId").getAsString());

            return jsonResponse.get("timezoneId").getAsString();
        }
    }

    public static String getCurrentTimeByZone(String timeZoneId) {
        ZoneId zoneId = ZoneId.of(timeZoneId);
        ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);
        return zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static LocalDateTime getFormattedCurrentTimeByZone(String timeZoneId) {
        ZoneId zoneId = ZoneId.of(timeZoneId);
        ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);
        return zonedDateTime.toLocalDateTime();
    }

    // ========== 位置检测相关方法 ==========

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
            String currency = isPrimary ? (json.has("currency") ? json.get("currency").getAsString() : CurrencyUtils.getDefaultCurrency()) :
                    CurrencyUtils.getCurrencyByCountryCode(json.has("countryCode") ? json.get("countryCode").getAsString() : "US");
            String timezone = json.has("timezone") ? json.get("timezone").getAsString() : DEFAULT_TIMEZONE;

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
                    String preciseTimezone = getTimeZoneId(lat, lng);
                    if (preciseTimezone != null && !preciseTimezone.isEmpty()) {
                        displayTimezone = preciseTimezone;
                    }
                } catch (Exception e) {
                    // 如果获取失败，保持原始时区
                }
            }

            // 获取汇率
            String exchangeRate = CurrencyUtils.getExchangeRate(currency);

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
     * 获取默认本地化信息
     */
    private static LocalInfo getDefaultLocalInfo() {
        String timezone = DEFAULT_TIMEZONE;
        String currency = CurrencyUtils.getDefaultCurrency();
        String location = DEFAULT_LOCATION;
        String exchangeRate = CurrencyUtils.getExchangeRate(currency);

        return new LocalInfo(timezone, currency, exchangeRate, location);
    }

    /**
     * 强制刷新缓存
     */
    public static void refreshCache() {
        cachedLocalInfo = null;
        lastUpdateTime = null;
    }

    /**
     * 格式化货币显示名称（兼容性方法）
     */
    public static String formatCurrencyName(String currency) {
        return CurrencyUtils.formatCurrencyName(currency);
    }

    /**
     * 测试位置检测功能
     */
    public static void testLocationDetection() {
        try {
            LocalInfo info = detectUserLocation();
            System.out.println("=== Location Detection Test ===");
            System.out.println("Location: " + info.getLocation());
            System.out.println("Currency: " + info.getCurrency() + " (" + CurrencyUtils.formatCurrencyName(info.getCurrency()) + ")");
            System.out.println("Timezone: " + info.getTimezone());
            System.out.println("Exchange Rate: " + info.getExchangeRate());
            System.out.println("==============================");
        } catch (Exception e) {
            System.err.println("Location detection test failed: " + e.getMessage());
        }
    }

    // ========== 获取默认配置值的方法 ==========

    /**
     * 获取默认位置
     */
    public static String getDefaultLocation() {
        return DEFAULT_LOCATION;
    }

    /**
     * 获取默认时区
     */
    public static String getDefaultTimezone() {
        return DEFAULT_TIMEZONE;
    }

    public static void main(String[] args) {
        try {
            String location = "Tokyo";
            String localTime = getLocalTime(location); // example
            LogUtils.info("input:" + location);
            LogUtils.info(" 当前时间: " + localTime);
        } catch (Exception e) {
            LogUtils.error( e.getMessage());
        }
    }
}