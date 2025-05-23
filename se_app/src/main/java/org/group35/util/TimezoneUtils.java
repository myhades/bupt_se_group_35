package org.group35.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimezoneUtils {

    private static final String GEONAMES_SEARCH_URL = "http://api.geonames.org/searchJSON";
    private static final String GEONAMES_TIMEZONE_URL = "http://api.geonames.org/timezoneJSON";
    private static final String GEONAMES_USERNAME = "Dusk_0027"; // GeoNames 免费账户用户名

    private static final OkHttpClient httpClient = new OkHttpClient();

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

    private static double[] getCoordinates(String location) throws IOException {
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

    private static String getTimeZoneId(double lat, double lng) throws IOException {
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

    private static String getCurrentTimeByZone(String timeZoneId) {
        ZoneId zoneId = ZoneId.of(timeZoneId);
        ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);
        return zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    public static LocalDateTime getFormattedCurrentTimeByZone(String timeZoneId) {
        ZoneId zoneId = ZoneId.of(timeZoneId);
        ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);
        return zonedDateTime.toLocalDateTime();
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