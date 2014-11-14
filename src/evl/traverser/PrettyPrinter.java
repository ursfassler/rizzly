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

package evl.traverser;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

import util.StreamWriter;

import common.Direction;
import common.Property;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.composition.Connection;
import evl.composition.EndpointSelf;
import evl.composition.EndpointSub;
import evl.composition.ImplComposition;
import evl.expression.AnyValue;
import evl.expression.ArrayValue;
import evl.expression.BoolValue;
import evl.expression.ExprList;
import evl.expression.NamedElementValue;
import evl.expression.Number;
import evl.expression.RangeValue;
import evl.expression.RecordValue;
import evl.expression.StringValue;
import evl.expression.TypeCast;
import evl.expression.UnionValue;
import evl.expression.UnsafeUnionValue;
import evl.expression.binop.And;
import evl.expression.binop.BinaryExp;
import evl.expression.binop.Div;
import evl.expression.binop.Equal;
import evl.expression.binop.Greater;
import evl.expression.binop.Greaterequal;
import evl.expression.binop.Less;
import evl.expression.binop.Lessequal;
import evl.expression.binop.Minus;
import evl.expression.binop.Mod;
import evl.expression.binop.Mul;
import evl.expression.binop.Notequal;
import evl.expression.binop.Or;
import evl.expression.binop.Plus;
import evl.expression.binop.Shl;
import evl.expression.binop.Shr;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefIndex;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.expression.reference.SimpleRef;
import evl.expression.unop.BitNot;
import evl.expression.unop.LogicNot;
import evl.expression.unop.Not;
import evl.expression.unop.Uminus;
import evl.function.Function;
import evl.function.header.FuncCtrlInDataIn;
import evl.function.header.FuncCtrlInDataOut;
import evl.function.header.FuncCtrlOutDataIn;
import evl.function.header.FuncCtrlOutDataOut;
import evl.function.header.FuncGlobal;
import evl.function.header.FuncPrivateRet;
import evl.function.header.FuncPrivateVoid;
import evl.function.header.FuncSubHandlerEvent;
import evl.function.header.FuncSubHandlerQuery;
import evl.hfsm.ImplHfsm;
import evl.hfsm.State;
import evl.hfsm.StateComposite;
import evl.hfsm.StateSimple;
import evl.hfsm.Transition;
import evl.other.CompUse;
import evl.other.Component;
import evl.other.ImplElementary;
import evl.other.Named;
import evl.other.Namespace;
import evl.other.Queue;
import evl.other.RizzlyProgram;
import evl.other.SubCallbacks;
import evl.statement.Assignment;
import evl.statement.Block;
import evl.statement.CallStmt;
import evl.statement.CaseOpt;
import evl.statement.CaseOptRange;
import evl.statement.CaseOptValue;
import evl.statement.CaseStmt;
import evl.statement.IfOption;
import evl.statement.IfStmt;
import evl.statement.ReturnExpr;
import evl.statement.ReturnVoid;
import evl.statement.VarDefStmt;
import evl.statement.WhileStmt;
import evl.statement.intern.MsgPush;
import evl.type.base.ArrayType;
import evl.type.base.BooleanType;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.type.base.RangeType;
import evl.type.base.StringType;
import evl.type.composed.NamedElement;
import evl.type.composed.RecordType;
import evl.type.composed.UnionType;
import evl.type.composed.UnsafeUnionType;
import evl.type.special.AnyType;
import evl.type.special.IntegerType;
import evl.type.special.NaturalType;
import evl.type.special.VoidType;
import evl.variable.Constant;
import evl.variable.DefVariable;
import evl.variable.FuncVariable;
import evl.variable.StateVariable;
import evl.variable.Variable;

public class PrettyPrinter extends NullTraverser<Void, StreamWriter> {

  final private boolean writeId;

  public PrettyPrinter(boolean writeId) {
    super();
    this.writeId = writeId;
  }

