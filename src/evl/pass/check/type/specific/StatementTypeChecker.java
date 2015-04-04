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

package evl.pass.check.type.specific;

import error.ErrorType;
import error.RError;
import evl.data.Evl;
import evl.data.EvlList;
import evl.data.expression.Expression;
import evl.data.expression.reference.Reference;
import evl.data.expression.reference.SimpleRef;
import evl.data.statement.AssignmentMulti;
import evl.data.statement.AssignmentSingle;
import evl.data.statement.Block;
import evl.data.statement.CallStmt;
import evl.data.statement.CaseOpt;
import evl.data.statement.CaseOptRange;
import evl.data.statement.CaseOptValue;
import evl.data.statement.CaseStmt;
import evl.data.statement.ForStmt;
import evl.data.statement.IfOption;
import evl.data.statement.IfStmt;
import evl.data.statement.ReturnExpr;
import evl.data.statement.ReturnVoid;
import evl.data.statement.Statement;
import evl.data.statement.VarDefStmt;
import evl.data.statement.WhileStmt;
import evl.data.statement.intern.MsgPush;
import evl.data.type.Type;
import evl.data.type.base.BooleanType;
import evl.data.type.base.EnumElement;
import evl.data.type.base.RangeType;
import evl.data.type.base.TupleType;
import evl.data.variable.ConstGlobal;
import evl.data.variable.ConstPrivate;
import evl.data.variable.Constant;
import evl.data.variable.FuncVariable;
import evl.data.variable.StateVariable;
import evl.data.variable.Variable;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowLeftIsContainerOfRight;
import evl.knowledge.KnowType;
import evl.knowledge.KnowledgeBase;
import evl.traverser.NullTraverser;

public class StatementTypeChecker extends NullTraverser<Void, Void> {
  final private KnowledgeBase kb;
  final private KnowType kt;
  final private KnowBaseItem kbi;
  final private KnowLeftIsContainerOfRight kc;
  final private Type funcReturn;

  public StatementTypeChecker(KnowledgeBase kb, Type funcReturn) {
    super();
    this.kb = kb;
    kbi = kb.getEntry(KnowBaseItem.class);
    kt = kb.getEntry(KnowType.class);
    kc = kb.getEntry(KnowLeftIsContainerOfRight.class);
    this.funcReturn = funcReturn;
  }

  public static void process(Statement obj, Type funcReturn, KnowledgeBase kb) {
    StatementTypeChecker adder = new StatementTypeChecker(kb, funcReturn);
    adder.traverse(obj, null);
  }

  public static void process(Variable obj, KnowledgeBase kb) {
    StatementTypeChecker adder = new StatementTypeChecker(kb, null);
    adder.traverse(obj, null);
  }

  private Type checkGetExpr(Expression expr) {
    ExpressionTypeChecker.process(expr, kb);
    return kt.get(expr);
  }

  @Override
  protected Void visitDefault(Evl obj, Void sym) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitVarDef(VarDefStmt obj, Void sym) {
    visit(obj.variable, sym);

    return null;
  }

  @Override
  protected Void visitStateVariable(StateVariable obj, Void sym) {
    return null;
  }

  @Override
  protected Void visitEnumElement(EnumElement obj, Void param) {
    return null;
  }

  private void checkConstant(Constant obj) {
    Type ret = obj.type.link;
    Type defType = checkGetExpr(obj.def);
    if (!kc.get(ret, defType)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Data type to big or incompatible in assignment: " + ret.getName() + " := " + defType.getName());
    }
  }

  @Override
  protected Void visitConstPrivate(ConstPrivate obj, Void param) {
    checkConstant(obj);
    return null;
  }

  @Override
  protected Void visitConstGlobal(ConstGlobal obj, Void param) {
    checkConstant(obj);
    return null;
  }

  @Override
  protected Void visitFuncVariable(FuncVariable obj, Void map) {
    return null;
  }

  @Override
  protected Void visitForStmt(ForStmt obj, Void param) {
    Type cond = obj.iterator.type.link;
    if (!(cond instanceof RangeType)) {
      RError.err(ErrorType.Error, obj.getInfo(), "For loop only supports range type (at the moment), got: " + cond.getName());
    }
    visit(obj.block, param);
    return null;
  }

