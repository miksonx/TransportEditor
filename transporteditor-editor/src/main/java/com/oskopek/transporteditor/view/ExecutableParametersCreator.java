package com.oskopek.transporteditor.view;

import com.oskopek.transporteditor.controller.ExecutableParametersController;
import com.oskopek.transporteditor.view.executables.DefaultExecutableWithParameters;
import com.oskopek.transporteditor.view.executables.ExecutableWithParameters;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javaslang.collection.List;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;

/**
 * Dialog for entering a two strings - an executable and it's parameters.
 */
@Singleton
public class ExecutableParametersCreator {

    @Inject
    @Named("fxmlloader")
    private Instance<FXMLLoader> fxmlLoader;

    @Inject
    private ResourceBundle messages;

    @Inject
    @Named("mainApp")
    private TransportEditorApplication application;

    /**
     * Create the dialog for creating a executable with parameters.
     *
     * @param parameterCount the number of parameters
     * @return the executable with parameters
     */
    public ExecutableWithParameters createExecutableWithParameters(int parameterCount, String executableInstructions,
            String parameterInstructions, String noteText, ExecutableWithParameters existing) {
        FXMLLoader fxmlLoader = this.fxmlLoader.get();
        BorderPane dialogPane = null;
        try (InputStream is = getClass().getResourceAsStream("ExecutableParametersCreatorPane.fxml")) {
            dialogPane = fxmlLoader.load(is);
        } catch (IOException e) {
            AlertCreator.handleLoadLayoutError(fxmlLoader.getResources(),
                    a -> application.centerInPrimaryStage(a, -200, -50), e);
        }
        dialogPane.getStylesheets().add(getClass().getResource("validation.css").toExternalForm());
        ExecutableParametersController executableParametersController = fxmlLoader.getController();

        Stage stage = new Stage();
        executableParametersController.setHeaderText(messages.getString("excreator.title"));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(dialogPane));
        stage.setOnShown(e -> executableParametersController.getExecutableArea().requestFocus());
        executableParametersController.setDialog(stage);
        executableParametersController.setExecutableSubLabelText(executableInstructions);
        executableParametersController.setParametersSubLabelText(parameterInstructions);
        executableParametersController.setNoteText(noteText);
        executableParametersController.enableValidation(
                executableString -> DefaultExecutableWithParameters.findExecutablePath(executableString.trim())
                        .isPresent(),
                messages.getString("excreator.valid.executable"), parameterString -> List.range(0, parameterCount)
                        .map(i -> "{" + i + "}").forAll(parameterString::contains),
                messages.getString("excreator.valid.paramcount") + ": " + parameterCount);

        if (existing != null) {
            executableParametersController.getExecutableArea().setText(existing.getExecutable());
            executableParametersController.getParametersArea().setText(existing.getParameters());
        } else {
            // default value in params
            StringBuilder builder = new StringBuilder();
            List.range(0, parameterCount).map(i -> "{" + i + "} ").forEach(builder::append);
            executableParametersController.getParametersArea().setText(builder.toString().trim());
        }

        stage.setTitle("TransportEditor");
        application.centerInPrimaryStage(stage, -200, -250);
        stage.showAndWait();
        return executableParametersController.getExecutable();
    }
}
