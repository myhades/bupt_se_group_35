package org.group35.controller;

import org.group35.config.Settings;
import org.group35.model.User;
import org.group35.persistence.PersistentDataManager;
import org.group35.runtime.ApplicationRuntime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class UserManagerTest {

    @TempDir
    Path tempDir;
    private TransactionManager uManager;

    /** Each JUnit test has the same initial conditions when running */
    @BeforeEach
    void setUp() {
        // Initialization
        Settings settings = Settings.getInstance();
        settings.setRelativePath("app_data_test");
        PersistentDataManager.initialize(settings);

        // Ensure a clean persistent store
        PersistentDataManager.getStore().setUsers(new ArrayList<>());
        PersistentDataManager.saveStore();
        ApplicationRuntime runtime = ApplicationRuntime.getInstance();

        // Initialize UserManager
        UserManager uManager = new UserManager();
        // runtime.getUserManager().registerUser //TODO: used in production code
        uManager.registerUser("TestUser", "abcd1234");

        // Set current user in runtime

        User testUser = uManager.getByUser("TestUser").get(0);
        uManager.setMonthlyBudget("TestUser", BigDecimal.valueOf(100));
        runtime.setCurrentUser(testUser);



    }

    @Test
    void registerUser() {
    }

    @Test
    void loginUser() {
    }

    @Test
    void getUsers() {
    }

    @Test
    void setUsers() {
    }

    @Test
    void setUserAvatar() {
    }

    @Test
    void getUserAvatar() {
    }

    @Test
    void getByUser() {
    }

    @Test
    void setMonthlyBudget() {
    }
}