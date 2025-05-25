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

    // GeoNames API Configuration
    private static final String GEONAMES_SEARCH_URL = "http://api.geonames.org/searchJSON";
    private static final String GEONAMES_TIMEZONE_URL = "http://api.geonames.org/timezoneJSON";
    private static final String GEONAMES_USERNAME = "Dusk_0027"; // GeoNames Free Account Username

    // IP Location API
    private static final String IP_LOCATION_API = "https://ipapi.co/json/";
    private static final String IP_LOCATION_API_BACKUP = "http://ip-api.com/json/";

    // Default Settings
    private static final String DEFAULT_LOCATION = "Unknown";
    private static final String DEFAULT_TIMEZONE = "UTC";

    private static final OkHttpClient httpClient = new OkHttpClient();

    // Cached local information
    private static LocalInfo cachedLocalInfo;
    private static LocalDateTime lastUpdateTime;

    /**
     * Local information class
     */
    public static class LocalInfo {
        private final String timezone;
        private final String currency;
        private final String exchangeRate;
        private final String location;
        private final LocalDateTime updateTime;

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

    // ========== Timezone-related methods ==========

    /**
     * Get the local time of the specified location.
     *
     * @param location the location to get the time for
     * @return the local time in the given location
     * @throws IOException if there is an issue with the network request
     */
    public static String getLocalTime(String location) throws IOException {
        // 1. Get geographical coordinates
        double[] coordinates = getCoordinates(location);
        double lat = coordinates[0];
        double lng = coordinates[1];

        // 2. Get timezone information
        String timeZoneId = getTimeZoneId(lat, lng);

        // 3. Get and format local time
        return getCurrentTimeByZone(timeZoneId);
    }

    /**
     * Get the geographical coordinates (latitude and longitude) of a location.
     *
     * @param location the location to search for
     * @return an array with the latitude and longitude of the location
     * @throws IOException if there is an issue with the network request
     */
    public static double[] getCoordinates(String location) throws IOException {
        HttpUrl url = HttpUrl.parse(GEONAMES_SEARCH_URL).newBuilder()
                .addQueryParameter("q", location)
                .addQueryParameter("maxRows", "1")    // Limit to 1 result
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

    /**
     * Get the timezone ID of a specific location using its latitude and longitude.
     *
     * @param lat the latitude of the location
     * @param lng the longitude of the location
     * @return the timezone ID of the location
     * @throws IOException if there is an issue with the network request
     */
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

            // Error handling (e.g., Antarctica coordinates return no data)
            if (jsonResponse.has("status")) {
                String errorMsg = jsonResponse.getAsJsonObject("status").get("message").getAsString();
                LogUtils.warn("finding error:" + errorMsg);
                throw new RuntimeException("fail to find TimeZone: " + errorMsg);
            }
            LogUtils.info("timezoneid:" + jsonResponse.get("timezoneId").getAsString());

            return jsonResponse.get("timezoneId").getAsString();
        }
    }

    /**
     * Get the current time in a specific timezone.
     *
     * @param timeZoneId the ID of the timezone
     * @return the current time in the specified timezone formatted as a string
     */
    public static String getCurrentTimeByZone(String timeZoneId) {
        ZoneId zoneId = ZoneId.of(timeZoneId);
        ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);
        return zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Get the current time in a specific timezone as a LocalDateTime object.
     *
     * @param timeZoneId the ID of the timezone
     * @return the current time in the specified timezone as LocalDateTime
     */
    public static LocalDateTime getFormattedCurrentTimeByZone(String timeZoneId) {
        ZoneId zoneId = ZoneId.of(timeZoneId);
        ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);
        return zonedDateTime.toLocalDateTime();
    }

    // ========== Location detection-related methods ==========

    /**
     * Automatically detect the user's location based on their IP address.
     *
     * @return LocalInfo object containing detected location data
     * @throws IOException if both primary and backup APIs fail
     */
    public static LocalInfo detectUserLocation() throws IOException {
        try {
            return detectLocationFromPrimaryAPI();
        } catch (Exception e) {
            LogUtils.error("Primary location API failed: " + e.getMessage());
            try {
                return detectLocationFromBackupAPI();
            } catch (Exception e2) {
                LogUtils.error("Backup location API failed: " + e2.getMessage());
                throw new IOException("All location detection APIs failed");
            }
        }
    }

    /**
     * General location detection method.
     *
     * @param apiUrl the URL of the location API
     * @param isPrimary boolean indicating whether the API is primary or backup
     * @return LocalInfo object containing detected location data
     * @throws IOException if there is an issue with the network request
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

            // Check for API errors
            if (isPrimary && json.has("error") && json.get("error").getAsBoolean()) {
                String reason = json.has("reason") ? json.get("reason").getAsString() : "Unknown error";
                throw new IOException("API Error: " + reason);
            }
            if (!isPrimary && json.has("status") && "fail".equals(json.get("status").getAsString())) {
                String message = json.has("message") ? json.get("message").getAsString() : "Unknown error";
                throw new IOException("API returned failure status: " + message);
            }

            // Parse the data
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

            // Format the location for display
            String displayLocation = !region.isEmpty() && !region.equals(city) ? region : city;
            if (!displayLocation.equals(country)) {
                displayLocation = displayLocation + ", " + country;
            }

            // Get precise timezone
            String displayTimezone = timezone;
            if (!timezone.contains("/") && lat != 0.0 && lng != 0.0) {
                try {
                    String preciseTimezone = getTimeZoneId(lat, lng);
                    if (preciseTimezone != null && !preciseTimezone.isEmpty()) {
                        displayTimezone = preciseTimezone;
                    }
                } catch (Exception e) {
                    // If failed, retain the original timezone
                }
            }

            // Get exchange rate
            String exchangeRate = CurrencyUtils.getExchangeRate(currency);

            // Log information
            LogUtils.info("Precise timezone: " + displayTimezone);
            LogUtils.info("Final processed data:");
            LogUtils.info("  Location: " + displayLocation);
            LogUtils.info("  Currency: " + currency);
            LogUtils.info("  Timezone: " + displayTimezone);
            LogUtils.info("  Exchange Rate: " + exchangeRate);

            return new LocalInfo(displayLocation, currency, exchangeRate, displayLocation);
        }
    }

    /**
     * Use the primary API to detect the user's location.
     *
     * @return LocalInfo object containing the detected location data
     * @throws IOException if the API fails
     */
    private static LocalInfo detectLocationFromPrimaryAPI() throws IOException {
        return detectLocationFromAPI(IP_LOCATION_API, true);
    }

    /**
     * Use the backup API to detect the user's location.
     *
     * @return LocalInfo object containing the detected location data
     * @throws IOException if the API fails
     */
    private static LocalInfo detectLocationFromBackupAPI() throws IOException {
        return detectLocationFromAPI(IP_LOCATION_API_BACKUP, false);
    }

    /**
     * Get the current localized information synchronously.
     *
     * @return LocalInfo object containing current localized information
     */
    public static LocalInfo getCurrentLocalInfo() {
        return getLocalInfoSync();
    }

    /**
     * Asynchronously get the current localized information.
     *
     * @return CompletableFuture containing the localized information
     */
    public static CompletableFuture<LocalInfo> getCurrentLocalInfoAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return detectUserLocation();
            } catch (Exception e) {
                LogUtils.error("Error getting local info: " + e.getMessage());
                return getDefaultLocalInfo();
            }
        });
    }

    /**
     * Synchronously get the current localized information.
     *
     * @return LocalInfo object containing the localized information
     */
    private static LocalInfo getLocalInfoSync() {
        try {
            LocalInfo info = detectUserLocation();
            cachedLocalInfo = info;
            lastUpdateTime = LocalDateTime.now();
            LogUtils.info("Local info updated - Timezone: " + info.getTimezone() + ", Currency: " + info.getCurrency());
            return info;
        } catch (Exception e) {
            LogUtils.error("Location detection failed: " + e.getMessage());
            return getDefaultLocalInfo();
        }
    }

    /**
     * Get the default localized information.
     *
     * @return LocalInfo object with default values
     */
    private static LocalInfo getDefaultLocalInfo() {
        String currency = CurrencyUtils.getDefaultCurrency();
        String exchangeRate = CurrencyUtils.getExchangeRate(currency);

        return new LocalInfo(DEFAULT_TIMEZONE, currency, exchangeRate, DEFAULT_LOCATION);
    }

    /**
     * Force refresh the cache of localized information.
     */
    public static void refreshCache() {
        cachedLocalInfo = null;
        lastUpdateTime = null;
    }

    /**
     * Format currency name for display purposes.
     *
     * @param currency the currency code
     * @return formatted currency name
     */
    public static String formatCurrencyName(String currency) {
        return CurrencyUtils.formatCurrencyName(currency);
    }

    /**
     * Test the location detection functionality.
     */
    public static void testLocationDetection() {
        try {
            LocalInfo info = detectUserLocation();
            LogUtils.info("=== Location Detection Test ===");
            LogUtils.info("Location: " + info.getLocation());
            LogUtils.info("Currency: " + info.getCurrency() + " (" + CurrencyUtils.formatCurrencyName(info.getCurrency()) + ")");
            LogUtils.info("Timezone: " + info.getTimezone());
            LogUtils.info("Exchange Rate: " + info.getExchangeRate());
            LogUtils.info("==============================");
        } catch (Exception e) {
            LogUtils.error("Location detection test failed: " + e.getMessage());
        }
    }

    // ========== Get default configuration values ==========

    /**
     * Get the default location.
     *
     * @return default location
     */
    public static String getDefaultLocation() {
        return DEFAULT_LOCATION;
    }

    /**
     * Get the default timezone.
     *
     * @return default timezone
     */
    public static String getDefaultTimezone() {
        return DEFAULT_TIMEZONE;
    }

    /**
     * Main method for testing the functionality of getting local time.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        try {
            String location = "Tokyo";
            String localTime = getLocalTime(location); // Example usage
            LogUtils.info("input:" + location);
            LogUtils.info(" Current Time: " + localTime);

        } catch (Exception e) {
            LogUtils.error( e.getMessage());
        }
    }
}
