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
import ast.meta.MetaList;
import ast.meta.MetaListImplementation;
import ast.pass.input.xml.parser.meta.MetaParser;
import ast.pass.input.xml.scanner.ExpectionParser;
import error.ErrorType;
import error.RizzlyError;

public class XmlParserImplementation implements XmlParser {
  final private ExpectionParser stream;
  final private Parsers parsers;
  final private MetaParser sourcePositionParser;
  final private RizzlyError error;

  public XmlParserImplementation(ExpectionParser stream, Parsers parsers, MetaParser sourcePositionParser, RizzlyError error) {
    this.stream = stream;
    this.parsers = parsers;
    this.sourcePositionParser = sourcePositionParser;
    this.error = error;
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
    Parser parser = getParser(itemClass);

    AstList<T> items = new AstList<T>();
    while (stream.hasElement()) {
      String name = stream.peekElement();
      Parser actualParser = parser.parserFor(name);
      if (actualParser == null) {
        break;
      }
      items.add((T) actualParser.parse());
    }
    return items;
  }

  @Override
  public Ast anyItem() {
    String name = stream.peekElement();
    Parser parser = parsers.parserFor(name);
    verifyNotNull(parser, name);
    return parser.parse();
  }

  private void verifyNotNull(Parser parser, String name) throws XmlParseError {
    if (parser == null) {
      error.err(ErrorType.Fatal, "parser not found for: " + name, new MetaListImplementation());
      throw new XmlParseError();
    }
  }

  @Override
  public <T extends Ast> T itemOf(Class<T> itemClass) {
    Parser parser = getParser(itemClass);
    return parser.parse();
  }

  @Override
  public String id() {
    return stream.attribute("id", "");
  }

  @Override
  public MetaList meta() {
    MetaList items = new MetaListImplementation();

    while (stream.hasElement() && (stream.peekElement().equals(sourcePositionParser.getElementName()))) {
      items.add(sourcePositionParser.parse());
    }

    return items;
  }

  private <T extends Ast> Parser getParser(Class<T> itemClass) throws XmlParseError {
    Parser parser = parsers.parserFor(itemClass);
    verifyNotNull(parser, itemClass.getSimpleName());
    return parser;
  }

}
