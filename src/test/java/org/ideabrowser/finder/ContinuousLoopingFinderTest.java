package org.ideabrowser.finder;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Text;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;

public class ContinuousLoopingFinderTest {
    private Finder delegate;
    private ContinuousLoopingFinder loopingFinder;

    @Before
    public void before() {
        this.delegate = mock(Finder.class);
        this.loopingFinder = new ContinuousLoopingFinder(delegate);
    }

    @Test
    public void firstMatchThenFirstMatchAgainUponNoNext() {
        FindMatch firstMatch = new FindMatch(mock(Text.class), 1, 2);

        when(delegate.findFirst("foo")).thenReturn(firstMatch);
        when(delegate.findNext("foo")).thenReturn(null);

        assertSame(firstMatch, loopingFinder.findNext("foo"));
        // findNext will return null but looping finder will start from the beginning again
        assertSame(firstMatch, loopingFinder.findNext("foo"));

        verify(delegate).findNext("foo");
    }

    @Test
    public void noFirstMatch() {
        when(delegate.findFirst("foo")).thenReturn(null);
        assertNull(loopingFinder.findNext("foo"));
        assertNull(loopingFinder.findNext("foo"));

        verify(delegate, never()).findNext("foo");
    }

}
