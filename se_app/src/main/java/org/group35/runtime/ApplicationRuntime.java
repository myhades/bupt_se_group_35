package org.group35.runtime;

import org.group35.model.User;
import org.group35.controller.UserManager;
import org.group35.util.SceneManager;
import org.group35.util.LoggerHelper;

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
        HOME,
        SPENDING,
        ManualEntry,
        RecogBill
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
        LoggerHelper.debug("Initializing ApplicationRuntime singleton");
        userManager = new UserManager();
        LoggerHelper.trace("UserManager initialized");
    }

    /**
     * Returns the singleton instance of ApplicationRuntime.
     * @return the ApplicationRuntime instance.
     */
    public static synchronized ApplicationRuntime getInstance() {
        if (instance == null) {
            instance = new ApplicationRuntime();
            LoggerHelper.trace("ApplicationRuntime instance created");
        }
        return instance;
    }

    /**
     * Returns the UserManager instance.
     * @return the UserManager.
     */
    public UserManager getUserManager() {
        return userManager;
    }

    /**
     * Handles user login by setting the current user and updating the program status.
     * @param user the User who has logged in.
     */

    /**
     * Returns the currently logged in user.
     * @return the current User, or null if no user is logged in.
     */
    public User getCurrentUser() {
        return loggedInUser;
    }

    /**
     * Sets the current user.
     * @param user the User to set as current.
     */
    public void setCurrentUser(User user) {
        this.loggedInUser = user;
        if (user == null) {
            LoggerHelper.info("Current user set to null");
        } else {
            LoggerHelper.info("Current user set to: " + user.getUsername());
        }
    }



    public void loginUser(User user) {
        this.loggedInUser = user;
        LoggerHelper.info("User logged in: " + user.getUsername());
        setProgramStatus(ProgramStatus.HOME);
    }

    /**
     * Logs out the current user and updates the program status to LOGIN.
     */
    public void logoutUser() {
        if (loggedInUser != null) {
            LoggerHelper.info("User logged out: " + loggedInUser.getUsername());
        }
        this.loggedInUser = null;
        setProgramStatus(ProgramStatus.LOGIN);
    }

    public void gotoSpending() {
        setProgramStatus(ProgramStatus.SPENDING); //TODO: compare with current status
    }

    public void gotoHome() {
        setProgramStatus(ProgramStatus.HOME);
    }

    public void gotoManualEntry() {
        setProgramStatus(ProgramStatus.ManualEntry);
    }

    public void gotoRecogBill() {
        setProgramStatus(ProgramStatus.RecogBill);
    }

    /**
     * Updates the program status and calls SceneManager to switch scenes based on the new status.
     * @param status the new program status.
     */
    public void setProgramStatus(ProgramStatus status) {
        this.programStatus = status;
        LoggerHelper.debug("ProgramStatus changed to: " + status);
        switch (status) {
            case LOGIN:
                SceneManager.showLoginPage();
                break;
            case HOME:
                SceneManager.showHomePage();
                break;
            case SPENDING:
                SceneManager.showSpendingPage();
                break;
            case ManualEntry:
                SceneManager.showManualEntryPage();
                break;
            case RecogBill:
                SceneManager.showRecognizeBillPage();
                break;
            default:
                LoggerHelper.warn("Unhandled ProgramStatus: " + status);
                break;
        }
    }
}