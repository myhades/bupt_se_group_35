package org.group35.model;

import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import javafx.scene.image.Image;
import java.math.BigDecimal;
import java.util.*;

import org.group35.util.TimezoneUtils;

import static org.group35.util.TimezoneUtils.getCoordinates;
import static org.group35.util.TimezoneUtils.getTimeZoneId;
import org.group35.util.ImageUtils;

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

    /** User's current location, country or city */
    private String location;

    /** User's current time zone */
    private String timezone;

    // Each user has their own categories
    @SerializedName("category")
    private final Map<String, Category> categoryMap = new HashMap<>();

    /** Dynamic Category enum to represent user-defined categories */
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
        this.monthlyBudget = BigDecimal.ZERO;
        this.location = "China, Shanghai"; //TODO: add default location and timezone
        try {
            this.timezone = TimezoneUtils.getTimeZoneId(getCoordinates(this.location)[0], getCoordinates(this.location)[1]);
        }
       catch (IOException e) {
                this.timezone = "Asia/Shanghai";
       }
        try {
            Image defaultAvatar = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/org/group35/view/assets/images/default_avatar.png")));
            this.avatar = ImageUtils.toBase64(defaultAvatar,"png");
        }
        catch (Exception e){
            this.avatar = null;
        }

        // Initialize default categories
        addCategory("Entertainment");
        addCategory("Cater");
        addCategory("Education");
        addCategory("Shopping");
        addCategory("Rent");
        addCategory("Transportation");
        addCategory("Salary");
        addCategory("Transfer");
        addCategory("Investment");
        addCategory("Other");
        addCategory("Unclassified");
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    /**
     * Define a new category dynamically.
     *
     * @param name the name of the category (e.g., "Salary", "Rent")
     * @return  whether new category is created or not
     */
    public Boolean addCategory(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
//            throw new IllegalArgumentException("Category name cannot be null or empty");
        }
        if (categoryMap.containsKey(name)) {
            return false;
//            throw new IllegalArgumentException("Category with name '" + name + "' already exists.");
        }
        Category newCategory = new Category(name);
        categoryMap.put(name, newCategory);
        return true;
    }


    /**
     * Get all defined categories.
     *
     * @return an unmodifiable List of all categories
     */
    public List<String> getCategory() {

        return Collections.unmodifiableList(new ArrayList<>(categoryMap.keySet()));
    }

    /**
     * Remove a category by name.
     *
     * @param name the name of the category to remove
     * @return  whether target category is deleted or not
     */
    public boolean removeCategory(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
//            throw new IllegalArgumentException("Category name cannot be null or empty");
        }
        if (! categoryMap.containsKey(name)) {
            return false;
//            throw new IllegalArgumentException("Category with name '" + name + "' doesnt exist.");
        }
        categoryMap.remove(name);
        return true;
//        return Collections.unmodifiableCollection(categoryMap.keySet());
    }

}
