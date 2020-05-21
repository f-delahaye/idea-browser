package org.ideabrowser.finder;

import com.intellij.openapi.diagnostic.Logger;

public class LoggingFinder implements Finder {

    private static final Logger LOG = Logger.getInstance(LoggingFinder.class);

    Finder delegate;

    public LoggingFinder(Finder delegate) {
        this.delegate = delegate;
    }

    @Override
    public FindMatch findNext(String text) {
        long start = System.currentTimeMillis();
         FindMatch match = delegate.findNext(text);
         if (match != null) {
             log("Match found for '" + text + "' @"+match.startNode.getTextContent()+" in "+(System.currentTimeMillis() - start)+" ms");
         } else {
             log("No match found for '" + text +"' in "+(System.currentTimeMillis() - start)+" ms");
         }
         return match;
    }

    private void log(String message) {
        // Don't get anything in intellij's console using LOG.info
        System.out.println(message);
        LOG.info(message);
    }
}
