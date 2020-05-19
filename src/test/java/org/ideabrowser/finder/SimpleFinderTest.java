package org.ideabrowser.finder;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Text;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SimpleFinderTest {
    private TextNodeBrowser nodeBrowser;

    private SimpleFinder finder;
    private Text fooNode;
    private Text barNode;
    private Text foobarNode;

    @Before
    public void before() {
        this.nodeBrowser = mock(TextNodeBrowser.class);
        finder = new SimpleFinder(nodeBrowser);
        this.fooNode = createTextNode("foo");
        this.barNode = createTextNode("bar");
        this.foobarNode = createTextNode("foobar");
    }

    private Text createTextNode(String content) {
        Text node = mock(Text.class, content);
        when(node.getTextContent()).thenReturn(content);
        return node;
    }

    @Test
    public void noMatchOnEmptyNodes() {
        when(nodeBrowser.first()).thenReturn(null);
        assertNull(finder.findNext("foo"));
    }

    @Test
    public void noMatchOnEmptyText() {
        Text emptyNode = createTextNode("");
        when(nodeBrowser.first()).thenReturn(emptyNode);
        assertNull(finder.findNext("foo"));
    }

    @Test
    public void findNoMatch() {
        when(nodeBrowser.first()).thenReturn(barNode);
        assertNull(finder.findNext("foo"));
    }

    @Test
    public void findMatchThenNoMatch() {
        when(nodeBrowser.first()).thenReturn(foobarNode);
        assertEquals(new FindMatch(foobarNode, 2, 4), finder.findNext("ob"));
        assertNull(finder.findNext("ob"));
    }

    @Test
    public void findMatchIsCaseInsensitive() {
        when(nodeBrowser.first()).thenReturn(foobarNode);
        assertEquals(new FindMatch(foobarNode, 2, 4), finder.findNext("oB"));
    }

    @Test
    public void findMatchAtBeginning() {
        when(nodeBrowser.first()).thenReturn(foobarNode);
        assertEquals(new FindMatch(foobarNode, 0, 3), finder.findNext("foo"));
    }

    @Test
    public void findMatchAtEnd() {
        when(nodeBrowser.first()).thenReturn(foobarNode);
        assertEquals(new FindMatch(foobarNode, 3, 6), finder.findNext("bar"));
        assertNull(finder.findNext("bar"));
    }

    @Test
    public void findMatchInSecondNode() {
        Text first = fooNode;
        Text second = barNode;
        when(nodeBrowser.first()).thenReturn(first);
        when(nodeBrowser.next(first)).thenReturn(second);
        assertEquals(new FindMatch(barNode, 0, 3), finder.findNext("bar"));
    }

    @Test
    public void findMatchAcrossTwoNodes() {
        Text first = fooNode;
        Text second = barNode;
        when(nodeBrowser.first()).thenReturn(first);
        when(nodeBrowser.next(first)).thenReturn(second);
        assertEquals(new FindMatch(first, 2, second, 1, Collections.emptyList()), finder.findNext("ob"));
    }

    @Test
    public void findMatchAcrossMultipleNodes() {
        Text first = fooNode;
        Text second = barNode;
        Text third = createTextNode("joe");
        when(nodeBrowser.first()).thenReturn(first);
        when(nodeBrowser.next(first)).thenReturn(second);
        when(nodeBrowser.next(second)).thenReturn(third);
        assertEquals(new FindMatch(first, 2, third, 1, Collections.singletonList(second)), finder.findNext("obarj"));
    }


    @Test
    public void findMultipleMatches() {
        Text foobarfoo = createTextNode("foobarfoo");
        when(nodeBrowser.first()).thenReturn(foobarfoo);
        finder.findNext("foo");
        assertEquals(new FindMatch(foobarfoo, 6, 9), finder.findNext("foo"));
    }

    @Test
    public void findMatchAcrossTwoNodesThenNext() {
        Text first = fooNode;
        Text second = barNode;
        Text third = createTextNode("ob");
        when(nodeBrowser.first()).thenReturn(first);
        when(nodeBrowser.next(first)).thenReturn(second);
        when(nodeBrowser.next(second)).thenReturn(third);
        // validated by another test
        assertNotNull(finder.findNext("ob"));
        // validate that all internal stacks / list have been cleared up after the first match (including the previous nodes one hence why the first match was across 2 nodes)
        assertEquals(new FindMatch(third, 0, 2), finder.findNext("ob"));
    }

    // TODO FIXME
    @Test
    public void firstMatchAtEndOfFirstNodeThenSecondMatchWithLongerText() {
        Text first = fooNode;
        Text second = barNode;
        when(nodeBrowser.first()).thenReturn(first);
        when(nodeBrowser.next(first)).thenReturn(second);
        assertEquals(new FindMatch(first, 1, first, 3, Collections.emptyList()), finder.findNext("oo"));
        assertEquals(new FindMatch(first, 1, second, 1, Collections.emptyList()), finder.findNext("oob"));
    }
}