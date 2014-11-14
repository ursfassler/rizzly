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

package evl.traverser;

import common.Designator;

import evl.Evl;
import evl.NullTraverser;
import evl.function.Function;
import evl.knowledge.KnowledgeBase;
import evl.other.EvlList;
import evl.other.Named;
import evl.other.Namespace;
import evl.other.Queue;
import evl.type.Type;
import evl.variable.Constant;
import evl.variable.StateVariable;

// reduces names of named objects in named lists
public class NamespaceReduction extends NullTraverser<Void, Designator> {
  private EvlList<Evl> list = new EvlList<Evl>();

  public NamespaceReduction(KnowledgeBase kb) {
  }

  public static EvlList<Evl> process(Namespace names, KnowledgeBase kb) {
    NamespaceReduction reducer = new NamespaceReduction(kb);
    for (Evl itr : names.getChildren()) {
      reducer.visit(itr, new Designator());
    }
    return reducer.list;
  }

  private void addToList(Designator param, Named itr) {
    if (param.size() > 0) {
      itr.setName(param.toString(Designator.NAME_SEP));
    }
    this.list.add(itr);
  }

  @Override
  protected Void visitDefault(Evl obj, Designator param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visit(Evl obj, Designator param) {
    if (obj instanceof Named) {
      String name = ((Named) obj).getName();

      assert (name.length() > 0);
      param = new Designator(param, name);
    }
    super.visit(obj, param);
    return null;
  }

  @Override
  protected Void visitNamespace(Namespace obj, Designator param) {
    visitList(obj.getChildren(), param);
    return null;
  }

  @Override
  protected Void visitFunctionImpl(Function obj, Designator param) {
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
