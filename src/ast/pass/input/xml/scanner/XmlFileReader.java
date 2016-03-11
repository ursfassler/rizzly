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

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import ast.meta.SourcePosition;
import parser.TokenReader;

public class XmlFileReader implements TokenReader<XmlToken> {
  private final XMLStreamReader streamReader;
  private final String filename;

  public XmlFileReader(XMLStreamReader streamReader, String filename) {
    this.streamReader = streamReader;
    this.filename = filename;
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
    SourcePosition position = new SourcePosition(filename, -1, -1);
    return XmlTokenFactory.endOfFile(position);
  }

  private XmlToken currentXmlToken() {
    Location location = streamReader.getLocation();
    SourcePosition position = new SourcePosition(filename, location.getLineNumber(), location.getColumnNumber());

    switch (streamReader.getEventType()) {
      case XMLStreamReader.START_DOCUMENT:
        return XmlTokenFactory.documentStart(position);
      case XMLStreamReader.END_DOCUMENT:
        return XmlTokenFactory.documentEnd(position);
      case XMLStreamReader.START_ELEMENT:
        Map<String, String> attribute = new HashMap<String, String>();
        for (int i = 0; i < streamReader.getAttributeCount(); i++) {
          attribute.put(streamReader.getAttributeLocalName(i), streamReader.getAttributeValue(i));
        }
        return XmlTokenFactory.elementStart(streamReader.getLocalName(), attribute, position);
      case XMLStreamReader.END_ELEMENT:
        return XmlTokenFactory.elementEnd(position);
    }
    throw new RuntimeException("not yet implemented: " + streamReader.getEventType());
  }

}
