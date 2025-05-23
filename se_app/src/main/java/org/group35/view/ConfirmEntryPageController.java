package org.group35.view;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.group35.controller.TransactionManager;
import org.group35.model.Transaction;
import org.group35.runtime.ApplicationRuntime;
import org.group35.runtime.ApplicationRuntime.ProgramStatus;
import org.group35.service.BillsRecognition;
import org.group35.util.ImageUtils;
import org.group35.util.LogUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfirmEntryPageController implements Initializable {

    @FXML private Label previousPageLabel;
    @FXML private Label currentPageLabel;
    @FXML private Label warningLabel;
    @FXML private VBox hintContainer;
    @FXML private VBox loadContainer;
    @FXML private VBox inputContainer;
    @FXML private VBox emptyInputContainer;
    @FXML private Arc spinnerArc;
    @FXML private TextField nameField;
    @FXML private TextField amountField;
    @FXML private TextField datetimeField;
    @FXML private TextField locationField;
    @FXML private ComboBox<String> categoryBox;
    @FXML private Text hintText;
    @FXML private SVGPath hintIcon;
    @FXML private Button deleteButton;
    @FXML private Button saveButton;

    private ProgramStatus fromStatus;
    private Transaction currentTx;

    private static final ApplicationRuntime rt = ApplicationRuntime.getInstance();
    private static final TransactionManager txm = rt.getTranscationManager();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Object fromPageObj = rt.getNavParam("fromPage");
        Object fromStatusObj = rt.getNavParam("fromStatus");
        Object needsProcessObj = rt.getNavParam("needsProcess");
        Object processTypeObj = rt.getNavParam("processType");
        String prevPage = (fromPageObj instanceof String)
                ? ((String) fromPageObj) : "UNKNOWN";
        fromStatus = (fromStatusObj instanceof ProgramStatus)
                ? ((ProgramStatus) fromStatusObj) : ProgramStatus.HOME;
        Boolean needsProcess = (needsProcessObj instanceof Boolean)
                ? ((Boolean) needsProcessObj) : Boolean.FALSE;
        String processType = (processTypeObj instanceof String)
                ? ((String) processTypeObj) : "none";

        warningLabel.managedProperty().bind(warningLabel.visibleProperty());
        hintContainer.managedProperty().bind(hintContainer.visibleProperty());
        loadContainer.managedProperty().bind(loadContainer.visibleProperty());
        inputContainer.managedProperty().bind(inputContainer.visibleProperty());
        emptyInputContainer.managedProperty().bind(emptyInputContainer.visibleProperty());
        deleteButton.managedProperty().bind(deleteButton.visibleProperty());
        warningLabel.setVisible(false);
        hintContainer.setVisible(false);
        loadContainer.setVisible(true);
        inputContainer.setVisible(false);
        emptyInputContainer.setVisible(true);
        deleteButton.setVisible(false);

        previousPageLabel.setText(prevPage);
        setCategoryBox();
        modifyingEntryCheck();
        toggleProcessing(needsProcess);
        doProcessCheck(processType);

    }

    private void doProcessCheck(String processType){
        switch (processType) {
            case "image": doImageProcess(); break;
            case "text":  doTextProcess(); break;
            default:      break;
        }
    }

    private void modifyingEntryCheck(){
        Object uuidObj = rt.getNavParam("uuid");
        String uuid = (uuidObj instanceof String)
                ? ((String) uuidObj) : "none";
        currentTx = txm.getById(uuid);
        if (currentTx!=null){
            previousPageLabel.setText("\""+currentTx.getName()+"\"");
            currentPageLabel.setText("Manage Entry");
            hintText.setText("Modify or\ndelete this entry");
            hintIcon.setContent("M14.06,9L15,9.94L5.92,19H5V18.08L14.06,9M17.66,3C17.41,3 17.15,3.1 16.96,3.29L15.13,5.12L18.88,8.87L20.71,7.04C21.1,6.65 21.1,6 20.71,5.63L18.37,3.29C18.17,3.09 17.92,3 17.66,3M14.06,6.19L3,17.25V21H6.75L17.81,9.94L14.06,6.19Z");
            deleteButton.setVisible(true);
            saveButton.setText("Modify");
            populateFields(currentTx);
        }
    }

    private void doImageProcess() {
        ApplicationRuntime rt = ApplicationRuntime.getInstance();
        Object snapshotObj = rt.getNavParam("snapshot");
        Image snapshot = (snapshotObj instanceof Image)
                ? ((Image) snapshotObj) : null;
        if (snapshot == null) {
            LogUtils.error("Image processing requested but got null for snapshot");
            return;
        }
        String base64;
        try {
            base64 = ImageUtils.toBase64(snapshot, "PNG");
        }
        catch (IOException e) {
            LogUtils.error("IOException occurred when converting snapshot to base64");
            return;
        }

        toggleProcessing(true);

        BillsRecognition.imageRecognitionAsync(base64)
                .whenComplete((tx, err) -> {
                    Platform.runLater(() -> {
                        toggleProcessing(false);
                        if (err != null) {
                            LogUtils.error("Recognition failed: " + err.getMessage());
                            showError("Recognition failed: " + err.getMessage());
                        } else {
                            populateFields(tx);
                        }
                    });
                });
    }

    private void doTextProcess() {
        ApplicationRuntime rt = ApplicationRuntime.getInstance();
        Object descriptionObj = rt.getNavParam("description");
        String rawText = (descriptionObj instanceof String) ? (String) descriptionObj : "";
        if (rawText.isEmpty()) {
            LogUtils.error("Text processing requested but no text provided");
            toggleProcessing(false);
            return;
        }

        toggleProcessing(true);

        BillsRecognition.textRecognitionAsync(rawText)
                .whenComplete((tx, err) -> Platform.runLater(() -> {
                    toggleProcessing(false);
                    if (err != null) {
                        LogUtils.error("Text recognition failed: " + err.getMessage());
                        showError("Text recognition failed: " + err.getMessage());
                    } else {
                        populateFields(tx);
                    }
                }));
    }

    private void populateFields(Transaction tx) {
        nameField.setText(tx.getName());
        amountField.setText(tx.getAmount().toPlainString());
        LocalDateTime ts = tx.getTimestamp();
        datetimeField.setText(ts != null ? ts.format(DATE_FMT) : "");
        String loc = tx.getLocation();
        locationField.setText(loc != null ? loc : "");
        String cat = tx.getCategory();
        if (cat != null && !cat.isEmpty()) {
            categoryBox.setValue(cat);
        }
    }

    private void setCategoryBox() {
        ApplicationRuntime rt = ApplicationRuntime.getInstance();
        List<String> categories = rt.getCurrentUser().getCategory();
        categoryBox.setItems(FXCollections.observableArrayList(categories));
    }

    private void toggleProcessing(boolean isProcessing) {
        if (isProcessing){
            hintContainer.setVisible(false);
            loadContainer.setVisible(true);
            inputContainer.setVisible(false);
            emptyInputContainer.setVisible(true);
            spinnerArc.setRadiusX(24);
            spinnerArc.setRadiusY(24);
            RotateTransition rotateTransition = new RotateTransition(Duration.seconds(2), spinnerArc);
            rotateTransition.setByAngle(360);
            rotateTransition.setCycleCount(RotateTransition.INDEFINITE);
            rotateTransition.setInterpolator(Interpolator.LINEAR);
            rotateTransition.play();
        }
        else {
            hintContainer.setVisible(true);
            loadContainer.setVisible(false);
            inputContainer.setVisible(true);
            emptyInputContainer.setVisible(false);
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (currentTx != null) {
            txm.delete(currentTx.getId());
            goBack(event);
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {

        String name = nameField.getText();
        if (name == null || name.trim().isEmpty()) {
            showError("Name cannot be empty.");
            return;
        }

        // 2) Validate amount
        BigDecimal amount;
        try {
            amount = new BigDecimal(amountField.getText().trim());
            if (amount.compareTo(BigDecimal.ZERO) == 0) {
                showError("Amount must be not be zero.");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Invalid amount format.");
            return;
        }

        // 3) Validate date/time
        String dtInput = datetimeField.getText() == null
                ? ""
                : datetimeField.getText().trim();
        LocalDateTime timestamp;

        if (dtInput.isEmpty()) {
            timestamp = LocalDateTime.now();
        } else {
            Pattern pattern = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2})(?: (\\d{2}:\\d{2}))?$");
            Matcher matcher = pattern.matcher(dtInput);
            if (!matcher.matches()) {
                showError("Invalid date/time. Use yyyy-MM-dd (HH:mm).");
                return;
            }
            LocalDate date;
            LocalTime time = LocalTime.MIDNIGHT;
            try {
                date = LocalDate.parse(matcher.group(1), DateTimeFormatter.ISO_LOCAL_DATE);
                String timePart = matcher.group(2);
                if (timePart != null) {
                    time = LocalTime.parse(timePart, DateTimeFormatter.ofPattern("HH:mm"));
                }
            } catch (DateTimeParseException e) {
                showError("Could not parse date or time.");
                return;
            }
            timestamp = LocalDateTime.of(date, time);
            if (timestamp.isAfter(LocalDateTime.now())) {
                showError("Date and time cannot be in the future.");
                return;
            }
        }

        // 4) Get location
        String location = locationField.getText().trim();

        // 5) Validate category
        String category = categoryBox.getValue();
        if (category == null || category.trim().isEmpty()) {
            showError("Please select a category.");
            return;
        }

        // 6) Construct transaction model
        Transaction tx = (currentTx==null)?(new Transaction()):(currentTx);

        String username = rt.getCurrentUser().getUsername();
        tx.setUsername(username);
        tx.setName(name);
        tx.setAmount(amount);
        tx.setTimestamp(timestamp);
        tx.setCategory(category);
        if (!location.isEmpty()) {
            tx.setLocation(location);
        }

        // 7) Saving and going back
        if (currentTx!=null) txm.update(currentTx);
        else txm.add(tx);
        goToSpending(event);
    }

    private void showError(String message) {
        warningLabel.setText(message);
        warningLabel.setVisible(true);
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        toggleProcessing(false);
    }

    @FXML
    public void goBack(Event e) {
        ApplicationRuntime.getInstance().navigateTo(fromStatus);
    }

    @FXML
    public void goToSpending(Event e) {
        ApplicationRuntime.getInstance().navigateTo(ProgramStatus.SPENDING);
    }
}