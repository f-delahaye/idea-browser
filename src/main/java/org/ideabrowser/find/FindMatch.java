package org.ideabrowser.find;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

class FindMatch {
    Text startNode;
    int startIndex;

    Text endNode;
    int endIndex;

    List<Text> intermediateNodes;

    /**
     * Convenience constructor for one-node match.
    */
    public FindMatch(@NotNull Text node, int startIndex, int endIndex) {
        this(node, startIndex, node, endIndex, Collections.emptyList());
    }

    public FindMatch(@NotNull Text startNode, int startIndex, @NotNull Text endNode, int endIndex, @NotNull List<Text> intermediateNodes) {
        this.startNode = startNode;
        this.startIndex = startIndex;
        this.endNode = endNode;
        this.endIndex = endIndex;
        this.intermediateNodes = intermediateNodes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FindMatch findMatch = (FindMatch) o;
        return startIndex == findMatch.startIndex &&
                endIndex == findMatch.endIndex &&
                startNode.equals(findMatch.startNode) &&
                endNode.equals(findMatch.endNode) &&
                intermediateNodes.equals(findMatch.intermediateNodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startNode, startIndex, endNode, endIndex, intermediateNodes);
    }

    @Override
    public String toString() {
        return "FindMatch{" +
                "startNode=" + startNode +
                ", startIndex=" + startIndex +
                ", endNode=" + endNode +
                ", endIndex=" + endIndex +
                ", intermediateNodes=" + intermediateNodes +
                '}';
    }
}
