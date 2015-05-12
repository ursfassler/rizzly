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

package ast.dispatcher.debug;

import java.util.HashSet;
import java.util.Set;

import ast.data.Ast;
import ast.data.component.composition.CompUse;
import ast.data.component.composition.Direction;
import ast.data.component.composition.ImplComposition;
import ast.data.component.elementary.ImplElementary;
import ast.data.function.header.FuncQuery;
import ast.data.function.header.FuncResponse;
import ast.data.function.header.FuncSignal;
import ast.data.function.header.FuncSlot;
import ast.dispatcher.DfsTraverser;

public class MsgNamesGetter extends DfsTraverser<Void, Set<String>> {

  public static Set<String> get(Ast obj) {
    Set<String> ret = new HashSet<String>();
    MsgNamesGetter counter = new MsgNamesGetter();
    counter.traverse(obj, ret);
    return ret;
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Set<String> param) {
    visitList(obj.getIface(Direction.in), param);
    visitList(obj.getIface(Direction.out), param);
    visitList(obj.component, param);
    return null;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, Set<String> param) {
    visitList(obj.getIface(Direction.in), param);
    visitList(obj.getIface(Direction.out), param);
    visitList(obj.component, param);
    return null;
  }

  @Override
  protected Void visitCompUse(CompUse obj, Set<String> param) {
    param.add(obj.name);
    return null;
  }

  @Override
  protected Void visitFuncSignal(FuncSignal obj, Set<String> param) {
    param.add(obj.name);
    return null;
  }

  @Override
  protected Void visitFuncQuery(FuncQuery obj, Set<String> param) {
    param.add(obj.name);
    return null;
  }

  @Override
  protected Void visitFuncSlot(FuncSlot obj, Set<String> param) {
    param.add(obj.name);
    return null;
  }

  @Override
  protected Void visitFuncResponse(FuncResponse obj, Set<String> param) {
    param.add(obj.name);
    return null;
  }

}
