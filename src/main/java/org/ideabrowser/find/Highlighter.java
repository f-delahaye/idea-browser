package org.ideabrowser.find;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class Highlighter {

    private final Finder finder;
    private String cssClass;

    public Highlighter(Finder finder, String cssClass) {
        this.finder = finder;
        this.cssClass = cssClass;
    }

    public void hightlightNext(String text) {
        FindMatch match = finder.findNext(text);
        if (match != null) {
            highlight(match);
        }
    }

    private void highlight(FindMatch match) {
        // chek now BEFORE the nodes get splitted.
        boolean endNodeIsDifferent = match.endNode != match.startNode;
        match.endNode.splitText(match.endIndex);
        Text startHighlighted =  match.startNode.splitText(match.startIndex);

        wrapInMark(startHighlighted);
        match.intermediateNodes.forEach(this::wrapInMark);
        if (endNodeIsDifferent) {
            wrapInMark(match.endNode);
        }
    }

    private void wrapInMark(Text node) {
        Element markElement = node.getOwnerDocument().createElement("mark");
        if (!StringUtils.isEmpty(cssClass)) {
            markElement.setAttribute("class", cssClass);
        }
        node.getParentNode().insertBefore(markElement, node);
        markElement.appendChild(node);

    }
}
