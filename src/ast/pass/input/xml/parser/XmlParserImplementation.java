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

package ast.pass.input.xml.parser;

import ast.data.Ast;
import ast.data.AstList;
import ast.pass.input.xml.infrastructure.Parser;
import ast.pass.input.xml.infrastructure.Parsers;
import ast.pass.input.xml.scanner.ExpectionParser;

public class XmlParserImplementation implements XmlParser {
  final private ExpectionParser stream;
  final private Parsers parsers;

  public XmlParserImplementation(ExpectionParser stream, Parsers parsers) {
    this.stream = stream;
    this.parsers = parsers;
  }

  @Override
  public AstList<Ast> astItems() {
    AstList<Ast> items = new AstList<Ast>();
    while (stream.hasElement()) {
      items.add(astItem());
    }
    return items;
  }

  @Override
  public Ast astItem() {
    String name = stream.peekElement();
    Parser parser = parsers.parserFor(name);
    return parser.parse();
  }

}
