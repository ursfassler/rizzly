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

package ast.data.statement;

import ast.data.AstList;
import ast.visitor.Visitor;

final public class IfStatement extends Statement {
  final public AstList<IfOption> option;
  public Block defblock;

  public IfStatement(AstList<IfOption> option, Block defblock) {
    this.option = option;
    this.defblock = defblock;
  }

  public IfStatement() {
    this.option = new AstList<IfOption>();
    this.defblock = new Block();
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

}