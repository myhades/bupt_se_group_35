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
     * The possible statuses of the application represented as enum.
     */
    public enum ProgramStatus {
        LOGIN("/org/group35/view/LoginPage.fxml"),
        HOME("/org/group35/view/HomePage.fxml"),
        SPENDING("/org/group35/view/SpendingPage.fxml"),
        PLAN("/org/group35/view/PlanPage.fxml"),
        MORE("/org/group35/view/MorePage.fxml"),
        MANUAL_ENTRY("/org/group35/view/ManualEntryPage.fxml"),
        RECOGNIZE_BILL("/org/group35/view/RecognizeBillPage.fxml"),
        IMPORT_CSV("/org/group35/view/ImportCSVPage.fxml"),
        CONFIRM_ENTRY("/org/group35/view/ConfirmEntryPage.fxml"),
        EDIT_BUDGET("/org/group35/view/EditBudgetPage.fxml"),
        AI_SUGGESTION("/org/group35/view/AISuggestionPage.fxml"),
        RECOMMENDATION("/org/group35/view/RecommendationPage.fxml"),
        ABOUT("/org/group35/view/AboutPage.fxml");

        private final String fxmlPath;

        ProgramStatus(String fxmlPath) {
            this.fxmlPath = fxmlPath;
        }

        public String getFxmlPath() {
            return fxmlPath;
        }
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
        SceneManager.showPage(status);
    }

    /**
     * Clean up method.
     */
    public void shutdown() {
        LogUtils.debug("ApplicationRuntime is being shut down, doing some clean up...");
        cameraService.shutdown();
    }
}