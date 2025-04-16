package org.group35.controller;

import org.group35.model.User;
import org.group35.persistence.PersistentDataManager;
import org.group35.util.PasswordUtils;

import java.util.List;

/**
 * Manages user registration and login operations.
 * Data persistence is handled by PersistentDataManager.
 */
public class UserManager {

    /**
     * Register a new user: hash the password, add the user to the store.
     */
    public static void registerUser(String username, String plainPassword) {
        String hashed = PasswordUtils.hashPassword(plainPassword);
        User newUser = new User(username, hashed);

        List<User> userList = getUsers();
        userList.add(newUser);
        // Save the updated user list to the persistent store
        PersistentDataManager.getStore().setUsers(userList);
        PersistentDataManager.saveStore();
    }

    /**
     * Verify user login credentials.
     */
    public static boolean loginUser(String username, String plainPassword) {
        List<User> users = getUsers();
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                return PasswordUtils.checkPassword(plainPassword, u.getHashedPassword());
            }
        }
        return false;
    }

    /**
     * Get the list of users from the persistent store.
     */
    public static List<User> getUsers() {
        return PersistentDataManager.getStore().getUsers();
    }

    /**
     * Set the list of users in the persistent store (used for testing or bulk updates).
     */
    public static void setUsers(List<User> users) {
        PersistentDataManager.getStore().setUsers(users);
        PersistentDataManager.saveStore();
    }
}
