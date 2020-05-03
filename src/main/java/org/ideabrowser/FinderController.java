package org.ideabrowser;

import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import org.apache.commons.lang.StringUtils;
import org.ideabrowser.finder.Highlighter;
import org.w3c.dom.Document;

/**
 * Brings the capabilities of Finder / Highlighter to the WebEngine/EmbeddedBrowser world.
 * It also handles creating a new highlighter upon first call to next, ...
 */
public class FinderController {
    private Highlighter highlighter;
    private WebEngine webEngine;
    private FinderControllerListener listener;
    private String text;

    public FinderController() {
    }

    public void setFinderListener(FinderControllerListener listener) {
        this.listener = listener;
    }

    /**
     * sets the text to be searched.
     */
    public void setText(String text) {
        if (StringUtils.isEmpty(text)) {
            listener.disableNextOccurrence();
            return;
        }

        String previousText = this.text;
        this.text = text;
        if (StringUtils.isEmpty(previousText) || !text.startsWith(previousText)) {
            // set to null. Will be created in findNext within the javafx thread.
            highlighter = null;
        }
        findNext();
    }

    public void findNext() {
        Platform.runLater( () -> {
            Document document = webEngine.getDocument();
            // https://bugs.openjdk.java.net/browse/JDK-8204856
            // document == null should not happen from jdk11 onwards
            if (document == null) {
                document = (Document) webEngine.executeScript("document");
            }
            if (highlighter == null) {
                highlighter = Highlighter.from(document.getElementsByTagName("body").item(0));
            } else {
                highlighter.clear(document);
            }

            if (highlighter.highlightNext(text)) {
                webEngine.executeScript("document.getElementsByClassName(\"idea_browser\")[0].scrollIntoView();");
                listener.enableNextOccurrence();
            } else {
                listener.disableNextOccurrence();
            }
        });
    }

    /**
     * Set after constructor and not in the constructor since the controller requires the WebEngine which
     * may not be ready when the controller is created.
     */
    public void setWebEngine(WebEngine webEngine) {
        this.webEngine = webEngine;
    }
}
