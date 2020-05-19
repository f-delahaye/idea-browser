package org.ideabrowser.finder;

public class TimedFinder implements Finder {

    Finder delegate;

    public TimedFinder(Finder delegate) {
        this.delegate = delegate;
    }

    @Override
    public FindMatch findNext(String text) {
        long start = System.currentTimeMillis();
         FindMatch match = delegate.findNext(text);
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
