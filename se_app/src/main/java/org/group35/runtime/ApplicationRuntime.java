package org.group35.runtime;

import org.group35.controller.TransactionManager;
import org.group35.model.User;
import org.group35.controller.UserManager;
import org.group35.controller.TransactionManager;
import org.group35.util.CameraUtils;
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
        PLAN,
        MORE,
        MANUAL_ENTRY,
        RECOGNIZE_BILL
        // Add other statuses as needed.
    }

    // Singleton instance of ApplicationRuntime.
    private static ApplicationRuntime instance;

    // Model managers
    private final UserManager         userManager;
    private final TransactionManager  transactionManager;

    // Shared services
    private final CameraUtils         cameraService;

    // Runtime state
    private User           loggedInUser;
    private ProgramStatus  programStatus;

    /**
     * Private constructor to enforce singleton pattern.
     * Initializes all model managers.
     */
    private ApplicationRuntime() {
        userManager        = new UserManager();
        transactionManager = new TransactionManager();
        cameraService      = new CameraUtils();

        Runtime.getRuntime().addShutdownHook(new Thread(cameraService::shutdown));

        LoggerHelper.debug("ApplicationRuntime singleton initialized");
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
     * Returns the TransactionManager instance.
     * @return the TransactionManager.
     */
    public TransactionManager getTranscationManager() {
        return transactionManager;
    }

    /** Returns the CameraService instance.
     * @return the shared CameraUtils service */
    public CameraUtils getCameraService() {
        return cameraService;
    }

    /**
     * Returns the currently logged-in user.
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

//    // [Deprecated]
//
//    public void gotoSpending() {
//        setProgramStatus(ProgramStatus.SPENDING);
//    }
//
//    public void gotoHome() {
//        setProgramStatus(ProgramStatus.HOME);
//    }
//
//    public void gotoManualEntry() {
//        setProgramStatus(ProgramStatus.MANUAL_ENTRY);
//    }
//
//    public void gotoRecogBill() {
//        setProgramStatus(ProgramStatus.RECOG_BILL);
//    }

    /**
     * Switch to the given scene / program status unless already there.
     */
    public void navigateTo(ProgramStatus target) {
        if (this.programStatus == target) return;
        setProgramStatus(target);
    }

    /**
     * Updates the program status and calls SceneManager to switch scenes based on the new status.
     * @param status the new program status.
     */
    public void setProgramStatus(ProgramStatus status) {
        this.programStatus = status;
        LoggerHelper.debug("ProgramStatus changed to: " + status);
        switch (status) {
            case LOGIN:          SceneManager.showLoginPage(); break;
            case HOME:           SceneManager.showHomePage(); break;
            case SPENDING:       SceneManager.showSpendingPage(); break;
            case MANUAL_ENTRY:   SceneManager.showManualEntryPage(); break;
            case RECOGNIZE_BILL: SceneManager.showRecognizeBillPage(); break;
            case PLAN:           SceneManager.showPlanPage(); break;
            default:             LoggerHelper.warn("Unhandled ProgramStatus: " + status); break;
        }
    }

    /**
     * Call this when your application is closing (e.g. in JavaFX Application.stop())
     * to ensure all services are cleanly shut down.
     */
    public void shutdown() {
        LoggerHelper.debug("ApplicationRuntime shutdown. Cleaning up services");
        cameraService.shutdown();
    }
}