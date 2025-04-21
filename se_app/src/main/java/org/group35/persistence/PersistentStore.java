package org.group35.persistence;

import java.util.ArrayList;
import java.util.List;
import org.group35.model.User;
import org.group35.model.Transaction;

/**
 * PersistentStore aggregates all persistent data.
 */
public class PersistentStore {
    private List<User> users = new ArrayList<>();
    private List<Transaction> transactions = new ArrayList<>();

    /** Users */
    public List<User> getUsers() {
        return users;
    }
    public void setUsers(List<User> users) {
        this.users = users;
    }

    /** Transactions */
    public List<Transaction> getTransactions() {
        return transactions;
    }
    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}
