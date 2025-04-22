package org.group35.controller;

import org.group35.model.Transaction;
import org.group35.persistence.PersistentDataManager;
import org.group35.util.LoggerHelper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages loading, saving, and querying financial transactions.
 */
public class TransactionManager {
    private List<Transaction> transactions;

    public TransactionManager() {
        LoggerHelper.debug("Initializing TransactionManager and loading transactions");
        transactions = PersistentDataManager.getStore().getTransactions();
        if (transactions == null) {
            LoggerHelper.info("No existing transactions found, starting with empty list");
            transactions = new ArrayList<>();
        } else {
            LoggerHelper.info("Loaded " + transactions.size() + " transactions from store");
        }
    }

    /** Returns all transactions. */
    public List<Transaction> getAll() {
        LoggerHelper.trace("Retrieving all transactions, count: " + transactions.size());
        return new ArrayList<>(transactions);
    }

    /** Returns transactions for a given user. */
    public List<Transaction> getByUser(String username) {
        LoggerHelper.trace("Filtering transactions for user: " + username);
        return transactions.stream()
                .filter(tx -> username.equals(tx.getUsername()))
                .collect(Collectors.toList());
    }

    /** Adds a new transaction and persists the store. */
    public void add(Transaction tx) {
        tx.setTimestamp(LocalDateTime.now());
        transactions.add(tx);
        LoggerHelper.info("Adding new transaction for user " + tx.getUsername() + ": " + tx.getName() + " (" + tx.getAmount() + ")");
        save();
    }

    /** Updates an existing transaction by ID. */
    public void update(Transaction updated) {
        LoggerHelper.debug("Updating transaction: " + updated.getId());
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getId().equals(updated.getId())) {
                transactions.set(i, updated);
                LoggerHelper.info("Transaction updated: " + updated.getId());
                save();
                return;
            }
        }
        LoggerHelper.warn("Transaction not found: " + updated.getId());
    }

    /** Deletes a transaction by ID. */
    public void delete(String id) {
        LoggerHelper.info("Deleting transaction: " + id);
        boolean removed = transactions.removeIf(tx -> tx.getId().equals(id));
        if (!removed) {
            LoggerHelper.warn("No transaction removed; ID not found: " + id);
        }
        save();
    }

//    /** Sets the monthly budget for a specific user's future entries. */
//    public void setMonthlyBudget(String username, BigDecimal budget) {
//        LoggerHelper.info("Setting monthly budget for user " + username + " to " + budget);
//        getByUser(username).forEach(tx -> tx.setMonthlyBudget(budget));
//        save();
//    }

    /** Save the current transaction list back to the persistent store. */
    private void save() {
        LoggerHelper.debug("Persisting " + transactions.size() + " transactions to store");
        PersistentDataManager.getStore().setTransactions(transactions);
        PersistentDataManager.saveStore();
        LoggerHelper.info("Transactions saved successfully");
    }
}