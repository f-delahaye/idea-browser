package org.ideabrowser.finder;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EnhancedFinderTest {
    private TextNodeBrowser nodeBrowser;
    private EnhancedFinder enhancedFinder;

    @Before
    public void before() {
        this.nodeBrowser = mock(TextNodeBrowser.class);
        this.enhancedFinder = EnhancedFinder.from(nodeBrowser);
    }

    @Test
    public void firstMatchThenFirstMatchAgainUponNoNext() {
        Text fooText = mock(Text.class);
        when(fooText.getTextContent()).thenReturn("foo");

        when(nodeBrowser.first()).thenReturn(fooText);
        when(nodeBrowser.next(fooText)).thenReturn(null);

        assertEquals(new FindMatch(fooText, 0, 3 ), enhancedFinder.findNext("foo"));
        // findNext will return null but looping finder will start from the beginning again
        assertEquals(new FindMatch(fooText, 0, 3 ), enhancedFinder.findNext("foo"));
    }

    // refine previous test by making sure we restart at the beginning of the doc instead of e.g. returning previous match.
    @Test
    public void firstMatchThenNextMatchThenFirstMatchAgainUponNoNext() {
        Text fooBarFooText = mock(Text.class);
        when(fooBarFooText.getTextContent()).thenReturn("foobarfoo");

        when(nodeBrowser.first()).thenReturn(fooBarFooText);
        when(nodeBrowser.next(fooBarFooText)).thenReturn(null);

        assertEquals(new FindMatch(fooBarFooText, 0, 3 ), enhancedFinder.findNext("foo"));
        assertEquals(new FindMatch(fooBarFooText, 6, 9 ), enhancedFinder.findNext("foo"));
        // findNext will return null but looping finder will start from the beginning again
        assertEquals(new FindMatch(fooBarFooText, 0, 3 ), enhancedFinder.findNext("foo"));

    }


    @Test
    public void noFirstMatch() {
        when(nodeBrowser.first()).thenReturn(null);
        assertNull(enhancedFinder.findNext("foo"));
        assertNull(enhancedFinder.findNext("foo"));
    }

    @Test
    public void findWithEnrichedText() {
        // https://github.com/f-delahaye/idea-browser/issues/1
        Text fooBarFooText = mock(Text.class);
        when(fooBarFooText.getTextContent()).thenReturn("foobarfoo");

        when(nodeBrowser.first()).thenReturn(fooBarFooText);
        when(nodeBrowser.next(fooBarFooText)).thenReturn(null);

        assertEquals(new FindMatch(fooBarFooText, 0, 2 ), enhancedFinder.findNext("fo"));
        assertEquals(new FindMatch(fooBarFooText, 0, 3 ), enhancedFinder.findNext("foo"));
        assertEquals(new FindMatch(fooBarFooText, 6, 9 ), enhancedFinder.findNext("foo"));
    }


}
