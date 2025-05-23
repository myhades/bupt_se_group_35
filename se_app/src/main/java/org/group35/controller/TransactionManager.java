package org.group35.controller;

import org.group35.model.Transaction;
import org.group35.model.User;
import org.group35.persistence.PersistentDataManager;
import org.group35.runtime.ApplicationRuntime;
import org.group35.service.CsvImport;
import org.group35.util.LogUtils;
import org.group35.util.TimezoneUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;

/**
 * Manages loading, saving, and querying financial transactions.
 * <br><br>
 * Usage Example:
 * <br>
 * {@code ApplicationRuntime runtime = ApplicationRuntime.getInstance();}
 * <br>
 * {@code TransactionManager txManager = runtime.getTranscationManager();}
 * <br>
 * {@code List<Transaction> tx = txManager.getByUser(runtime.getCurrentUser().getUsername());}
 * <br>
 *
 */
public class TransactionManager {
    private List<Transaction> transactions;
    private final CsvImport csvImportService = new CsvImport();

    public TransactionManager() {
        LogUtils.debug("Initializing TransactionManager and loading transactions");
        transactions = PersistentDataManager.getStore().getTransactions();
        if (transactions == null) {
            LogUtils.info("No existing transactions found, starting with empty list");
            transactions = new ArrayList<>();
        } else {
            LogUtils.info("Loaded " + transactions.size() + " transactions from store");
        }
    }

    /**
     * Get the list of transactions from the runtime store.
     */
    public List<Transaction> getTransactions() {
        LogUtils.trace("Retrieving all transactions, count: " + transactions.size());
//        return new ArrayList<>(transactions); //TODO: return copy maybe safer?
        return transactions;
    }

    /**
     * Get the list of transactions from the persistent store.
     */
    public List<Transaction> getPersistentTxs() {
        List<Transaction> transactions = PersistentDataManager.getStore().getTransactions();
        LogUtils.trace("Retrieving all transactions, count: " + (transactions != null ? transactions.size() : 0));
        return transactions;
    }


    /** Returns transactions for a given user. */
    public List<Transaction> getByUser(String username) {
        LogUtils.trace("Filtering transactions for user: " + username);
        return transactions.stream()
                .filter(tx -> username.equals(tx.getUsername()))
                .collect(Collectors.toList());
    }

