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

import java.math.BigInteger;

import ast.data.Ast;
import ast.data.expression.value.NumberValue;
import ast.meta.MetaListImplementation;
import ast.pass.input.xml.infrastructure.Parser;
import ast.pass.input.xml.infrastructure.XmlParseError;
import ast.pass.input.xml.infrastructure.XmlParser;
import ast.pass.input.xml.scanner.ExpectionParser;
import error.ErrorType;
import error.RizzlyError;

public class NumberValueParser implements Parser {
  private static final String Name = "NumberValue";
  private final ExpectionParser stream;
  private final XmlParser parser;
  private final RizzlyError error;

  public NumberValueParser(ExpectionParser stream, XmlParser parser, RizzlyError error) {
    this.stream = stream;
    this.parser = parser;
    this.error = error;
  }

  @Override
  public Parser parserFor(String name) {
    if (Name.equals(name)) {
      return this;
    } else {
      return null;
    }
  }

  @Override
  public Parser parserFor(Class<? extends Ast> type) {
    if (type == NumberValue.class) {
      return this;
    } else {
      return null;
    }
  }

  @Override
  public NumberValue parse() {
    stream.elementStart(Name);
    String value = stream.attribute("value");
    stream.elementEnd();

    return new NumberValue(toNumber(value));
  }

  private BigInteger toNumber(String value) {
    try {
      return new BigInteger(value);
    } catch (NumberFormatException e) {
      error.err(ErrorType.Error, "attribute value does not contain a number: \"" + value + "\"", new MetaListImplementation());
      throw new XmlParseError();
    }
  }
}
