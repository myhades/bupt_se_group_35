package org.group35.controller;

import org.group35.config.Settings;
import org.group35.model.Transaction;
import org.group35.persistence.PersistentDataManager;
import org.group35.runtime.ApplicationRuntime;
import org.group35.model.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
        PersistentDataManager.getStore().setTransactions(new ArrayList<>());
        PersistentDataManager.saveStore();

        // Set current user in runtime
        ApplicationRuntime runtime = ApplicationRuntime.getInstance();
        User testUser = new User("TestUser", "xx");
        testUser.setTimezone("Asia/Shanghai");
        runtime.setCurrentUser(testUser);

        // Initialize TransactionManager
//        txManager = runtime.getTranscationManager(); //TODO: used in production code
        this.txManager = new TransactionManager();
        // Add some transactions to the store for testing
        Transaction tx1 = new Transaction();
        tx1.setUsername("TestUser");
        tx1.setName("ATestTransaction1");
        tx1.setAmount(new BigDecimal("100.00"));
        txManager.add(tx1, "Shopping");
        tx1.setTimestamp(LocalDateTime.of(2025, 5, 20, 10, 0));

        Transaction tx2 = new Transaction();
        tx2.setUsername("TestUser");
        tx2.setName("BTestTransaction2");
        tx2.setAmount(new BigDecimal("200.00"));
        txManager.add(tx2);
        tx2.setTimestamp(LocalDateTime.of(2025, 5, 21, 9, 0));

        Transaction tx3 = new Transaction();
        tx3.setUsername("TestUser");
        tx3.setName("CTestTransaction3");
        tx3.setAmount(new BigDecimal("-50.00"));
        txManager.add(tx3);
        tx3.setTimestamp(LocalDateTime.of(2025, 5, 22, 8, 0));

        Transaction tx4 = new Transaction();
        tx4.setUsername("TestUser");
        tx4.setName("DTestTransaction4");
        tx4.setAmount(new BigDecimal("-150.00"));
        txManager.add(tx4);
        tx4.setTimestamp(LocalDateTime.of(2025, 5, 22, 14, 0));

    }

    @Test
    void getAll() {
        List<Transaction> all = txManager.getTransactions();
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
    void getByName() {
        Transaction tx1 = new Transaction();
        tx1.setUsername("OtherTestUser");
        tx1.setName("TestTransaction_x");
        txManager.add(tx1);

        // Invoke getByUser() and verify
        List<Transaction> byUser = txManager.getByName("TestTransaction_x");
        assertEquals(1, byUser.size(), "Should return transactions for the specified name");
        assertEquals("OtherTestUser", byUser.get(0).getUsername());
    }

    @Test
    void add() {
        // Invoke add() and verify
        Transaction tx = new Transaction();
        tx.setUsername("TestUser");
        tx.setName("TestTransaction7");
        tx.setAmount(new BigDecimal("50.00"));
        txManager.add(tx);

        List<Transaction> all = txManager.getTransactions();
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
        assertEquals(new BigDecimal("100.00"), ByTimestampRange.get(0).getAmount());
    }

    @Test
    void sortByAmount_Ascending() {
        List<Transaction> sorted = txManager.sortByAmount(true);
        assertEquals(new BigDecimal("-150.00"), sorted.get(0).getAmount()); // biggest
        assertEquals(new BigDecimal("200.00"), sorted.get(3).getAmount()); // smallest
    }

    @Test
    void sortByAmount_Descending() {
        List<Transaction> sorted = txManager.sortByAmount(false);
        assertEquals(new BigDecimal("200.00"), sorted.get(0).getAmount()); // biggest
        assertEquals(new BigDecimal("-150.00"), sorted.get(3).getAmount()); // smallest
    }

    @Test
    void sortByTimestamp_Ascending() {
        List<Transaction> sorted = txManager.sortByTimestamp(true);
        assertEquals(LocalDateTime.of(2025, 5, 20, 10, 0), sorted.get(0).getTimestamp()); // earliest
        assertEquals(LocalDateTime.of(2025, 5, 22, 14, 0), sorted.get(3).getTimestamp()); // latest
    }

    @Test
    void sortByTimestamp_Descending() {
        List<Transaction> sorted = txManager.sortByTimestamp(false);
        assertEquals(LocalDateTime.of(2025, 5, 22, 14, 0), sorted.get(0).getTimestamp()); // lastest
        assertEquals(LocalDateTime.of(2025, 5, 20, 10, 0), sorted.get(3).getTimestamp()); // earliest
    }

    @Test
    void sortByName_Ascending() {
        List<Transaction> sorted = txManager.sortByName(true);
        assertEquals("ATestTransaction1", sorted.get(0).getName()); // smallest
        assertEquals("DTestTransaction4", sorted.get(3).getName()); // biggest
    }

    @Test
    void sortByName_Descending() {
        List<Transaction> sorted = txManager.sortByName(false);
        assertEquals("DTestTransaction4", sorted.get(0).getName()); // biggest
        assertEquals("ATestTransaction1", sorted.get(3).getName()); // smallest
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

        List<Transaction> all = txManager.getTransactions();
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
        List<Transaction> transactions = txManager.getTransactions();

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
        List<Transaction> transactions = txManager.getTransactions();
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
        List<Transaction> transactions = txManager.getTransactions();
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
        List<Transaction> transactions = txManager.getTransactions();
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
        List<Transaction> transactions = txManager.getTransactions();
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
//        List<Transaction> transactions = txManager.getTransactions();
//        assertFalse(transactions.isEmpty());
//
//        Transaction tx = transactions.get(0);
//        String originalCategory = tx.getCategory();
//        String newCategory = "Entertainment";
//
//        txManager.setTxCategory(tx.getId(), newCategory);
//
//        Transaction updated = txManager.getById(tx.getId());
//        assertEquals(newCategory, updated.getCategory());
//        assertNotEquals(originalCategory, updated.getCategory());
    }

    @Test
    void setTxCurrency() {
        List<Transaction> transactions = txManager.getTransactions();
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
        List<Transaction> transactions = txManager.getTransactions();
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
        List<Transaction> transactions = txManager.getTransactions();
        assertFalse(transactions.isEmpty());

        Transaction tx = transactions.get(0);
        String originalPattern = tx.getRecurrencePattern();
        String newPattern = "Every Monday";

        txManager.setTxRecurrencePattern(tx.getId(), newPattern);

        Transaction updated = txManager.getById(tx.getId());
        assertEquals(newPattern, updated.getRecurrencePattern());
        assertNotEquals(originalPattern, updated.getRecurrencePattern());
    }

    @Test
    void getTxCategory() {
        List<Transaction> transactions = txManager.getTransactions();
        assertFalse(transactions.isEmpty());
        Transaction tx = transactions.get(0);
        assertEquals("Shopping", tx.getCategory());
        assertEquals("Shopping", txManager.getTxCategory(tx.getId()));
    }

    @Test
    void testPartialMatchName() {
        List<Transaction> result = txManager.searchByKeyword("DTest");
        assertEquals(1, result.size());
        assertEquals("DTestTransaction4", result.get(0).getName());
    }

    @AfterEach
    void tearDown() {
        Transaction tx1 = new Transaction();
        tx1.setUsername("TestUser");
        tx1.setName("Starbucks Coffee");
        tx1.setAmount(new BigDecimal("-50.00"));
        tx1.setCurrency(Transaction.Currency.CNY);
        txManager.add(tx1, "Cater");
        tx1.setTimestamp(LocalDateTime.of(2025, 5, 20, 10, 0));
        tx1.setLocation("China, Beijing");

        Transaction tx2 = new Transaction();
        tx2.setUsername("TestUser");
        tx2.setName("Ginza Mall");
        tx2.setAmount(new BigDecimal("-200.00"));
        tx2.setCurrency(Transaction.Currency.CNY);
        txManager.add(tx2, "Shopping");
        tx2.setTimestamp(LocalDateTime.of(2025, 5, 21, 9, 0));
        tx2.setLocation("Japan, Tokyo");

        Transaction tx3 = new Transaction();
        tx3.setUsername("TestUser");
        tx3.setName("Beijing Subway");
        tx3.setAmount(new BigDecimal("-6.00"));
        tx3.setCurrency(Transaction.Currency.CNY);
        txManager.add(tx3, "Transportation");
        tx3.setTimestamp(LocalDateTime.of(2025, 5, 21, 12, 0));
        tx3.setLocation("China, Beijing");

        Transaction tx4 = new Transaction();
        tx4.setUsername("TestUser");
        tx4.setName("ByteDance");
        tx4.setAmount(new BigDecimal("5000.00"));
        tx4.setCurrency(Transaction.Currency.CNY);
        txManager.add(tx4, "Salary");
        tx4.setTimestamp(LocalDateTime.of(2025, 5, 22, 11, 12));
        tx4.setLocation("China, Beijing");

        Transaction tx5 = new Transaction();
        tx5.setUsername("TestUser");
        tx5.setName("Wechat Pay");
        tx5.setAmount(new BigDecimal("200.00"));
        tx5.setCurrency(Transaction.Currency.CNY);
        txManager.add(tx5, "Transfer");
        tx5.setTimestamp(LocalDateTime.of(2025, 5, 22, 21, 05));
        tx5.setLocation("China, Beijing");

        Transaction tx6 = new Transaction();
        tx6.setUsername("TestUser");
        tx6.setName("Coursera");
        tx6.setAmount(new BigDecimal("-199.00"));
        tx6.setCurrency(Transaction.Currency.CNY);
        tx6.setTimestamp(LocalDateTime.of(2025, 5, 23, 14, 30));
        txManager.add(tx6, "Education");
        tx6.setLocation("United States, New York");

        Transaction tx7 = new Transaction();
        tx7.setUsername("TestUser");
        tx7.setName("Apple Store");
        tx7.setAmount(new BigDecimal("-8999.00"));
        tx7.setCurrency(Transaction.Currency.CNY);
        tx7.setTimestamp(LocalDateTime.of(2025, 5, 23, 16, 45));
        txManager.add(tx7, "Shopping");
        tx7.setLocation("United States, New York");

        Transaction tx8 = new Transaction();
        tx8.setUsername("TestUser");
        tx8.setName("iQIYI VIP");
        tx8.setAmount(new BigDecimal("-30.00"));
        tx8.setCurrency(Transaction.Currency.CNY);
        tx8.setTimestamp(LocalDateTime.of(2025, 5, 24, 10, 15));
        txManager.add(tx8, "Entertainment");
        tx8.setLocation("China, Shanghai");

        Transaction tx9 = new Transaction();
        tx9.setUsername("TestUser");
        tx9.setName("Alibaba Rent");
        tx9.setAmount(new BigDecimal("-3500.00"));
        tx9.setCurrency(Transaction.Currency.CNY);
        tx9.setTimestamp(LocalDateTime.of(2025, 5, 24, 11, 0));
        txManager.add(tx9, "Rent");
        tx9.setLocation("China, Hangzhou");

        Transaction tx10 = new Transaction();
        tx10.setUsername("TestUser");
        tx10.setName("BOC Fund");
        tx10.setAmount(new BigDecimal("-2000.00"));
        tx10.setCurrency(Transaction.Currency.CNY);
        tx10.setTimestamp(LocalDateTime.of(2025, 5, 25, 9, 30));
        txManager.add(tx10, "Investment");
        tx10.setLocation("China, Beijing");

        Transaction tx11 = new Transaction();
        tx11.setUsername("TestUser");
        tx11.setName("Amazon Bookstore");
        tx11.setAmount(new BigDecimal("-150.00"));
        tx11.setCurrency(Transaction.Currency.CNY);
        tx11.setTimestamp(LocalDateTime.of(2025, 4, 5, 18, 20));
        txManager.add(tx11, "Shopping");
        tx11.setLocation("United States, Seattle");

        Transaction tx12 = new Transaction();
        tx12.setUsername("TestUser");
        tx12.setName("Tutoring Center");
        tx12.setAmount(new BigDecimal("-400.00"));
        tx12.setCurrency(Transaction.Currency.CNY);
        tx12.setTimestamp(LocalDateTime.of(2025, 4, 6, 14, 0));
        txManager.add(tx12, "Education");
        tx12.setLocation("China, Shanghai");

        Transaction tx13 = new Transaction();
        tx13.setUsername("TestUser");
        tx13.setName("KFC Lunch");
        tx13.setAmount(new BigDecimal("-60.00"));
        tx13.setCurrency(Transaction.Currency.CNY);
        tx13.setTimestamp(LocalDateTime.of(2025, 4, 8, 12, 30));
        txManager.add(tx13, "Cater");
        tx13.setLocation("China, Beijing");

        Transaction tx14 = new Transaction();
        tx14.setUsername("TestUser");
        tx14.setName("ByteDance");
        tx14.setAmount(new BigDecimal("300.00"));
        tx14.setCurrency(Transaction.Currency.CNY);
        tx14.setTimestamp(LocalDateTime.of(2025, 4, 10, 20, 15));
        txManager.add(tx14, "Transfer");
        tx14.setLocation("China, Shanghai");

        Transaction tx15 = new Transaction();
        tx15.setUsername("TestUser");
        tx15.setName("Shanghai Metro");
        tx15.setAmount(new BigDecimal("-8.00"));
        tx15.setCurrency(Transaction.Currency.CNY);
        tx15.setTimestamp(LocalDateTime.of(2025, 4, 12, 8, 45));
        txManager.add(tx15, "Transportation");
        tx15.setLocation("China, Shanghai");

        Transaction tx16 = new Transaction();
        tx16.setUsername("TestUser");
        tx16.setName("Netflix Subscription");
        tx16.setAmount(new BigDecimal("-45.00"));
        tx16.setCurrency(Transaction.Currency.CNY);
        tx16.setTimestamp(LocalDateTime.of(2025, 4, 15, 22, 0));
        txManager.add(tx16, "Entertainment");
        tx16.setLocation("United States, New York");

        Transaction tx17 = new Transaction();
        tx17.setUsername("TestUser");
        tx17.setName("scholarship");
        tx17.setAmount(new BigDecimal("8000.00"));
        tx17.setCurrency(Transaction.Currency.CNY);
        tx17.setTimestamp(LocalDateTime.of(2025, 4, 18, 9, 30));
        txManager.add(tx17, "Other");
        tx17.setLocation("China, Beijing");

        Transaction tx18 = new Transaction();
        tx18.setUsername("TestUser");
        tx18.setName("Monthly Rent");
        tx18.setAmount(new BigDecimal("-4000.00"));
        tx18.setCurrency(Transaction.Currency.CNY);
        tx18.setTimestamp(LocalDateTime.of(2025, 4, 20, 10, 0));
        txManager.add(tx18, "Rent");
        tx18.setLocation("China, Beijing");


        Transaction tx19 = new Transaction();
        tx19.setUsername("TestUser");
        tx19.setName("HKEX");
        tx19.setAmount(new BigDecimal("-5000.00"));
        tx19.setCurrency(Transaction.Currency.CNY);
        tx19.setTimestamp(LocalDateTime.of(2025, 4, 25, 15, 45));
        txManager.add(tx19, "Investment");
        tx19.setLocation("China, Beijing");

        Transaction tx20 = new Transaction();
        tx20.setUsername("TestUser");
        tx20.setName("Disney Plus");
        tx20.setAmount(new BigDecimal("-35.00"));
        tx20.setCurrency(Transaction.Currency.CNY);
        tx20.setTimestamp(LocalDateTime.of(2025, 5, 1, 20, 0));
        txManager.add(tx20, "Entertainment");
        tx20.setLocation("United States, Los Angeles");

        Transaction tx21 = new Transaction();
        tx21.setUsername("TestUser");
        tx21.setName("Amazon Prime");
        tx21.setAmount(new BigDecimal("-98.00"));
        tx21.setCurrency(Transaction.Currency.CNY);
        tx21.setTimestamp(LocalDateTime.of(2025, 2, 3, 21, 45));
        txManager.add(tx21, "Entertainment");
        tx21.setLocation("United States, New York");

        Transaction tx22 = new Transaction();
        tx22.setUsername("TestUser");
        tx22.setName("Tsinghua Online Course");
        tx22.setAmount(new BigDecimal("-800.00"));
        tx22.setCurrency(Transaction.Currency.CNY);
        tx22.setTimestamp(LocalDateTime.of(2025, 2, 6, 10, 30));
        txManager.add(tx22, "Education");
        tx22.setLocation("China, Beijing");

        Transaction tx23 = new Transaction();
        tx23.setUsername("TestUser");
        tx23.setName("Starbucks Coffee");
        tx23.setAmount(new BigDecimal("-35.00"));
        tx23.setCurrency(Transaction.Currency.CNY);
        tx23.setTimestamp(LocalDateTime.of(2025, 2, 8, 8, 45));
        txManager.add(tx23, "Cater");
        tx23.setLocation("China, Beijing");

        Transaction tx24 = new Transaction();
        tx24.setUsername("TestUser");
        tx24.setName("Alipay");
        tx24.setAmount(new BigDecimal("150.00"));
        tx24.setCurrency(Transaction.Currency.CNY);
        tx24.setTimestamp(LocalDateTime.of(2025, 2, 10, 15, 20));
        txManager.add(tx24, "Transfer");
        tx24.setLocation("China, Beijing");

        Transaction tx25 = new Transaction();
        tx25.setUsername("TestUser");
        tx25.setName("Beijing Subway");
        tx25.setAmount(new BigDecimal("-2.50"));
        tx25.setCurrency(Transaction.Currency.CNY);
        tx25.setTimestamp(LocalDateTime.of(2025, 2, 12, 7, 55));
        txManager.add(tx25, "Transportation");
        tx25.setLocation("China, Beijing");

        Transaction tx26 = new Transaction();
        tx26.setUsername("TestUser");
        tx26.setName("ByteDance");
        tx26.setAmount(new BigDecimal("20000.00"));
        tx26.setCurrency(Transaction.Currency.CNY);
        tx26.setTimestamp(LocalDateTime.of(2025, 2, 15, 9, 0));
        txManager.add(tx26, "Salary");
        tx26.setLocation("China, Beijing");

        Transaction tx27 = new Transaction();
        tx27.setUsername("TestUser");
        tx27.setName("IKEA Furniture");
        tx27.setAmount(new BigDecimal("-1200.00"));
        tx27.setCurrency(Transaction.Currency.CNY);
        tx27.setTimestamp(LocalDateTime.of(2025, 2, 18, 14, 0));
        txManager.add(tx27, "Shopping");
        tx27.setLocation("China, Beijing");

        Transaction tx28 = new Transaction();
        tx28.setUsername("TestUser");
        tx28.setName("Monthly Rent");
        tx28.setAmount(new BigDecimal("-4200.00"));
        tx28.setCurrency(Transaction.Currency.CNY);
        tx28.setTimestamp(LocalDateTime.of(2025, 2, 20, 10, 0));
        txManager.add(tx28, "Rent");
        tx28.setLocation("China, Beijing");

        Transaction tx29 = new Transaction();
        tx29.setUsername("TestUser");
        tx29.setName("HKEX");
        tx29.setAmount(new BigDecimal("-3000.00"));
        tx29.setCurrency(Transaction.Currency.CNY);
        tx29.setTimestamp(LocalDateTime.of(2025, 2, 25, 16, 30));
        txManager.add(tx29, "Investment");
        tx29.setLocation("Hong Kong, Hong Kong");

        Transaction tx30 = new Transaction();
        tx30.setUsername("TestUser");
        tx30.setName("Miscellaneous Expense");
        tx30.setAmount(new BigDecimal("-80.00"));
        tx30.setCurrency(Transaction.Currency.CNY);
        tx30.setTimestamp(LocalDateTime.of(2025, 3, 2, 12, 15));
        tx30.setLocation("China, Beijing");
        txManager.add(tx30, "Other");
    }
}