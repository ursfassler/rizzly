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

package ast.pass.output.xml;

import java.io.OutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import ast.pass.output.xml.visitor.XmlStreamWriter;

public class XmlFileWriter implements XmlStreamWriter {
  private final XMLStreamWriter writer;

  public XmlFileWriter(OutputStream outputStream) throws XMLStreamException {
    XMLOutputFactory xof = XMLOutputFactory.newInstance();
    writer = xof.createXMLStreamWriter(outputStream);
  }

  public void writeNamespacePrefix(String prefix, String uri) {
    try {
      writer.setPrefix(prefix, uri);
      writer.writeNamespace(prefix, uri);
    } catch (XMLStreamException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void beginNode(String name) {
    try {
      writer.writeStartElement(name);
    } catch (XMLStreamException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void beginNode(String namespace, String localName) {
    try {
      writer.writeStartElement(namespace, localName);
    } catch (XMLStreamException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void endNode() {
    try {
      writer.writeEndElement();
    } catch (XMLStreamException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void attribute(String name, String value) {
    try {
      writer.writeAttribute(name, value);
    } catch (XMLStreamException e) {
      e.printStackTrace();
    }
  }

}
