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

package ast.pass.input.xml.parser.expression;

import ast.data.Ast;
import ast.data.expression.value.BooleanValue;
import ast.meta.MetaListImplementation;
import ast.pass.input.xml.infrastructure.Parser;
import ast.pass.input.xml.infrastructure.XmlParseError;
import ast.pass.input.xml.infrastructure.XmlParser;
import ast.pass.input.xml.scanner.ExpectionParser;
import error.ErrorType;
import error.RizzlyError;

public class BooleanValueParser implements Parser {
  private static final String Name = "BooleanValue";
  private final ExpectionParser stream;
  private final XmlParser parser;
  private final RizzlyError error;

  public BooleanValueParser(ExpectionParser stream, XmlParser parser, RizzlyError error) {
    this.stream = stream;
    this.parser = parser;
    this.error = error;
  }

  @Override
  public Parser parserFor(String name) {
    return Name.equals(name) ? this : null;
  }

  @Override
  public Parser parserFor(Class<? extends Ast> type) {
    return type == BooleanValue.class ? this : null;
  }

  @Override
  public BooleanValue parse() {
    stream.elementStart(Name);
    String value = stream.attribute("value");
    stream.elementEnd();

    return new BooleanValue(parseBool(value));
  }

  private boolean parseBool(String value) {
    switch (value) {
      case "True":
        return true;
      case "False":
        return false;
      default:
        error.err(ErrorType.Error, "attribute value does not contain a boolean value: \"" + value + "\"", new MetaListImplementation());
        throw new XmlParseError();
    }
  }

}
