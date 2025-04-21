package org.group35.view.components;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import org.group35.runtime.ApplicationRuntime;
import org.group35.runtime.ApplicationRuntime.ProgramStatus;

import java.io.IOException;

public class NavBarController extends HBox {
    @FXML private Button dashboardBtn;
    @FXML private Button spendingBtn;
    @FXML private Button planBtn;
    @FXML private Button moreBtn;

    // the Java‑bean property
    private final ObjectProperty<ProgramStatus> activePage =
            new SimpleObjectProperty<>(this, "activePage", null);

    public NavBarController() {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("NavBar.fxml")
        );
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load NavBar.fxml", e);
        }

        // whenever somebody does setActivePage(...), update the highlight:
        activePage.addListener((obs, oldP, newP) -> highlight(newP));
    }

    @FXML
    private void initialize() {
        dashboardBtn.setOnAction(this::gotoHome);
        spendingBtn.setOnAction(this::gotoSpending);
        planBtn.setOnAction(this::gotoPlan);
        moreBtn.setOnAction(this::gotoMore);
    }

    private void gotoHome(ActionEvent e) {
        ApplicationRuntime.getInstance().gotoHome();
    }
    private void gotoSpending(ActionEvent e) {
        ApplicationRuntime.getInstance().gotoSpending();
    }
    private void gotoPlan(ActionEvent e) {
        // …
    }
    private void gotoMore(ActionEvent e) {
        // …
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
