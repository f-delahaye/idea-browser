package org.ideabrowser.finder;

import com.intellij.openapi.diagnostic.Logger;

public class TimedFinder implements Finder {

    private static Logger LOG = Logger.getInstance(TimedFinder.class);

    Finder delegate;

    public TimedFinder(Finder delegate) {
        this.delegate = delegate;
    }

    @Override
    public FindMatch findFirst(String text) {
        return findAndLogTime(text, true);
    }

    @Override
    public FindMatch findNext(String text) {
        return findAndLogTime(text, false);
    }

    private FindMatch findAndLogTime(String text, boolean first) {
        long start = System.currentTimeMillis();
         FindMatch match = first ? delegate.findFirst(text) : delegate.findNext(text);
         if (match != null) {
             log("Match found for " + text + " @"+match.startNode.getTextContent()+" in "+(System.currentTimeMillis() - start)+" ms");
         } else {
             log("No match found for " + text +" in "+(System.currentTimeMillis() - start)+" ms");
         }
         return match;
    }

    private void log(String message) {
        System.out.println(message);
    }
}
