package org.group35.service;

import java.io.IOException;
import java.util.List;
import org.group35.model.User;
import org.group35.persistence.PersistentDataManager;
import org.mindrot.jbcrypt.BCrypt;

/**
 * UserManager handles user-specific logic, such as login and registration.
 * It interacts with persistent data via PersistentDataManager.
 */
public class UserManager {

    /**
     * Returns the list of users from the persistent store.
     *
     * @return the list of users.
     */
    public static List<User> loadUsers() {
        return PersistentDataManager.getStore().getUsers();
    }

    /**
     * Validates the provided credentials against the persistent user data.
     *
     * @param username the username provided.
     * @param password the plain text password.
     * @return true if credentials are valid; false otherwise.
     */
    public static boolean loginUser(String username, String password) {
        List<User> users = loadUsers();
        for (User user : users) {
            if (user.getUsername().equals(username)
                    && BCrypt.checkpw(password, user.getHashedPassword())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Registers a new user if the username is not already taken.
     * After registration, it saves the updated persistent store.
     *
     * @param username the username to register.
     * @param password the plain text password.
     * @return true if registration is successful; false otherwise.
     */
    public static boolean registerUser(String username, String password) {
        List<User> users = loadUsers();
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                return false; // Username already exists.
            }
        }
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
        User newUser = new User(username, hashed);
        users.add(newUser);
        try {
            PersistentDataManager.saveStore();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
