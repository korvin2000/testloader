package de.microtools.cs.lol.loader.application.writer;

import org.springframework.batch.item.xml.StaxEventItemWriter;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;

public class LolStaxEventItemWriter extends StaxEventItemWriter<Object> {

   @Override
   protected void endDocument(XMLEventWriter writer) throws XMLStreamException {
      // we don't need to write end tag of the root element manually
   }
}
