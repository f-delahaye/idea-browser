package org.ideabrowser.finder;

public interface Finder {
    /**
     * Returns the first occurrence of the supplied text from the start of the underlying document.
     * This method is exposed in the api mainly for advanced customization of the behavior.
     * From a simple clients perspective, it is generally a good idea to always invoke {@link #findNext(String)}
     * as its first invocation will behave as this method.
     * 
     * @param text text to search. Search will be case insensitive
     * @return a FindMatch or null if no occurrence was found when the end of the document was reached.
     */
    FindMatch findFirst(String text);

    /**
     * Returns the next occurrence from the previous match (found by either {@link #findFirst(String)} or this method).
     * If no call was previously made to any of those, this method behaves as {@link #findFirst(String)} so it is generally a good idea to always call it.
     *
     * A reference to the previous match is kept internally so it does not need to be passed it.
     * However, that makes instances of this interface stateful objects (unlike {@link TextNodeBrowser})
     *
     * @param text text to search. Search will be case insensitive
     * @return a FindMatch or null if no occurrence was found when the end of the document was reached.
     */
    FindMatch findNext(String text);
}
