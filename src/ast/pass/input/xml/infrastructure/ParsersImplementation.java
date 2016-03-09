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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ast.data.Ast;
import error.RizzlyError;

public class ParsersImplementation implements Parsers {
  final private RizzlyError error;
  final private List<Parser> parsers = new ArrayList<>();

  public ParsersImplementation(RizzlyError error) {
    this.error = error;
  }

  @Override
  public Parser parserFor(String elementName) {
    for (Parser itr : parsers) {
      Parser parser = itr.parserFor(elementName);
      if (parser != null) {
        return parser;
      }
    }

    return null;
  }

  @Override
  public Parser parserFor(Class<? extends Ast> itemType) {
    for (Parser itr : parsers) {
      Parser parser = itr.parserFor(itemType);
      if (parser != null) {
        return parser;
      }
    }

    return null;
  }

  @Override
  public void add(Parser parser) {
    parsers.add(parser);
  }

  @Deprecated
  @Override
  public Collection<String> names() {
    throw new RuntimeException("not yet implemented");
  }

}
