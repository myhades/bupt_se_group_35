package org.group35.controller;

import org.group35.model.Transaction;
import org.group35.model.User;
import org.group35.persistence.PersistentDataManager;
import org.group35.util.ImageUtils;
import org.group35.util.PasswordUtils;
import org.group35.util.LoggerHelper;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * Manages user registration and login operations.
 * Data persistence is handled by PersistentDataManager.
 * Logging is added via LoggerHelper.
 */
public class UserManager {

    private List<User> users;

    public UserManager() {
        LoggerHelper.debug("Initializing UserManager and loading users");
        users = PersistentDataManager.getStore().getUsers();
        if (users == null) {
            LoggerHelper.info("No existing users found, starting with empty list");
            users = new ArrayList<>();
        } else {
            LoggerHelper.info("Loaded " + users.size() + " users from store");
        }
    }

    /** Saves the current user list back to the persistent store. */
    private void save() {
        LoggerHelper.debug("Persisting " + users.size() + " users to store");
        PersistentDataManager.getStore().setUsers(users);
        PersistentDataManager.saveStore();
        LoggerHelper.info("Users saved successfully");
    }

    /**
     * Register a new user: hash the password, add the user to the store.
     */
    public static void registerUser(String username, String plainPassword) {
        LoggerHelper.debug("Attempting to register user: " + username);
        String hashed = PasswordUtils.hashPassword(plainPassword);
        User newUser = new User(username, hashed);

        List<User> userList = getUsers();
        userList.add(newUser);
        // Save the updated user list to the persistent store
        PersistentDataManager.getStore().setUsers(userList);
        PersistentDataManager.saveStore();
        LoggerHelper.info("User registered successfully: " + username);
    }

    /**
     * Verify user login credentials.
     */
    public static boolean loginUser(String username, String plainPassword) {
        LoggerHelper.debug("Attempting login for user: " + username);
        List<User> users = getUsers();
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                boolean authenticated = PasswordUtils.checkPassword(plainPassword, u.getHashedPassword());
                if (authenticated) {
                    LoggerHelper.info("Login successful for user: " + username);
                } else {
                    LoggerHelper.warn("Login failed for user: " + username + " - incorrect password");
                }
                return authenticated;
            }
        }
        LoggerHelper.warn("Login failed for user: " + username + " - user not found");
        return false;
    }

    /**
     * Get the list of users from the persistent store.
     */
    public static List<User> getUsers() {
        List<User> users = PersistentDataManager.getStore().getUsers();
        LoggerHelper.trace("Number of users retrieved from persistent store: " + (users != null ? users.size() : 0));
        return users;
    }

    /**
     * Set the list of users in the persistent store (used for testing or bulk updates).
     */
    public static void setUsers(List<User> users) {
        LoggerHelper.debug("Setting user list with " + (users != null ? users.size() : 0) + " entries");
        PersistentDataManager.getStore().setUsers(users);
        PersistentDataManager.saveStore();
        LoggerHelper.info("User list updated in persistent store");
    }

    /**
     * Compresses, crops to square, encodes to Base64 and stores as the user's avatar.
     * @param username  the user to update
     * @param imagePath path to the source image file
     */
    public static void setUserAvatar(String username, String imagePath) {
        LoggerHelper.debug("Attempting to set avatar for user: " + username);
        List<User> users = getUsers();
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                try {
                    byte[] compressed = ImageUtils.loadCompressImage(imagePath);
                    String base64 = ImageUtils.toBase64(compressed);
                    u.setAvatar(base64);
                    PersistentDataManager.getStore().setUsers(users);
                    PersistentDataManager.saveStore();
                    LoggerHelper.info("Avatar updated for user: " + username);
                } catch (IOException e) {
                    LoggerHelper.error("Failed to process avatar image for user: " + username);
                }
                return;
            }
        }
        LoggerHelper.warn("Failed to set avatar. User not found: " + username);
    }

    /**
     * Retrieves the Base64‑encoded avatar string for a user.
     * @param username the user to look up
     * @return Base64 avatar or null if not set / user not found
     */
    public static String getUserAvatar(String username) {
        for (User u : getUsers()) {
            if (u.getUsername().equals(username)) {
                return u.getAvatar();
            }
        }
        LoggerHelper.warn("Failed to get avatar. User not found: " + username);
        return null;
    }

    /** Returns users for a given username. */
    public List<User> getByUser(String username) {
        LoggerHelper.trace("Filtering transactions for user: " + username);
        return users.stream()
                .filter(tx -> username.equals(tx.getUsername()))
                .collect(Collectors.toList());
    }

    /** Sets the monthly budget for a specific user's future entries. */
    public void setMonthlyBudget(String username, BigDecimal budget) {
        LoggerHelper.info("Setting monthly budget for user " + username + " to " + budget);
        getByUser(username).forEach(tx -> tx.setMonthlyBudget(budget));
        save();
    }
}
