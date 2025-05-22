package org.group35.controller;

import org.group35.model.Transaction;
import org.group35.model.User;
import org.group35.persistence.PersistentDataManager;
import org.group35.runtime.ApplicationRuntime;
import org.group35.util.ImageUtils;
import org.group35.util.LogUtils;
import org.group35.util.PasswordUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * Manages user registration and login operations.
 * Data persistence is handled by PersistentDataManager.
 * Logging is added via LogUtils.
 */
public class UserManager {

    private List<User> users;

    public UserManager() {
        LogUtils.debug("Initializing UserManager and loading users");
        users = PersistentDataManager.getStore().getUsers();
        if (users == null) {
            LogUtils.info("No existing users found, starting with empty list");
            users = new ArrayList<>();
        } else {
            LogUtils.info("Loaded " + users.size() + " users from store");
        }
    }

    /** Saves the current user list back to the persistent store. */
    private void save() {
        LogUtils.debug("Persisting " + (users != null ? users.size() : 0) + " users to store");
        PersistentDataManager.getStore().setUsers(users);
        PersistentDataManager.saveStore();
        LogUtils.info("Users saved successfully");
    }

    /**
     * Register a new user: hash the password, add the user to the store.
     */
    public void registerUser(String username, String plainPassword) {
        LogUtils.debug("Attempting to register user: " + username);
        String hashed = PasswordUtils.hashPassword(plainPassword);
        User newUser = new User(username, hashed);

//        List<User> userList = getPersistentUsers();
//        userList.add(newUser);
        // Save the updated user list to the persistent store
//        PersistentDataManager.getStore().setUsers(userList);
//        PersistentDataManager.saveStore();
        users.add(newUser);
        save();
        LogUtils.info("User registered successfully: " + username);
    }

    /**
     * Verify user login credentials.
     */
    public static boolean loginUser(String username, String plainPassword) {
        LogUtils.debug("Attempting login for user: " + username);
        List<User> users = getPersistentUsers();
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                boolean authenticated = PasswordUtils.checkPassword(plainPassword, u.getHashedPassword());
                if (authenticated) {
                    LogUtils.info("Login successful for user: " + username);
                } else {
                    LogUtils.warn("Login failed for user: " + username + " - incorrect password");
                }
                return authenticated;
            }
        }
        LogUtils.warn("Login failed for user: " + username + " - user not found");
        return false;
    }

    /**
     * Get the list of users from the persistent store.
     */
    public static List<User> getPersistentUsers() {
        List<User> users = PersistentDataManager.getStore().getUsers();
        LogUtils.trace("Number of users retrieved from persistent store: " + (users != null ? users.size() : 0));
        return users;
    }

    /**
     * Get the list of users from the runtime store.
     */
    public List<User> getUsers() {
        LogUtils.trace("Number of users retrieved from runtime store: " + (users != null ? users.size() : 0));
        return users;
    }

    /**
     * Get the current logged user.
     */
    public static User getCurrentUser(){
        ApplicationRuntime runtime = ApplicationRuntime.getInstance();
        return runtime.getCurrentUser();
    }

    /**
     * Set the list of users in the persistent store (used for testing or bulk updates).
     */
    public static void setUsers(List<User> users) {
        LogUtils.debug("Setting user list with " + (users != null ? users.size() : 0) + " entries");
        PersistentDataManager.getStore().setUsers(users);
        PersistentDataManager.saveStore();
        LogUtils.info("User list updated in persistent store");
    }

    /** Updates an existing user by name. */
    public void update(User updated) {
        LogUtils.debug("Updating user: " + updated.getUsername());
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUsername().equals(updated.getUsername())) {
                users.set(i, updated);
                LogUtils.info("Transaction updated: " + updated.getUsername());
                save();
                return;
            }
        }
        LogUtils.warn("Transaction not found: " + updated.getUsername());
    }

    /**
     * Compresses, crops to square, encodes to Base64 and stores as the user's avatar.
     * @param username  the user to update
     * @param imagePath path to the source image file
     */
    public static void setUserAvatar(String username, String imagePath) {
        LogUtils.debug("Attempting to set avatar for user: " + username);
        List<User> users = getPersistentUsers();
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                try {
                    byte[] compressed = ImageUtils.loadCompressImage(imagePath);
                    String base64 = ImageUtils.toBase64(compressed);
                    u.setAvatar(base64);
                    PersistentDataManager.getStore().setUsers(users);
                    PersistentDataManager.saveStore();
                    LogUtils.info("Avatar updated for user: " + username);
                } catch (IOException e) {
                    LogUtils.error("Failed to process avatar image for user: " + username);
                }
                return;
            }
        }
        LogUtils.warn("Failed to set avatar. User not found: " + username);
    }

    /**
     * Set current user's avatar.
     * @param imagePath path to the source image file
     */
    public void setAvatar(String imagePath) {
        User user = ApplicationRuntime.getInstance().getCurrentUser();
        LogUtils.debug("Attempting to set avatar for current user: " + user.getUsername());
        try {
            byte[] compressed = ImageUtils.loadCompressImage(imagePath);
            String base64 = ImageUtils.toBase64(compressed);
            user.setAvatar(base64);
            save();
            LogUtils.info("Avatar updated for user: " + user.getUsername());
        } catch (IOException e) {
            LogUtils.error("Failed to process avatar image for user: " + user.getUsername());
        }
        LogUtils.warn("Failed to set avatar. User not found: " + user.getUsername());
    }


    /**
     * Retrieves the Base64‑encoded avatar string for a user.
     * @param username the user to look up
     * @return Base64 avatar or null if not set / user not found
     */
    public String getUserAvatar(String username) {
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                return u.getAvatar();
            }
        }
        LogUtils.warn("Failed to get avatar. User not found: " + username);
        return null;
    }

    /** Returns users for a given username. */
    public List<User> getByUser(String username) {
        LogUtils.trace("Filtering transactions for user: " + username);
        return users.stream()
                .filter(user -> username.equals(user.getUsername()))
                .collect(Collectors.toList());
    }

    /**
     * Sets the budget for a specific user.
     * @param username the user to update
     * @param budget the new budget amount
     */
    public void setMonthlyBudget(String username, BigDecimal budget) {
        LogUtils.info("Setting monthly budget for user " + username + " to " + budget);
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                u.setMonthlyBudget(budget);
                PersistentDataManager.getStore().setUsers(users);
                PersistentDataManager.saveStore();
                LogUtils.info("Budget updated for user: " + username);
                return;
            }
        }
    }

}
