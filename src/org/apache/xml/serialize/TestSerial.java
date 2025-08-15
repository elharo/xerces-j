package org.apache.xml.serialize;

import org.xml.sax.SAXException;

public class TestSerial {
  public static void main(String []args) throws SAXException {
    XMLSerializer serializer = new XMLSerializer();
    serializer.setOutputByteStream(System.out);

    serializer.startDocument();
    serializer.processingInstruction("foo", "bar");
    serializer.startDTD("foo", "bar", "baz");
    serializer.startElement("foo", "bar", "bar", null);
    serializer.endElement("foo", "bar", "bar");
    serializer.endDocument();
  }
}
