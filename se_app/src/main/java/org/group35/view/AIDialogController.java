package org.group35.view;

import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.group35.view.EditBudgetDialogController;

public class AIDialogController {

    private Stage dialogStage;
    private EditBudgetDialogController editBudgetDialogController;

    @FXML
    private Text suggestionText;

    private String suggestionContent;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setEditBudgetDialogController(EditBudgetDialogController controller) {
        this.editBudgetDialogController = controller;
    }

    // API here
    public void setSuggestionContent(String content) {
        this.suggestionContent = content;
        if (suggestionText != null) {
            suggestionText.setText(content);
        }
    }

    @FXML
    private void handleApply() {
        if (editBudgetDialogController != null && suggestionContent != null) {
            editBudgetDialogController.setNewBudget(suggestionContent);
        }
        dialogStage.close();
    }
}
