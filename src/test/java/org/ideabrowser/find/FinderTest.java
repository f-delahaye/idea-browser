package org.ideabrowser.find;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FinderTest {
    private TextNodeBrowser nodeBrowser;

    private Finder finder;
    private Text fooTextNode;
    private Text barTextNode;
    private Text foobarTextNode;

    @Before
    public void before() {
        this.nodeBrowser = mock(TextNodeBrowser.class);
        finder = new Finder(nodeBrowser);
        this.fooTextNode = createTextNode("foo");
        this.barTextNode = createTextNode("bar");
        this.foobarTextNode = createTextNode("foobar");
    }

    private Text createTextNode(String content) {
        Text node = mock(Text.class);
        when(node.getTextContent()).thenReturn(content);
        return node;
    }

    @Test
    public void noMatchOnEmptySources() {
        when(nodeBrowser.first()).thenReturn(null);
        assertNull(finder.findNext("foo"));
    }

    @Test
    public void noMatchOnEmptySource() {
        Text emptyNode = createTextNode("");
        when(nodeBrowser.first()).thenReturn(emptyNode);
        assertNull(finder.findNext("foo"));
    }

    @Test
    public void findNoMatch() {
        when(nodeBrowser.first()).thenReturn(barTextNode);
        assertNull(finder.findNext("foo"));
    }

    @Test
    public void findMatch() {
        when(nodeBrowser.first()).thenReturn(foobarTextNode);
        assertEquals(new Finder.FindMatch(foobarTextNode, 2, foobarTextNode, 4), finder.findNext("ob"));
    }

    @Test
    public void findMatchAtBeginning() {
        when(nodeBrowser.first()).thenReturn(foobarTextNode);
        assertEquals(new Finder.FindMatch(foobarTextNode, 0, foobarTextNode, 3), finder.findNext("foo"));
    }

    @Test
    public void findMatchAtEnd() {
        when(nodeBrowser.first()).thenReturn(foobarTextNode);
        assertEquals(new Finder.FindMatch(foobarTextNode, 3, foobarTextNode, 6), finder.findNext("bar"));
    }

    @Test
    public void findMatchInSecondSource() {
        Text first = fooTextNode;
        Text second = barTextNode;
        when(nodeBrowser.first()).thenReturn(first);
        when(nodeBrowser.next(first)).thenReturn(second);
        assertEquals(new Finder.FindMatch(barTextNode, 0, barTextNode, 3), finder.findNext("bar"));
    }

    @Test
    public void findMatchAcrossMultipleSources() {
        Text first = fooTextNode;
        Text second = barTextNode;
        when(nodeBrowser.first()).thenReturn(first);
        when(nodeBrowser.next(first)).thenReturn(second);
        when(nodeBrowser.previous(second)).thenReturn(first); // browser is used from second to first to reconstruct the start node.
        assertEquals(new Finder.FindMatch(fooTextNode, 2, barTextNode, 1), finder.findNext("ob"));
    }


    @Test
    public void findMultipleMatches() {
        Text foobarfoo = createTextNode("foobarfoo");
        when(nodeBrowser.first()).thenReturn(foobarfoo);
        finder.findNext("foo");
        assertEquals(new Finder.FindMatch(foobarfoo, 6, foobarfoo, 9), finder.findNext("foo"));
    }

}