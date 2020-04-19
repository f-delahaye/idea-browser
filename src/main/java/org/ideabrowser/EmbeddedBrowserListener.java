package org.ideabrowser;

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
