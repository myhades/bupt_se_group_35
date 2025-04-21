package org.group35.model;

import com.google.gson.annotations.SerializedName;

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

    public User(String username, String hashedPassword) {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.avatar = null;
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
}
