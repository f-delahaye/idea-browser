package org.ideabrowser;

import com.intellij.util.ui.JBUI;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;

/**
 * JavaFX based implementation of a web viewer.
 *
 * It implements WebViewerListener so that all the logic is delegated to the controller and this class merely reacts to actions from listeners
 * (refresh UI upon controller events; update controller upon UI events)
 *
 */
// WebEngine is created and used within this class but could have lived in the controller instead.
// WebView however MUST be in here, so it seems like a good approach to have both javafx classes in the same place. It also makes handling the javafx / swing interoperability easier
//  see https://docs.oracle.com/javafx/2/swing/swing-fx-interoperability.htm

public class EmbeddedBrowser extends JPanel implements EmbeddedBrowserListener {

    private final EmbeddedBrowserController controller;
    private SearchWithHistoryTextField queryBar;

    private WebEngine engine;

    EmbeddedBrowser() {
        super(new BorderLayout());
        // Required otherwise the javafx platform is stopped when the panel is minimized and can never be restarted again
        // even if the panel is open again (unless there's a callback when a plugin is reactivated? I could not find any)
        Platform.setImplicitExit(false);
        this.controller = new EmbeddedBrowserController();
        controller.setViewListener(this);
        createComponents();
    }

    // creates and configures all the swing components.
    private void createComponents() {
        JFXPanel fxPanel = createJavaFXScene();

        queryBar = new SearchWithHistoryTextField();
        controller.setSearchHistoryListener(queryBar);
        queryBar.setEditable(true);

        queryBar.addActionListener(e-> controller.request(queryBar.getText()));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(JBUI.Borders.empty(3));
        topPanel.add(queryBar, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);
        add(fxPanel, BorderLayout.CENTER);

    }

    // creates the javafx scene
    private JFXPanel createJavaFXScene() {
        JFXPanel fxPanel = new JFXPanel();
        Platform.runLater(() -> {
            WebView view = new WebView();
            engine = view.getEngine();
            engine.getLoadWorker().stateProperty().addListener(this::onStateChanged);

            BorderPane borderPane = new BorderPane();
            borderPane.setCenter(view);
            Scene scene = new Scene(borderPane);
            fxPanel.setScene(scene);
        });
        return fxPanel;
    }

    private void onStateChanged(ObservableValue<? extends Worker.State> property, Worker.State oldState, Worker.State newState) {
        runInEDT(() -> updateStatusLabel(newState));
        if (newState == Worker.State.SUCCEEDED) {
            controller.onLoaded(engine.getTitle(), engine.getLocation());
        }
    }

    private void updateStatusLabel(Worker.State newState) {
        if (newState == Worker.State.RUNNING) {
            queryBar.setSearchingStatus(SearchWithHistoryTextField.SearchingStatus.RUNNING);
        } else if (newState == Worker.State.FAILED){
            queryBar.setSearchingStatus(SearchWithHistoryTextField.SearchingStatus.FAILED);
        } else if (newState == Worker.State.SUCCEEDED){
            queryBar.setSearchingStatus(SearchWithHistoryTextField.SearchingStatus.SUCCEEDED);
        }
    }

    @Override
    public void onRequestedURLChanged(String url) {
        if (Platform.isFxApplicationThread()) {
            engine.load(url);
        } else {
            Platform.runLater(() -> engine.load(url));
        }
    }

    @Override
    public void onURLChanged(String url) {
        runInEDT(() -> queryBar.setText(url));
    }

    private void runInEDT(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }

    }
}
