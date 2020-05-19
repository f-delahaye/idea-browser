package org.ideabrowser.finder;

/**
 * A finder with advanced features.
 * It supports:
 * - continuous looping: when a search reaches the end of the node browser, a null FindeMatch is returned to user but if they still request the next occurrence, this finder
 * will restart the search from the first node of the node browser.
 * - findNext with a text which an enriched version of the text previously matched. See https://github.com/f-delahaye/idea-browser/issues/1
 */
public class EnhancedFinder implements Finder {

    private Finder delegate;
    private FindMatch previousMatch = null;
    private String previousText = "";
    private TextNodeBrowser nodeBrowser;

    // This is an interesting decision design:
    // Pre https://github.com/f-delahaye/idea-browser/issues/1, EnhancedFinder would take a Finder delegate (typically a SimpleFinder but in theory could be something else)
    // Post https://github.com/f-delahaye/idea-browser/issues/1, it takes a nodeBrowser and creates SimpleFinders internally.
    // This ties EnhancedFinder to SimpleFinder but it allows to implement the issue while keeping a clean&simple Finder interface (see also javadoc in SimpleFinder).
    // Also, it sort of aligns the constructors of EnhancedFinder and SimpleFinder as they both take a node browser.
    // Not sure if this is a good thing or not. I tend to believe it is not but unit tests look cleaner like that, so ...
    private EnhancedFinder(TextNodeBrowser nodeBrowser) {
        this.nodeBrowser = nodeBrowser;
    }

    @Override
    public FindMatch findNext(String text) {
        if (previousMatch != null && text != null && text.length() > previousText.length() && text.startsWith(previousText)) {
            delegate = new SimpleFinder(nodeBrowser, previousMatch.startNode, previousMatch.startIndex);
        } else if (delegate == null) {
            delegate = new SimpleFinder(nodeBrowser);
        }
        FindMatch match = delegate.findNext(text);
        if (match == null) {
            // If there is at least one match...
            if (previousMatch != null) {
                // ... create a new delegate and start over from the first node!
                delegate = new SimpleFinder(nodeBrowser);
                return delegate.findNext(text);
            }
        } else {
            previousText = text;
            previousMatch = match;
        }
        return match;
    }

    public static EnhancedFinder from(TextNodeBrowser nodeBrowser) {
        return new EnhancedFinder(nodeBrowser);
    }
}
