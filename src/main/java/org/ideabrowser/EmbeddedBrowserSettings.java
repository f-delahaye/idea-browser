package org.ideabrowser;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *  Persistent settings used by EmbeddedBrowser.
 *  see https://www.jetbrains.org/intellij/sdk/docs/basics/persisting_state_of_components.html
 *
 */
// Implementation note: initial version of this class was designed to extend BrowserSpecificSettings and implement createConfigurable to make it look like any of the predefined web browsers' configuration screen
// It turned out that was not possible  see javadoc in {@link EmbeddedBrowserConfigurable}
@State(
        name="EmbeddedBrowserSettings",
        storages = {
                @Storage("EmbeddedBrowserSettings.xml")}
)
public class EmbeddedBrowserSettings implements PersistentStateComponent<EmbeddedBrowserSettings> {

    public static EmbeddedBrowserSettings getInstance() {
        return ServiceManager.getService(EmbeddedBrowserSettings.class);
    }

    private int maxHistorySize = 5;
    private String searchEngineTemplate;

    /**
     * Returns the maximum number of items to keep in the history.
     * 0 means history is disabled
     * < 0 means history is unbounded
     *
     * @return maxHistorySize
     */
    public int getMaxHistorySize() {
        return maxHistorySize;
    }

    /**
     * Returns the template to be used to generate the query sent to a search engine.
     * The template MUST contain the word TOKEN which will be replaced by the user entered query.
     *
     * @return a template containing TOKEN e.g. www.google.com?q=TOKEN
     */
    @Nullable
    public String getSearchEngineTemplate() {
        return searchEngineTemplate;
    }

    public void setMaxHistorySize(int maxHistorySize) {
        this.maxHistorySize = maxHistorySize;
    }

    public void setSearchEngineTemplate(String searchEngineTemplate) {
        this.searchEngineTemplate = searchEngineTemplate;
    }

    @Override
    public EmbeddedBrowserSettings getState() {
        return this;
    }

    public void loadState(@NotNull EmbeddedBrowserSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
