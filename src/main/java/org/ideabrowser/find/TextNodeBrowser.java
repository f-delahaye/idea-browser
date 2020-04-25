package org.ideabrowser.find;

/**
 * Interface which allows bi directional browsing of a DOM.
 *
 * It uses the built-in getChildNodes / getSiblings methods provided by DOM's Node interface but:
 * - only exposes text nodes
 * - hides the underlying graph view of DOM, so callers have a simpler API.
 *
 * This differs from a DFS traversal of the DOM graph in that the whole DOM is not traversed to build {@link TextNodeBrowser}.
 * Instead, getChildNodes / getSiblings / getParent will be called appropriately for each invokation of next / previous.
 * Note also that it doesn't hold internally a current text reference so it has to be passed as a parameter.
 */
public interface TextNodeBrowser {
    String first();
    String next(String text);
    String previous(String text);
}
