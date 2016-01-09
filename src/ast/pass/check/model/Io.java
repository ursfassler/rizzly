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

package ast.pass.check.model;

import ast.data.Ast;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.function.Function;
import ast.data.statement.Statement;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.repository.query.Collector;
import ast.repository.query.FunctionTypeName;
import ast.specification.PureFunction;
import ast.specification.StateChangeStmt;
import ast.visitor.VisitorAcceptor;
import error.ErrorType;
import error.RError;

/**
 * Checks that functions do not change state
 */
public class Io extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    AstList<Function> functions = getPureFunctions(ast);
    for (Function func : functions) {
      checkFunc(func);
    }
  }

  private void checkFunc(Function func) {
    AstList<Statement> modifiers = getStateModifiers(func);
    if (!modifiers.isEmpty()) {
      for (Ast modifier : modifiers) {
        RError.err(ErrorType.Hint, modifier.getInfo(), "here");
      }
      RError.err(ErrorType.Error, func.getInfo(), FunctionTypeName.get(func) + " (" + func.name + ") is not allowed to change state");
    }
  }

  private AstList<Statement> getStateModifiers(VisitorAcceptor ast) {
    Collector visitor = new Collector(new StateChangeStmt());
    ast.accept(visitor);
    return visitor.getMatched().castTo(Statement.class);
  }

  private AstList<Function> getPureFunctions(VisitorAcceptor ast) {
    Collector visitor = new Collector(new PureFunction());
    ast.accept(visitor);
    return visitor.getMatched().castTo(Function.class);
  }

}
