package org.group35.view.components;

import java.io.IOException;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import org.group35.model.User;
import org.group35.runtime.ApplicationRuntime;
import org.group35.runtime.ApplicationRuntime.ProgramStatus;
import org.group35.util.ImageUtils;

/**
 * Navigation bar component including nav buttons, username, and avatar.
 */
public class NavBarController extends HBox {
    @FXML private Button dashboardBtn, spendingBtn, planBtn, moreBtn, logoutBtn;
    @FXML private Label usernameLabel;
    @FXML private ImageView avatarImage;

    // Property to track which page is active
    private final ObjectProperty<ProgramStatus> activePage =
            new SimpleObjectProperty<>(this, "activePage", null);

    public NavBarController() {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/group35/view/components/NavBar.fxml")
        );
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load NavBar.fxml", e);
        }
        activePage.addListener((obs, oldStatus, newStatus) -> highlight(newStatus));
    }

    @FXML
    private void initialize() {
        // Navigation button handlers
        dashboardBtn.setOnAction(e -> ApplicationRuntime.getInstance().navigateTo(ProgramStatus.HOME));
        spendingBtn.setOnAction(e -> ApplicationRuntime.getInstance().navigateTo(ProgramStatus.SPENDING));
        planBtn.setOnAction(e -> ApplicationRuntime.getInstance().navigateTo(ProgramStatus.PLAN));
        moreBtn.setOnAction(e -> ApplicationRuntime.getInstance().navigateTo(ProgramStatus.MORE));

        // Display current user's name and avatar
        User current = ApplicationRuntime.getInstance().getCurrentUser();
        if (current != null) {
            usernameLabel.setText(current.getUsername());
            Image avatar = ImageUtils.fromBase64(current.getAvatar());
            if (avatar != null) {
                avatarImage.setImage(avatar);
                double radius = avatarImage.getFitWidth() / 2.0;
                Circle clip = new Circle(radius, radius, radius);
                avatarImage.setClip(clip);
            }
        } else {
            usernameLabel.setText("Guest");
        }

        // Logout action
        logoutBtn.setOnAction(this::handleLogout);
    }

    private void handleLogout(ActionEvent event) {
        ApplicationRuntime.getInstance().logoutUser();
    }

    private void highlight(ProgramStatus status) {
        dashboardBtn.getStyleClass().remove("active");
        spendingBtn.getStyleClass().remove("active");
        planBtn.getStyleClass().remove("active");
        moreBtn.getStyleClass().remove("active");

        if (status == null) return;
        switch (status) {
            case HOME:     dashboardBtn.getStyleClass().add("active"); break;
            case SPENDING: spendingBtn.getStyleClass().add("active"); break;
            default:       break;
        }
    }

    // Java‑bean property
    public void setActivePage(ProgramStatus page) {
        activePage.set(page);
    }
    public ProgramStatus getActivePage() {
        return activePage.get();
    }
    public ObjectProperty<ProgramStatus> activePageProperty() {
        return activePage;
    }
}