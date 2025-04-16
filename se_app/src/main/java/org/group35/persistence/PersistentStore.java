package org.group35.persistence;

import java.util.ArrayList;
import java.util.List;
import org.group35.model.User;

/**
 * PersistentStore aggregates all persistent data.
 * Currently, it only contains a list of users.
 */
public class PersistentStore {
    private List<User> users = new ArrayList<>();

    /**
     * Returns the list of users.
     *
     * @return the list of users.
     */
    public List<User> getUsers() {
        return users;
    }

    /**
     * Sets the list of users.
     *
     * @param users the list of users.
     */
    public void setUsers(List<User> users) {
        this.users = users;
    }
}
