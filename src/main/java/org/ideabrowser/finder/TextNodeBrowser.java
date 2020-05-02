package org.ideabrowser.finder;

import org.w3c.dom.Text;

/**
 * Interface which allows browsing of a DOM.
 *
 * It uses the built-in getChildNodes / getSiblings methods provided by DOM's Node interface but:
 * - only exposes text nodes
 * - hides the underlying graph view of DOM, so callers have a simpler API.
 *
 * nodes will be returned in a pre order DFS fashion, however the interface does not require that a full DFS is performed initially.
 * Instead, subsequent nodes will be retrieved on-demand through calls to {@link #next(Text)}.
 * Implementation classes may therefore elect to not store all the matches in memory although some internal data structure will probably be needed for baktracking purposes.
 *
 */
public interface TextNodeBrowser {
    Text first();
    Text next(Text node);
}
