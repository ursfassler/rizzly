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

package evl.copy;

import evl.Evl;
import evl.NullTraverser;
import evl.statement.AssignmentMulti;
import evl.statement.AssignmentSingle;
import evl.statement.Block;
import evl.statement.CallStmt;
import evl.statement.CaseStmt;
import evl.statement.ForStmt;
import evl.statement.IfStmt;
import evl.statement.ReturnExpr;
import evl.statement.ReturnVoid;
import evl.statement.Statement;
import evl.statement.VarDefStmt;
import evl.statement.WhileStmt;
import evl.statement.intern.MsgPush;

public class CopyStatement extends NullTraverser<Statement, Void> {
  private CopyEvl cast;

  public CopyStatement(CopyEvl cast) {
    super();
    this.cast = cast;
  }

  @Override
  protected Statement visitDefault(Evl obj, Void param) {
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

}
