package org.ideabrowser;

/**
 * This interface defines a Model that will be passed to the registered callback and which will handle history items.
 *
 * A model may for example:s
 * - sort the items
 * - filter out duplicates
 * - bound the list (with a maximum number of items)
 * - discard invalid items
 * - shorten names (if item represents an url, we may not want to display it in fully in the pop up menu
 *
 * How the history is populated is up to implementation classes.
*/
public interface SearchHistoryModel {
    int historySize();
    String historyItemDisplayName(int i);
    String historyItemQuery(int i);

    /**
     * Registers a callback that will be notified when history changes. This method is called internally when the model is set and should NOT be called.
     * @param listener an implementation of {@link SearchHistoryListener} (possibly just a lambda)
     */
    void setSearchHistoryListener(SearchHistoryListener listener);

    @FunctionalInterface
    interface SearchHistoryListener  {
        void onHistoryChanged(SearchHistoryModel model);
    }
}
