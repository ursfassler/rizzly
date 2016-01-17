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

import ast.data.AstBase;
import ast.data.AstList;
import ast.meta.SourcePosition;
import ast.visitor.Visitor;

final public class CaseOpt extends AstBase {
  final public AstList<CaseOptEntry> value;
  public Block code;

  public CaseOpt(AstList<CaseOptEntry> value, Block code) {
    this.value = value;
    this.code = code;
  }

  @Deprecated
  public CaseOpt(SourcePosition info, AstList<CaseOptEntry> value, Block code) {
    metadata().add(info);
    this.value = value;
    this.code = code;
  }

  @Override
  public String toString() {
    return value.toString();
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }
}
