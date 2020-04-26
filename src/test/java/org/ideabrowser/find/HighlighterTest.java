package org.ideabrowser.find;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Text;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HighlighterTest {

    private Finder finder;
    private Highlighter highlighter;

    @Before
    public void before() {
        this.finder = mock(Finder.class);
        this.highlighter = new Highlighter(finder, null);
    }

    @Test
    public void matchInSameTextNode() throws Exception {
        Document document = TestDocumentBuilder.loadDocument("<html><body>foobar</body></html>");
        Text foobarText = (Text)  document.getElementsByTagName("body").item(0).getFirstChild();
        when(finder.findNext("ob")).thenReturn(new Finder.FindMatch(foobarText, 2, foobarText, 4));
        highlighter.hightlightNext("ob");
        String expectedContent = "<html><body>fo<mark>ob</mark>ar</body></html>";
        Assert.assertEquals(expectedContent, TestDocumentBuilder.toString(document));
    }

    @Test
    public void matchInDifferentTextNodes() throws Exception {
        Document document = TestDocumentBuilder.loadDocument("<html><body>foo<span>bar</span></body></html>");
        Text fooText = (Text)  document.getElementsByTagName("body").item(0).getFirstChild();
        Text barText = (Text)  document.getElementsByTagName("span").item(0).getFirstChild();
        when(finder.findNext("ob")).thenReturn(new Finder.FindMatch(fooText, 2, barText, 1));
        highlighter.hightlightNext("ob");
        String expectedContent = "<html><body>fo<mark>o</mark><span><mark>b</mark>ar</span></body></html>";
        Assert.assertEquals(expectedContent, TestDocumentBuilder.toString(document));
    }

}
