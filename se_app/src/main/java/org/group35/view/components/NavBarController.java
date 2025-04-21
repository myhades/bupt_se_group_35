package org.group35.view.components;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.group35.model.User;
import org.group35.runtime.ApplicationRuntime;
import org.group35.runtime.ApplicationRuntime.ProgramStatus;

import java.io.IOException;

public class NavBarController extends HBox {
    @FXML private Button dashboardBtn, spendingBtn, planBtn, moreBtn, logoutBtn;
    @FXML private Label usernameLabel;
    @FXML private ImageView avatarImage;

    // the Java‑bean property
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
        activePage.addListener((obs, oldP, newP) -> highlight(newP));
    }

    @FXML
    private void initialize() {
        // Navigation button handlers
        dashboardBtn.setOnAction(e -> ApplicationRuntime.getInstance().gotoHome());
        spendingBtn.setOnAction(e -> ApplicationRuntime.getInstance().gotoSpending());
        planBtn.setOnAction(e -> ApplicationRuntime.getInstance().gotoManualEntry());
        moreBtn.setOnAction(e -> ApplicationRuntime.getInstance().gotoRecogBill());

        // Populate username from the logged‑in user
        User current = ApplicationRuntime.getInstance().getCurrentUser();
        if (current != null) {
            usernameLabel.setText(current.getUsername());
            // optionally: avatarImage.setImage(current.getAvatarImage());
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

    /** Java‑bean setter */
    public void setActivePage(ProgramStatus page) {
        activePage.set(page);
    }
    /** Java‑bean getter (optional)  */
    public ProgramStatus getActivePage() {
        return activePage.get();
    }
    /** Java‑bean property accessor (necessary for FXML nested & binding)  */
    public ObjectProperty<ProgramStatus> activePageProperty() {
        return activePage;
    }
}
