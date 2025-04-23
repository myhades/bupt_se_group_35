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

    // Path and filename for settings persistence
    private static final String SETTINGS_DIR = System.getProperty("user.dir") + File.separator + "app_data";
    private static final String SETTINGS_FILE = SETTINGS_DIR + File.separator + "settings.txt";

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
        this.dataFilePath = System.getProperty("user.dir") + File.separator + "app_data" + File.separator + "data";
        this.logFilePath = System.getProperty("user.dir") + File.separator + "app_data" + File.separator + "logs";
        this.logLevel = "INFO";
        this.fileLoggingEnabled = false;
        this.windowWidth = 1000;
        this.windowHeight = 560;
    }

    /**
     * Returns the singleton Settings instance,
     * loading defaults on first call.
     */
    public static synchronized Settings getInstance() {
        if (instance == null) {
            File settingsFile = new File(SETTINGS_FILE);

            if (settingsFile.exists()) {
                instance = loadFromFile(settingsFile);
            } else {
                new File(SETTINGS_DIR).mkdirs();
                instance = new Settings();
                try {
                    instance.saveToFile(settingsFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return instance;
    }

    // Getters and Setters
    public String getDataFilePath() { return dataFilePath; }
    public void setDataFilePath(String dataFilePath) { this.dataFilePath = dataFilePath; }
    public String getLogLevel() { return logLevel; }
    public void setLogLevel(String logLevel) { this.logLevel = logLevel; }
    public boolean isFileLoggingEnabled() { return fileLoggingEnabled; }
    public void setFileLoggingEnabled(boolean fileLoggingEnabled) { this.fileLoggingEnabled = fileLoggingEnabled; }
    public String getLogFilePath() { return logFilePath; }
    public void setLogFilePath(String logFilePath) { this.logFilePath = logFilePath; }
    public Integer getWindowWidth() { return windowWidth; }
    public void setWindowWidth(Integer windowWidth) { this.windowWidth = windowWidth; }
    public Integer getWindowHeight() { return windowHeight; }
    public void setWindowHeight(Integer windowHeight) { this.windowHeight = windowHeight; }

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
