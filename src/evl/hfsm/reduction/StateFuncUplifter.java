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

package evl.hfsm.reduction;

import java.util.ArrayList;
import java.util.List;

import common.Designator;

import evl.Evl;
import evl.NullTraverser;
import evl.function.header.FuncPrivateRet;
import evl.function.header.FuncPrivateVoid;
import evl.hfsm.ImplHfsm;
import evl.hfsm.State;
import evl.hfsm.StateItem;
import evl.knowledge.KnowledgeBase;

/**
 * Moves all functions of all states to the top-state.
 *
 * @author urs
 *
 */
public class StateFuncUplifter extends NullTraverser<Void, Designator> {
  final private List<StateItem> func = new ArrayList<StateItem>();

  public StateFuncUplifter(KnowledgeBase kb) {
    super();
  }

  static public void process(ImplHfsm obj, KnowledgeBase kb) {
    StateFuncUplifter know = new StateFuncUplifter(kb);
    know.traverse(obj, null);
  }

  @Override
  protected Void visitDefault(Evl obj, Designator param) {
    if (obj instanceof StateItem) {
      return null;
    } else {
      throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
    }
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, Designator param) {
    visit(obj.getTopstate(), new Designator());
    obj.getTopstate().getItem().addAll(func);
    return null;
  }

  @Override
  protected Void visitState(State obj, Designator param) {
    param = new Designator(param, obj.getName());
    // visit(obj.getEntryCode(), param);//TODO correct? It is no longer a function and should not exist at this point
    // visit(obj.getExitCode(), param);
    visitList(obj.getItem(), param);

    obj.getItem().removeAll(func);

    return null;
  }

  @Override
  protected Void visitFuncPrivateVoid(FuncPrivateVoid obj, Designator param) {
    param = new Designator(param, obj.getName());
    obj.setName(param.toString(Designator.NAME_SEP));
    func.add(obj);
    return null;
  }

  @Override
  protected Void visitFuncPrivateRet(FuncPrivateRet obj, Designator param) {
    param = new Designator(param, obj.getName());
    obj.setName(param.toString(Designator.NAME_SEP));
    func.add(obj);
    return null;
  }
}
