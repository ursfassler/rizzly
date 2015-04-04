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
import evl.data.component.elementary.ImplElementary;
import evl.data.component.hfsm.State;
import evl.data.type.Type;
import evl.knowledge.KnowledgeBase;
import evl.traverser.DefTraverser;
import evl.traverser.other.ClassGetter;

/**
 * Moves all types to the top level
 *
 * @author urs
 *
 */
public class TypeUplift extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    EvlList<Type> newTypes = new EvlList<Type>();
    TypeUpliftWorker worker = new TypeUpliftWorker(newTypes);
    for (Evl itm : evl.children) {
      if (!(itm instanceof Type)) {
        worker.visit(itm, new Designator());
      }
    }
    evl.children.addAll(newTypes);
  }
}

class TypeUpliftWorker extends DefTraverser<Void, Designator> {
  final private EvlList<Type> types;

  public TypeUpliftWorker(EvlList<Type> types) {
    super();
    this.types = types;
  }

  @Override
  protected Void visit(Evl obj, Designator param) {
    if (obj instanceof Named) {
      param = new Designator(param, ((Named) obj).name);
    }
    return super.visit(obj, param);
  }

  @Override
  protected Void visitType(Type obj, Designator param) {
    obj.name = param.toString();
    types.add(obj);
    return null;
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Designator param) {
    super.visitImplElementary(obj, param);
    obj.type.clear();
    return null;
  }

  @Override
  protected Void visitState(State obj, Designator param) {
    super.visitState(obj, param);
    EvlList<Type> types = obj.item.getItems(Type.class);
    obj.item.removeAll(types);
    return null;
  }

  @Override
  protected Void visitNamespace(Namespace obj, Designator param) {
    super.visitNamespace(obj, param);
    EvlList<Type> types = ClassGetter.filter(Type.class, obj.children);
    obj.children.removeAll(types);
    return null;
  }

}
