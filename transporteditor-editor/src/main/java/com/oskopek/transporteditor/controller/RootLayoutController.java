package com.oskopek.transporteditor.controller;

import com.oskopek.transporteditor.domain.DefaultStudyPlan;
import com.oskopek.transporteditor.domain.StudyPlan;
import com.oskopek.transporteditor.persistence.DataWriter;
import com.oskopek.transporteditor.persistence.JsonDataReaderWriter;
import com.oskopek.transporteditor.persistence.MFFHtmlScraper;
import com.oskopek.transporteditor.view.AlertCreator;
import com.oskopek.transporteditor.view.EnterStringDialogPaneCreator;
import com.oskopek.transporteditor.view.ProgressCreator;
import com.oskopek.transporteditor.view.TransportEditorApplication;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Handles all action in the menu bar of the main app.
 */
@Singleton
public class RootLayoutController extends AbstractController {

    private final DataWriter writer = new JsonDataReaderWriter();

    private File openedFile;

    @Inject
    private EnterStringDialogPaneCreator enterStringDialogPaneCreator;

    @Inject
    private transient Logger logger;

    /**
     * Menu item: File->New.
     * Creates a new model in the main app.
     * Doesn't save the currently opened one!
     */
    @FXML
    private void handleNew() {
        openedFile = null;
        studyGuideApplication.setStudyPlan(new DefaultStudyPlan());
    }

