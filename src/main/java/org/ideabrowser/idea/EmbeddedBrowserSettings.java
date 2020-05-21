package org.ideabrowser.idea;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.ideabrowser.finder.LoggingFinder;
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
        name="org.ideabrowser.idea.EmbeddedBrowserSettings",
        storages = {
                @Storage("org.ideabrowser.idea.EmbeddedBrowserSettings.xml")}
)
public class EmbeddedBrowserSettings implements PersistentStateComponent<EmbeddedBrowserSettings> {

    public static EmbeddedBrowserSettings getInstance() {
        return ServiceManager.getService(EmbeddedBrowserSettings.class);
    }


    private int maxHistorySize = 5;
    private String searchEngineTemplate;
    private boolean logsEnabled = false;
    private String[] tagsToIgnore = new String[] {"script", "form", "style"};

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
     * Returns whether {@link LoggingFinder} is enabled or not.
     * We may want to enable it to see if a / which match is found e.g when the highlighter doesn't highlight anything.
     * If the logs do show a match, it could be that it is contained in a non visible text node. See also {@link #getTagsToIgnore()}
     */
    public boolean isLogsEnabled() {
        return logsEnabled;
    }

    /**
     * Returns a list of tags which are usually non visible and hence should be ignored by the finder as it would be a really poor user experience if upon clicking "next occurrence" nothing gets highlighted.
     */
    public String[] getTagsToIgnore() {
        return tagsToIgnore;
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

    public void setLogsEnabled(boolean logsEnabled) {
        this.logsEnabled = logsEnabled;
    }

    public void setTagsToIgnore(String[] tagsToIgnore) {
        this.tagsToIgnore = tagsToIgnore;
    }

    @Override
    public EmbeddedBrowserSettings getState() {
        return this;
    }

    public void loadState(@NotNull EmbeddedBrowserSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
