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

package ast.pass.check.type;

import ast.data.Ast;
import ast.data.AstList;
import ast.data.expression.Expression;
import ast.data.expression.reference.Reference;
import ast.data.expression.reference.SimpleRef;
import ast.data.expression.reference.TypeRef;
import ast.data.statement.AssignmentMulti;
import ast.data.statement.AssignmentSingle;
import ast.data.statement.Block;
import ast.data.statement.CallStmt;
import ast.data.statement.CaseOpt;
import ast.data.statement.CaseOptRange;
import ast.data.statement.CaseOptValue;
import ast.data.statement.CaseStmt;
import ast.data.statement.ForStmt;
import ast.data.statement.IfOption;
import ast.data.statement.IfStmt;
import ast.data.statement.MsgPush;
import ast.data.statement.ReturnExpr;
import ast.data.statement.ReturnVoid;
import ast.data.statement.Statement;
import ast.data.statement.VarDefStmt;
import ast.data.statement.WhileStmt;
import ast.data.type.Type;
import ast.data.type.base.BooleanType;
import ast.data.type.base.EnumElement;
import ast.data.type.base.RangeType;
import ast.data.type.base.TupleType;
import ast.data.variable.ConstGlobal;
import ast.data.variable.ConstPrivate;
import ast.data.variable.Constant;
import ast.data.variable.FuncVariable;
import ast.data.variable.StateVariable;
import ast.data.variable.Variable;
import ast.knowledge.KnowBaseItem;
import ast.knowledge.KnowLeftIsContainerOfRight;
import ast.knowledge.KnowType;
import ast.knowledge.KnowledgeBase;
import ast.traverser.NullTraverser;
import error.ErrorType;
import error.RError;

public class StatementTypecheck extends NullTraverser<Void, Void> {
  final private KnowledgeBase kb;
  final private KnowType kt;
  final private KnowBaseItem kbi;
  final private KnowLeftIsContainerOfRight kc;
  final private Type funcReturn;

  public StatementTypecheck(KnowledgeBase kb, Type funcReturn) {
    super();
    this.kb = kb;
    kbi = kb.getEntry(KnowBaseItem.class);
    kt = kb.getEntry(KnowType.class);
    kc = kb.getEntry(KnowLeftIsContainerOfRight.class);
    this.funcReturn = funcReturn;
  }

  public static void process(Statement obj, Type funcReturn, KnowledgeBase kb) {
    StatementTypecheck adder = new StatementTypecheck(kb, funcReturn);
    adder.traverse(obj, null);
  }

  public static void process(Variable obj, KnowledgeBase kb) {
    StatementTypecheck adder = new StatementTypecheck(kb, null);
    adder.traverse(obj, null);
  }

  private Type checkGetExpr(Expression expr) {
    ExpressionTypecheck.process(expr, kb);
    return kt.get(expr);
  }

  @Override
  protected Void visitDefault(Ast obj, Void sym) {
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
    Type ret = kt.get(obj.type);
    Type defType = checkGetExpr(obj.def);
    if (!kc.get(ret, defType)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Data type to big or incompatible in assignment: " + ret.name + " := " + defType.name);
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
    Type cond = kt.get(obj.iterator.type);
    if (!(cond instanceof RangeType)) {
      RError.err(ErrorType.Error, obj.getInfo(), "For loop only supports range type (at the moment), got: " + cond.name);
    }
    visit(obj.block, param);
    return null;
  }

  @Override
  protected Void visitWhileStmt(WhileStmt obj, Void param) {
    Type cond = checkGetExpr(obj.condition);
    if (!(cond instanceof BooleanType)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Need boolean type, got: " + cond.name);
    }
    visit(obj.body, param);
    return null;
  }

  @Override
  protected Void visitIfStmt(IfStmt obj, Void param) {
    for (IfOption opt : obj.option) {
      Type cond = checkGetExpr(opt.condition);
      if (!(cond instanceof BooleanType)) {
        RError.err(ErrorType.Error, opt.getInfo(), "Need boolean type, got: " + cond.name);
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
      RError.err(ErrorType.Error, obj.getInfo(), "Condition variable has to be an integer, got: " + cond.name);
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
      RError.err(ErrorType.Error, obj.getInfo(), "Case value has to be an integer (start), got: " + start.name);
    }
    if (!kc.get(kbi.getIntegerType(), end)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Case value has to be an integer (end), got: " + end.name);
    }
    return null;
  }

  @Override
  protected Void visitCaseOptValue(CaseOptValue obj, Void map) {
    Type value = checkGetExpr(obj.value);
    if (!kc.get(kbi.getIntegerType(), value)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Case value has to be an integer, got: " + value.name);
    }
    return null;
  }

  @Override
  protected Void visitCallStmt(CallStmt obj, Void map) {
    ExpressionTypecheck.process(obj.call, kb);
    return null;
  }

  @Override
  protected Void visitAssignmentSingle(AssignmentSingle obj, Void param) {
    Type lhs = checkGetExpr(obj.left);
    Type rhs = checkGetExpr(obj.right);
    if (!kc.get(lhs, rhs)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Data type to big or incompatible in assignment: " + lhs.name + " := " + rhs.name);
    }
    return null;
  }

  @Override
  protected Void visitAssignmentMulti(AssignmentMulti obj, Void param) {
    AstList<Type> ll = new AstList<Type>();
    for (Reference ref : obj.left) {
      ll.add(checkGetExpr(ref));
    }
    Type lhs;
    if (ll.size() == 1) {
      lhs = ll.get(0);
    } else {
      AstList<TypeRef> tl = new AstList<TypeRef>();
      for (Type lt : ll) {
        tl.add(new SimpleRef<Type>(lt.getInfo(), lt));
      }
      lhs = new TupleType(obj.getInfo(), "", tl);
    }
    Type rhs = checkGetExpr(obj.right);
    if (!kc.get(lhs, rhs)) {
      RError.err(ErrorType.Error, obj.getInfo(), "Data type to big or incompatible in assignment: " + lhs.name + " := " + rhs.name);
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
      RError.err(ErrorType.Error, obj.getInfo(), "Data type to big or incompatible to return: " + funcReturn.name + " := " + ret.name);
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
