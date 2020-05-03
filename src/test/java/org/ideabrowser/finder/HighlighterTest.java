package org.ideabrowser.finder;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Text;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HighlighterTest {

    private Finder finder;
    private Highlighter highlighter;

    @Before
    public void before() {
        this.finder = mock(Finder.class);
        this.highlighter = new Highlighter(finder);
    }

    @Test
    public void matchInSameNode() throws Exception {

        Document document = TestDocumentBuilder.loadDocument("<html><body>foobar</body></html>");
        Document initialDocument = (Document) document.cloneNode(true);

        Text foobarText = (Text)  document.getElementsByTagName("body").item(0).getFirstChild();
        when(finder.findNext("ob")).thenReturn(new FindMatch(foobarText, 2, 4));

        assertTrue(highlighter.highlightNext("ob"));
        assertEquals("<html><body>fo<mark class=\"idea_browser\">ob</mark>ar</body></html>", TestDocumentBuilder.toString(document));

        highlighter.clear(document);
        assertTrue(initialDocument.isEqualNode(document));
    }

    @Test
    public void matchInTwoNodes() throws Exception {

        Document document = TestDocumentBuilder.loadDocument("<html><body>foo<span>bar</span></body></html>");
        Document initialDocument = (Document) document.cloneNode(true);

        Text fooText = (Text)  document.getElementsByTagName("body").item(0).getFirstChild();
        Text barText = (Text)  document.getElementsByTagName("span").item(0).getFirstChild();
        when(finder.findNext("ob")).thenReturn(new FindMatch(fooText, 2, barText, 1, Collections.emptyList()));

        assertTrue(highlighter.highlightNext("ob"));
        assertEquals("<html><body>fo<mark class=\"idea_browser\">o</mark><span><mark class=\"idea_browser\">b</mark>ar</span></body></html>", TestDocumentBuilder.toString(document));

        highlighter.clear(document);
        assertTrue(initialDocument.isEqualNode(document));
    }

    @Test
    public void matchInThreeNodes() throws Exception {

        Document document = TestDocumentBuilder.loadDocument("<html><body>foo<span>bar</span><p>joe</p></body></html>");
        Document initialDocument = (Document) document.cloneNode(true);

        Text fooNode = (Text)  document.getElementsByTagName("body").item(0).getFirstChild();
        Text barNode = (Text)  document.getElementsByTagName("span").item(0).getFirstChild();
        Text joeNode = (Text)  document.getElementsByTagName("p").item(0).getFirstChild();
        when(finder.findNext("obarj")).thenReturn(new FindMatch(fooNode, 2, joeNode, 1, Collections.singletonList(barNode)));

        assertTrue(highlighter.highlightNext("obarj"));
        assertEquals("<html><body>fo<mark class=\"idea_browser\">o</mark><span><mark class=\"idea_browser\">bar</mark></span><p><mark class=\"idea_browser\">j</mark>oe</p></body></html>", TestDocumentBuilder.toString(document));

        highlighter.clear(document);
        assertTrue(initialDocument.isEqualNode(document));

    }


}
