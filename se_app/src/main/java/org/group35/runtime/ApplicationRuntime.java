package org.group35.runtime;

import org.group35.model.User;
import org.group35.controller.UserManager;
import org.group35.util.SceneManager;

/**
 * ApplicationRuntime is a singleton class that encapsulates all model manager instances.
 * It allows different parts of the application to access model managers conveniently.
 */
public final class ApplicationRuntime {

    /**
     * The possible statuses of the application.
     */
    public enum ProgramStatus {
        LOGIN,
        HOME
        // Add other statuses as needed.
    }

    // Singleton instance of ApplicationRuntime.
    private static ApplicationRuntime instance;

    // Manager instances for different models.
    private final UserManager userManager;

    // Miscellaneous runtime data
    private User loggedInUser;
    private ProgramStatus programStatus;

    /**
     * Private constructor to enforce singleton pattern.
     * Initializes all model managers.
     */
    private ApplicationRuntime() {
        userManager = new UserManager();
        // Initialize additional managers here.
    }

    /**
     * Returns the singleton instance of ApplicationRuntime.
     *
     * @return the ApplicationRuntime instance.
     */
    public static synchronized ApplicationRuntime getInstance() {
        if (instance == null) {
            instance = new ApplicationRuntime();
        }
        return instance;
    }

    /**
     * Returns the UserManager instance.
     *
     * @return the UserManager.
     */
    public UserManager getUserManager() {
        return userManager;
    }

    // Provide getters for additional managers as needed.

    /**
     * Handles user login by setting the current user and updating the program status.
     *
     * @param user the User who has logged in.
     */
    public void loginUser(User user) {
        this.loggedInUser = user;
        setProgramStatus(ProgramStatus.HOME);
    }

    /**
     * Logs out the current user and updates the program status to LOGIN.
     */
    public void logoutUser() {
        this.loggedInUser = null;
        setProgramStatus(ProgramStatus.LOGIN);
    }

    /**
     * Updates the program status and calls SceneManager to switch scenes based on the new status.
     *
     * @param status the new program status.
     */
    public void setProgramStatus(ProgramStatus status) {
        this.programStatus = status;
        // Trigger scene switching using the SceneManager.
        switch (status) {
            case LOGIN:
                SceneManager.showLoginPage();
                break;
            case HOME:
                SceneManager.showHomePage();
                break;
            default:
                // Handle additional statuses as needed.
                break;
        }
    }
}
