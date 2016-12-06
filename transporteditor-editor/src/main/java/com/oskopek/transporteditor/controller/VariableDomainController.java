package com.oskopek.transporteditor.controller;

import com.oskopek.transporteditor.model.domain.PddlLabel;
import com.oskopek.transporteditor.model.domain.VariableDomain;
import com.oskopek.transporteditor.persistence.VariableDomainBuilder;
import com.oskopek.transporteditor.validation.TextAreaValidator;
import com.oskopek.transporteditor.view.ValidationProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * Controller for choosing a course out of several choices.
 */
public class VariableDomainController extends AbstractController {

    private final VariableDomainBuilder domainBuilder = new VariableDomainBuilder();
    private final ToggleGroup group = new ToggleGroup();
    private final BooleanProperty radioButtonsValid = new SimpleBooleanProperty();
    private Stage dialog;
    @FXML
    private Label headerText;
    @FXML
    private TextField nameField;
    @FXML
    private RadioButton sequentialRadio;
    private final BooleanProperty sequentialRadioValid = new ValidationProperty(messages.getString("vdcreator.valid.domainType"), sequentialRadio);
    @FXML
    private RadioButton temporalRadio;
    private final BooleanProperty temporalRadioValid = new ValidationProperty(messages.getString("vdcreator.valid.domainType"), temporalRadio);
    @FXML
    private CheckBox fuelCheck;
    private final BooleanProperty fuelCheckValid = new ValidationProperty(messages.getString("vdcreator.valid.fuelCheck"), fuelCheck);
    @FXML
    private CheckBox numericCheck;
    private final BooleanProperty numericCheckValid = new ValidationProperty(messages.getString("vdcreator.valid.numericCheck"), numericCheck);
    @FXML
    private TextArea goalArea;
    private final BooleanProperty goalAreaValid = new ValidationProperty(messages.getString("vdcreator.valid.goalArea"), goalArea);
    @FXML
    private TextArea metricArea;
    private final BooleanProperty metricAreaValid = new ValidationProperty(messages.getString("vdcreator.valid.metricArea"), metricArea);
    @FXML
    private CheckBox capacityCheck;
    private final BooleanProperty capacityCheckValid = new ValidationProperty(messages.getString("vdcreator.valid.capacity"), capacityCheck);
    @FXML
    private Label goalLabel;
    @FXML
    private Label metricLabel;
    @FXML
    private Label fuelLabel;
    @FXML
    private Label capacityLabel;
    @FXML
    private Label numericLabel;
    @FXML
    private Button applyButton;
    @FXML
    private Button cancelButton;
    private ButtonBar.ButtonData result;

    @FXML
    private void initialize() {
        sequentialRadioValid.bind(radioButtonsValid);
        temporalRadioValid.bind(radioButtonsValid);
        temporalRadioValid.bind(group.selectedToggleProperty().isNotNull());

        fuelCheckValid.bind(sequentialRadioValid.not());
        capacityCheckValid.set(true);
        numericCheckValid.bind(sequentialRadioValid.not());

        goalAreaValid.bind(new TextAreaValidator(goalArea.textProperty(), s -> !s.isEmpty()).isValidProperty()); // TODO goal area validation
        metricAreaValid.bind(new TextAreaValidator(metricArea.textProperty(), s -> !s.isEmpty()).isValidProperty()); // TODO metric area validation

        ButtonBar.setButtonData(applyButton, ButtonBar.ButtonData.APPLY);
        ButtonBar.setButtonData(cancelButton, ButtonBar.ButtonData.CANCEL_CLOSE);

        domainBuilder.nameProperty().bind(nameField.textProperty());

        sequentialRadio.setToggleGroup(group);
        sequentialRadio.setUserData(PddlLabel.ActionCost);
        temporalRadio.setToggleGroup(group);
        temporalRadio.setUserData(PddlLabel.Temporal);
        group.selectedToggleProperty().addListener(e -> domainBuilder.setDomainType((PddlLabel) group.getSelectedToggle().getUserData()));

        capacityCheck.selectedProperty().addListener(e -> {
            if (capacityCheck.isSelected()) {
                domainBuilder.getPddlLabelSet().add(PddlLabel.Capacity);
                domainBuilder.getPddlLabelSet().add(PddlLabel.MaxCapacity);
            } else {
                domainBuilder.getPddlLabelSet().remove(PddlLabel.Capacity);
                domainBuilder.getPddlLabelSet().remove(PddlLabel.MaxCapacity);
            }
        });
        fuelCheck.selectedProperty().addListener(e -> {
            if (fuelCheck.isSelected()) {
                domainBuilder.getPddlLabelSet().add(PddlLabel.Fuel);
            } else {
                domainBuilder.getPddlLabelSet().remove(PddlLabel.Fuel);
            }
        });
        numericCheck.selectedProperty().addListener(e -> {
            if (numericCheck.isSelected()) {
                domainBuilder.getPddlLabelSet().add(PddlLabel.Numeric);
            } else {
                domainBuilder.getPddlLabelSet().remove(PddlLabel.Numeric);
            }
        });

        // TODO: Metric and goal parsing

        numericLabel.disableProperty().bind(group.selectedToggleProperty().isNull());
        numericCheck.disableProperty().bind(group.selectedToggleProperty().isNull());
        capacityLabel.disableProperty().bind(group.selectedToggleProperty().isNull());
        capacityCheck.disableProperty().bind(group.selectedToggleProperty().isNull());
        fuelLabel.disableProperty().bind(group.selectedToggleProperty().isNull());
        fuelCheck.disableProperty().bind(group.selectedToggleProperty().isNull());

        metricArea.disableProperty().bind(numericCheck.selectedProperty().not());
        metricLabel.disableProperty().bind(numericCheck.selectedProperty().not());
        goalArea.disableProperty().bind(numericCheck.selectedProperty().not());
        goalLabel.disableProperty().bind(numericCheck.selectedProperty().not());
    }

    public void setHeaderText(String headerText) {
        this.headerText.setText(headerText);
    }

    public TextField getNameField() {
        return nameField;
    }

    @FXML
    private void handleApplyButton() {
        if (validate()) {
            result = ButtonBar.ButtonData.APPLY;
            dialog.close();
        }
    }

    @FXML
    private void handleCancelButton() {
        result = ButtonBar.ButtonData.CANCEL_CLOSE;
        dialog.close();
    }

    private boolean validate() {
        return false; // TODO: implement validation
    }

    /**
     * Set the dialog (used for reporting double clicks in the table).
     *
     * @param dialog the dialog wrapper for {@link com.oskopek.transporteditor.view.VariableDomainCreator}
     */
    public void setDialog(Stage dialog) {
        this.dialog = dialog;
    }

    /**
     * Get the domain the user submitted.
     *
     * @return the submitted domain, null if cancelled
     */
    public VariableDomain getChosenDomain() {
        return result != null && ButtonBar.ButtonData.APPLY.equals(result) ? domainBuilder.toDomain() : null;
    }
}
