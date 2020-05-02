package org.ideabrowser.finder;

import org.w3c.dom.*;

public class Highlighter {
    // Class that will be used to lookup the mark elements that we added.
    public static final String HIGHLIGHTER_CLASS = "idea_browser";
    public static final String HIGHLIGHTER_TAG_NAME = "mark";
    private final Finder finder;

    public Highlighter(Finder finder) {
        this.finder = finder;
    }

    /**
     * Searches the nexxt ocurence of {@code text} in the constructor supplied Finder.
     * If its found then the underlying DOM is modified to hightlight it.
     *
     * @return true if an occurrence is found, false otherwise
     */
    public boolean hightlightNext(String text) {
        FindMatch match = finder.findNext(text);
        if (match != null) {
            highlight(match);
            return true;
        }
        return false;
    }

    private void highlight(FindMatch match) {
        // check now BEFORE the nodes get split.
        boolean endNodeIsDifferent = match.endNode != match.startNode;
        match.endNode.splitText(match.endIndex);
        Text startHighlighted =  match.startNode.splitText(match.startIndex);

        wrapInMark(startHighlighted);
        match.intermediateNodes.forEach(this::wrapInMark);
        if (endNodeIsDifferent) {
            wrapInMark(match.endNode);
        }
    }

    public void clear(Document document) {
        NodeList markElements = document.getElementsByTagName(HIGHLIGHTER_TAG_NAME);
        for (int i=markElements.getLength() - 1; i >= 0; i--) {
            // use our (hopefully!) specific class to lookup mark elements to remove.
            // Alternatively we could store the elements added in highlight() but that would make Highlighter stateful
            // (although it kind of is already since Finder is stateful, but still ...)
            Node markElement = markElements.item(i);
            if (HIGHLIGHTER_CLASS.equals(markElement.getAttributes().getNamedItem("class").getNodeValue())) {
                while (markElement.getChildNodes().getLength() > 0) {
                    markElement.getParentNode().insertBefore(markElement.getFirstChild(), markElement);
                }
                markElement.getParentNode().removeChild(markElement);
            }
        }
    }

    private void wrapInMark(Text node) {
        Element markElement = node.getOwnerDocument().createElement(HIGHLIGHTER_TAG_NAME);
        markElement.setAttribute("class", HIGHLIGHTER_CLASS);
        node.getParentNode().insertBefore(markElement, node);
        markElement.appendChild(node);
    }
}
