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

package ast.copy;

import ast.data.Ast;
import ast.data.expression.reference.RefCall;
import ast.data.expression.reference.RefIndex;
import ast.data.expression.reference.RefItem;
import ast.data.expression.reference.RefName;
import ast.data.expression.reference.RefTemplCall;
import ast.traverser.NullTraverser;

public class CopyRef extends NullTraverser<RefItem, Void> {
  private CopyAst cast;

  public CopyRef(CopyAst cast) {
    super();
    this.cast = cast;
  }

  @Override
  protected RefItem visitDefault(Ast obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected RefItem visitRefTemplCall(RefTemplCall obj, Void param) {
    return new RefTemplCall(obj.getInfo(), cast.copy(obj.actualParameter));
  }

  @Override
  protected RefItem visitRefCall(RefCall obj, Void param) {
    return new RefCall(obj.getInfo(), cast.copy(obj.actualParameter));
  }

  @Override
  protected RefItem visitRefName(RefName obj, Void param) {
    return new RefName(obj.getInfo(), obj.name);
  }

  @Override
  protected RefItem visitRefIndex(RefIndex obj, Void param) {
    return new RefIndex(obj.getInfo(), cast.copy(obj.index));
  }

}
