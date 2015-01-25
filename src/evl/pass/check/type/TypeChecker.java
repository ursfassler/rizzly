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

package evl.pass.check.type;

import pass.EvlPass;
import evl.DefTraverser;
import evl.Evl;
import evl.composition.ImplComposition;
import evl.expression.Expression;
import evl.function.Function;
import evl.hfsm.ImplHfsm;
import evl.knowledge.KnowledgeBase;
import evl.other.ImplElementary;
import evl.other.Namespace;
import evl.pass.check.type.specific.ExpressionTypeChecker;
import evl.pass.check.type.specific.StatementTypeChecker;
import evl.type.Type;
import evl.type.base.EnumType;
import evl.variable.Variable;

public class TypeChecker extends EvlPass {
  static public void process(Evl evl, KnowledgeBase kb) {
    TypeCheckerWorker adder = new TypeCheckerWorker(kb);
    adder.traverse(evl, null);
  }

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    TypeCheckerWorker adder = new TypeCheckerWorker(kb);
    adder.traverse(evl, null);
  }
}

class TypeCheckerWorker extends DefTraverser<Void, Void> {
  private KnowledgeBase kb;

  public TypeCheckerWorker(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  static public void process(Evl obj, KnowledgeBase kb) {
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Void sym) {
    visitList(obj.getFunction(), sym);
    visitList(obj.getIface(), sym);
    visit(obj.getQueue(), sym);
    visitList(obj.getType(), sym);
    visitList(obj.getVariable(), sym);
    visitList(obj.getConstant(), sym);
    visitList(obj.getFunction(), sym);
    visitList(obj.getSubCallback(), sym);
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
  protected Void visitFunctionImpl(Function obj, Void sym) {
    checkFunc(obj, kb);
    return null;
  }

  @Override
  protected Void visitVariable(Variable obj, Void sym) {
    StatementTypeChecker.process(obj, kb);
    return null;
  }

  @Override
  protected Void visitExpression(Expression obj, Void sym) {
    ExpressionTypeChecker.process(obj, kb);
    return null;
  }

  @Override
  protected Void visitEnumType(EnumType obj, Void param) {
    return null; // we do not type check them (assignment of number does not work)
  }

  public static void checkFunc(Function obj, KnowledgeBase kb) {
    for (Variable param : obj.getParam()) {
      TypeChecker.process(param, kb);
    }
    Type ret;
    ret = obj.getRet().getLink();
    StatementTypeChecker.process(obj.getBody(), ret, kb);
  }
}
