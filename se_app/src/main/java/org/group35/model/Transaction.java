package org.group35.model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A financial transaction (spending or income) linked to a user.
 */
public class Transaction {
    private String id;
    private String username;
    private String name;
    private LocalDateTime timestamp;
    private BigDecimal amount;
    private String location;
    private String category; //TODO: add category field as enum

    /** CNY: Chinese Yuan, HKD: Hong Kong Dollar, EUR: Euro, USD: US Dollar, GBP: British Pound, JPY: Japanese Yen, SGD: Singapore Dollar */
    public enum Currency { CNY, HKD, EUR, USD, GBP, JPY, SGD }
    @SerializedName("currency")
    private Currency currency;

    /** Whether this entry was manual or recurring */
    public enum Mode { MANUAL, RECURRING }
    @SerializedName("mode")
    private Mode mode;

    /** If recurring, the recurrence pattern (e.g. CRON or RRULE) */
    @SerializedName("recurrencePattern")
    private String recurrencePattern;

//    /** User's current monthly budget at time of entry */
//    @SerializedName("monthlyBudget")
//    private BigDecimal monthlyBudget;

    public Transaction() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Mode getMode() {
        return mode;
    }
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public Currency getCurrency() {
        return currency;
    }
    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getRecurrencePattern() {
        return recurrencePattern;
    }
    public void setRecurrencePattern(String recurrencePattern) {
        this.recurrencePattern = recurrencePattern;
    }

//    public BigDecimal getMonthlyBudget() {
//        return monthlyBudget;
//    }
//    public void setMonthlyBudget(BigDecimal monthlyBudget) {
//        this.monthlyBudget = monthlyBudget;
//    }
}
