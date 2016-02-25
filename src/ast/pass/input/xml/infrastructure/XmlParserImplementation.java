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

package ast.pass.input.xml.infrastructure;

import ast.data.Ast;
import ast.data.AstList;
import ast.pass.input.xml.scanner.ExpectionParser;

public class XmlParserImplementation implements XmlParser {
  final private ExpectionParser stream;
  final private Parsers parsers;

  public XmlParserImplementation(ExpectionParser stream, Parsers parsers) {
    this.stream = stream;
    this.parsers = parsers;
  }

  public void add(Parser parser) {
    parsers.add(parser);
  }

  @Override
  public AstList<Ast> anyItems() {
    AstList<Ast> items = new AstList<Ast>();
    while (stream.hasElement()) {
      items.add(anyItem());
    }
    return items;
  }

  @Override
  public <T extends Ast> AstList<T> itemsOf(Class<T> itemClass) {
    Parser parser = parsers.parserFor(itemClass);
    AstList<T> items = new AstList<T>();
    while (stream.hasElement()) {
      String name = stream.peekElement();
      if (!parser.names().contains(name)) {
        break;
      }
      items.add((T) parser.parse());
    }
    return items;
  }

  @Override
  public Ast anyItem() {
    String name = stream.peekElement();
    Parser parser = parsers.parserFor(name);
    return parser.parse();
  }

  @Override
  public <T extends Ast> T itemOf(Class<T> itemClass) {
    Parser parser = parsers.parserFor(itemClass);
    return parser.parse();
  }

}
