package org.ideabrowser.finder;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Text;

import static org.ideabrowser.finder.TestDocumentBuilder.loadDocument;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class IterativeDFSTextNodeBrowserTest {

    @Test
    public void textAtLevelOne() {
        Document doc = loadDocument("<html>first</html>");
        IterativeDFSTextNodeBrowser browser = new IterativeDFSTextNodeBrowser(doc.getDocumentElement());
        Text first = browser.first();
        assertEquals("first", first.getTextContent());
        assertNull(browser.next(first));
    }

    @Test
    public void textAtLevelTwo() {
        Document doc = loadDocument("<html><body>first</body></html>");
        IterativeDFSTextNodeBrowser browser = new IterativeDFSTextNodeBrowser(doc.getDocumentElement());
        Text first = browser.first();
        assertEquals("first", first.getTextContent());
        assertNull(browser.next(first));
    }

    @Test
    public void textsAtLevelTwoAndThreeWithBacktracking() {
        Document doc = loadDocument("<html><body>first<p>second</p>third</body></html>");
        IterativeDFSTextNodeBrowser browser = new IterativeDFSTextNodeBrowser(doc.getDocumentElement());
        Text first = browser.first();
        assertEquals("first", first.getTextContent());
        Text second = browser.next(first);
        assertEquals("second", second.getTextContent());
        // Once we got to second we need to backtrack to get to third, hence the test case name
        Text third = browser.next(second);
        assertEquals("third", third.getTextContent());
        assertNull(browser.next(third));
    }


}
