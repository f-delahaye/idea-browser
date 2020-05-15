package org.ideabrowser.finder;

import org.w3c.dom.*;

/**
 * Instances of this class may be used to find & highlight occurrences of a given text in a DOM node.
 *
 * In order for consecutive calls to {@link #highlightNext(String)} to work as expected, especially if {@link #clear(Document)} is called between, it is critical
 * for the supplied finder to use normalized nodes.
 *
 * The reason lies in how finder works:
 * Lets say we have a node Text(intellij idea), and we search for "ide".
 * Finder will keep a reference to that node as the current node, and calling findNext will resume the search where the previous one left off.
 * {@link #highlightNext(String)}, however, will change the dom to Text(intellij ) - Mark(ide) - Text(a)
 * If {@link #clear(Document)} does not return normalized elements, there's a risk it will return Text(intellij ) - Text(a) so the reference stored in the finder is not valid anymore.
 * so {@link #clear(Document)} returns normalized elements Text(intellij idea).
 * Obviously, if the initial element supplied is NOT normalized, the finder will be broken too.
 *
 *
 */
public class Highlighter {
    // Class that will be used to lookup the mark elements that we added.
    public static final String HIGHLIGHTER_CLASS = "idea_browser";
    public static final String HIGHLIGHTER_TAG_NAME = "mark";
    private Finder finder;

    /**
     * Creates a Highlighter using the supplied finder.
     *
     * Please make sure that the supplied Finder is fed with normalized DOM elements.
     * If in doubt, consider using {@link #from(Node)} instead.
     *
     */
    public Highlighter(Finder finder) {
        this.finder = finder;
    }

    public static Highlighter from(Node element) {
        // clear returns normalized elements so for finder to work as expected especially when calling findNext() after a first match
        // we must provide normalized elements too.
        element.normalize();
        return new Highlighter(new TimedFinder(new ContinuousLoopingFinder(new SimpleFinder(new IterativeDFSTextNodeBrowser(element, "script", "form", "style")))));
    }

    /**
     * Searches the next occurrence of {@code text} in the constructor supplied Finder.
     * If its found then the underlying DOM is modified to hightlight it.
     *
     * @return true if an occurrence is found, false otherwise
     */
    public boolean highlightNext(String text) {
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
                Node parentNode = markElement.getParentNode();
                while (markElement.getChildNodes().getLength() > 0) {
                    parentNode.insertBefore(markElement.getFirstChild(), markElement);
                }
                parentNode.removeChild(markElement);
                parentNode.normalize();
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
