package org.ideabrowser;

/**
 * Defines a listener that will be notified when the search history changes.
 * This class is designed to be implemented by the UI Component {@link SearchWithHistoryTextField} and offers minimal decoupling.
 *
 * In a pure MVC design, there would be:
 * - a SearchHistoryModel that manages the history
 * - the UI component which holds a model and registers itself as a listener by calling the model's setListener method
 * - the model notifies the listener (ie the view) was needed
 *
 * To keep things simpler here, and to mirror {@link EngineControllerListener}:
 * - there's no model. {@link EngineController} acts as the de facto Model, even if it doesn't implement any interface
 * - the UI component cannot register itself, instead it is registered as a listener by {@link EmbeddedBrowser (which is the only class that knows both of the controller and the UI component}
 *
 * Whatever class manages the history and notifies the listener may implement various operations:
 *  - sort the items
 *  - filter out duplicates
 *  - bound the list (with a maximum number of items)
 *  - discard invalid items
 *  - shorten names (if item represents an url, we may not want to display it in fully in the pop up menu
 * ...
 */
@FunctionalInterface
public interface SearchHistoryListener {
    void onHistoryChanged(SearchHistoryIterator iterator);

    interface SearchHistoryIterator {
        int size();
        String displayName(int i);
        String url(int i);
    }
}
