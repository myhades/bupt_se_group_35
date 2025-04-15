package org.group35.persistence;

import java.io.File;
import java.io.IOException;
import org.group35.config.Settings;
import org.group35.util.JsonUtils;

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
     * It ensures the base folder exists, sets DATA_FILE to "<dataFilePath>/persistent_data.json",
     * and loads the persistent store.
     *
     * @param settings the application settings.
     */
    public static void initialize(Settings settings) {
        // Ensure the base folder exists.
        File baseFolder = new File(settings.getDataFilePath());
        if (!baseFolder.exists()) {
            baseFolder.mkdirs();
        }
        // Set the DATA_FILE path.
        DATA_FILE = settings.getDataFilePath() + File.separator + "persistent_data.json";
        // Load the store.
        loadStore();
    }

    /**
     * Returns the persistent store from memory. If not yet loaded, loads it.
     */
    public static PersistentStore getStore() {
        if (store == null) {
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
            // Default fallback.
            DATA_FILE = "persistent_data.json";
        }
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            store = new PersistentStore();
        } else {
            try {
                store = JsonUtils.readJsonFromFile(file, PersistentStore.class);
                if (store == null) {
                    store = new PersistentStore();
                }
            } catch (IOException e) {
                e.printStackTrace();
                store = new PersistentStore();
            }
        }
    }

    /**
     * Saves the current persistent store to the DATA_FILE.
     */
    public static void saveStore() {
        try {
            JsonUtils.writeJsonToFile(new File(DATA_FILE), getStore());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
