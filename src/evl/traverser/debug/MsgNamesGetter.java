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

package evl.traverser.debug;

import java.util.HashSet;
import java.util.Set;

import common.Direction;

import evl.DefTraverser;
import evl.Evl;
import evl.composition.ImplComposition;
import evl.function.header.FuncCtrlInDataIn;
import evl.function.header.FuncCtrlInDataOut;
import evl.function.header.FuncCtrlOutDataIn;
import evl.function.header.FuncCtrlOutDataOut;
import evl.other.CompUse;
import evl.other.ImplElementary;

public class MsgNamesGetter extends DefTraverser<Void, Set<String>> {

  public static Set<String> get(Evl obj) {
    Set<String> ret = new HashSet<String>();
    MsgNamesGetter counter = new MsgNamesGetter();
    counter.traverse(obj, ret);
    return ret;
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Set<String> param) {
    visitList(obj.getIface(Direction.in), param);
    visitList(obj.getIface(Direction.out), param);
    visitList(obj.getComponent(), param);
    return null;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, Set<String> param) {
    visitList(obj.getIface(Direction.in), param);
    visitList(obj.getIface(Direction.out), param);
    visitList(obj.getComponent(), param);
    return null;
  }

  @Override
  protected Void visitCompUse(CompUse obj, Set<String> param) {
    param.add(obj.getName());
    return null;
  }

  @Override
  protected Void visitFuncIfaceOutVoid(FuncCtrlOutDataOut obj, Set<String> param) {
    param.add(obj.getName());
    return null;
  }

  @Override
  protected Void visitFuncIfaceOutRet(FuncCtrlOutDataIn obj, Set<String> param) {
    param.add(obj.getName());
    return null;
  }

  @Override
  protected Void visitFuncIfaceInVoid(FuncCtrlInDataIn obj, Set<String> param) {
    param.add(obj.getName());
    return null;
  }

  @Override
  protected Void visitFuncIfaceInRet(FuncCtrlInDataOut obj, Set<String> param) {
    param.add(obj.getName());
    return null;
  }

}
