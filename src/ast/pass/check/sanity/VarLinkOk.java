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

package ast.pass.check.sanity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import main.Configuration;
import ast.data.Ast;
import ast.data.Namespace;
import ast.data.component.composition.SubCallbacks;
import ast.data.component.elementary.ImplElementary;
import ast.data.component.hfsm.State;
import ast.data.component.hfsm.Transition;
import ast.data.function.Function;
import ast.data.reference.LinkedReferenceWithOffset_Implementation;
import ast.data.statement.ForStmt;
import ast.data.statement.VarDefStmt;
import ast.data.template.Template;
import ast.data.type.Type;
import ast.data.variable.ConstPrivate;
import ast.data.variable.StateVariable;
import ast.data.variable.Variable;
import ast.dispatcher.DfsTraverser;
import ast.knowledge.KnowParent;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.repository.query.TypeFilter;
import error.ErrorType;
import error.RError;

/**
 * Verifies that links to variables are ok, i.e. they are declared before use and in a visible scope.
 *
 * @author urs
 *
 */
public class VarLinkOk extends AstPass {
  public VarLinkOk(Configuration configuration) {
    super(configuration);
  }

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    VarLinkOkWorker worker = new VarLinkOkWorker(kb);
    worker.traverse(ast, new HashSet<Ast>());
  }

}

class VarLinkOkWorker extends DfsTraverser<Void, Set<Ast>> {
  private final KnowParent kp;

  public VarLinkOkWorker(KnowledgeBase kb) {
    super();
    this.kp = kb.getEntry(KnowParent.class);
  }

  private Set<Ast> add(Set<Ast> param, Collection<? extends Ast> items) {
    param = new HashSet<Ast>(param);
    param.addAll(items);
    return param;
  }

  @Override
  protected Void visitNamespace(Namespace obj, Set<Ast> param) {
    param = add(param, TypeFilter.select(obj.children, Variable.class));
    param = add(param, TypeFilter.select(obj.children, Type.class));
    return super.visitNamespace(obj, param);
  }

  @Override
  protected Void visitTemplate(Template obj, Set<Ast> param) {
    param = new HashSet<Ast>(param);
    param.addAll(obj.getTempl());
    return super.visitTemplate(obj, param);
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Set<Ast> param) {
    param = new HashSet<Ast>(param);
    param.addAll(obj.variable);
    param.addAll(obj.constant);
    param.addAll(obj.type);
    return super.visitImplElementary(obj, param);
  }

  @Override
  protected Void visitState(State obj, Set<Ast> param) {
    param = new HashSet<Ast>(param);
    param = add(param, TypeFilter.select(obj.item, StateVariable.class));
    return super.visitState(obj, param);
  }

  @Override
  protected Void visitVarDef(VarDefStmt obj, Set<Ast> param) {
    super.visitVarDef(obj, param);
    param.add(obj.variable);
    return null;
  }

  @Override
  protected Void visitForStmt(ForStmt obj, Set<Ast> param) {
    param = new HashSet<Ast>(param);
    param.add(obj.iterator);
    super.visitForStmt(obj, param);
    return null;
  }

  @Override
  protected Void visitFunction(Function obj, Set<Ast> param) {
    param = add(param, obj.param);
    return super.visitFunction(obj, param);
  }

  @Override
  protected Void visitReference(LinkedReferenceWithOffset_Implementation obj, Set<Ast> param) {
    if ((obj.getLink() instanceof Variable) || (obj.getLink() instanceof Type)) {
      if (!param.contains(obj.getLink())) {
        RError.err(ErrorType.Fatal, "object " + obj.getLink().toString() + " not visible from here", obj.metadata());
      }
    }
    return super.visitReference(obj, param);
  }

  @Override
  protected Void visitTransition(Transition obj, Set<Ast> param) {
    param = new HashSet<Ast>(param);
    param.addAll(obj.param);
    visit(obj.src, param);
    visit(obj.dst, param);
    visit(obj.eventFunc, param);
    visit(obj.body, param);
    addAllToTop((State) obj.src.getTarget(), param);
    visit(obj.guard, param);
    return null;
  }

  private void addAllToTop(State state, Set<Ast> param) {
    while (true) {
      param.addAll(TypeFilter.select(state.item, StateVariable.class));
      param.addAll(TypeFilter.select(state.item, ConstPrivate.class));
      Ast parent = kp.get(state);
      if (parent instanceof State) {
        state = (State) parent;
      } else {
        return;
      }
    }
  }

  @Override
  protected Void visitSubCallbacks(SubCallbacks obj, Set<Ast> param) {
    return super.visitSubCallbacks(obj, param);
  }

}
