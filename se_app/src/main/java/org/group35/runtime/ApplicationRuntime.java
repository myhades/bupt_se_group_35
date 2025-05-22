package org.group35.runtime;

import org.group35.controller.TransactionManager;
import org.group35.controller.UserManager;
import org.group35.model.User;
import org.group35.util.CameraUtils;
import org.group35.util.LogUtils;
import org.group35.util.SceneManager;

import java.util.HashMap;
import java.util.Map;

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
        RECOGNIZE_BILL,
        CONFIRM_ENTRY,
        EDIT_BUDGET,
        AI_SUGGESTION,
        RECOMMENDATION
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
    private final Map<String,Object> navParams = new HashMap<>();

    /**
     * Private constructor to enforce singleton pattern.
     * Initializes all model managers.
     */
    private ApplicationRuntime() {
        userManager        = new UserManager();
        transactionManager = new TransactionManager();
        cameraService      = new CameraUtils();

        Runtime.getRuntime().addShutdownHook(new Thread(cameraService::shutdown));

        LogUtils.debug("ApplicationRuntime singleton initialized");
    }

    /**
     * Returns the singleton instance of ApplicationRuntime.
     * @return the ApplicationRuntime instance.
     */
    public static synchronized ApplicationRuntime getInstance() {
        if (instance == null) {
            instance = new ApplicationRuntime();
            LogUtils.trace("ApplicationRuntime instance created");
        }
        return instance;
    }

    /**
     * Getter for the current ProgramStatus enum.
     * @return the current ProgramStatus
     */
    public ProgramStatus getProgramStatus() {
        return this.programStatus;
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
        if (loggedInUser == null) {
            LogUtils.debug("Current user is null");
        } else {
            LogUtils.debug("Current user set to: " + loggedInUser.getUsername());
        }
        return loggedInUser;
    }

    /**
     * Sets the current user.
     * @param user the User to set as current.
     */
    public void setCurrentUser(User user) {
        this.loggedInUser = user;
        if (user == null) {
            LogUtils.info("Current user set to null");
        } else {
            LogUtils.info("Current user set to: " + user.getUsername());
        }
    }

    public void loginUser(User user) {
        this.loggedInUser = user;
        LogUtils.info("User logged in: " + user.getUsername());
        setProgramStatus(ProgramStatus.HOME);
    }

    /**
     * Logs out the current user and updates the program status to LOGIN.
     */
    public void logoutUser() {
        if (loggedInUser != null) {
            LogUtils.info("User logged out: " + loggedInUser.getUsername());
        }
        this.loggedInUser = null;
        setProgramStatus(ProgramStatus.LOGIN);
    }

    /**
     * Scene navigation method
     */
    public void navigateTo(ProgramStatus target) {
        setProgramStatus(target);
    }

    /**
     * Overloaded scene navigation method with parameter transfer
     */
    public void navigateTo(ProgramStatus target, Map<String,Object> params) {
        this.navParams.clear();
        if (params != null) navParams.putAll(params);
        setProgramStatus(target);
    }

    /**
     * Retrieve parameters passed from last call to navigate
     */
    @SuppressWarnings("unchecked")
    public <T> T getNavParam(String key) {
        return (T) navParams.get(key);
    }

    /**
     * Updates the program status and calls SceneManager to switch scenes based on the new status.
     * @param status the new program status.
     */
    public void setProgramStatus(ProgramStatus status) {
        if (this.programStatus == status)
            return;
        if (this.programStatus == ProgramStatus.RECOGNIZE_BILL) {
            LogUtils.debug("Leaving bill recognition page, shutting down camera...");
            cameraService.shutdown();
        }

        this.programStatus = status;
        LogUtils.debug("ProgramStatus changed to: " + status);
        switch (status) {
            case LOGIN:          SceneManager.showLoginPage(); break;
            case HOME:           SceneManager.showHomePage(); break;
            case SPENDING:       SceneManager.showSpendingPage(); break;
            case PLAN:           SceneManager.showPlanPage(); break;
            case MORE:           SceneManager.showMorePage(); break;
            case MANUAL_ENTRY:   SceneManager.showManualEntryPage(); break;
            case RECOGNIZE_BILL: SceneManager.showRecognizeBillPage(); break;
            case CONFIRM_ENTRY:  SceneManager.showConfirmEntryPage(); break;
            case EDIT_BUDGET:    SceneManager.showEditBudgetPage(); break;
            case AI_SUGGESTION:  SceneManager.showAISuggestionPage(); break;
            case RECOMMENDATION: SceneManager.showRecommendationPage(); break;
            default:             LogUtils.warn("Unhandled ProgramStatus: " + status); break;
        }
    }

    /**
     * Clean up method.
     */
    public void shutdown() {
        LogUtils.debug("ApplicationRuntime is being shut down, doing some clean up...");
        cameraService.shutdown();
    }
}