    /** Returns transactions for a given name. */
    public List<Transaction> getByName(String name) {
        LogUtils.trace("Filtering transactions for user: " + name);
        return transactions.stream()
                .filter(tx -> name.equals(tx.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Fuzzy search transaction records and perform partial matching based on the name or category fields.
     *
     * @param keyword keyword to search
     * @return matched transactions
     */
    public List<Transaction> searchByKeyword(String keyword) {
        return searchByKeyword(transactions, keyword);
    }

    /**
     * Fuzzy search transaction records and perform partial matching based on the name or category fields.
     *
     * @param txs list of transactions to search from
     * @param keyword keyword to search
     * @return matched transactions
     */
    public List<Transaction> searchByKeyword(List<Transaction> txs, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return new ArrayList<>();
        }

        LogUtils.trace("Fuzzy searching transactions by name or category with keyword: " + keyword);
        String lowerKeyword = keyword.trim().toLowerCase();

        return txs.stream()
                .filter(tx -> tx.getName() != null && tx.getName().toLowerCase().contains(lowerKeyword)
                        || tx.getCategory() != null && tx.getCategory().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }

    /** Returns transactions within the given amount range. bigger or equal, smaller or equal */
    public List<Transaction> getByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        LogUtils.trace("Filtering transactions by amount range: " + minAmount + " - " + maxAmount);
        if (minAmount != null && maxAmount != null && minAmount.compareTo(maxAmount) > 0) {
            LogUtils.warn("Invalid amount range: minAmount > maxAmount");
            return new ArrayList<>();
        }
        return transactions.stream()
                .filter(tx -> {
                    if (minAmount != null && maxAmount != null) {
                        return tx.getAmount().compareTo(minAmount) >= 0 && tx.getAmount().compareTo(maxAmount) <= 0;
                    } else if (minAmount != null) {
                        return tx.getAmount().compareTo(minAmount) >= 0;
                    } else if (maxAmount != null) {
                        return tx.getAmount().compareTo(maxAmount) <= 0;
                    } else {
                        return true;
                    }
                })
                .collect(Collectors.toList());
    }

    /** Returns transactions within the given timestamp range. bigger or equal, smaller or equal */
    public List<Transaction> getByTimestampRange(LocalDateTime start, LocalDateTime end) {
        LogUtils.trace("Filtering transactions by timestamp range: " + start + " - " + end);
        if (start != null && end != null && start.isAfter(end)) {
            LogUtils.warn("Invalid timestamp range: start > end");
            return new ArrayList<>();
        }
        return transactions.stream()
                .filter(tx -> {
                    if (start != null && end != null) {
                        return (tx.getTimestamp().isEqual(start) || tx.getTimestamp().isAfter(start)) &&
                                (tx.getTimestamp().isEqual(end) || tx.getTimestamp().isBefore(end));
                    } else if (start != null) {
                        return tx.getTimestamp().isEqual(start) || tx.getTimestamp().isAfter(start);
                    } else if (end != null) {
                        return tx.getTimestamp().isEqual(end) || tx.getTimestamp().isBefore(end);
                    } else {
                        return true;
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * sort by amount
     * @param ascending = true sort by ascending，= false sort by descending
     */
    public List<Transaction> sortByAmount(boolean ascending) {
        return sortByAmount(transactions, ascending);
    }

    /**
     * sort by amount
     * @param txs list of transactions to sort
     * @param ascending = true sort by ascending，= false sort by descending
     */
    public List<Transaction> sortByAmount(List<Transaction> txs, boolean ascending) {
        Comparator<Transaction> comparator = Comparator.comparing(Transaction::getAmount);
        if (!ascending) {
            comparator = comparator.reversed();
        }
        return txs.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * Sort by timestamp
     * @param ascending = true sort by ascending，= false sort by descending
     */
    public List<Transaction> sortByTimestamp(boolean ascending) {
        return sortByTimestamp(transactions, ascending);
    }

    /**
     * Sort by timestamp
     * @param txs list of transactions to sort
     * @param ascending = true sort by ascending，= false sort by descending
     */
    public List<Transaction> sortByTimestamp(List<Transaction> txs, boolean ascending) {
        Comparator<Transaction> comparator = Comparator.comparing(Transaction::getTimestamp);
        if (!ascending) {
            comparator = comparator.reversed();
        }
        return txs.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * sort by name
     * @param ascending = true sort by ascending，= false sort by descending
     */
    public List<Transaction> sortByName(boolean ascending) {
        return sortByName(transactions, ascending);
    }

    /**
     * sort by name
     * @param txs list of transactions to sort
     * @param ascending = true sort by ascending，= false sort by descending
     */
    public List<Transaction> sortByName(List<Transaction> txs, boolean ascending) {
        Comparator<Transaction> comparator = Comparator.comparing(Transaction::getName);
        if (!ascending) {
            comparator = comparator.reversed();
        }
        return txs.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /** Adds a new transaction and persists the store. */
    public void add(Transaction tx) {
        transactions.add(tx);
        LogUtils.info("Adding new transaction for user " + tx.getUsername() + ": " + tx.getName() + " (" + tx.getAmount() + ")");
        save();
    }

    public void add(Transaction tx, String category) {
        User currentUser = ApplicationRuntime.getInstance().getCurrentUser();
        tx.setCategory(currentUser.getCategory().contains(category) ? category : null);
        tx.setTimestamp(TimezoneUtils.getFormattedCurrentTimeByZone(currentUser.getTimezone())); //TODO
        transactions.add(tx);
        save();
        LogUtils.info("Adding new transaction for user " + tx.getUsername() + ": " + tx.getName() + " (" + tx.getAmount() + ")");
    }

    /** Updates an existing transaction by ID. */
    public void update(Transaction updated) {
        LogUtils.debug("Updating transaction: " + updated.getId());
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getId().equals(updated.getId())) {
                transactions.set(i, updated);
                LogUtils.info("Transaction updated: " + updated.getId());
                save();
                return;
            }
        }
        LogUtils.warn("Transaction not found: " + updated.getId());
    }

    /** Deletes a transaction by ID. */
    public void delete(String id) {
        LogUtils.info("Deleting transaction: " + id);
        boolean removed = transactions.removeIf(tx -> tx.getId().equals(id));
        if (!removed) {
            LogUtils.warn("No transaction removed; ID not found: " + id);
        }
        save();
    }

    /**
     * Import a CSV, tag each Transaction with the current user,
     * and add them to the store.
     */
    public void importFromCsv(String filePath) throws IOException {
        String currentUser = ApplicationRuntime
                .getInstance()
                .getCurrentUser()
                .getUsername();

        List<Transaction> txs = csvImportService.parseTransactions(filePath);
        for (Transaction tx : txs) {
            tx.setUsername(currentUser);
            add(tx);
        }
        save();
    }

    /** Save the current transaction list back to the persistent store. */
    private void save() {
        LogUtils.debug("Persisting " + transactions.size() + " transactions to store");
        PersistentDataManager.getStore().setTransactions(transactions);
        PersistentDataManager.saveStore();
        LogUtils.info("Transactions saved successfully");
    }
    

    /** Returns the transaction with the given ID. */
    public Transaction getById(String id) {
        LogUtils.trace("Retrieving transaction by ID: " + id);
        return transactions.stream()
                .filter(tx -> id.equals(tx.getId()))
                .findFirst()
                .orElse(null);
    }

    /** Sets the username of the transaction with the given ID. */
    public void setTxUsername(String id, String username) {
        LogUtils.debug("Setting username for transaction: " + id);
        Transaction tx = getById(id);
        if (tx != null) {
            tx.setUsername(username);
            save();
        } else {
            LogUtils.warn("Transaction not found: " + id);
        }
        save();
    }

    /** Sets the name of the transaction with the given ID. */
    public void setTxName(String id, String name) {
        LogUtils.debug("Setting name for transaction: " + id);
        Transaction tx = getById(id);
        if (tx != null) {
            tx.setName(name);
            save();
        } else {
            LogUtils.warn("Transaction not found: " + id);
        }
        save();
    }

    /** Sets the timestamp of the transaction with the given ID. */
    public void setTxTimestamp(String id, LocalDateTime timestamp) {
        LogUtils.debug("Setting timestamp for transaction: " + id);
        Transaction tx = getById(id);
        if (tx != null) {
            tx.setTimestamp(timestamp);
            save();
        } else {
            LogUtils.warn("Transaction not found: " + id);
        }
    }

    /** Sets the amount of the transaction with the given ID. */
    public void setTxAmount(String id, BigDecimal amount) {
        LogUtils.debug("Setting amount for transaction: " + id);
        Transaction tx = getById(id);
        if (tx != null) {
            tx.setAmount(amount);
            save();
        } else {
            LogUtils.warn("Transaction not found: " + id);
        }
    }

    /** Sets the location of the transaction with the given ID. */
    public void setTxLocation(String id, String location) {
        LogUtils.debug("Setting location for transaction: " + id);
        Transaction tx = getById(id);
        if (tx != null) {
            tx.setLocation(location);
            save();
        } else {
            LogUtils.warn("Transaction not found: " + id);
        }
    }

    /** Sets the category of the transaction with the given ID. */
    public void setTxCategory(String id, String category) {
        LogUtils.debug("Setting category for transaction: " + id);
        Transaction tx = getById(id);
        if (tx != null) {
            tx.setCategory(category); //TODO: add robust design
            save();
        } else {
            LogUtils.warn("Transaction not found: " + id);
        }
    }

    /** Sets the category of the transaction with the given ID. */
    public String getTxCategory(String id, String category) {
        LogUtils.debug("Setting category for transaction: " + id);
        Transaction tx = getById(id);
        if (tx != null) {
            return tx.getCategory();
        } else {
            LogUtils.warn("Transaction not found: " + id);
            return "Other";
        }
    }

    /** Sets the currency of the transaction with the given ID. */
    public void setTxCurrency(String id, Transaction.Currency currency) {
        LogUtils.debug("Setting currency for transaction: " + id);
        Transaction tx = getById(id);
        if (tx != null) {
            tx.setCurrency(currency);
            save();
        } else {
            LogUtils.warn("Transaction not found: " + id);
        }
    }

    /** Sets the mode of the transaction with the given ID. */
    public void setTxMode(String id, Transaction.Mode mode) {
        LogUtils.debug("Setting mode for transaction: " + id);
        Transaction tx = getById(id);
        if (tx != null) {
            tx.setMode(mode);
            save();
        } else {
            LogUtils.warn("Transaction not found: " + id);
        }
    }

    /** Sets the recurrence pattern of the transaction with the given ID. */
    public void setTxRecurrencePattern(String id, String recurrencePattern) {
        LogUtils.debug("Setting recurrence pattern for transaction: " + id);
        Transaction tx = getById(id);
        if (tx != null) {
            tx.setRecurrencePattern(recurrencePattern);
            save();
        } else {
            LogUtils.warn("Transaction not found: " + id);
        }
    }

    /**
     * Retrieves the list of transactions for the current user.
     *
     * @return List of Transaction objects associated with the current user.
     */
    public static List<Transaction> getTransaction() {
        ApplicationRuntime runtime = ApplicationRuntime.getInstance();
        TransactionManager txManager = runtime.getTranscationManager();
        return txManager.getByUser(runtime.getCurrentUser().getUsername());
    }

    /**
     * Converts the list of transactions to a JSON-like string representation.
     * Each transaction is converted to a string with special characters escaped.
     *
     * @return A JSON-like string representing all transactions.
     */
    public static String transferTransaction() {
        List<Transaction> transactions = getTransaction();
        StringBuilder result = new StringBuilder();
        result.append("["); // Start of JSON array

        for (int i = 0; i < transactions.size(); i++) {
            Transaction t = transactions.get(i);
            if (i > 0) result.append(", ");
            result.append(convertTransactionToEscapedString(t)); // Convert each transaction to string
        }

        result.append("]"); // End of JSON array
        return result.toString();
    }

    /**
     * Converts a single Transaction object into a string, escaping special characters
     * to ensure valid JSON formatting.
     *
     * @param t The Transaction object to convert.
     * @return A string representation of the transaction in JSON-like format with escaped characters.
     */
    private static String convertTransactionToEscapedString(Transaction t) {
        return String.format(
                "{\"id\":\"%s\",\"username\":\"%s\",\"name\":\"%s\",\"timestamp\":\"%s\"," +
                        "\"amount\":%.2f,\"category\":\"%s\",\"currency\":\"%s\"}",
                escapeString(t.getId()),
                escapeString(t.getUsername()),
                escapeString(t.getName()),
                escapeString(t.getTimestamp().toString()),
                t.getAmount(),
                escapeString(t.getCategory()),
                escapeString(t.getCurrency().name())
        );
    }

    /**
     * Escapes special characters in a string to ensure proper formatting for JSON output.
     * The following characters are escaped: backslash, double quotes, newlines, carriage
     * returns, and tabs.
     *
     * @param input The input string to escape.
     * @return A string with escaped special characters.
     */
    private static String escapeString(String input) {
        if (input == null) return "";
        return input
                .replace("\\", "\\\\")   // Escape backslash
                .replace("\"", "\\\"")   // Escape double quotes
                .replace("\n", "\\n")    // Escape newline
                .replace("\r", "\\r")    // Escape carriage return
                .replace("\t", "\\t");   // Escape tab
    }

}