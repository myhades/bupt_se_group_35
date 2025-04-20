package org.group35.controller;

import org.group35.model.User;
import org.group35.persistence.PersistentDataManager;
import org.group35.util.PasswordUtils;
import org.group35.util.LoggerHelper;

import java.util.List;

/**
 * Manages user registration and login operations.
 * Data persistence is handled by PersistentDataManager.
 * Logging is added via LoggerHelper.
 */
public class UserManager {

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
}
