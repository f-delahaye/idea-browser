package org.ideabrowser.finder;

public interface Finder {
   /**
     * Returns the next occurrence from the previous match.
     * If no call was previously made to any of those, this method starts from the first text node of the underlying document.
     *
     * A reference to the previous match is kept internally so it does not need to be passed it.
     * However, that makes instances of this interface stateful objects (unlike {@link TextNodeBrowser})
     *
     * @param text text to search. Search will be case insensitive
     * @return a FindMatch or null if no occurrence was found when the end of the document was reached.
     */
    FindMatch findNext(String text);
}
