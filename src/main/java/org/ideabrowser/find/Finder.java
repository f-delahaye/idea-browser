package org.ideabrowser.find;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * A class which will parse a given DOM Document or Element to find occurrences of the specified word and will highlight them.
 *
 * There are quite a few Javascript scripts which do that already and could just be injected in the html:
 * hilitor.js
 * mark.js
 * and some other implementations / discussions:
 * https://stackoverflow.com/questions/8644428/how-to-highlight-text-using-javascript
 * https://stackoverflow.com/questions/14029964/execute-a-javascript-function-for-a-webview-from-a-javafx-program
 * https://stackoverflow.com/questions/13719669/append-text-webengine-javafx
 *
 * Implementing it in java is more out of curiosity, and also because of the "next/previous occurrence" feature.
 * Having this in javascript would involve adding stateful behaviour in the page which does not seem very clean.
 * It is not meant to be optimized or to use well known algorithms.
 */
public class Finder {

    private final TextNodeBrowser nodeBrowser;

    private int indexInSource = 0;
    private String currentNode;
    private boolean first = true;

    public Finder(TextNodeBrowser nodeBrowser) {
        this.nodeBrowser = nodeBrowser;
    }
    /**
     * Searches index of text in source
     *
      */
    public FindResult nextFind(String text) {
        if (first) {
            // nodeBrowser.first() could have been called in the constructor and would have saved the extra "first" boolean
            // but delaying until now makes unit test a bit easier.
            currentNode = nodeBrowser.first();
            first = false;
        }
        int indexInText = 0;
        if (!StringUtils.isEmpty(text)) {
            while (!StringUtils.isEmpty(getContent(currentNode))) {
                if (text.charAt(indexInText) == getContent(currentNode).charAt(indexInSource)) {
                    // current search still going on
                    indexInText++;
                    indexInSource++;
                } else {
                    // mismatch, reset and start a new search
                    indexInSource = indexInSource - indexInText + 1;
                    indexInText = 0;
                }
                if (indexInText == text.length()) {
                    // The one successful return condition.
                    return buildFindResult(currentNode, indexInSource - text.length());
                }
                if (indexInSource == getContent(currentNode).length()) {
                    // this has to support 2 cases:
                    // - current search is still going on and needs to continue on the next source or
                    // - current search has been reset and we will start a new one on the next source.
                    currentNode = nodeBrowser.next(currentNode);
                    indexInSource = 0;
                }
            }
        }
        return null;
    }

    private FindResult buildFindResult(String currentNode, int startIndex) {
        int tmpStartIndex = startIndex;
        String startNode = currentNode;
        while (tmpStartIndex < 0) {
            startNode = nodeBrowser.previous(startNode);
            tmpStartIndex += getContent(startNode).length();
        }

        return new Finder.FindResult(startNode, tmpStartIndex, currentNode, indexInSource);
    }

    private String getContent(String currentNode) {
        return currentNode;
    }

    static class FindResult {
        String startNode;
        int startIndex;

        String endNode;
        int endIndex;

        public FindResult(@NotNull String startNode, int startIndex, @NotNull String endNode, int endIndex) {
            this.startNode = startNode;
            this.startIndex = startIndex;
            this.endNode = endNode;
            this.endIndex = endIndex;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FindResult that = (FindResult) o;
            return startIndex == that.startIndex &&
                    endIndex == that.endIndex &&
                    startNode.equals(that.startNode) &&
                    endNode.equals(that.endNode);
        }

        @Override
        public String toString() {
            return "FindResult{" +
                    "startNode='" + startNode + '\'' +
                    ", startIndex=" + startIndex +
                    ", endNode='" + endNode + '\'' +
                    ", endIndex=" + endIndex +
                    '}';
        }
    }
}
