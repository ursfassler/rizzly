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

package evl.pass.hfsm.reduction;

import java.util.ArrayList;
import java.util.List;

import pass.EvlPass;

import common.Designator;

import evl.data.Evl;
import evl.data.Namespace;
import evl.data.component.hfsm.ImplHfsm;
import evl.data.component.hfsm.State;
import evl.data.component.hfsm.StateItem;
import evl.data.function.header.FuncPrivateRet;
import evl.data.function.header.FuncPrivateVoid;
import evl.data.type.Type;
import evl.data.variable.ConstPrivate;
import evl.knowledge.KnowledgeBase;
import evl.traverser.NullTraverser;

/**
 * Moves items of all states to the top-state.
 *
 * @author urs
 *
 */
public class StateItemUplifter extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    for (ImplHfsm hfsm : evl.getItems(ImplHfsm.class, true)) {
      StateItemUplifterWorker know = new StateItemUplifterWorker(kb);
      know.traverse(hfsm, null);
    }
  }
}

class StateItemUplifterWorker extends NullTraverser<Void, Designator> {
  final private List<StateItem> func = new ArrayList<StateItem>();

  public StateItemUplifterWorker(KnowledgeBase kb) {
    super();
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
    visit(obj.topstate, new Designator());
    obj.topstate.item.addAll(func);
    return null;
  }

  @Override
  protected Void visitState(State obj, Designator param) {
    param = new Designator(param, obj.getName());
    // visit(obj.getEntryCode(), param);//TODO correct? It is no longer a function and should not exist at this point
    // visit(obj.getExitCode(), param);
    visitList(obj.item, param);

    obj.item.removeAll(func);

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

  @Override
  protected Void visitType(Type obj, Designator param) {
    param = new Designator(param, obj.getName());
    obj.setName(param.toString(Designator.NAME_SEP));
    func.add(obj);
    return null;
  }

  @Override
  protected Void visitConstPrivate(ConstPrivate obj, Designator param) {
    param = new Designator(param, obj.getName());
    obj.setName(param.toString(Designator.NAME_SEP));
    func.add(obj);
    return null;
  }

}
