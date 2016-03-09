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
import ast.pass.input.xml.scanner.ExpectionParser;
import error.RizzlyError;

public class ParserDispatcher implements Parser {
  private final Class<? extends Ast> type;
  private final Parsers parsers;
  private final ExpectionParser stream;
  private final XmlParser parser;
  private final RizzlyError error;

  public ParserDispatcher(Class<? extends Ast> type, Parsers parsers, ExpectionParser stream, XmlParser parser, RizzlyError error) {
    this.type = type;
    this.parsers = parsers;
    this.stream = stream;
    this.parser = parser;
    this.error = error;
  }

  @Override
  public <T extends Ast> T parse() {
    String next = stream.peekElement();
    Parser parser = parsers.parserFor(next);
    return parser.parse();
  }

  public void add(Parser parser) {
    parsers.add(parser);
  }

  @Override
  public Parser parserFor(String elementName) {
    return parsers.parserFor(elementName);
  }

  @Override
  public Parser parserFor(Class<? extends Ast> elementType) {
    return type == elementType ? this : parsers.parserFor(elementType);
  }

}
