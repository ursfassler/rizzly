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

package ast.pass.input.xml.parser.type;

import ast.data.Ast;
import ast.data.type.special.IntegerType;
import ast.pass.input.xml.infrastructure.Parser;
import ast.pass.input.xml.infrastructure.XmlParser;
import ast.pass.input.xml.linker.ObjectRegistrar;
import ast.pass.input.xml.scanner.ExpectionParser;
import error.RizzlyError;

public class IntegerParser implements Parser {
  private static final String Name = "Integer";
  private final ExpectionParser stream;
  private final ObjectRegistrar objectRegistrar;
  private final XmlParser parser;
  private final RizzlyError error;

  public IntegerParser(ExpectionParser stream, ObjectRegistrar objectRegistrar, XmlParser parser, RizzlyError error) {
    this.stream = stream;
    this.objectRegistrar = objectRegistrar;
    this.parser = parser;
    this.error = error;
  }

  @Override
  public Parser parserFor(String name) {
    return Name.equals(name) ? this : null;
  }

  @Override
  public Parser parserFor(Class<? extends Ast> type) {
    return type == IntegerType.class ? this : null;
  }

  @Override
  public IntegerType parse() {
    stream.elementStart(Name);
    String name = stream.attribute("name");
    String id = parser.id();
    stream.elementEnd();

    IntegerType object = new IntegerType(name);
    objectRegistrar.register(id, object);
    return object;
  }

}
