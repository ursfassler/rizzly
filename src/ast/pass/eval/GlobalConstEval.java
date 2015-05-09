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

package ast.pass.eval;

import ast.data.AstList;
import ast.data.Namespace;
import ast.data.expression.Expression;
import ast.data.type.Type;
import ast.data.type.TypeRef;
import ast.data.type.TypeRefFactory;
import ast.data.variable.ConstGlobal;
import ast.interpreter.Memory;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.pass.specializer.ExprEvaluator;
import ast.pass.specializer.InstanceRepo;
import ast.repository.query.Collector;
import ast.specification.IsClass;

public class GlobalConstEval extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    AstList<ConstGlobal> constants = Collector.select(ast, new IsClass(ConstGlobal.class)).castTo(ConstGlobal.class);

    // TODO build dependency graph of constants and evaluate in order

    // for (ConstGlobal cg : constants) {
    // evaluate(cg, kb);
    // }
  }

  private void evaluate(ConstGlobal constant, KnowledgeBase kb) {
    constant.def = evalExpr(constant.def, kb);
    constant.type = TypeRefFactory.create(constant.type.getInfo(), evalType(constant.type));
  }

  private Expression evalExpr(Expression def, KnowledgeBase kb) {
    InstanceRepo ir = new InstanceRepo();
    return ExprEvaluator.evaluate(def, new Memory(), ir, kb);
  }

  private Type evalType(TypeRef type) {
    throw new RuntimeException("not yet implemented");
  }
}
