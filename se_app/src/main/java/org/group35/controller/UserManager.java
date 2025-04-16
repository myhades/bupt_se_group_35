package org.group35.controller;

import org.group35.model.User;
import org.group35.util.JsonUtils;
import org.group35.util.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages user registration and login operations, with data persisted to a JSON file.
 */
public class UserManager {
    private static final File USER_FILE = new File("users.json");

    /**
     * Register a new user: hash the password, add the user, and save to file.
     */
    public static void registerUser(String username, String plainPassword) throws IOException {
        String hashed = PasswordUtils.hashPassword(plainPassword);
        User newUser = new User(username, hashed);

        List<User> userList = loadUsers();
        userList.add(newUser);
        JsonUtils.writeJsonToFile(USER_FILE, userList);
    }

    /**
     * Load the list of registered users. Returns an empty list if the file does not exist.
     */
    public static List<User> loadUsers() {
        if (!USER_FILE.exists()) {
            return new ArrayList<>();
        }
        try {
            String content = new String(Files.readAllBytes(USER_FILE.toPath()));
            // Assumes the JSON stores an array of User objects
            User[] users = JsonUtils.fromJson(content, User[].class);
            List<User> list = new ArrayList<>();
            if (users != null) {
                for (User u : users) {
                    list.add(u);
                }
            }
            return list;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Verify user login credentials.
     */
    public static boolean loginUser(String username, String plainPassword) {
        List<User> users = loadUsers();
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                return PasswordUtils.checkPassword(plainPassword, u.getHashedPassword());
            }
        }
        return false;
    }
}
