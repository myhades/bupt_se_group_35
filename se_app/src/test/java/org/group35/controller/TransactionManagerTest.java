package org.group35.controller;

import org.group35.config.Settings;
import org.group35.model.Transaction;
import org.group35.persistence.PersistentDataManager;
import org.group35.runtime.ApplicationRuntime;
import org.group35.model.User;
import org.group35.util.LogUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionManagerTest {

    @TempDir
    Path tempDir;
    private TransactionManager txManager;

    @BeforeEach
    void setUp() {
        // Initialization
        Settings settings = Settings.getInstance();
        settings.setRelativePath("app_data_test");
        PersistentDataManager.initialize(settings);

        // Ensure a clean persistent store
        PersistentDataManager.getStore().setTransactions(new java.util.ArrayList<>());
        PersistentDataManager.saveStore();

        // Set current user in runtime
        ApplicationRuntime runtime = ApplicationRuntime.getInstance();
        User testUser = new User("TestUser", "xx");
        runtime.setCurrentUser(testUser);

        // Initialize TransactionManager
        txManager = new TransactionManager();
    }

    @Test
    void importFromCsv_shouldReadAndAddTransactionsWithCurrentUser() throws IOException {
        // Prepare a sample CSV file
        String csvContent = "date,amount,payee,type\n" +
                "2025-05-20,100.00,TestMerchant,credit\n" +
                "2025-05-19,-25.50,CoffeeShop,debit\n";
        Path csvFile = tempDir.resolve("sample.csv");
        Files.writeString(csvFile, csvContent, StandardCharsets.UTF_8);

        // Invoke import
        txManager.importFromCsv(csvFile.toString());

        // Verify transactions were added
        List<Transaction> all = txManager.getAll();
        assertEquals(2, all.size(), "Should import two transactions");

        Transaction first = all.get(0);
        assertEquals("TestUser", first.getUsername(), "Username should be set to current user");
        assertEquals("TestMerchant", first.getName());
        assertEquals(new BigDecimal("100.00"), first.getAmount());
        assertNotNull(first.getTimestamp(), "Timestamp should be set by add() method");

        Transaction second = all.get(1);
        assertEquals("TestUser", second.getUsername());
        assertEquals("CoffeeShop", second.getName());
        assertEquals(new BigDecimal("-25.50"), second.getAmount());
        assertNotNull(second.getTimestamp());
    }
}