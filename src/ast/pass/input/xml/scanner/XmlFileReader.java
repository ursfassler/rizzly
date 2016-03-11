/**
 *  This file is part of Rizzly.
 *
 *  Rizzly is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Rizzly is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Rizzly.  If not, see <http://www.gnu.org/licenses/>.
 */

package ast.pass.input.xml.scanner;

import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import parser.TokenReader;

public class XmlFileReader implements TokenReader<XmlToken> {
  private final XMLStreamReader streamReader;

  public XmlFileReader(XMLStreamReader streamReader) {
    this.streamReader = streamReader;
  }

  @Override
  public XmlToken next() {
    try {
      while (streamReader.hasNext()) {
        streamReader.nextTag();
        String ns = streamReader.getNamespaceURI();
        if (ns == null) {
          return currentXmlToken();
        }
      }
    } catch (XMLStreamException e) {
    }
    return new XmlToken(XmlType.EndOfFile);
  }

  private XmlToken currentXmlToken() {
    switch (streamReader.getEventType()) {
      case XMLStreamReader.START_DOCUMENT:
        return XmlTokenFactory.documentStart();
      case XMLStreamReader.END_DOCUMENT:
        return XmlTokenFactory.documentEnd();
      case XMLStreamReader.START_ELEMENT:
        Map<String, String> attribute = new HashMap<String, String>();
        for (int i = 0; i < streamReader.getAttributeCount(); i++) {
          attribute.put(streamReader.getAttributeLocalName(i), streamReader.getAttributeValue(i));
        }
        return XmlTokenFactory.elementStart(streamReader.getLocalName(), attribute);
      case XMLStreamReader.END_ELEMENT:
        return XmlTokenFactory.elementEnd();
    }
    throw new RuntimeException("not yet implemented: " + streamReader.getEventType());
  }

}
