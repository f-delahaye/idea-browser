package org.ideabrowser;

import org.apache.commons.lang.StringUtils;
import org.ideabrowser.idea.EmbeddedBrowserSettings;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Controller used by a WebViewer.
 * <p>
 * Its main responsibilities are:
 * a) to handle queries / urls / descriptions, which are 3 separate concepts.
 * - A query is what user enters in the query bar.
 * This may represent a url (http://www.w3.org) which will be loaded in the WebViewer;
 * or a search item (Intellij plugin) which will be sent to a search engine and whose results will be displayed in the WebViewer
 * - A description is a short display name for a query. For queries which are urls, this will the document's name. For queries which are search items, this will the search item itself.
 * Descriptions are typically used in the search history where we don't want to display the full urls
 * <p>
 * b) to handle history and notify any registered {@link SearchHistoryListener}
 */
public class EngineController {
    private EngineControllerListener viewListener;

    private final EmbeddedBrowserSettings settings;
    private final URLChecker urlChecker;
    private final LinkedList<SearchHistoryItem> history;
    private SearchHistoryListener historyListener;
    private String lastQuery;

    public EngineController() {
        this(EmbeddedBrowserSettings.getInstance(), EngineController::checkURL);
    }

    EngineController(EmbeddedBrowserSettings settings, URLChecker urlChecker) {
        this.settings = settings;
        this.urlChecker = urlChecker;
        this.history = new LinkedList<>();
    }

    /*
     *
     *
     * Search History Methods
     *
     *
     *  */

    /**
     * Adds a new item in the history.
     *
     * @param displayName the name that will be displayed in the popup menu
     * @param url         the url that will actually be searched for if this item is selected in the popup menu
     */
    private void addHistoryItem(String displayName, String url) {
        int maxHistorySize = settings.getMaxHistorySize();
        // maxHistorySize == 0 means history is disabled
        // maxHistorySize < 0 means unbounded history
        if (maxHistorySize != 0) {
            SearchHistoryItem historyItem = new SearchHistoryItem(displayName, url);
            if (!history.contains(historyItem)) {
                if (history.size() == maxHistorySize) {
                    history.remove();
                }
                history.add(historyItem);
                if (historyListener != null) {
                    historyListener.onHistoryChanged(new SearchHistoryIteratorImpl(history));
                }
            }
        }
    }

    public void setSearchHistoryListener(SearchHistoryListener listener) {
        this.historyListener = listener;
    }

    /**
     * Class that store both the displayName (to be displayed in the history drop down) and the url (to be searched for)
     * It is designed to be used internally. Listeners will call {@link SearchHistoryListener.SearchHistoryIterator#displayName(int)}
     * and other methods from the model instead which abstract away the internal representation (list or set, HistoryItem or Pair or 2 collections, ...)
     */
    private static class SearchHistoryItem {
        final String displayName;
        final String url;

        public SearchHistoryItem(String displayName, String url) {
            this.displayName = displayName;
            this.url = url;
        }

        public boolean equals(Object other) {
            if (other instanceof SearchHistoryItem) {
                SearchHistoryItem otherHistoryItem = (SearchHistoryItem) other;
                return Objects.equals(otherHistoryItem.displayName, displayName) &&
                        Objects.equals(otherHistoryItem.url, url);
            }
            return false;
        }
    }

    /*
     *
     *
     * View Methods
     *
     *
     *  */

    public void setViewListener(EngineControllerListener listener) {
        this.viewListener = listener;
    }

    /**
     * Requests that the supplied query be loaded in the viewer.
     * <p>
     * Converts query into a url, since listener only supports urls.
     * <p>
     * If query is well formed url, it will pass as is to the listener.
     * Else if a well formed url is obtained by prepending http to query the resulting url is passed to the listener
     * Else query is appended to the configured search engine token and the resulting url is passed to the listener
     */
    public void request(String query) {
        lastQuery = query;
        String url;
        // if a protocol is specified (we assume it is if :// is found), use it. valid protocols include http, https, file.
        // if no protocol is specified, we assume http. We could also try with and without www ...
        try {
            String normalizedUrl = query.indexOf("://") > 0 ? query : "http://" + query;
            urlChecker.checkURL(new URL(normalizedUrl));
            url = normalizedUrl;
        } catch (IOException ioe) {
            String searchEngineTemplate = settings.getSearchEngineTemplate();
            if (searchEngineTemplate != null) {
                url = searchEngineTemplate.replace("TOKEN", query);
            } else {
                // We did our best ... By default just use query as is ...
                url = query;
            }
        }
        viewListener.onRequestedURLChanged(url);
    }

    /**
     * Notifies th
     * e controller that a new document  has been loaded.
     * This method is expected to be called by the view when a url change triggered by {@link EngineControllerListener#onRequestedURLChanged(String)} call has completed.
     *
     * @param title title of the new loaded document
     * @param url   url of the new loaded page
     */
    public void onLoaded(String title, String url) {
        // If no title is found, we don't use url as it can be long and verbose, especially if it is a search engine based query where lots of parameters are added by the engine.
        // Instead, we display the query that the used entered.
        // For example, "intellij plugin" rather than "https://www.google.com/search?source=hp&ei=qZ2cXuu_AaGSrgSy47WwBg&q=intellij+plugin&oq=intellij+plugin&gs_lcp=CgZwc3ktYWIQAzIECAAQDTIECAAQDTIECAAQDTIECAAECAAQDTIECAAQDToFCAAQgwE6AggAOgQIABADOgQIABAKUIAfWP5NYItTaARwAHgCgAGcAogBzxiSAQUzLjkuNpgBAKABAaoBB2d3cy13aXqwAQA&sclient=psy-ab&ved=0ahUKEwiruNGllPHbJxDWYQ4dUDCAo&uact=5"
        // This is one of the reasons why the controller hqs its own concept of history, and does not rely on WebEngine.getHistory feature.
        addHistoryItem(StringUtils.isEmpty(title) ? lastQuery : title, url);
        viewListener.onURLChanged(url);
    }

    // An abstraction useful for test purposes
    @FunctionalInterface
    protected interface URLChecker {
        /**
         * Throws an exception if the supplied url does not exist ie if a connection to it can not be opened:
         *
         * @param url a url to check
         */
        void checkURL(URL url) throws IOException;
    }

    // default runtime implementation of URLChecker
    private static void checkURL(URL url) throws IOException {
        url.openConnection().connect();
    }

    private static class SearchHistoryIteratorImpl implements SearchHistoryListener.SearchHistoryIterator {

        private final List<SearchHistoryItem> history;

        SearchHistoryIteratorImpl(List<SearchHistoryItem> history) {
            // Clone to prevent issues in this case:
            // SearchHistoryListener listener = new SearchHistoryListener() {
            //      List<SearchHistoryIterator> iterators = new ArrayList();
            //      public void onHistoryChanged(SearchHistoryIterator it) {
            //          iterators.add(it);
            //      }
            // }
            // controller.setSearchHistoryListener(listener);
            // controller.onLoaded("title", "url");
            // controller.onLoaded("title2", "url2");
            // Without cloning, iterators would have 2 references to (title2, url2) ie (title, url) would be lost
            // because both SearchHistoryListeners would reference the same list that would be modified by the second onLoaded call
            this.history = new ArrayList<>(history);
        }

        @Override
        public int size() {
            return history.size();
        }

        @Override
        public String displayName(int i) {
            return history.get(i).displayName;
        }

        @Override
        public String url(int i) {
            return history.get(i).url;
        }
    }

}
