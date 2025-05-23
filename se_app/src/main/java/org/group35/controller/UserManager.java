package org.group35.controller;

import org.group35.model.User;
import org.group35.persistence.PersistentDataManager;
import org.group35.runtime.ApplicationRuntime;
import org.group35.util.LogUtils;
import org.group35.util.PasswordUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages user registration and login operations.
 * Data persistence is handled by PersistentDataManager.
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
     * Get the user by name from the runtime store.
     */
    public User getUser(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        LogUtils.warn("User not found: " + username);
        return null;
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

    public void setHashedPassword(String hashedPassword){
        User user = ApplicationRuntime.getInstance().getCurrentUser();
        user.setHashedPassword(hashedPassword);
        save();
        LogUtils.info("Password updated for user: " + user.getUsername());
    }

    public void setHashedPassword(String username, String hashedPassword){
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                user.setHashedPassword(hashedPassword);
                save();
                LogUtils.info("Password updated for user: " + username);
            }
        }
        LogUtils.info("Failed to set hashedpassword. User not found: " + username);
    }

    public String getHashedPassword(){
        User user = ApplicationRuntime.getInstance().getCurrentUser();
        return user.getHashedPassword();
    }

    public String getHashedPassword(String username){
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user.getHashedPassword();
            }
        }
        LogUtils.info("Failed to get hashedpassword. User not found: " + username);
        return "";
    }

    /**
     * Compresses, crops to square, encodes to Base64 and stores as the user's avatar.
     * @param username  the user to update
     * @param base64  source image file in base64 string
     */
    public void setUserAvatar(String username, String base64) {
        LogUtils.debug("Attempting to set avatar for user: " + username);
        List<User> users = getPersistentUsers();
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                u.setAvatar(base64);
                save();
                LogUtils.info("Avatar updated for user: " + username);
                return;
            }
        }
        LogUtils.warn("Failed to set avatar. User not found: " + username);
    }

    /**
     * Set user's avatar.
     * @param base64  source image file in base64 string
     */
    public void setAvatar(String username, String base64) {
        User user = getUser(username);
        LogUtils.debug("Attempting to set avatar for user: " + user.getUsername());
        user.setAvatar(base64);
        save();
        LogUtils.info("Avatar updated for user: " + user.getUsername());
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
                save();
                LogUtils.info("Budget updated for user: " + username);
                return;
            }
        }
    }

    /**
     * Sets the budget for current user.
     * @param budget the new budget amount
     */
    public void setMonthlyBudget(BigDecimal budget) {
        User user = ApplicationRuntime.getInstance().getCurrentUser();
        LogUtils.info("Setting monthly budget for user " + user.getUsername() + " to " + budget);
        user.setMonthlyBudget(budget);
        save();
    }

    /**
     * Get the budget for a specific user.
     * @param username the user to update
     */
    public BigDecimal getMonthlyBudget(String username) {
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                LogUtils.info("Budget for user: " + username + " is " + u.getMonthlyBudget());
                return u.getMonthlyBudget();
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * Get the budget for a specific user.
     */
    public static BigDecimal getMonthlyBudget() {
        User user = ApplicationRuntime.getInstance().getCurrentUser();
        return user.getMonthlyBudget();
    }

    public void setLocation(String username, String location) {
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                u.setLocation(location);
                save();
                LogUtils.info("Location updated for user: " + username);
            }
        }
    }

    public String getLocation(String username) {
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                LogUtils.info("Location for user: " + username + " is " + u.getLocation());
                return u.getLocation();
            }
        }
        return ""; //FIXME
    }

    public static String getLocation() {
        User user = ApplicationRuntime.getInstance().getCurrentUser();
        return user.getLocation();
    }

    public void setTimezone(String username, String timezone) {
        User user = getUser(username);
        if (user != null) {
            user.setTimezone(timezone);
            LogUtils.info("Timezone updated for user: " + user.getUsername());
        }
        else{
            LogUtils.warn("Timezone update failed. User not found: " + username);
        }
    }

    public String getTimezone(String username) {
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                LogUtils.info("Timezone for user: " + username + " is " + u.getTimezone());
                return u.getTimezone();
            }
        }
        return "notimezone"; //FIXME
    }

    public String getTimezone() {
        User user = ApplicationRuntime.getInstance().getCurrentUser();
        return user.getTimezone();
    }

    /**
     * Get the categories for current user.
     */
    public List<String> getCategory() {
        User user = ApplicationRuntime.getInstance().getCurrentUser();
        return user.getCategory();
    }

    /**
     * Get the categories for a specific user.
     */
    public List<String> getCategory(String username) {
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                LogUtils.info("Categories for user: " + username + " are " + u.getCategory());
                return u.getCategory();
            }
        }
        return Collections.emptyList(); //FIXME
    }

    /**
     * Add new category for a specific user.
     * @param username the user to update
     * @param category the category to add
     */
    public boolean addCategory(String username, String category) {
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                if (! u.addCategory(category)) {
                    LogUtils.error("Category " + category + " already exists for user: " + username);
                    return false;
                }
                save();
                LogUtils.info("Category " + category + " added for user: " + username);
                return true;
            }
        }
        LogUtils.error("User " + username + " does not exist.");
        return false;
    }

    /**
     * Add new category for current user.
     * @param category the category to add
     */
    public boolean addCategory(String category) {
        User user = ApplicationRuntime.getInstance().getCurrentUser();
        if (! user.addCategory(category)){
            LogUtils.error("Category " + category + " already exists for user: " + user.getUsername());
            return false;
        }
        save();
        LogUtils.info("Category " + category + " added for current user: " + user.getUsername());
        return true;
    }

    /**
     * remove a category for a specific user.
     * @param username the user to update
     * @param category the category to add
     */
    public boolean removeCategory(String username, String category) {
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                if (! u.removeCategory(category)) {
                    LogUtils.error("Category " + category + " does not exist for user: " + username);
                    return false;
                }
                save();
                LogUtils.info("Category " + category + " removed for user: " + username);
                return true;
            }
        }
        LogUtils.error("User " + username + " does not exist.");
        return false;
    }

    /**
     * remove a category for current user.
     * @param category the category to add
     */
    public boolean removeCategory(String category) {
        User user = ApplicationRuntime.getInstance().getCurrentUser();
        if (! user.removeCategory(category)) {
            LogUtils.error("Category " + category + " does not exist for current user: " + user.getUsername());
            return false;
        }
        save();
        LogUtils.info("Category " + category + " removed for current user: " + user.getUsername());
        return true;
    }

}
