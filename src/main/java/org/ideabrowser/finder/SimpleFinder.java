package org.ideabrowser.finder;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple implementation of Finder, which is not meant to be optimized or to use well known algorithms.
 *
 * https://github.com/f-delahaye/idea-browser/issues/2, while fixed, kind of indicates that a more robust algorithm may be needed
 */
//This class underwent some refactoring to support idea-browser/issues/1
// which in fact has made the api simpler:
// findFirst is gone and instead, a new constructor has been added which allows to pass in the start node and the index in the start node,
// so findFirst can easily be replaced with new SimpleFinder()
//
// This new constructor may also be used to support idea-browser/issues/1. As long as the previous match's text node and positions are kept
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
        int nodeIndex = 0; // only used for debug purposes
        if (isNotEmpty(lowerOrUpperCaseText)) {
            String lowerCaseText = lowerOrUpperCaseText.toLowerCase();
            while (true) {
                try {
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
                            // Make sure indexInCurrentNode is not < 0 else  https://github.com/f-delahaye/idea-browser/issues/2
                            indexInCurrentNode = Math.max(indexInCurrentNode - indexInText + 1, 0);
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
                        nodeIndex++;
                        // only add if the searchis on going ie if indexInText > 0;
                        if (nextNode != null && indexInText > 0) {
                            previousNodes.add(currentNode);
                        }
                        currentNode = nextNode;
                        indexInCurrentNode = 0;
                    }
                } catch (Exception e) {
                    throw new RuntimeException(String.format("Failed to search '%s' in '%s', nodeIndex was %d, indexInCurrentNode was %d, indexInText was %d", lowerCaseText, getContent(currentNode), nodeIndex, indexInCurrentNode, indexInText), e);
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
