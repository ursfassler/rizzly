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

import java.util.HashMap;
import java.util.Map;

import ast.meta.MetaListImplementation;
import error.ErrorType;
import error.RizzlyError;

public class ParsersImplementation implements Parsers {
  final private RizzlyError error;
  final private Map<String, Parser> parsers = new HashMap<String, Parser>();

  public ParsersImplementation(RizzlyError error) {
    this.error = error;
  }

  @Override
  public Parser parserFor(String elementName) {
    Parser parser = parsers.get(elementName);

    if (parser == null) {
      error.err(ErrorType.Error, "unknown element \"" + elementName + "\"", new MetaListImplementation());
      throw new XmlParseError();
    }

    return parser;
  }

  public void add(Parser parser) {
    String name = parser.name();

    if (parsers.containsKey(name)) {
      error.err(ErrorType.Fatal, "parser with name \"" + name + "\" already registered", new MetaListImplementation());
    } else {
      parsers.put(name, parser);
    }
  }

}
