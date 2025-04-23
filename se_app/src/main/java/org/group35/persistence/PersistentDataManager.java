package org.group35.persistence;

import java.io.File;
import java.io.IOException;
import org.group35.config.Settings;
import org.group35.util.JsonUtils;
import org.group35.util.LogUtils;

/**
 * PersistentDataManager is responsible for loading and saving the entire persistent data store.
 * It uses the file location specified in the Settings.
 */
public class PersistentDataManager {
    // DATA_FILE will be set during initialization.
    private static String DATA_FILE;

    // The in-memory persistent store.
    private static PersistentStore store;

    /**
     * Initializes the persistent data manager based on the provided settings.
     */
    public static void initialize(Settings settings) {
        LogUtils.debug("Initializing PersistentDataManager with dataFilePath: " + settings.getDataFilePath());
        // Ensure the base folder exists.
        File baseFolder = new File(settings.getDataFilePath());
        if (!baseFolder.exists()) {
            LogUtils.debug("Creating data directory: " + baseFolder.getAbsolutePath());
            baseFolder.mkdirs();
        }
        // Set the DATA_FILE path.
        DATA_FILE = settings.getDataFilePath() + File.separator + "persistent_data.json";
        LogUtils.debug("Data file is set to: " + DATA_FILE);
        // Load the store.
        loadStore();
    }

    /**
     * Returns the persistent store from memory. If not yet loaded, loads it.
     */
    public static PersistentStore getStore() {
        if (store == null) {
            LogUtils.debug("Store is null, loading store");
            loadStore();
        }
        return store;
    }

    /**
     * Loads the persistent store from the DATA_FILE.
     * If the file does not exist or loading fails, creates a new store.
     */
    public static void loadStore() {
        if (DATA_FILE == null) {
            LogUtils.warn("Data file is null, defaulting to 'persistent_data.json'");
            DATA_FILE = "persistent_data.json";
        }
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            LogUtils.info("Data file not found, initializing new PersistentStore");
            store = new PersistentStore();
        } else {
            try {
                LogUtils.debug("Loading PersistentStore from file: " + DATA_FILE);
                store = JsonUtils.readJsonFromFile(file, PersistentStore.class);
                if (store == null) {
                    LogUtils.warn("Loaded store was null, creating new PersistentStore");
                    store = new PersistentStore();
                } else {
                    LogUtils.info("PersistentStore loaded successfully from file");
                }
            } catch (IOException e) {
                LogUtils.error("Error reading PersistentStore: " + e.getMessage());
                store = new PersistentStore();
            }
        }
    }

    /**
     * Saves the current persistent store to the DATA_FILE.
     */
    public static void saveStore() {
        if (DATA_FILE == null) {
            LogUtils.warn("Data file is null on save, defaulting to 'persistent_data.json'");
            DATA_FILE = "persistent_data.json";
        }
        try {
            LogUtils.debug("Saving PersistentStore to file: " + DATA_FILE);
            JsonUtils.writeJsonToFile(new File(DATA_FILE), getStore());
            LogUtils.info("PersistentStore saved successfully to: " + DATA_FILE);
        } catch (IOException e) {
            LogUtils.error("Error saving PersistentStore: " + e.getMessage());
        }
    }
}
