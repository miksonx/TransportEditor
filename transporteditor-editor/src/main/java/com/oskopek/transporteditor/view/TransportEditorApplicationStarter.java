package com.oskopek.transporteditor.view;

import com.google.common.eventbus.EventBus;
import com.oskopek.transporteditor.weld.DeadEventListener;
import com.oskopek.transporteditor.weld.StartupStage;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;

/**
 * A CDI-enabled version of {@link TransportEditorApplication} that initializes the root layout.
 */
class TransportEditorApplicationStarter {

    @Inject
    @Named("fxmlloader")
    private Instance<FXMLLoader> fxmlLoader;

    @Inject
    private TransportEditorApplication application;

    @Inject
    private EventBus eventBus;

    @Inject
    private DeadEventListener deadEventListener;

    /**
     * Initializes the root layout.
     *
     * @param primaryStage the primaryStage delegated from the {@link javafx.application.Application} that calls us
     */
    private void initRootLayout(@Observes @StartupStage Stage primaryStage) {
        FXMLLoader fxmlLoader = this.fxmlLoader.get();
        VBox rootLayout = null;
        fxmlLoader.setLocation(getClass().getResource("RootLayoutPane.fxml"));
        try (InputStream is = getClass().getResourceAsStream("RootLayoutPane.fxml")) {
            rootLayout = fxmlLoader.load(is);
        } catch (IOException e) {
            AlertCreator.handleLoadLayoutError(fxmlLoader.getResources(), e);
        }
        Scene scene = new Scene(rootLayout);
        Platform.runLater(() -> {
            primaryStage.setMinHeight(650d);
            primaryStage.setMinWidth(650d);
            primaryStage.setScene(scene);
            primaryStage.show();
        });
        eventBus.register(deadEventListener);
        application.setPrimaryStage(primaryStage);
    }
}
