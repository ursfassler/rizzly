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

package ast.pass.instantiation;

import pass.AstPass;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.Named;
import ast.data.Namespace;
import ast.data.component.composition.Queue;
import ast.data.function.Function;
import ast.data.type.Type;
import ast.data.variable.Constant;
import ast.data.variable.StateVariable;
import ast.knowledge.KnowledgeBase;
import ast.traverser.DefTraverser;
import ast.traverser.other.ClassGetter;

import common.Designator;

public class Flattner extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    NamespaceReduction reducer = new NamespaceReduction();
    for (Ast itr : ast.children) {
      reducer.visit(itr, new Designator());
    }

    AstList<Ast> flat = reducer.getList();

    ast.children.clear();

    ast.children.addAll(ClassGetter.filter(Function.class, flat));
    ast.children.addAll(ClassGetter.filter(StateVariable.class, flat));
    ast.children.addAll(ClassGetter.filter(Constant.class, flat));
    ast.children.addAll(ClassGetter.filter(Type.class, flat));
  }

}

// reduces names of named objects in named lists
class NamespaceReduction extends DefTraverser<Void, Designator> {
  final private AstList<Ast> list = new AstList<Ast>();

  public AstList<Ast> getList() {
    return list;
  }

  private void addToList(Designator param, Named itr) {
    if (param.size() > 0) {
      itr.name = param.toString(Designator.NAME_SEP);
    }
    list.add(itr);
  }

  @Override
  protected Void visit(Ast obj, Designator param) {
    if (obj instanceof Named) {
      String name = ((Named) obj).name;

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
