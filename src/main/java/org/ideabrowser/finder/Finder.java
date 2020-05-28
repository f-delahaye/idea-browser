package org.ideabrowser.finder;

/**
 * A class which will parse a given DOM Document or Element to find occurrences of the specified word.
 *
 * There are quite a few Javascript scripts which do that already and could just be injected in the html:
 * hilitor.js
 * mark.js
 * and some other implementations / discussions:
 * https://stackoverflow.com/questions/8644428/how-to-highlight-text-using-javascript
 * https://stackoverflow.com/questions/14029964/execute-a-javascript-function-for-a-webview-from-a-javafx-program
 * https://stackoverflow.com/questions/13719669/append-text-webengine-javafx
 *
 * Implementing it in java is more out of curiosity, and also because of the "next occurrence" feature.
 * Having this in javascript would involve adding stateful behaviour in the page which does not seem very clean.
 */
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
