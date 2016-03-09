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

package ast.pass.input.xml.parser.variable;

import ast.data.Ast;
import ast.data.reference.Reference;
import ast.data.variable.FunctionVariable;
import ast.pass.input.xml.infrastructure.Parser;
import ast.pass.input.xml.infrastructure.XmlParser;
import ast.pass.input.xml.linker.ObjectRegistrar;
import ast.pass.input.xml.scanner.ExpectionParser;
import error.RizzlyError;

public class FunctionVariableParser implements Parser {
  private static final String Name = "FunctionVariable";
  private final ExpectionParser stream;
  private final ObjectRegistrar objectRegistrar;
  private final XmlParser parser;
  private final RizzlyError error;

  public FunctionVariableParser(ExpectionParser stream, ObjectRegistrar objectRegistrar, XmlParser parser, RizzlyError error) {
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
    return type == FunctionVariable.class ? this : null;
  }

  @Override
  public FunctionVariable parse() {
    stream.elementStart(Name);
    String name = stream.attribute("name");
    String id = parser.id();

    Reference type = parser.itemOf(Reference.class);

    stream.elementEnd();

    FunctionVariable object = new FunctionVariable(name, type);
    objectRegistrar.register(id, object);
    return object;
  }

}