    /**
     * Menu item: File->Open.
     * Opens an existing model into the main app.
     * Doesn't save the currently opened one!
     */
    @FXML
    private void handleOpen() {
        FileChooser chooser = new FileChooser();
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("JSON StudyPlan", ".json", ".JSON"));
        File chosen = chooser.showOpenDialog(studyGuideApplication.getPrimaryStage());
        if (chosen == null) {
            return;
        }
        openFromFile(chosen);
    }

    /**
     * Menu item: File->Open From....
     * Scrape a study plan off the network resource and load it into the main app.
     * Doesn't save the currently opened one!
     */
    @FXML
    private void handleOpenFrom() {
        EnterStringController enterStringController = enterStringDialogPaneCreator.create(
                messages.getString("root.enterUrl"));
        Optional<ButtonType> result = enterStringController.getDialog().showAndWait();
        if (result.isPresent() && result.get() == ButtonType.APPLY) {
            String submittedURL = enterStringController.getSubmittedString();
            MFFHtmlScraper scraper = new MFFHtmlScraper(studyGuideApplication.getSisUrl());
            Stage progressDialog = ProgressCreator.showProgress(scraper, messages.getString("progress.pleaseWait"));
            Task<StudyPlan> studyPlanTask = new Task<StudyPlan>() {
                @Override
                protected StudyPlan call() throws Exception {
                    return scraper.scrapeStudyPlan(submittedURL);
                }
            };
            studyPlanTask.setOnSucceeded(event -> {
                progressDialog.close();
                studyGuideApplication.setStudyPlan(studyPlanTask.getValue());
                openedFile = null;
            });
            studyPlanTask.setOnFailed(event -> {
                progressDialog.close();
                AlertCreator.showAlert(Alert.AlertType.ERROR,
                        messages.getString("root.openFromFailed") + ":\n\n" + event.getSource().getException());
            });

            new Thread(studyPlanTask).start();
        }
    }

    /**
     * Menu item: File->Save.
     * Save the opened model from the main app.
     * If there is no opened file and the model is not null, calls {@link #handleSaveAs()}.
     */
    @FXML
    private void handleSave() {
        if (openedFile == null && studyGuideApplication.getStudyPlan() != null) {
            handleSaveAs();
            return;
        }
        saveToFile(openedFile);
    }

    /**
     * Menu item: File->Save As.
     * Save the opened model from the main app into a new file.
     */
    @FXML
    private void handleSaveAs() {
        FileChooser chooser = new FileChooser();
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("JSON StudyPlan", ".json", ".JSON"));
        File chosen = chooser.showSaveDialog(studyGuideApplication.getPrimaryStage());
        if (chosen == null) {
            return;
        }
        saveToFile(chosen);
    }

    /**
     * Menu item: File->Quit.
     * Exit the main app.
     * Doesn't save the currently opened model!
     */
    @FXML
    private void handleQuit() {
        System.exit(0);
    }

    /**
     * Menu item: SIS->Set SIS URL
     * Sets the SIS's URL to be used for course scraping.
     */
    @FXML
    private void handleSetSisUrl() {
        EnterStringController enterStringController = enterStringDialogPaneCreator.create(
                messages.getString("root.enterUrl"));
        enterStringController.getTextField().setText(studyGuideApplication.getSisUrl());
        Optional<ButtonType> result = enterStringController.getDialog().showAndWait();
        if (result.isPresent() && result.get() == ButtonType.APPLY) {
            String newSisUrl = enterStringController.getSubmittedString().trim();
            while (newSisUrl.endsWith("/")) {
                newSisUrl = newSisUrl.substring(0, newSisUrl.length() - 1);
            }
            try {
                studyGuideApplication.setSisUrl(newSisUrl);
            } catch (IllegalArgumentException e) {
                AlertCreator.showAlert(Alert.AlertType.ERROR,
                        String.format(messages.getString("root.urlInvalid"), e.getLocalizedMessage()));
            }
        }
    }

    /**
     * Menu item: Help->Help.
     * Shows a how-to help dialog.
     */
    @FXML
    private void handleHelp() {
        WebView webView = new WebView();
        webView.setContextMenuEnabled(false);
        String manualHtml = readResourceToString(messages.getString("root.manualResource"));

        Pattern messagePattern = Pattern.compile("%([a-z.A-Z]+)");
        Matcher messageMatcher = messagePattern.matcher(manualHtml);
        StringBuffer replaceHtmlBuffer = new StringBuffer();
        while (messageMatcher.find()) {
            String found = messageMatcher.group(1);
            logger.trace("Replacing {} in manual", found);
            String replacement = null;
            try {
                replacement = messages.getString(found);
            } catch (MissingResourceException e) {
                logger.warn("Couldn't find resource \"{}\" in messages.", found);
            }
            messageMatcher.appendReplacement(replaceHtmlBuffer, replacement == null ? found : replacement);
        }
        messageMatcher.appendTail(replaceHtmlBuffer);
        manualHtml = replaceHtmlBuffer.toString();

        if (manualHtml == null) {
            AlertCreator.showAlert(Alert.AlertType.WARNING,
                    messages.getString("root.manualNotAvailableInYourLanguage"));
            return;
        }
        webView.getEngine().loadContent(manualHtml);

        Stage webViewDialogStage = new Stage(StageStyle.DECORATED);
        webViewDialogStage.initOwner(studyGuideApplication.getPrimaryStage());
        webViewDialogStage.setResizable(true);
        webViewDialogStage.setTitle("TransportEditor - " + messages.getString("root.help"));
        webViewDialogStage.initModality(Modality.NONE);
        webViewDialogStage.setScene(new Scene(webView));
        webViewDialogStage.toFront();
        webViewDialogStage.showAndWait();
    }

    /**
     * Reads a String resource into a stream and returns it as String.
     *
     * @param resource the resource to load from classpath
     * @return null iff the input stream was null (resource not found)
     */
    private String readResourceToString(String resource) {
        InputStream is = getClass().getResourceAsStream(resource);
        if (is == null) {
            logger.warn("Couldn't find resource \"{}\"", resource);
            return null;
        }
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, "utf-8"))) {
            return bufferedReader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't read resource to string: " + resource, e);
        }
    }

    /**
     * Menu item: Help->About.
     * Shows a short modal about dialog.
     */
    @FXML
    private void handleAbout() {
        Dialog<Label> dialog = new Dialog<>();
        dialog.setContentText("                               TransportEditor\n"
                + "    <https://github.com/oskopek/TransportEditor>\n" + messages.getString("menu.author")
                + ": Ondrej Skopek <oskopek@matfyz.cz>");
        dialog.setTitle(messages.getString("root.about"));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    /**
     * Saves the currently opened model to a file.
     * If an {@link IOException} occurs, opens a modal dialog window notifying the user and prints a stack trace.
     *
     * @param file if null, does nothing
     * @see TransportEditorApplication#getStudyPlan()
     */
    private void saveToFile(File file) {
        if (file == null) {
            logger.debug("Cannot save to null file.");
            return;
        }
        try {
            writer.writeTo(studyGuideApplication.getStudyPlan(), file.getAbsolutePath());
        } catch (IOException e) {
            AlertCreator.showAlert(Alert.AlertType.ERROR, messages.getString("root.openFailed") + ": " + e);
            e.printStackTrace();
            return;
        }
        openedFile = file;
    }

    /**
     * Loads a model from a file into the main app.
     * If an {@link IOException} occurs, opens a modal dialog window notifying the user and prints a stack trace.
     *
     * @param file if null, does nothing
     * @see TransportEditorApplication#setStudyPlan(com.oskopek.transporteditor.domain.StudyPlan)
     */
    private void openFromFile(File file) {
        if (file == null) {
            logger.error("Cannot open from null file.");
            return;
        }
        StudyPlan oldPlan = studyGuideApplication.getStudyPlan();
        // notify the user something will happen (erase semester boxes)
        studyGuideApplication.setStudyPlan(new DefaultStudyPlan());
        Task<StudyPlan> loadFromFileTask = new Task<StudyPlan>() {
            @Override
            protected StudyPlan call() throws Exception {
                JsonDataReaderWriter reader = new JsonDataReaderWriter(messages, eventBus);
                StudyPlan studyPlan = reader.readFrom(file.getAbsolutePath());
                return studyPlan;
            }
        };
        loadFromFileTask.setOnFailed(event -> {
            Throwable e = event.getSource().getException();
            Platform.runLater(() -> studyGuideApplication.setStudyPlan(oldPlan));
            AlertCreator.showAlert(Alert.AlertType.ERROR, messages.getString("root.openFailed") + ":\n\n" + e);
            e.printStackTrace();
        });
        loadFromFileTask.setOnSucceeded(event -> {
            StudyPlan newPlan = loadFromFileTask.getValue();
            Platform.runLater(() -> {
                studyGuideApplication.setStudyPlan(newPlan);
                newPlan.getConstraints().recheckAll();
            });
            openedFile = file;
        });
        new Thread(loadFromFileTask).start();
    }
}