  @Override
  protected Void visitWhileStmt(WhileStmt obj, Void param) {
    Type cond = checkGetExpr(obj.condition);
    if (!(cond instanceof BooleanType)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Need boolean type, got: " + cond.getName());
    }
    visit(obj.body, param);
    return null;
  }

  @Override
  protected Void visitIfStmt(IfStmt obj, Void param) {
    for (IfOption opt : obj.option) {
      Type cond = checkGetExpr(opt.condition);
      if (!(cond instanceof BooleanType)) {
        RError.err(ErrorType.Error, opt.getInfo(), "Need boolean type, got: " + cond.getName());
      }
      visit(opt.code, param);
    }
    visit(obj.defblock, param);
    return null;
  }

  @Override
  protected Void visitCaseStmt(CaseStmt obj, Void map) {
    Type cond = checkGetExpr(obj.condition);
    // TODO enumerator, union and boolean should also be allowed
    if (!kc.get(kbi.getIntegerType(), cond)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Condition variable has to be an integer, got: " + cond.getName());
    }
    // TODO check somewhere if case values are disjunct
    visitList(obj.option, map);
    visit(obj.otherwise, map);
    return null;
  }

  @Override
  protected Void visitCaseOpt(CaseOpt obj, Void map) {
    visitList(obj.value, map);
    visit(obj.code, map);
    return null;
  }

  @Override
  protected Void visitCaseOptRange(CaseOptRange obj, Void map) {
    Type start = checkGetExpr(obj.start);
    Type end = checkGetExpr(obj.end);
    if (!kc.get(kbi.getIntegerType(), start)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Case value has to be an integer (start), got: " + start.getName());
    }
    if (!kc.get(kbi.getIntegerType(), end)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Case value has to be an integer (end), got: " + end.getName());
    }
    return null;
  }

  @Override
  protected Void visitCaseOptValue(CaseOptValue obj, Void map) {
    Type value = checkGetExpr(obj.value);
    if (!kc.get(kbi.getIntegerType(), value)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Case value has to be an integer, got: " + value.getName());
    }
    return null;
  }

  @Override
  protected Void visitCallStmt(CallStmt obj, Void map) {
    ExpressionTypeChecker.process(obj.call, kb);
    return null;
  }

  @Override
  protected Void visitAssignmentSingle(AssignmentSingle obj, Void param) {
    Type lhs = checkGetExpr(obj.left);
    Type rhs = checkGetExpr(obj.right);
    if (!kc.get(lhs, rhs)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Data type to big or incompatible in assignment: " + lhs.getName() + " := " + rhs.getName());
    }
    return null;
  }

  @Override
  protected Void visitAssignmentMulti(AssignmentMulti obj, Void param) {
    EvlList<Type> ll = new EvlList<Type>();
    for (Reference ref : obj.left) {
      ll.add(checkGetExpr(ref));
    }
    Type lhs;
    if (ll.size() == 1) {
      lhs = ll.get(0);
    } else {
      EvlList<SimpleRef<Type>> tl = new EvlList<SimpleRef<Type>>();
      for (Type lt : ll) {
        tl.add(new SimpleRef<Type>(lt.getInfo(), lt));
      }
      lhs = new TupleType(obj.getInfo(), "", tl);
    }
    Type rhs = checkGetExpr(obj.right);
    if (!kc.get(lhs, rhs)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Data type to big or incompatible in assignment: " + lhs.getName() + " := " + rhs.getName());
    }
    return null;
  }

  @Override
  protected Void visitBlock(Block obj, Void map) {
    visitList(obj.statements, null);
    return null;
  }

  @Override
  protected Void visitReturnExpr(ReturnExpr obj, Void map) {
    Type ret = checkGetExpr(obj.expr);
    if (!kc.get(funcReturn, ret)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Data type to big or incompatible to return: " + funcReturn.getName() + " := " + ret.getName());
    }
    return null;
  }

  @Override
  protected Void visitReturnVoid(ReturnVoid obj, Void map) {
    return null;
  }

  @Override
  protected Void visitMsgPush(MsgPush obj, Void param) {
    Type func = checkGetExpr(obj.func);
    // TODO implement check
    RError.err(ErrorType.Warning, obj.getInfo(), "Type check for push function not yet implemented");
    return null;
  }

}
