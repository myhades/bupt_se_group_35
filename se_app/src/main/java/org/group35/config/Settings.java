package org.group35.config;

import java.io.File;
import java.io.IOException;
import org.group35.util.JsonUtils;

/**
 * The Settings class holds the application configuration settings.
 * It includes settings such as the data file reading location.
 *
 * This class supports persistence to a JSON file using the JsonUtils helper.
 */
public class Settings {

    // The directory where data files are stored.
    // Default is the current working directory.
    private String dataFilePath;

    // You can add more settings fields here if needed.
    // For example:
    // private String appTheme;
    // private String language;

    /**
     * Default constructor.
     * It initializes settings with default values.
     */
    public Settings() {
        // Set the default data file location to the current directory.
        this.dataFilePath = System.getProperty("user.dir");
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
     * Loads the settings from the specified JSON file.
     * If the file does not exist or an error occurs, default settings are returned.
     *
     * @param file the JSON file to load settings from.
     * @return the loaded Settings instance, or a new default Settings if loading fails.
     */
    public static Settings loadFromFile(File file) {
        if (!file.exists()) {
            return new Settings(); // Return default settings if file does not exist
        }
        try {
            return JsonUtils.readJsonFromFile(file, Settings.class);
        } catch (IOException e) {
            // Print the exception stack trace for debugging and return default settings
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
