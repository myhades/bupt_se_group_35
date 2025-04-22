package org.group35.config;

import java.io.File;
import java.io.IOException;
import org.group35.util.JsonUtils;

/**
 * The Settings class holds the application configuration settings.
 * It includes settings such as the data file reading location,
 * log level, and file logging options.
 *
 * This class supports persistence to a JSON file using the JsonUtils helper.
 */
public class Settings {

    private static Settings instance;

    // App's name
    public final String appName = "Monora";

    // The directory where data files are stored.
    // Default is the current working directory.
    private String dataFilePath;

    // The log level for the application.
    // Valid values: "TRACE", "DEBUG", "INFO", "WARN", "ERROR".
    // Default value is "DEBUG".
    private String logLevel;

    // Enable or disable logging to file.
    // Default is false.
    private boolean fileLoggingEnabled;

    // The file path for log output.
    // Default is "logs/app.log" (relative to the working directory).
    private String logFilePath;

    // The width of the application window in pixels.
    // Default is 1000.
    private Integer windowWidth;

    // The width of the application window in pixels.
    // Default is 560.
    private Integer windowHeight;

    /**
     * Default constructor that initializes settings with default values.
     */
    private Settings() {
        // Set the default data file location.
        this.dataFilePath = System.getProperty("user.dir") + "\\app_data\\data";
        // Set the default log level.
        this.logLevel = "INFO";
        // Enable file logging by default.
        this.fileLoggingEnabled = true;
        // Set default log file path.
        this.logFilePath = System.getProperty("user.dir") + "\\app_data\\logs";
        // Set default window width to 1000 pixels.
        this.windowWidth = 1000;
        // Set default window height to 560 pixels.
        this.windowHeight = 560;
    }

    /**
     * Returns the singleton Settings instance,
     * loading defaults on first call.
     */
    public static synchronized Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    /**
     * Returns the data file directory path.
     *
     * @return the data file directory path.
     */
    public String getDataFilePath() {
        return dataFilePath;
    }

    /**
     * Sets the data file directory path.
     *
     * @param dataFilePath the new data file directory path.
     */
    public void setDataFilePath(String dataFilePath) {
        this.dataFilePath = dataFilePath;
    }

    /**
     * Returns the log level setting.
     *
     * @return the log level as a String.
     */
    public String getLogLevel() {
        return logLevel;
    }

    /**
     * Sets the log level.
     *
     * @param logLevel the log level to be set (e.g., "DEBUG", "INFO").
     */
    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * Checks if file logging is enabled.
     *
     * @return true if file logging is enabled; false otherwise.
     */
    public boolean isFileLoggingEnabled() {
        return fileLoggingEnabled;
    }

    /**
     * Enables or disables file logging.
     *
     * @param fileLoggingEnabled true to enable file logging; false to disable.
     */
    public void setFileLoggingEnabled(boolean fileLoggingEnabled) {
        this.fileLoggingEnabled = fileLoggingEnabled;
    }

    /**
     * Returns the log file path.
     *
     * @return the log file path.
     */
    public String getLogFilePath() {
        return logFilePath;
    }

    /**
     * Sets the log file path.
     *
     * @param logFilePath the path to the log file.
     */
    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    /**
     * Gets the window width in pixels.
     *
     * @return the width of the application window.
     */
    public Integer getWindowWidth() {
        return windowWidth;
    }

    /**
     * Gets the window height in pixels.
     *
     * @return the height of the application window.
     */
    public Integer getWindowHeight() {
        return windowHeight;
    }


    /**
     * Loads the settings from the specified JSON file.
     * If the file does not exist or an error occurs, default settings are returned.
     *
     * @param file the JSON file to load settings from.
     * @return the loaded Settings instance, or a new default Settings if loading fails.
     */
    public static Settings loadFromFile(File file) {
        if (!file.exists()) {
            return new Settings();
        }
        try {
            return JsonUtils.readJsonFromFile(file, Settings.class);
        } catch (IOException e) {
            e.printStackTrace();
            return new Settings();
        }
    }

    /**
     * Saves the current settings instance to the specified JSON file.
     *
     * @param file the file to save settings to.
     * @throws IOException if an input/output error occurs during saving.
     */
    public void saveToFile(File file) throws IOException {
        JsonUtils.writeJsonToFile(file, this);
    }
}
