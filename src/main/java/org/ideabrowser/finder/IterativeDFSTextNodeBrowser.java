package org.ideabrowser.finder;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.util.*;
import java.util.stream.Collectors;

public class IterativeDFSTextNodeBrowser implements TextNodeBrowser{

    List<Integer> childIndex = new ArrayList<>();
    private final Node root;
    private SortedSet<String> excludedTags;

    /**
     * Creates a browser starting at the first text node BELOW root.
     * So root itself will typically not be a text node itself but its parent.
     *
     */
    public IterativeDFSTextNodeBrowser(Node root) {
        this.root = root;
        this.excludedTags = Collections.emptySortedSet();
    }

    public IterativeDFSTextNodeBrowser(Node root, String... excludedTags) {
        this.root = root;
        this.excludedTags = Arrays.stream(excludedTags).map(String::toLowerCase).sorted().collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public Text first() {
        childIndex.add(0);
        return doNext(root);
    }

    @Override
    public Text next(Text node) {
        return doNext(node.getParentNode());
    }

    private Text doNext(final Node from) {
        Node node = from;
        while (!childIndex.isEmpty()) {
            int index = childIndex.get(childIndex.size() - 1);
            NodeList childNodes = node.getChildNodes();
            if (index < childNodes.getLength()) {
                childIndex.set(childIndex.size() - 1, index + 1);
                node = childNodes.item(index);
                if (node instanceof Text) {
                    return (Text) node;
                } else {
                    if (node instanceof Element && excludedTags.contains(((Element) node).getTagName().toLowerCase())) {
                        node = node.getParentNode();
                    } else {
                        childIndex.add(0);
                    }
                    continue;
                }
            }
            node = node.getParentNode();
            childIndex.remove(childIndex.size() - 1);
        }
        return null;
    }
}
