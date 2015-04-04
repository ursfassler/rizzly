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

package evl.pass;

import pass.EvlPass;

import common.Designator;

import evl.data.Evl;
import evl.data.EvlList;
import evl.data.Named;
import evl.data.Namespace;
import evl.data.component.composition.Queue;
import evl.data.function.Function;
import evl.data.type.Type;
import evl.data.variable.Constant;
import evl.data.variable.StateVariable;
import evl.knowledge.KnowledgeBase;
import evl.traverser.DefTraverser;

public class Flattner extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    NamespaceReduction reducer = new NamespaceReduction();
    for (Evl itr : evl.getChildren()) {
      reducer.visit(itr, new Designator());
    }

    EvlList<Evl> flat = reducer.getList();

    evl.clear();

    evl.addAll(flat.getItems(Function.class));
    evl.addAll(flat.getItems(StateVariable.class));
    evl.addAll(flat.getItems(Constant.class));
    evl.addAll(flat.getItems(Type.class));
  }

}

// reduces names of named objects in named lists
class NamespaceReduction extends DefTraverser<Void, Designator> {
  final private EvlList<Evl> list = new EvlList<Evl>();

  public EvlList<Evl> getList() {
    return list;
  }

  private void addToList(Designator param, Named itr) {
    if (param.size() > 0) {
      itr.setName(param.toString(Designator.NAME_SEP));
    }
    list.add(itr);
  }

  @Override
  protected Void visit(Evl obj, Designator param) {
    if (obj instanceof Named) {
      String name = ((Named) obj).getName();

      // assert (name.length() > 0);
      param = new Designator(param, name);
    }
    super.visit(obj, param);
    return null;
  }

  @Override
  protected Void visitFunction(Function obj, Designator param) {
    addToList(param, obj);
    return null;
  }

  @Override
  protected Void visitStateVariable(StateVariable obj, Designator param) {
    addToList(param, obj);
    return null;
  }

  @Override
  protected Void visitConstant(Constant obj, Designator param) {
    addToList(param, obj);
    return null;
  }

  @Override
  protected Void visitType(Type obj, Designator param) {
    addToList(param, obj);
    return null;
  }

  @Override
  protected Void visitQueue(Queue obj, Designator param) {
    addToList(param, obj);
    return null;
  }

}
