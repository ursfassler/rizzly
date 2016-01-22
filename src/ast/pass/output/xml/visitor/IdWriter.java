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

package ast.pass.output.xml.visitor;

import ast.data.Ast;
import ast.pass.output.xml.IdReader;
import ast.visitor.DefaultHandler;

public class IdWriter implements DefaultHandler {
  private final XmlStreamWriter stream;
  private final IdReader astId;

  public IdWriter(XmlStreamWriter stream, IdReader astId) {
    this.stream = stream;
    this.astId = astId;
  }

  @Override
  public void visit(Ast ast) {
    if (astId.hasId(ast)) {
      stream.attribute("id", astId.getId(ast));
    }
  }

}
