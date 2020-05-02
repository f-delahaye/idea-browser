package org.ideabrowser.finder;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class IterativeDFSTextNodeBrowser implements TextNodeBrowser{

    List<Integer> childIndex = new ArrayList<>();
    private final Node root;

    /**
     * Creates a browser starting at the first text node BELOW root.
     * So root itself will typically not be a text node itself but its parent.
     *
     */
    public IterativeDFSTextNodeBrowser(Node root) {
        this.root = root;
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
                    childIndex.add(0);
                    continue;
                }
            }
            node = node.getParentNode();
            childIndex.remove(childIndex.size() - 1);
        }
        return null;
    }
}
