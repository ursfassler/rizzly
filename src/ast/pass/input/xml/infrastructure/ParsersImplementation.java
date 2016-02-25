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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ast.data.Ast;
import ast.meta.MetaListImplementation;
import error.ErrorType;
import error.RizzlyError;

public class ParsersImplementation implements Parsers {
  // TODO is parser by name and type needed?
  final private RizzlyError error;
  final private Map<String, Parser> parsersByName = new HashMap<String, Parser>();
  final private Map<Class<? extends Ast>, Parser> parsersByType = new HashMap<Class<? extends Ast>, Parser>();

  public ParsersImplementation(RizzlyError error) {
    this.error = error;
  }

  @Override
  public Collection<String> names() {
    return parsersByName.keySet();
  }

  @Override
  public Parser parserFor(String elementName) {
    Parser parser = parsersByName.get(elementName);

    if (parser == null) {
      error.err(ErrorType.Error, "unknown element \"" + elementName + "\"", new MetaListImplementation());
      throw new XmlParseError();
    }

    return parser;
  }

  @Override
  public Parser parserFor(Class<? extends Ast> itemType) {
    Parser parser = parsersByType.get(itemType);

    if (parser == null) {
      error.err(ErrorType.Error, "unknown type \"" + itemType.getSimpleName() + "\"", new MetaListImplementation());
      throw new XmlParseError();
    }

    return parser;
  }

  @Override
  public void add(Parser parser) {
    // TODO simplify
    Collection<String> names = parser.names();
    if (nameAlreadyRegistered(names)) {
      return;
    }

    for (String name : names) {
      parsersByName.put(name, parser);
    }

    Class type = parser.type();
    if (parsersByType.containsKey(type)) {
      error.err(ErrorType.Fatal, "parser with type \"" + type.getSimpleName() + "\" already registered", new MetaListImplementation());
    } else {
      parsersByType.put(type, parser);
    }
  }

  private boolean nameAlreadyRegistered(Collection<String> names) {
    for (String name : names) {
      if (parsersByName.containsKey(name)) {
        error.err(ErrorType.Fatal, "parser with name \"" + name + "\" already registered", new MetaListImplementation());
        return true;
      }
    }

    return false;
  }

}
