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
import ast.data.AstList;

public final class Copy {

  public static <T extends Ast> T copy(T obj) {
    CopyAst copier = new CopyAst();
    T nobj = copier.copy(obj);
    Relinker.relink(nobj, copier.getCopied());
    return nobj;
  }

  public static <T extends Ast> AstList<T> copy(AstList<T> obj) {
    CopyAst copier = new CopyAst();
    AstList<T> nobj = copier.copy(obj);
    Relinker.relink(nobj, copier.getCopied());
    return nobj;
  }

}
