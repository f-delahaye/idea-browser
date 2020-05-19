package org.ideabrowser.finder;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
 * It is not meant to be optimized or to use well known algorithms.
 */
//This class underwent some refactoring to support https://github.com/f-delahaye/idea-browser/issues/1
// which in fact has made the api simpler:
// findFirst is gone and instead, a new constructor has been added which allows to pass in the start node and the index in the start node,
// so findFirst can easily be replaced with new SimpleFinder()
//
// This new constructor may also be used to support https://github.com/f-delahaye/idea-browser/issues/1. As long as the previous match's text node and positions are kept
// a new SimpleFinder may be created passing in those and findNext called with the new text.
public class SimpleFinder implements Finder {

    private final TextNodeBrowser nodeBrowser;

    private int indexInCurrentNode = 0;
    private Text currentNode;

    // only contains the previous nodes, NOT the current one which is available in currentNode.
    // Only filled when the search spans multiple nodes. In this case, it will be ordered with the start node at index 0
    private List<Text> previousNodes = new ArrayList<>(3);

    /**
     * Basic constructor.
     * Creates an instance which will start the search from the start of supplied nodeBrowser.
     */
    public SimpleFinder(TextNodeBrowser nodeBrowser) {
        this.nodeBrowser = nodeBrowser;
    }

    /**
     * Advanced constructor meant to be used only as a solution for https://github.com/f-delahaye/idea-browser/issues/.
     * The preferred way to create a Finder is to use the above constructor.
     */
    SimpleFinder(TextNodeBrowser nodeBrowser, Text startingNode, int indexInStartingNode) {
        this.nodeBrowser = nodeBrowser;
        this.indexInCurrentNode = indexInStartingNode;
        this.currentNode =  startingNode;
    }

    private boolean isNotEmpty(String str) {
        return str != null && str.length() != 0;
    }

    /**
     * Searches index of text in source
     *
      */
    @Override
    public FindMatch findNext(String lowerOrUpperCaseText) {
        if (currentNode == null) {
            currentNode = nodeBrowser.first();
            indexInCurrentNode= 0;
        }
        // input parameter is converted to lower case since:
        // - it is small
        // - it will be iterated over many times, once per text nodes of the document
        // Text nodes on the other hand, are big (or at least they will be a lot of small nodes) and will be iterated only a few items (only once unless user loop over results)
        previousNodes.clear();
        int indexInText = 0;
        if (isNotEmpty(lowerOrUpperCaseText)) {
            String lowerCaseText = lowerOrUpperCaseText.toLowerCase();
            while (true) {
                String content = getContent(currentNode);
                if (!isNotEmpty(content)) {
                    break;
                }
                if (indexInCurrentNode < content.length()) {
                    if (lowerCaseText.charAt(indexInText) == Character.toLowerCase(content.charAt(indexInCurrentNode))) {
                        // current search still going on
                        indexInText++;
                        indexInCurrentNode++;
                    } else {
                        // mismatch, reset and start a new search
                        previousNodes.clear();
                        indexInCurrentNode = indexInCurrentNode - indexInText + 1;
                        indexInText = 0;
                    }
                }
                if (indexInText == lowerCaseText.length()) {
                    // The one successful return condition.
                    return buildFindMatch(currentNode, indexInCurrentNode - lowerCaseText.length());
                }
                if (indexInCurrentNode == content.length()) {
                    // this has to support 2 cases:
                    // - current search is still going on and needs to continue on the next source or
                    // - current search has been reset and we will start a new one on the next source.
                    Text nextNode = nodeBrowser.next(currentNode);
                    // only add if the searchis on going ie if indexInText > 0;
                    if (nextNode != null && indexInText > 0) {
                        previousNodes.add(currentNode);
                    }
                    currentNode = nextNode;
                    indexInCurrentNode = 0;
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

        return new FindMatch(previousNodes.size() > 0 ? previousNodes.get(0) : currentNode, tmpStartIndex, currentNode, indexInCurrentNode, previousNodes.isEmpty() ? Collections.emptyList() : new ArrayList<>(previousNodes.subList(1, previousNodes.size())));
    }

    private String getContent(Text currentNode) {
        return currentNode == null ? null : currentNode.getTextContent();
    }

}
