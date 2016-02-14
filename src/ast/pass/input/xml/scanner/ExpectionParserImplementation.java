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

import parser.PeekNReader;
import ast.meta.MetaListImplementation;
import ast.pass.input.xml.infrastructure.XmlParseError;
import error.ErrorType;
import error.RizzlyError;

public class ExpectionParserImplementation implements ExpectionParser {
  private final PeekNReader<XmlToken> stream;
  private final RizzlyError error;
  private XmlToken current;

  public ExpectionParserImplementation(PeekNReader<XmlToken> stream, RizzlyError error) {
    this.stream = stream;
    this.error = error;
  }

  @Override
  public boolean hasElement() {
    return stream.peek(0).type == XmlType.ElementStart;
  }

  @Override
  public String peekElement() {
    XmlToken node = stream.peek(0);
    expectType(node, XmlType.ElementStart);
    return node.name;
  }

  @Override
  public void elementStart(String value) {
    XmlToken node = next();
    expectTypeAndName(node, XmlType.ElementStart, value);
  }

  @Override
  public void elementEnd() {
    XmlToken node = next();
    expectType(node, XmlType.ElementEnd);
  }

  private void expectTypeAndName(XmlToken node, XmlType type, String value) throws XmlParseError {
    expectType(node, type);
    expectName(node, value);
  }

  private void expectType(XmlToken node, XmlType type) throws XmlParseError {
    if (node.type != type) {
      error.err(ErrorType.Error, "expected " + type + ", got type " + node.type, new MetaListImplementation());
      throw new XmlParseError();
    }
  }

  private void expectName(XmlToken node, String value) throws XmlParseError {
    if (node.name != value) {
      error.err(ErrorType.Error, "expected \"" + value + "\" for " + node.type + ", got \"" + node.name + "\"", new MetaListImplementation());
      throw new XmlParseError();
    }
  }

  private XmlToken next() {
    current = stream.next();
    return current;
  }

  @Override
  public String attribute(String name) {
    expectType(current, XmlType.ElementStart);

    if (!current.hasAttribute(name)) {
      error.err(ErrorType.Error, "missing attribute \"" + name + "\" for element \"" + current.name + "\"", new MetaListImplementation());
      throw new XmlParseError();
    }

    return current.getAttribute(name);
  }

}
