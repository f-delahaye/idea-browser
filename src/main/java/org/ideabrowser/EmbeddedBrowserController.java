package org.ideabrowser;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Objects;

/**
 * Controller used by a WebViewer.
 * <p>
 * Its main responsibilities are:
 * a) to handle queries / urls / descriptions, which are 3 separate concepts.
 * - A query is what user enters in the query bar.
 * This may represent a url (http://www.w3.org) which will be loaded in the WebViewer;
 * or a search item (coronavirus) which will be sent to a search engine and whose results will be displayed in the WebViewer
 * - A description is a short display name for a query. For queries which are urls, this will the document's name. For queries which are search items, this will the search item itself.
 * Descriptions are typically used in the search history where we don't want to display the full urls
 * <p>
 * b) to act as the history model by implementing SearchHistoryModel.
 */
public class EmbeddedBrowserController implements SearchHistoryModel {
    private EmbeddedBrowserListener viewListener;

    private final EmbeddedBrowserSettings settings;
    private final URLChecker urlChecker;
    private final LinkedList<HistoryItem> history;
    private SearchHistoryListener historyListener;

    public EmbeddedBrowserController() {
        this(EmbeddedBrowserSettings.getInstance(), EmbeddedBrowserController::checkURL);
    }

    EmbeddedBrowserController(EmbeddedBrowserSettings settings, URLChecker urlChecker) {
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
    @Override
    public int historySize() {
        return history.size();
    }

    @Override
    public String historyItemDisplayName(int i) {
        return history.get(i).displayName;
    }

    @Override
    public String historyItemQuery(int i) {
        return history.get(i).url;
    }

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
        if (maxHistorySize != 0 && historyListener != null) {
            HistoryItem historyItem = new HistoryItem(displayName, url);
            if (!history.contains(historyItem)) {
                if (historySize() == maxHistorySize && maxHistorySize > 0) {
                    history.remove();
                }
                history.add(historyItem);
                historyListener.onHistoryChanged(this);
            }
        }
    }

    @Override
    public void setSearchHistoryListener(SearchHistoryListener listener) {
        this.historyListener = listener;
    }

    /**
     * Class that store both the displayName (to be displayed in the history drop down) and the url (to be searched for)
     * It is designed to be used internally. Listeners will call {@link SearchHistoryModel#historyItemDisplayName(int)}
     * and other methods from the model instead which abstract away the internal representation (list or set, HistoryItem or Pair or 2 collections, ...)
     */
    private static class HistoryItem {
        final String displayName;
        final String url;

        public HistoryItem(String displayName, String url) {
            this.displayName = displayName;
            this.url = url;
        }

        public boolean equals(Object other) {
                HistoryItem otherHistoryItem = (HistoryItem) other;
                return Objects.equals(otherHistoryItem.displayName, displayName) &&
                        Objects.equals(otherHistoryItem.url, url);
        }
    }

    /*
     *
     *
     * View Methods
     *
     *
     *  */

    public void setViewListener(EmbeddedBrowserListener listener) {
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
     * Notifies the controller that a new document  has been loaded.
     * This method is expected to be called by the view when a url change triggered by {@link EmbeddedBrowserListener#onRequestedURLChanged(String)} call has completed.
     *
     * @param description short description of the new loaded document
     * @param url         url of the new loaded page
     */
    public void onLoaded(String description, String url) {
        addHistoryItem(StringUtils.isEmpty(description) ? url : description, url);
        viewListener.onURLChanged(url);
    }

    /**
     * Defines callbacks triggered by the controller that its associated view must respond to.
     * This interface is expected to be implemented by the view.
     * <p>
     * Typical workflow is:
     * - user enters query in UI component
     * - view listens on the component and notifies the controller
     * - controller works out the actual url from the query and notifies the view (ie the listener) that the requested url has changed.
     * - view loads it and notifies the controller when the loading is complete
     * - controller notifies the view that the url has changed
     * <p>
     * That's quite a lot of steps but it allows for clear separation of concerns.
     */
    // Implementation note: last step could be removed ... if the view notifies the controller that the loading is complete, it *could* refresh the url itself ...
    // However, it has to notify the controller any way so that the history is updated so its an opportunity to have all the logic handled in the controller at the price of an extra method in the interface (onURLChanged)
    public interface EmbeddedBrowserListener {
        /**
         * Notifies the view (ie the listener) that the controller is requesting the specified url to be loaded and displayed.
         * In most cases, the controller will request this in response to a notification from the view that user has entered a query in the query bar.
         *
         * @param url new url to be loaded by the view
         */
        void onRequestedURLChanged(String url);

        /**
         * Notifies the view (ie the listener) listener that the url has changed.
         * <p>
         * Unlike {@link  #onRequestedURLChanged(String)} which is triggered to request a new url to be loaded, this one is triggered AFTER an url has been loaded.
         * This may be because user entered a new query in the query bar, or because they clicked an a new link in the loaded web page.
         *
         * @param url url which is currently loaded in the view
         */
        void onURLChanged(String url);

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

}
