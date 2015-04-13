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

package ast.copy;

import ast.data.Ast;
import ast.data.statement.AssignmentMulti;
import ast.data.statement.AssignmentSingle;
import ast.data.statement.Block;
import ast.data.statement.CallStmt;
import ast.data.statement.CaseStmt;
import ast.data.statement.ForStmt;
import ast.data.statement.IfStmt;
import ast.data.statement.MsgPush;
import ast.data.statement.ReturnExpr;
import ast.data.statement.ReturnVoid;
import ast.data.statement.Statement;
import ast.data.statement.VarDefInitStmt;
import ast.data.statement.VarDefStmt;
import ast.data.statement.WhileStmt;
import ast.traverser.NullTraverser;

public class CopyStatement extends NullTraverser<Statement, Void> {
  private CopyAst cast;

  public CopyStatement(CopyAst cast) {
    super();
    this.cast = cast;
  }

  @Override
  protected Statement visitDefault(Ast obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Statement visitBlock(Block obj, Void param) {
    Block ret = new Block(obj.getInfo());
    ret.statements.addAll(cast.copy(obj.statements));
    return ret;
  }

  @Override
  protected Statement visitVarDef(VarDefStmt obj, Void param) {
    return new VarDefStmt(obj.getInfo(), cast.copy(obj.variable));
  }

  @Override
  protected Statement visitAssignmentMulti(AssignmentMulti obj, Void param) {
    return new AssignmentMulti(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Statement visitAssignmentSingle(AssignmentSingle obj, Void param) {
    return new AssignmentSingle(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Statement visitCallStmt(CallStmt obj, Void param) {
    return new CallStmt(obj.getInfo(), cast.copy(obj.call));
  }

  @Override
  protected Statement visitReturnExpr(ReturnExpr obj, Void param) {
    return new ReturnExpr(obj.getInfo(), cast.copy(obj.expr));
  }

  @Override
  protected Statement visitReturnVoid(ReturnVoid obj, Void param) {
    return new ReturnVoid(obj.getInfo());
  }

  @Override
  protected Statement visitCaseStmt(CaseStmt obj, Void param) {
    return new CaseStmt(obj.getInfo(), cast.copy(obj.condition), cast.copy(obj.option), cast.copy(obj.otherwise));
  }

  @Override
  protected Statement visitIfStmt(IfStmt obj, Void param) {
    return new IfStmt(obj.getInfo(), cast.copy(obj.option), cast.copy(obj.defblock));
  }

  @Override
  protected Statement visitWhileStmt(WhileStmt obj, Void param) {
    return new WhileStmt(obj.getInfo(), cast.copy(obj.condition), cast.copy(obj.body));
  }

  @Override
  protected Statement visitMsgPush(MsgPush obj, Void param) {
    return new MsgPush(obj.getInfo(), cast.copy(obj.queue), cast.copy(obj.func), cast.copy(obj.data));
  }

  @Override
  protected Statement visitForStmt(ForStmt obj, Void param) {
    return new ForStmt(obj.getInfo(), cast.copy(obj.iterator), cast.copy(obj.block));
  }

  @Override
  protected Statement visitVarDefInitStmt(VarDefInitStmt obj, Void param) {
    return new VarDefInitStmt(obj.getInfo(), cast.copy(obj.variable), cast.copy(obj.initial));
  }
}
