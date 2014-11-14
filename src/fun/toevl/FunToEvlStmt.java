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

package fun.toevl;

import evl.expression.Expression;
import evl.expression.reference.Reference;
import evl.other.EvlList;
import evl.statement.Statement;
import evl.variable.FuncVariable;
import fun.Fun;
import fun.NullTraverser;
import fun.statement.Assignment;
import fun.statement.Block;
import fun.statement.CallStmt;
import fun.statement.CaseOpt;
import fun.statement.CaseStmt;
import fun.statement.IfOption;
import fun.statement.IfStmt;
import fun.statement.ReturnExpr;
import fun.statement.VarDefStmt;
import fun.statement.While;

public class FunToEvlStmt extends NullTraverser<Statement, Void> {
  private FunToEvl fta;

  public FunToEvlStmt(FunToEvl fta) {
    super();
    this.fta = fta;
  }

  @Override
  protected Statement visitDefault(Fun obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  // ----------------------------------------------------------------------------

  @Override
  protected Statement visitBlock(Block obj, Void param) {
    evl.statement.Block block = new evl.statement.Block(obj.getInfo());
    for (fun.statement.Statement stmt : obj.getStatements()) {
      block.getStatements().add((Statement) fta.visit(stmt, null));
    }
    return block;
  }

  @Override
  protected Statement visitAssignment(Assignment obj, Void param) {
    return new evl.statement.Assignment(obj.getInfo(), (evl.expression.reference.Reference) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null));
  }

  @Override
  protected Statement visitReturnExpr(ReturnExpr obj, Void param) {
    return new evl.statement.ReturnExpr(obj.getInfo(), (Expression) fta.traverse(obj.getExpr(), null));
  }

  @Override
  protected Statement visitVarDef(VarDefStmt obj, Void param) {
    FuncVariable var = (FuncVariable) fta.traverse(obj.getVariable(), null);
    return new evl.statement.VarDefStmt(obj.getInfo(), var);
  }

  @Override
  protected Statement visitWhile(While obj, Void param) {
    return new evl.statement.WhileStmt(obj.getInfo(), (Expression) fta.traverse(obj.getCondition(), null), (evl.statement.Block) fta.visit(obj.getBody(), null));
  }

  @Override
  protected Statement visitCallStmt(CallStmt obj, Void param) {
    return new evl.statement.CallStmt(obj.getInfo(), (Reference) fta.traverse(obj.getCall(), null));
  }

  @Override
  protected Statement visitCaseStmt(CaseStmt obj, Void param) {
    EvlList<evl.statement.CaseOpt> opt = new EvlList<evl.statement.CaseOpt>();
    for (CaseOpt itr : obj.getOption()) {
      opt.add((evl.statement.CaseOpt) fta.traverse(itr, null));
    }
    return new evl.statement.CaseStmt(obj.getInfo(), (Expression) fta.traverse(obj.getCondition(), null), opt, (evl.statement.Block) fta.traverse(obj.getOtherwise(), null));
  }

  @Override
  protected Statement visitIfStmt(IfStmt obj, Void param) {
    EvlList<evl.statement.IfOption> opt = new EvlList<evl.statement.IfOption>();
    for (IfOption itr : obj.getOption()) {
      evl.statement.IfOption nopt = new evl.statement.IfOption(obj.getInfo(), (Expression) fta.traverse(itr.getCondition(), null), (evl.statement.Block) fta.traverse(itr.getCode(), null));
      opt.add(nopt);
    }

    return new evl.statement.IfStmt(obj.getInfo(), opt, (evl.statement.Block) fta.traverse(obj.getDefblock(), null));
  }

}
