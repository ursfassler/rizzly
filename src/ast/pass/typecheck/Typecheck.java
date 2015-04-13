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

package ast.pass.typecheck;

import pass.AstPass;
import ast.data.Ast;
import ast.data.Namespace;
import ast.data.component.composition.ImplComposition;
import ast.data.component.elementary.ImplElementary;
import ast.data.component.hfsm.ImplHfsm;
import ast.data.expression.Expression;
import ast.data.function.Function;
import ast.data.type.Type;
import ast.data.type.base.EnumType;
import ast.data.variable.Variable;
import ast.knowledge.KnowType;
import ast.knowledge.KnowledgeBase;
import ast.traverser.DefTraverser;

public class Typecheck extends AstPass {
  static public void process(Ast ast, KnowledgeBase kb) {
    TypeCheckerWorker adder = new TypeCheckerWorker(kb);
    adder.traverse(ast, null);
  }

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    TypeCheckerWorker adder = new TypeCheckerWorker(kb);
    adder.traverse(ast, null);
  }
}

class TypeCheckerWorker extends DefTraverser<Void, Void> {
  private KnowledgeBase kb;

  public TypeCheckerWorker(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Void sym) {
    visitList(obj.function, sym);
    visitList(obj.iface, sym);
    visit(obj.queue, sym);
    visitList(obj.type, sym);
    visitList(obj.variable, sym);
    visitList(obj.constant, sym);
    visitList(obj.function, sym);
    visitList(obj.subCallback, sym);
    return null;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, Void sym) {
    throw new RuntimeException("should not reach this point");
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, Void param) {
    throw new RuntimeException("should not reach this point");
  }

  @Override
  protected Void visitFunction(Function obj, Void sym) {
    checkFunc(obj, kb);
    return null;
  }

  @Override
  protected Void visitVariable(Variable obj, Void sym) {
    StatementTypecheck.process(obj, kb);
    return null;
  }

  @Override
  protected Void visitExpression(Expression obj, Void sym) {
    ExpressionTypecheck.process(obj, kb);
    return null;
  }

  @Override
  protected Void visitEnumType(EnumType obj, Void param) {
    return null; // we do not type check them (assignment of number does not
                 // work)
  }

  public static void checkFunc(Function obj, KnowledgeBase kb) {
    for (Variable param : obj.param) {
      Typecheck.process(param, kb);
    }
    KnowType kt = kb.getEntry(KnowType.class);
    Type ret = kt.get(obj.ret);
    StatementTypecheck.process(obj.body, ret, kb);
  }
}
