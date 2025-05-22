package org.group35.model;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.*;

/**
 * User data model, including username and encrypted password etc.
 */
public class User {
    private String username;
    private String hashedPassword;

    /**
     * Base64‑encoded, square‑ratio JPEG avatar (or null if none set).
     */
    @SerializedName("avatar")
    private String avatar;

    /** User's current monthly budget at time of entry */
    @SerializedName("monthlyBudget")
    private BigDecimal monthlyBudget;

    // Each user has their own categories
    private final Map<String, Category> categoryMap = new HashMap<>();

    // Dynamic Category enum to represent user-defined categories
    public class Category {
        private final String name;

        private Category(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Category category)) return false;
            return Objects.equals(name, category.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public String toString() {
            return name;
        }
    }


    public User(String username, String hashedPassword) {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.avatar = null;
        this.monthlyBudget = BigDecimal.ZERO;

        // Initialize default categories
        defineCategory("Entertainment");
        defineCategory("Cater");
        defineCategory("Education");
        defineCategory("Shopping");
        defineCategory("Rent");
        defineCategory("Transportation");
        defineCategory("Salary");
        defineCategory("Transfer");
        defineCategory("Investment");
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getHashedPassword() {
        return hashedPassword;
    }
    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }
    public String getAvatar() {
        return avatar;
    }
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    public BigDecimal getMonthlyBudget() {
        return monthlyBudget;
    }
    public void setMonthlyBudget(BigDecimal monthlyBudget) {
        this.monthlyBudget = monthlyBudget;
    }

    /**
     * Define a new category dynamically.
     *
     * @param name the name of the category (e.g., "Salary", "Rent")
     * @return the newly created Category instance
     */
    public Category defineCategory(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }
        if (categoryMap.containsKey(name)) {
            throw new IllegalArgumentException("Category with name '" + name + "' already exists.");
        }
        Category newCategory = new Category(name);
        categoryMap.put(name, newCategory);
        return newCategory;
    }

    /**
     * Retrieve an existing category by its name.
     *
     * @param name the name of the category
     * @return the corresponding Category instance
     */
    public Category getCategory(String name) {
        Category category = categoryMap.get(name);
        if (category == null) {
            throw new IllegalArgumentException("No such category: " + name);
        }
        return category;
    }

    /**
     * Get all defined categories.
     *
     * @return an unmodifiable collection of all categories
     */
    public Collection<Category> getAllCategories() {
        return Collections.unmodifiableCollection(categoryMap.values());
    }

    /**
     * Remove a category by name.
     *
     * @param name the name of the category to remove
     */
    public void removeCategory(String name) {
        categoryMap.remove(name);
    }

}