  public static void print(Evl ast, String filename, boolean writeId) {
    PrettyPrinter pp = new PrettyPrinter(writeId);
    try {
      pp.traverse(ast, new StreamWriter(new PrintStream(filename)));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected Void visitDefault(Evl obj, StreamWriter param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getSimpleName());
  }

  private void visitList(List<? extends Evl> list, StreamWriter param) {
    for (int i = 0; i < list.size(); i++) {
      visit(list.get(i), param);
    }
  }

  private void visitListNl(List<? extends Evl> list, StreamWriter param) {
    for (int i = 0; i < list.size(); i++) {
      visit(list.get(i), param);
      param.nl();
    }
  }

  private void list(Iterable<? extends Evl> list, String sep, StreamWriter param) {
    Iterator<? extends Evl> itr = list.iterator();
    boolean first = true;
    while (itr.hasNext()) {
      if (first) {
        first = false;
      } else {
        param.wr(sep);
      }
      visit(itr.next(), param);
    }
  }

  private <T extends Evl> void visitList(Iterable<? extends Evl> list, StreamWriter param) {
    Iterator<? extends Evl> itr = list.iterator();
    while (itr.hasNext()) {
      visit(itr.next(), param);
    }
  }

  private void wrId(Evl obj, StreamWriter wr) {
    if (writeId) {
      wr.wr("[" + obj.hashCode() % 10000 + "]");
    }
  }

  private void name(Named obj, StreamWriter param) {
    param.wr(obj.getName());
  }

  @Override
  protected Void visitRizzlyProgram(RizzlyProgram obj, StreamWriter param) {
    param.nl();
    visitListNl(obj.getType(), param);
    visitListNl(obj.getConstant(), param);
    visitListNl(obj.getVariable(), param);
    param.nl();
    visitList(obj.getFunction(), param);
    param.nl();
    param.nl();
    param.wr(" // ------------------------------------ ");
    param.nl();
    param.nl();
    return null;
  }

  protected void visitOptList(String name, Iterable<? extends Evl> items, StreamWriter param) {
    if (!items.iterator().hasNext()) {
      return;
    }
    param.wr(name);
    param.nl();
    param.incIndent();
    visitList(items, param);
    param.decIndent();
  }

  @Override
  protected Void visitNamespace(Namespace obj, StreamWriter param) {
    param.wr("namespace '");
    name(obj, param);
    param.wr("'");
    wrId(obj, param);
    param.nl();
    param.incIndent();
    visitListNl(obj.getItems(), param);
    visitList(obj.getSpaces(), param);
    param.decIndent();
    param.wr("end");
    param.nl();
    param.nl();
    return null;
  }

  @Override
  protected Void visitSubCallbacks(SubCallbacks obj, StreamWriter param) {
    param.wr("subcallback ");
    name(obj.getCompUse().getLink(), param);
    wrId(obj, param);
    param.nl();
    param.incIndent();
    visitList(obj.getFunc(), param);
    param.decIndent();
    param.wr("end");
    param.nl();
    param.nl();
    return null;
  }

  @Override
  protected Void visitCompUse(CompUse obj, StreamWriter param) {
    name(obj, param);
    wrId(obj, param);
    param.wr(": ");
    wrId(obj.getLink(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitComponent(Component obj, StreamWriter param) {
    super.visitComponent(obj, param);

    visitListNl(obj.getIface(), param);
    visitList(obj.getFunction(), param);

    param.nl();
    param.wr("queue ");
    visit(obj.getQueue(), param);
    param.nl();

    param.decIndent();
    param.wr("end");
    param.nl();
    param.nl();
    return null;
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, StreamWriter param) {
    param.wr("elementary");
    wrId(obj, param);
    param.nl();
    param.nl();
    param.incIndent();

    visitListNl(obj.getComponent(), param);
    visitListNl(obj.getConstant(), param);
    visitListNl(obj.getVariable(), param);

    param.wr("entry: ");
    visit(obj.getEntryFunc(), param);
    param.wr(";");
    param.nl();
    param.wr("exit: ");
    visit(obj.getExitFunc(), param);
    param.wr(";");
    param.nl();
    param.nl();

    visitList(obj.getSubCallback(), param);
    return null;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, StreamWriter param) {
    param.wr("implementation composition");
    param.nl();
    param.nl();
    param.incIndent();

    visitOptList("component", obj.getComponent(), param);
    visitOptList("connection", obj.getConnection(), param);

    return null;
  }

  @Override
  protected Void visitConnection(Connection obj, StreamWriter param) {
    visit(obj.getEndpoint(Direction.in), param);
    switch (obj.getType()) {
      case sync:
        param.wr(" -> ");
        break;
      case async:
        param.wr(" >> ");
        break;
      default:
        RError.err(ErrorType.Fatal, obj.getInfo(), "Not yet implemented connection type: " + obj.getType());
    }
    visit(obj.getEndpoint(Direction.out), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitFunctionImpl(Function obj, StreamWriter param) {
    name(obj, param);
    param.wr(" ");
    super.visitFunctionImpl(obj, param);
    wrId(obj, param);
    param.wr("(");
    list(obj.getParam(), "; ", param);
    param.wr(")");
    param.wr(":");
    visit(obj.getRet(), param);
    if (obj.properties().get(Property.Extern) == Boolean.TRUE) {
      param.wr(" extern");
    }
    if (obj.properties().get(Property.Public) == Boolean.TRUE) {
      param.wr(" public");
    }
    param.nl();

    param.incIndent();
    visit(obj.getBody(), param);
    param.decIndent();
    param.wr("end");
    param.nl();
    return null;
  }

  @Override
  protected Void visitFuncIfaceOutVoid(FuncCtrlOutDataOut obj, StreamWriter param) {
    param.wr("signal");
    return null;
  }

  @Override
  protected Void visitFuncIfaceOutRet(FuncCtrlOutDataIn obj, StreamWriter param) {
    param.wr("query");
    return null;
  }

  @Override
  protected Void visitFuncIfaceInVoid(FuncCtrlInDataIn obj, StreamWriter param) {
    param.wr("slot");
    return null;
  }

  @Override
  protected Void visitFuncIfaceInRet(FuncCtrlInDataOut obj, StreamWriter param) {
    param.wr("response");
    return null;
  }

  @Override
  protected Void visitFuncGlobal(FuncGlobal obj, StreamWriter param) {
    param.wr("function");
    return null;
  }

  @Override
  protected Void visitFuncPrivateVoid(FuncPrivateVoid obj, StreamWriter param) {
    param.wr("procedure");
    return null;
  }

  @Override
  protected Void visitFuncPrivateRet(FuncPrivateRet obj, StreamWriter param) {
    param.wr("function");
    return null;
  }

  @Override
  protected Void visitFuncSubHandlerQuery(FuncSubHandlerQuery obj, StreamWriter param) {
    param.wr("subresponse");
    return null;
  }

  @Override
  protected Void visitFuncSubHandlerEvent(FuncSubHandlerEvent obj, StreamWriter param) {
    param.wr("subslot");
    return null;
  }

  // ---- Type ----------------------------------------------------------------
  @Override
  protected Void visitEnumType(EnumType obj, StreamWriter param) {
    param.wr("Enum ");
    param.wr(obj.getName());
    wrId(obj, param);
    param.nl();
    param.incIndent();
    visitList(obj.getElement(), param);
    param.decIndent();
    param.wr("end");
    param.nl();
    param.nl();
    return null;
  }

  @Override
  protected Void visitEnumElement(EnumElement obj, StreamWriter param) {
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitUnionType(UnionType obj, StreamWriter param) {
    param.wr("Union(");
    visit(obj.getTag(), param);
    param.wr(")");
    wrId(obj, param);
    param.nl();
    param.incIndent();
    visitListNl(obj.getElement(), param);
    param.decIndent();
    param.wr("end");
    param.nl();
    param.nl();
    return null;
  }

  @Override
  protected Void visitUnsafeUnionType(UnsafeUnionType obj, StreamWriter param) {
    param.wr("UnsafeUnion");
    wrId(obj, param);
    param.nl();
    param.incIndent();
    visitListNl(obj.getElement(), param);
    param.decIndent();
    param.wr("end");
    param.nl();
    param.nl();
    return null;
  }

  @Override
  protected Void visitRecordType(RecordType obj, StreamWriter param) {
    param.wr("Record");
    wrId(obj, param);
    param.nl();
    param.incIndent();
    visitListNl(obj.getElement(), param);
    param.decIndent();
    param.wr("end");
    param.nl();
    param.nl();
    return null;
  }

  @Override
  protected Void visitNamedElement(NamedElement obj, StreamWriter param) {
    param.wr(obj.getName());
    param.wr(": ");
    visit(obj.getRef(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitBooleanType(BooleanType obj, StreamWriter param) {
    param.wr("False..True");
    wrId(obj, param);
    param.nl();
    return null;
  }

  @Override
  protected Void visitRangeValue(RangeValue obj, StreamWriter param) {
    param.wr(obj.toString());
    wrId(obj, param);
    return null;
  }

  @Override
  protected Void visitRangeType(RangeType obj, StreamWriter param) {
    param.wr(obj.getNumbers().getLow().toString());
    param.wr("..");
    param.wr(obj.getNumbers().getHigh().toString());
    wrId(obj, param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitIntegerType(IntegerType obj, StreamWriter param) {
    param.wr("ℤ");
    wrId(obj, param);
    param.nl();
    return null;
  }

  @Override
  protected Void visitNaturalType(NaturalType obj, StreamWriter param) {
    param.wr("ℕ");
    wrId(obj, param);
    param.nl();
    return null;
  }

  @Override
  protected Void visitStringType(StringType obj, StreamWriter param) {
    param.wr("*");
    param.nl();
    return null;
  }

  @Override
  protected Void visitAnyType(AnyType obj, StreamWriter param) {
    param.wr("*");
    wrId(obj, param);
    param.nl();
    return null;
  }

  @Override
  protected Void visitVoidType(VoidType obj, StreamWriter param) {
    param.wr("∅");
    wrId(obj, param);
    param.nl();
    return null;
  }

  @Override
  protected Void visitArrayType(ArrayType obj, StreamWriter param) {
    param.wr("Array{");
    param.wr(obj.getSize().toString());
    param.wr(",");
    visit(obj.getType(), param);
    param.wr("}");
    wrId(obj, param);
    param.wr("\'");
    param.wr(obj.getName());
    param.wr("\'");
    param.nl();
    return null;
  }

  // ---- Statement -----------------------------------------------------------

  @Override
  protected Void visitBlock(Block obj, StreamWriter param) {
    visitList(obj.getStatements(), param);
    return null;
  }

  @Override
  protected Void visitAssignment(Assignment obj, StreamWriter param) {
    visit(obj.getLeft(), param);
    param.wr(" := ");
    visit(obj.getRight(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitVarDef(VarDefStmt obj, StreamWriter param) {
    visit(obj.getVariable(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitCallStmt(CallStmt obj, StreamWriter param) {
    visit(obj.getCall(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitReturnExpr(ReturnExpr obj, StreamWriter param) {
    param.wr("return ");
    visit(obj.getExpr(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitReturnVoid(ReturnVoid obj, StreamWriter param) {
    param.wr("return;");
    param.nl();
    return null;
  }

  @Override
  protected Void visitWhileStmt(WhileStmt obj, StreamWriter param) {
    param.wr("while ");
    visit(obj.getCondition(), param);
    param.wr(" do");
    param.nl();
    param.incIndent();
    visit(obj.getBody(), param);
    param.decIndent();
    param.wr("end");
    param.nl();
    return null;
  }

  @Override
  protected Void visitIfStmt(IfStmt obj, StreamWriter param) {
    assert (!obj.getOption().isEmpty());

    boolean first = true;
    for (IfOption itr : obj.getOption()) {
      if (first) {
        param.wr("if ");
        first = false;
      } else {
        param.wr("ef ");
      }
      visit(itr.getCondition(), param);
      param.wr(" then");
      param.nl();
      param.incIndent();
      visit(itr.getCode(), param);
      param.decIndent();
    }

    param.wr("else");
    param.nl();
    param.incIndent();
    visit(obj.getDefblock(), param);
    param.decIndent();
    param.wr("end");
    param.nl();

    return null;
  }

  @Override
  protected Void visitCaseStmt(CaseStmt obj, StreamWriter param) {
    param.wr("case ");
    visit(obj.getCondition(), param);
    param.wr(" of");
    param.nl();
    param.incIndent();
    visitList(obj.getOption(), param);
    if (!obj.getOtherwise().getStatements().isEmpty()) {
      param.wr("else");
      param.nl();
      param.incIndent();
      visit(obj.getOtherwise(), param);
      param.decIndent();
      param.wr("end");
      param.nl();
    }
    param.decIndent();
    param.wr("end");
    param.nl();
    return null;
  }

  @Override
  protected Void visitCaseOpt(CaseOpt obj, StreamWriter param) {
    list(obj.getValue(), ",", param);
    param.wr(":");
    param.nl();
    param.incIndent();
    visit(obj.getCode(), param);
    param.decIndent();
    param.wr("end");
    param.nl();
    return null;
  }

  @Override
  protected Void visitCaseOptRange(CaseOptRange obj, StreamWriter param) {
    visit(obj.getStart(), param);
    param.wr("..");
    visit(obj.getEnd(), param);
    return null;
  }

  @Override
  protected Void visitCaseOptValue(CaseOptValue obj, StreamWriter param) {
    visit(obj.getValue(), param);
    return null;
  }

  // ---- Expression ----------------------------------------------------------
  @Override
  protected Void visitBinaryExp(BinaryExp obj, StreamWriter param) {
    param.wr("(");
    visit(obj.getLeft(), param);
    param.wr(" ");
    param.wr(obj.getOpName());
    param.wr(" ");
    visit(obj.getRight(), param);
    param.wr(")");
    return null;
  }

  @Override
  protected Void visitAnyValue(AnyValue obj, StreamWriter param) {
    param.wr("_");
    return null;
  }

  @Override
  protected Void visitBoolValue(BoolValue obj, StreamWriter param) {
    param.wr(obj.isValue() ? "True" : "False");
    return null;
  }

  @Override
  protected Void visitNumber(Number obj, StreamWriter param) {
    param.wr(obj.getValue().toString());
    return null;
  }

  @Override
  protected Void visitStringValue(StringValue obj, StreamWriter param) {
    param.wr("'");
    param.wr(obj.getValue());
    param.wr("'");
    return null;
  }

  @Override
  protected Void visitArrayValue(ArrayValue obj, StreamWriter param) {
    param.wr("[");
    list(obj.getValue(), ", ", param);
    param.wr("]");
    return null;
  }

  @Override
  protected Void visitExprList(ExprList obj, StreamWriter param) {
    param.wr("(");
    list(obj.getValue(), ", ", param);
    param.wr(")");
    return null;
  }

  @Override
  protected Void visitNamedElementValue(NamedElementValue obj, StreamWriter param) {
    param.wr(obj.getName());
    param.wr(" := ");
    visit(obj.getValue(), param);
    return null;
  }

  @Override
  protected Void visitUnsafeUnionValue(UnsafeUnionValue obj, StreamWriter param) {
    param.wr("(");
    visit(obj.getContentValue(), param);
    param.wr(")");
    return null;
  }

  @Override
  protected Void visitUnionValue(UnionValue obj, StreamWriter param) {
    param.wr("(");
    visit(obj.getTagValue(), param);
    param.wr(" := ");
    visit(obj.getContentValue(), param);
    param.wr(")");
    return null;
  }

  @Override
  protected Void visitRecordValue(RecordValue obj, StreamWriter param) {
    param.wr("(");
    list(obj.getValue(), ", ", param);
    param.wr(")");
    return null;
  }

  @Override
  protected Void visitNot(Not obj, StreamWriter param) {
    param.wr("not ");
    visit(obj.getExpr(), param);
    return null;
  }

  @Override
  protected Void visitLogicNot(LogicNot obj, StreamWriter param) {
    param.wr("lnot ");
    visit(obj.getExpr(), param);
    return null;
  }

  @Override
  protected Void visitBitNot(BitNot obj, StreamWriter param) {
    param.wr("bnot ");
    visit(obj.getExpr(), param);
    return null;
  }

  @Override
  protected Void visitUminus(Uminus obj, StreamWriter param) {
    param.wr("- ");
    visit(obj.getExpr(), param);
    return null;
  }

  private void visitBinop(String op, BinaryExp obj, StreamWriter param) {
    param.wr("(");
    visit(obj.getLeft(), param);
    param.wr(" ");
    param.wr(op);
    param.wr(" ");
    visit(obj.getRight(), param);
    param.wr(")");
  }

  @Override
  protected Void visitAnd(And obj, StreamWriter param) {
    visitBinop("and", obj, param);
    return null;
  }

  @Override
  protected Void visitDiv(Div obj, StreamWriter param) {
    visitBinop("/", obj, param);
    return null;
  }

  @Override
  protected Void visitEqual(Equal obj, StreamWriter param) {
    visitBinop("=", obj, param);
    return null;
  }

  @Override
  protected Void visitGreater(Greater obj, StreamWriter param) {
    visitBinop(">", obj, param);
    return null;
  }

  @Override
  protected Void visitGreaterequal(Greaterequal obj, StreamWriter param) {
    visitBinop(">=", obj, param);
    return null;
  }

  @Override
  protected Void visitLess(Less obj, StreamWriter param) {
    visitBinop("<", obj, param);
    return null;
  }

  @Override
  protected Void visitLessequal(Lessequal obj, StreamWriter param) {
    visitBinop("<=", obj, param);
    return null;
  }

  @Override
  protected Void visitMinus(Minus obj, StreamWriter param) {
    visitBinop("-", obj, param);
    return null;
  }

  @Override
  protected Void visitMod(Mod obj, StreamWriter param) {
    visitBinop("mod", obj, param);
    return null;
  }

  @Override
  protected Void visitMul(Mul obj, StreamWriter param) {
    visitBinop("*", obj, param);
    return null;
  }

  @Override
  protected Void visitNotequal(Notequal obj, StreamWriter param) {
    visitBinop("<>", obj, param);
    return null;
  }

  @Override
  protected Void visitOr(Or obj, StreamWriter param) {
    visitBinop("or", obj, param);
    return null;
  }

  @Override
  protected Void visitPlus(Plus obj, StreamWriter param) {
    visitBinop("+", obj, param);
    return null;
  }

  @Override
  protected Void visitShl(Shl obj, StreamWriter param) {
    visitBinop("shl", obj, param);
    return null;
  }

  @Override
  protected Void visitShr(Shr obj, StreamWriter param) {
    visitBinop("shr", obj, param);
    return null;
  }

  @Override
  protected Void visitVariable(Variable obj, StreamWriter param) {
    name(obj, param);
    wrId(obj, param);
    param.wr(": ");
    visit(obj.getType(), param);
    super.visitVariable(obj, param);
    return null;
  }

  @Override
  protected Void visitDefVariable(DefVariable obj, StreamWriter param) {
    param.wr(" = ");
    visit(obj.getDef(), param);
    super.visitDefVariable(obj, param);
    return null;
  }

  @Override
  protected Void visitFuncVariable(FuncVariable obj, StreamWriter param) {
    return null;
  }

  @Override
  protected Void visitStateVariable(StateVariable obj, StreamWriter param) {
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitConstant(Constant obj, StreamWriter param) {
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitTypeRef(SimpleRef obj, StreamWriter param) {
    name(obj.getLink(), param);
    wrId(obj.getLink(), param);
    return null;
  }

  @Override
  protected Void visitReference(Reference obj, StreamWriter param) {
    name(obj.getLink(), param);
    wrId(obj.getLink(), param);
    visitList(obj.getOffset(), param);
    return null;
  }

  @Override
  protected Void visitRefName(RefName obj, StreamWriter param) {
    param.wr(".");
    param.wr(obj.getName());
    return null;
  }

  @Override
  protected Void visitRefCall(RefCall obj, StreamWriter param) {
    param.wr("(");
    list(obj.getActualParameter(), ", ", param);
    param.wr(")");
    return null;
  }

  @Override
  protected Void visitRefIndex(RefIndex obj, StreamWriter param) {
    param.wr("[");
    visit(obj.getIndex(), param);
    param.wr("]");
    return null;
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, StreamWriter param) {
    param.wr("implementation hfsm");
    param.nl();
    param.nl();

    visit(obj.getTopstate(), param);

    return null;
  }

  @Override
  protected Void visitStateSimple(StateSimple obj, StreamWriter param) {
    name(obj, param);
    wrId(obj, param);
    param.wr(" : state ");
    param.nl();
    param.incIndent();
    printStateContent(obj, param);
    param.decIndent();
    param.wr("end");
    param.nl();
    return null;
  }

  @Override
  protected Void visitStateComposite(StateComposite obj, StreamWriter param) {
    name(obj, param);
    wrId(obj, param);
    param.wr(" : state ");
    param.wr("(");
    name(obj.getInitial().getLink(), param);
    wrId(obj.getInitial().getLink(), param);
    param.wr(")");
    param.nl();
    param.incIndent();
    printStateContent(obj, param);
    param.decIndent();
    param.wr("end");
    param.nl();
    return null;
  }

  protected void printStateContent(State obj, StreamWriter param) {
    param.wr("entry ");
    visit(obj.getEntryFunc(), param);
    param.wr(";");
    param.nl();
    param.wr("entry ");
    visit(obj.getExitFunc(), param);
    param.wr(";");
    param.nl();
    param.nl();

    visitListNl(obj.getItem(), param);
  }

  @Override
  protected Void visitTransition(Transition obj, StreamWriter param) {
    name(obj.getSrc().getLink(), param);
    wrId(obj.getSrc().getLink(), param);
    param.wr(" to ");
    name(obj.getDst().getLink(), param);
    wrId(obj.getDst().getLink(), param);
    param.wr(" by ");
    visit(obj.getEventFunc(), param);
    param.wr("(");
    list(obj.getParam(), "; ", param);
    param.wr(")");
    param.wr(" if ");
    visit(obj.getGuard(), param);
    param.wr(" do");
    param.nl();
    param.incIndent();
    visit(obj.getBody(), param);
    param.decIndent();
    param.wr("end");
    param.nl();
    param.nl();

    return null;
  }

  @Override
  protected Void visitEndpointSelf(EndpointSelf obj, StreamWriter param) {
    name(obj.getLink(), param);
    wrId(obj.getLink(), param);
    return null;
  }

  @Override
  protected Void visitEndpointSub(EndpointSub obj, StreamWriter param) {
    name(obj.getLink(), param);
    wrId(obj.getLink(), param);
    param.wr(".");
    param.wr(obj.getFunction());
    return null;
  }

  @Override
  protected Void visitTypeCast(TypeCast obj, StreamWriter param) {
    visit(obj.getCast(), param);
    param.wr("(");
    visit(obj.getValue(), param);
    param.wr(")");
    return null;
  }

  @Override
  protected Void visitMsgPush(MsgPush obj, StreamWriter param) {
    param.wr("!push(");
    visit(obj.getQueue(), param);
    param.wr(",");
    visit(obj.getFunc(), param);
    param.wr(",[");
    list(obj.getData(), ",", param);
    param.wr("]);");
    param.nl();
    return null;
  }

  @Override
  protected Void visitQueue(Queue obj, StreamWriter param) {
    wrId(obj, param);
    param.nl();
    return null;
  }

}
