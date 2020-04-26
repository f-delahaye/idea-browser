package org.ideabrowser.find;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Text;

import java.util.Collections;

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
    public void matchInSameNode() throws Exception {
        Document document = TestDocumentBuilder.loadDocument("<html><body>foobar</body></html>");
        Text foobarText = (Text)  document.getElementsByTagName("body").item(0).getFirstChild();
        when(finder.findNext("ob")).thenReturn(new FindMatch(foobarText, 2, 4));
        highlighter.hightlightNext("ob");
        String expectedContent = "<html><body>fo<mark>ob</mark>ar</body></html>";
        Assert.assertEquals(expectedContent, TestDocumentBuilder.toString(document));
    }

    @Test
    public void matchInTwoNodes() throws Exception {
        Document document = TestDocumentBuilder.loadDocument("<html><body>foo<span>bar</span></body></html>");
        Text fooText = (Text)  document.getElementsByTagName("body").item(0).getFirstChild();
        Text barText = (Text)  document.getElementsByTagName("span").item(0).getFirstChild();
        when(finder.findNext("ob")).thenReturn(new FindMatch(fooText, 2, barText, 1, Collections.emptyList()));
        highlighter.hightlightNext("ob");
        String expectedContent = "<html><body>fo<mark>o</mark><span><mark>b</mark>ar</span></body></html>";
        Assert.assertEquals(expectedContent, TestDocumentBuilder.toString(document));
    }

    @Test
    public void matchInThreeNodes() throws Exception {
        Document document = TestDocumentBuilder.loadDocument("<html><body>foo<span>bar</span><p>joe</p></body></html>");
        Text fooNode = (Text)  document.getElementsByTagName("body").item(0).getFirstChild();
        Text barNode = (Text)  document.getElementsByTagName("span").item(0).getFirstChild();
        Text joeNode = (Text)  document.getElementsByTagName("p").item(0).getFirstChild();
        when(finder.findNext("obarj")).thenReturn(new FindMatch(fooNode, 2, joeNode, 1, Collections.singletonList(barNode)));
        highlighter.hightlightNext("obarj");
        String expectedContent = "<html><body>fo<mark>o</mark><span><mark>bar</mark></span><p><mark>j</mark>oe</p></body></html>";
        Assert.assertEquals(expectedContent, TestDocumentBuilder.toString(document));
    }

}
