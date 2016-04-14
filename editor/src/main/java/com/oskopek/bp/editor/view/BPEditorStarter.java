package com.oskopek.bp.editor.view;

import com.oskopek.bp.editor.weld.StartupStage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;

/**
 * A CDI-enabled version of {@link BPEditorApplication} that initializes the root layout.
 */
public class BPEditorStarter {

    @Inject
    private FXMLLoader fxmlLoader;

    /**
     * Initializes the root layout.
     * @param primaryStage the primaryStage delegated from the {@link javafx.application.Application} that calls us
     */
    private void initRootLayout(@Observes @StartupStage Stage primaryStage) {
        VBox rootLayout;
        try (InputStream is = getClass().getResourceAsStream("RootLayout.fxml")) {
            rootLayout = fxmlLoader.load(is);
        } catch (IOException e) {
            throw new IllegalStateException("An error occurred while reading the root layout.", e);
        }
        Scene scene = new Scene(rootLayout);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
