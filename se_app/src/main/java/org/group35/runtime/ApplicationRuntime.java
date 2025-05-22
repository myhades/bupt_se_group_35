package org.group35.runtime;

import org.group35.controller.TransactionManager;
import org.group35.controller.UserManager;
import org.group35.model.User;
import org.group35.util.AudioUtils;
import org.group35.util.CameraUtils;
import org.group35.util.LogUtils;
import org.group35.util.SceneManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
        DESCRIBE("/org/group35/view/DescribePage.fxml"),
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
    private final UserManager        userManager;
    private final TransactionManager transactionManager;

    // Shared services
    private final CameraUtils cameraService;
    private final AudioUtils  audioService;

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
        audioService       = new AudioUtils();

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

    /** Returns the AudioService instance.
     * @return the shared AudioUtils service */
    public AudioUtils getAudioService() {
        return audioService;
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
     * Basic navigation: always populate fromPage/fromStatus.
     */
    public void navigateTo(ProgramStatus target) {
        navigateTo(target, null, true);
    }

    /**
     * Navigation with parameter transfer (clears existing params by default).
     */
    public void navigateTo(ProgramStatus target, Map<String,Object> params) {
        navigateTo(target, params, true);
    }

    /**
     * Navigation with parameter transfer and control over clearing old params.
     * @param target      page to navigate to
     * @param params      extra key/value pairs to merge
     * @param clearParams if true, clears existing navParams first; otherwise merges
     */
    public void navigateTo(ProgramStatus target, Map<String,Object> params, boolean clearParams) {
        if (clearParams) {
            navParams.clear();
        }
        navParams.put("fromStatus", this.programStatus);
        navParams.put("fromPage",   formatPageName(this.programStatus));
        if (params != null) {
            navParams.putAll(params);
        }
        setProgramStatus(target);
    }

    /**
     * Formats an enum name into Title Case sentence: ENUM_NAME -> "Enum Name"
     */
    private String formatPageName(ProgramStatus status) {
        if (status == null) return "";
        return Arrays.stream(status.name().split("_"))
                .map(s -> {
                    String lower = s.toLowerCase();
                    return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
                })
                .collect(Collectors.joining(" "));
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
        if (this.programStatus == ProgramStatus.DESCRIBE) {
            LogUtils.debug("Leaving describe page, shutting down audio...");
            audioService.shutdown();
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
        audioService.shutdown();
    }
}