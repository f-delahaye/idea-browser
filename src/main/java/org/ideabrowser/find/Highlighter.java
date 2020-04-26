package org.ideabrowser.find;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class Highlighter {

    private final Finder finder;
    private String cssClass;

    public Highlighter(Finder finder, String cssClass) {
        this.finder = finder;
        this.cssClass = cssClass;
    }

    public void hightlightNext(String text) {
        highlight(finder.findNext(text));
    }

    private void highlight(Finder.FindMatch match) {
        Text endHighlighted =  match.endNode.splitText(match.endIndex);
        Text startHighlighted =  match.startNode.splitText(match.startIndex);

        Element markElement = match.startNode.getOwnerDocument().createElement("mark");
        if (!StringUtils.isEmpty(cssClass)) {
            markElement.setAttribute("class", cssClass);
        }
        startHighlighted.getParentNode().insertBefore(markElement, startHighlighted);
        Node nodeToMove = startHighlighted;
        do {
            Node nextNodeToMove = nodeToMove.getNextSibling();
            markElement.appendChild(nodeToMove);
            nodeToMove = nextNodeToMove;
        } while (nodeToMove != null && nodeToMove != endHighlighted);
    }
}
