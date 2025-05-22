package org.group35.controller;

import org.group35.config.Settings;
import org.group35.model.Transaction;
import org.group35.persistence.PersistentDataManager;
import org.group35.runtime.ApplicationRuntime;
import org.group35.model.User;
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

    /** Each JUnit test has the same initial conditions when running */
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
        // Add some transactions to the store for testing
        Transaction tx1 = new Transaction();
        tx1.setUsername("TestUser");
        tx1.setName("TestTransaction1");
        tx1.setAmount(new BigDecimal("100.00"));
        txManager.add(tx1);
        tx1.setTimestamp(LocalDateTime.of(2025, 5, 20, 10, 0));

        Transaction tx2 = new Transaction();
        tx2.setUsername("TestUser");
        tx2.setName("TestTransaction2");
        tx2.setAmount(new BigDecimal("200.00"));
        txManager.add(tx2);
        tx2.setTimestamp(LocalDateTime.of(2025, 5, 21, 9, 0));

        Transaction tx3 = new Transaction();
        tx3.setUsername("TestUser");
        tx3.setName("TestTransaction3");
        tx3.setAmount(new BigDecimal("-50.00"));
        txManager.add(tx3);
        tx3.setTimestamp(LocalDateTime.of(2025, 5, 22, 8, 0));

        Transaction tx4 = new Transaction();
        tx4.setUsername("TestUser");
        tx4.setName("TestTransaction4");
        tx4.setAmount(new BigDecimal("-150.00"));
        txManager.add(tx4);
        tx4.setTimestamp(LocalDateTime.of(2025, 5, 22, 14, 0));
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
        assertEquals(6, all.size(), "Should import two transactions");

        Transaction first = all.get(4);
        assertEquals("TestUser", first.getUsername(), "Username should be set to current user");
        assertEquals("TestMerchant", first.getName());
        assertEquals(new BigDecimal("100.00"), first.getAmount());
        assertNotNull(first.getTimestamp(), "Timestamp should be set by add() method");

        Transaction second = all.get(5);
        assertEquals("TestUser", second.getUsername());
        assertEquals("CoffeeShop", second.getName());
        assertEquals(new BigDecimal("-25.50"), second.getAmount());
        assertNotNull(second.getTimestamp());
    }

    @Test
    void getAll() {
        List<Transaction> all = txManager.getAll();
        assertEquals(4, all.size(), "Should exist 4 transaction by now");
    }

    @Test
    void getByUser() {
        Transaction tx1 = new Transaction();
        tx1.setUsername("OtherTestUser");
        tx1.setName("TestTransaction1");
        txManager.add(tx1);

        // Invoke getByUser() and verify
        List<Transaction> byUser = txManager.getByUser("OtherTestUser");
        assertEquals(1, byUser.size(), "Should return transactions for the specified user");
        assertEquals("TestTransaction1", byUser.get(0).getName());
    }

    @Test
    void add() {
        // Invoke add() and verify
        Transaction tx = new Transaction();
        tx.setUsername("TestUser");
        tx.setName("TestTransaction7");
        tx.setAmount(new BigDecimal("50.00"));
        txManager.add(tx);

        List<Transaction> all = txManager.getAll();
        assertEquals(5, all.size(), "Should add a new transaction");
        assertEquals("TestTransaction7", all.get(4).getName());
    }

    @Test
    void getByAmountRange() {
        Transaction tx1 = new Transaction();
        tx1.setUsername("TestUser");
        tx1.setName("TestTransaction8");
        tx1.setAmount(new BigDecimal("300.00"));
        txManager.add(tx1);

        // Invoke getByAmountRange() and verify
        List<Transaction> byAmountRange = txManager.getByAmountRange(new BigDecimal("201.00"), new BigDecimal("300.00"));
        assertEquals(1, byAmountRange.size(), "Should return transactions within the specified amount range");
        assertEquals("TestTransaction8", byAmountRange.get(0).getName());
    }

    @Test
    void getByTimestampRange() {
        LocalDateTime start = LocalDateTime.of(2025, 5, 20, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 5, 22, 10, 0);

        List<Transaction> ByTimestampRange = txManager.getByTimestampRange(start, end);

        assertEquals(3, ByTimestampRange.size());
        assertEquals(new BigDecimal("-150.00"), ByTimestampRange.get(0).getAmount());
    }

    @Test
    void update() {

        List<Transaction> TargetTransaction = txManager.getByAmountRange(new BigDecimal("199.00"), new BigDecimal("200.00"));
        String targetid = TargetTransaction.get(0).getId();
        String targetname = TargetTransaction.get(0).getName();
        BigDecimal targetamount = TargetTransaction.get(0).getAmount(); // should be 200

        // Invoke update() and verify
        Transaction updated = new Transaction();
        updated.setId(targetid);
        updated.setUsername(targetname);
        updated.setName("UpdatedTransaction");
        updated.setAmount(targetamount);
        txManager.update(updated);

        List<Transaction> UpdateTransaction = txManager.getByAmountRange(new BigDecimal("199.00"), new BigDecimal("200.00"));
        assertEquals(1, UpdateTransaction.size(), "Should update the transaction");
        assertEquals("UpdatedTransaction", UpdateTransaction.get(0).getName());
    }

    @Test
    void delete() {

        List<Transaction> TargetTransaction = txManager.getByAmountRange(new BigDecimal("199.00"), new BigDecimal("300.00"));
        String targetid = TargetTransaction.get(0).getId();

        // Invoke delete() and verify
        txManager.delete(targetid);

        List<Transaction> all = txManager.getAll();
        assertEquals(3, all.size(), "Should leave 3 transaction");
    }

    @Test
    void getById() {

        // Add a transaction to the store
        Transaction tx = new Transaction();
        tx.setUsername("TestUser");
        tx.setName("TestTransaction8");
        tx.setAmount(new BigDecimal("300.00"));
        txManager.add(tx);

        // Invoke getById() and verify
        Transaction found = txManager.getById(tx.getId());
        assertNotNull(found, "Should return the transaction with the specified ID");
        assertEquals("TestTransaction8", found.getName());
    }

    @Test
    void setTxManager() {
        // 获取已有交易
        List<Transaction> transactions = txManager.getAll();

        Transaction tx = transactions.get(0);
        String originalUsername = tx.getUsername();
        String newUsername = "NewUserName";

        // 执行更新
        txManager.setTxUsername(tx.getId(), newUsername);

        // 验证用户名被修改
        Transaction updated = txManager.getById(tx.getId());
        assertEquals(newUsername, updated.getUsername(), "Username should be updated");
        assertNotEquals(originalUsername, updated.getUsername(), "Username should change");
    }

    @Test
    void setTxName() {
        List<Transaction> transactions = txManager.getAll();
        assertFalse(transactions.isEmpty());

        Transaction tx = transactions.get(0);
        String originalName = tx.getName();
        String newName = "NewTransactionName";

        txManager.setTxName(tx.getId(), newName);

        Transaction updated = txManager.getById(tx.getId());
        assertEquals(newName, updated.getName());
        assertNotEquals(originalName, updated.getName());
    }

    @Test
    void setTxTimestamp() {
        List<Transaction> transactions = txManager.getAll();
        assertFalse(transactions.isEmpty());

        Transaction tx = transactions.get(0);
        LocalDateTime originalTime = tx.getTimestamp();
        LocalDateTime newTime = LocalDateTime.of(2025, 1, 1, 12, 0);

        txManager.setTxTimestamp(tx.getId(), newTime);

        Transaction updated = txManager.getById(tx.getId());
        assertEquals(newTime, updated.getTimestamp());
        assertNotEquals(originalTime, updated.getTimestamp());
    }

    @Test
    void setTxAmount() {
        List<Transaction> transactions = txManager.getAll();
        assertFalse(transactions.isEmpty());

        Transaction tx = transactions.get(0);
        BigDecimal originalAmount = tx.getAmount();
        BigDecimal newAmount = new BigDecimal("999.99");

        txManager.setTxAmount(tx.getId(), newAmount);

        Transaction updated = txManager.getById(tx.getId());
        assertEquals(newAmount, updated.getAmount());
        assertNotEquals(originalAmount, updated.getAmount());
    }

    @Test
    void setTxLocation() {
        List<Transaction> transactions = txManager.getAll();
        assertFalse(transactions.isEmpty());

        Transaction tx = transactions.get(0);
        String originalLocation = tx.getLocation();
        String newLocation = "New York";

        txManager.setTxLocation(tx.getId(), newLocation);

        Transaction updated = txManager.getById(tx.getId());
        assertEquals(newLocation, updated.getLocation());
        assertNotEquals(originalLocation, updated.getLocation());
    }

    @Test
    void setTxCategory() {
        List<Transaction> transactions = txManager.getAll();
        assertFalse(transactions.isEmpty());

        Transaction tx = transactions.get(0);
        String originalCategory = tx.getCategory();
        String newCategory = "Entertainment";

        txManager.setTxCategory(tx.getId(), newCategory);

        Transaction updated = txManager.getById(tx.getId());
        assertEquals(newCategory, updated.getCategory());
        assertNotEquals(originalCategory, updated.getCategory());
    }

    @Test
    void setTxCurrency() {
        List<Transaction> transactions = txManager.getAll();
        assertFalse(transactions.isEmpty());

        Transaction tx = transactions.get(0);
        Transaction.Currency originalCurrency = tx.getCurrency();
        Transaction.Currency newCurrency = Transaction.Currency.USD;

        txManager.setTxCurrency(tx.getId(), newCurrency);

        Transaction updated = txManager.getById(tx.getId());
        assertEquals(newCurrency, updated.getCurrency());
        assertNotEquals(originalCurrency, updated.getCurrency());
    }

    @Test
    void setTxMode() {
        List<Transaction> transactions = txManager.getAll();
        assertFalse(transactions.isEmpty());

        Transaction tx = transactions.get(0);
        Transaction.Mode originalMode = tx.getMode();
        Transaction.Mode newMode = Transaction.Mode.RECURRING;

        txManager.setTxMode(tx.getId(), newMode);

        Transaction updated = txManager.getById(tx.getId());
        assertEquals(newMode, updated.getMode());
        assertNotEquals(originalMode, updated.getMode());
    }

    @Test
    void setTxRecurrencePattern() {
        List<Transaction> transactions = txManager.getAll();
        assertFalse(transactions.isEmpty());

        Transaction tx = transactions.get(0);
        String originalPattern = tx.getRecurrencePattern();
        String newPattern = "Every Monday";

        txManager.setTxRecurrencePattern(tx.getId(), newPattern);

        Transaction updated = txManager.getById(tx.getId());
        assertEquals(newPattern, updated.getRecurrencePattern());
        assertNotEquals(originalPattern, updated.getRecurrencePattern());
    }
}