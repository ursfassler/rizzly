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
import ast.data.Namespace;
import ast.pass.input.xml.infrastructure.Parser;
import ast.pass.input.xml.infrastructure.XmlParser;
import ast.pass.input.xml.scanner.ExpectionParser;
import error.RizzlyError;

public class XmlTopParser implements Parser {
  private static final String Name = "rizzly";
  private final ExpectionParser expect;
  private final XmlParser parser;
  private final RizzlyError error;

  public XmlTopParser(ExpectionParser expect, XmlParser parser, RizzlyError error) {
    this.expect = expect;
    this.parser = parser;
    this.error = error;
  }

  @Override
  public Parser parserFor(String name) {
    return Name.equals(name) ? this : null;
  }

  @Override
  public Parser parserFor(Class<? extends Ast> type) {
    return type == Namespace.class ? this : null;
  }

  @Override
  public Namespace parse() {
    expect.elementStart(Name);
    AstList<Ast> children = parser.anyItems();
    expect.elementEnd();

    Namespace namespace = new Namespace("!");
    namespace.children.addAll(children);
    return namespace;
  }

}
