package org.ideabrowser.finder;

/**
 * A
 */
public class ContinuousLoopingFinder implements Finder {
    private final Finder delegate;
    private boolean first = true;

    public ContinuousLoopingFinder(Finder delegate) {
        this.delegate = delegate;
    }

    @Override
    public FindMatch findFirst(String text) {
        first = false;
        return delegate.findFirst(text);
    }

    @Override
    public FindMatch findNext(String text) {
        if (first) {
            FindMatch findMatch = delegate.findFirst(text);
            if (findMatch != null) {
                first = false;
            }
            return findMatch;
        }
        FindMatch match = delegate.findNext(text);
        if (match == null) {
            match = findFirst(text);
        }
        return match;
    }
}
