package org.ideabrowser.find;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class TestDocumentBuilder {

    private static DocumentBuilder documentBuilder = builder();

    private static DocumentBuilder builder()  {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Document loadDocument(String content) {
        try {
            return documentBuilder.parse(new ByteArrayInputStream(content.getBytes()));
        } catch (SAXException | IOException e) {
            throw new IllegalArgumentException("Failed to load "+content, e);
        }
    }

    public static String toString(Document document) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        printDocument(document, baos);
        return baos.toString();
    }

    public static void printDocument(Document document, OutputStream out) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "html");
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        transformer.transform(new DOMSource(document),
                new StreamResult(new OutputStreamWriter(out, StandardCharsets.UTF_8)));
    }

}
