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

package ast.pass;

import pass.AstPass;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.Named;
import ast.data.Namespace;
import ast.data.component.elementary.ImplElementary;
import ast.data.component.hfsm.State;
import ast.data.type.Type;
import ast.knowledge.KnowledgeBase;
import ast.traverser.DefTraverser;
import ast.traverser.other.ClassGetter;

import common.Designator;

/**
 * Moves all types to the top level
 *
 * @author urs
 *
 */
public class TypeUplift extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    AstList<Type> newTypes = new AstList<Type>();
    TypeUpliftWorker worker = new TypeUpliftWorker(newTypes);
    for (Ast itm : ast.children) {
      if (!(itm instanceof Type)) {
        worker.visit(itm, new Designator());
      }
    }
    ast.children.addAll(newTypes);
  }
}

class TypeUpliftWorker extends DefTraverser<Void, Designator> {
  final private AstList<Type> types;

  public TypeUpliftWorker(AstList<Type> types) {
    super();
    this.types = types;
  }

  @Override
  protected Void visit(Ast obj, Designator param) {
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
    AstList<Type> types = ClassGetter.filter(Type.class, obj.item);
    obj.item.removeAll(types);
    return null;
  }

  @Override
  protected Void visitNamespace(Namespace obj, Designator param) {
    super.visitNamespace(obj, param);
    AstList<Type> types = ClassGetter.filter(Type.class, obj.children);
    obj.children.removeAll(types);
    return null;
  }

}
