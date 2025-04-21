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
        transactions = PersistentDataManager.getStore().getTransactions();
        if (transactions == null) {
            transactions = new ArrayList<>();
        }
    }

    /** Returns all transactions. */
    public List<Transaction> getAll() {
        return new ArrayList<>(transactions);
    }

    /** Returns transactions for a given user. */
    public List<Transaction> getByUser(String username) {
        return transactions.stream()
                .filter(tx -> username.equals(tx.getUsername()))
                .collect(Collectors.toList());
    }

    /** Adds a new transaction and persists the store. */
    public void add(Transaction tx) {
        tx.setTimestamp(LocalDateTime.now());
        transactions.add(tx);
        save();
    }

    /** Updates an existing transaction by ID. */
    public void update(Transaction updated) {
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getId().equals(updated.getId())) {
                transactions.set(i, updated);
                save();
                return;
            }
        }
        LoggerHelper.warn("Transaction not found: " + updated.getId());
    }

    /** Deletes a transaction by ID. */
    public void delete(String id) {
        transactions.removeIf(tx -> tx.getId().equals(id));
        save();
    }

    /** Sets the monthly budget for a specific user's future entries. */
    public void setMonthlyBudget(String username, BigDecimal budget) {
        // update existing entries' budget field if needed
        getByUser(username).forEach(tx -> tx.setMonthlyBudget(budget));
        save();
    }

    private void save() {
        PersistentDataManager.getStore().setTransactions(transactions);
        PersistentDataManager.saveStore();
    }
}
