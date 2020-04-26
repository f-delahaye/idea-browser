package org.ideabrowser.find;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private Text currentNode;
    private boolean first = true;

    // only contains the previous nodes, NOT the current one which is available in currentNode.
    // Only filled when the search spans multiple nodes. In this case, it will be ordered with the start node at index 0
    private List<Text> previousNodes = new ArrayList<>(3);

    public Finder(TextNodeBrowser nodeBrowser) {
        this.nodeBrowser = nodeBrowser;
    }
    /**
     * Searches index of text in source
     *
      */
    public FindMatch findNext(String text) {
        if (first) {
            // nodeBrowser.first() could have been called in the constructor and would have saved the extra "first" boolean
            // but delaying until now makes unit testing a bit easier.
            currentNode = nodeBrowser.first();
            first = false;
        }
        previousNodes.clear();
        int indexInText = 0;
        if (!StringUtils.isEmpty(text)) {
            while (!StringUtils.isEmpty(getContent(currentNode))) {
                if (text.charAt(indexInText) == getContent(currentNode).charAt(indexInSource)) {
                    // current search still going on
                    indexInText++;
                    indexInSource++;
                } else {
                    // mismatch, reset and start a new search
                    previousNodes.clear();
                    indexInSource = indexInSource - indexInText + 1;
                    indexInText = 0;
                }
                if (indexInText == text.length()) {
                    // The one successful return condition.
                    return buildFindMatch(currentNode, indexInSource - text.length());
                }
                if (indexInSource == getContent(currentNode).length()) {
                    // this has to support 2 cases:
                    // - current search is still going on and needs to continue on the next source or
                    // - current search has been reset and we will start a new one on the next source.
                    Text nextNode = nodeBrowser.next(currentNode);
                    // only add if the searchis on going ie if indexInText > 0;
                    if (nextNode != null && indexInText > 0) {
                        previousNodes.add(currentNode);
                    }
                    currentNode = nextNode;
                    indexInSource = 0;
                }
            }
        }
        return null;
    }

    /**
     * {@link #previousNodes} contains the nodes, but we still need to work out startIndex and endIndex.
     * As mentioned in {@link #previousNodes}'s javadoc, it will NOT contain currentNode.
     * It may be empty if the match only involves currentNode, which will be both the startNode and the endNode
     */
    private FindMatch buildFindMatch(Text currentNode, int startIndex) {
        int tmpStartIndex = startIndex;
        for (int i = previousNodes.size() - 1; i>=0; i--) {
            Text previousNode = previousNodes.get(i);
            tmpStartIndex += getContent(previousNode).length();
        }

        return new FindMatch(previousNodes.size() > 0 ? previousNodes.get(0) : currentNode, tmpStartIndex, currentNode, indexInSource, previousNodes.isEmpty() ? Collections.emptyList() : new ArrayList<>(previousNodes.subList(1, previousNodes.size())));
    }

    private String getContent(Text currentNode) {
        return currentNode == null ? null : currentNode.getTextContent();
    }

}
