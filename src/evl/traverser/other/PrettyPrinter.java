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

package evl.traverser.other;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

import util.StreamWriter;

import common.Direction;
import common.Property;

import error.ErrorType;
import error.RError;
import evl.data.Evl;
import evl.data.Named;
import evl.data.Namespace;
import evl.data.component.Component;
import evl.data.component.composition.CompUse;
import evl.data.component.composition.Connection;
import evl.data.component.composition.EndpointSelf;
import evl.data.component.composition.EndpointSub;
import evl.data.component.composition.ImplComposition;
import evl.data.component.composition.Queue;
import evl.data.component.composition.SubCallbacks;
import evl.data.component.elementary.ImplElementary;
import evl.data.component.hfsm.ImplHfsm;
import evl.data.component.hfsm.State;
import evl.data.component.hfsm.StateComposite;
import evl.data.component.hfsm.StateSimple;
import evl.data.component.hfsm.Transition;
import evl.data.expression.AnyValue;
import evl.data.expression.ArrayValue;
import evl.data.expression.BoolValue;
import evl.data.expression.NamedElementsValue;
import evl.data.expression.NamedValue;
import evl.data.expression.Number;
import evl.data.expression.RecordValue;
import evl.data.expression.StringValue;
import evl.data.expression.TupleValue;
import evl.data.expression.TypeCast;
import evl.data.expression.UnionValue;
import evl.data.expression.UnsafeUnionValue;
import evl.data.expression.binop.And;
import evl.data.expression.binop.BinaryExp;
import evl.data.expression.binop.Div;
import evl.data.expression.binop.Equal;
import evl.data.expression.binop.Greater;
import evl.data.expression.binop.Greaterequal;
import evl.data.expression.binop.Less;
import evl.data.expression.binop.Lessequal;
import evl.data.expression.binop.Minus;
import evl.data.expression.binop.Mod;
import evl.data.expression.binop.Mul;
import evl.data.expression.binop.Notequal;
import evl.data.expression.binop.Or;
import evl.data.expression.binop.Plus;
import evl.data.expression.binop.Shl;
import evl.data.expression.binop.Shr;
import evl.data.expression.reference.RefCall;
import evl.data.expression.reference.RefIndex;
import evl.data.expression.reference.RefName;
import evl.data.expression.reference.Reference;
import evl.data.expression.reference.SimpleRef;
import evl.data.expression.unop.BitNot;
import evl.data.expression.unop.LogicNot;
import evl.data.expression.unop.Not;
import evl.data.expression.unop.Uminus;
import evl.data.function.Function;
import evl.data.function.header.FuncCtrlInDataIn;
import evl.data.function.header.FuncCtrlInDataOut;
import evl.data.function.header.FuncCtrlOutDataIn;
import evl.data.function.header.FuncCtrlOutDataOut;
import evl.data.function.header.FuncGlobal;
import evl.data.function.header.FuncPrivateRet;
import evl.data.function.header.FuncPrivateVoid;
import evl.data.function.header.FuncSubHandlerEvent;
import evl.data.function.header.FuncSubHandlerQuery;
import evl.data.function.ret.FuncReturnNone;
import evl.data.function.ret.FuncReturnTuple;
import evl.data.function.ret.FuncReturnType;
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
import evl.data.statement.VarDefStmt;
import evl.data.statement.WhileStmt;
import evl.data.statement.intern.MsgPush;
import evl.data.type.base.ArrayType;
import evl.data.type.base.BooleanType;
import evl.data.type.base.EnumElement;
import evl.data.type.base.EnumType;
import evl.data.type.base.RangeType;
import evl.data.type.base.StringType;
import evl.data.type.composed.NamedElement;
import evl.data.type.composed.RecordType;
import evl.data.type.composed.UnionType;
import evl.data.type.composed.UnsafeUnionType;
import evl.data.type.out.AliasType;
import evl.data.type.out.SIntType;
import evl.data.type.out.UIntType;
import evl.data.type.special.AnyType;
import evl.data.type.special.IntegerType;
import evl.data.type.special.NaturalType;
import evl.data.type.special.VoidType;
import evl.data.variable.Constant;
import evl.data.variable.DefVariable;
import evl.data.variable.FuncVariable;
import evl.data.variable.StateVariable;
import evl.data.variable.Variable;
import evl.traverser.NullTraverser;

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
    param.wr(obj.name);
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
    name(obj.compUse.link, param);
    wrId(obj, param);
    param.nl();
    param.incIndent();
    visitList(obj.func, param);
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
    wrId(obj.instance, param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitComponent(Component obj, StreamWriter param) {
    super.visitComponent(obj, param);

    visitListNl(obj.iface, param);
    visitList(obj.function, param);

    param.nl();
    param.wr("queue ");
    visit(obj.queue, param);
    param.nl();

    param.decIndent();
    param.wr("end");
    param.nl();
    param.nl();
    return null;
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, StreamWriter param) {
    param.wr("elementary ");
    name(obj, param);
    wrId(obj, param);
    param.nl();
    param.nl();
    param.incIndent();

    visitListNl(obj.component, param);
    visitListNl(obj.type, param);
    visitListNl(obj.constant, param);
    visitListNl(obj.variable, param);

    param.wr("entry: ");
    visit(obj.entryFunc, param);
    param.wr(";");
    param.nl();
    param.wr("exit: ");
    visit(obj.exitFunc, param);
    param.wr(";");
    param.nl();
    param.nl();

    visitList(obj.subCallback, param);
    return null;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, StreamWriter param) {
    param.wr("implementation composition ");
    name(obj, param);
    wrId(obj, param);
    param.nl();
    param.nl();
    param.incIndent();

    visitOptList("component", obj.component, param);
    visitOptList("connection", obj.connection, param);

    return null;
  }

  @Override
  protected Void visitConnection(Connection obj, StreamWriter param) {
    visit(obj.endpoint.get(Direction.in), param);
    switch (obj.type) {
      case sync:
        param.wr(" -> ");
        break;
      case async:
        param.wr(" >> ");
        break;
      default:
        RError.err(ErrorType.Fatal, obj.getInfo(), "Not yet implemented connection type: " + obj.type);
    }
    visit(obj.endpoint.get(Direction.out), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitFuncReturnTuple(FuncReturnTuple obj, StreamWriter param) {
    param.wr(":");
    param.wr("(");
    list(obj.param, "; ", param);
    param.wr(")");
    return null;
  }

  @Override
  protected Void visitFuncReturnType(FuncReturnType obj, StreamWriter param) {
    param.wr(":");
    visit(obj.type, param);
    return null;
  }

  @Override
  protected Void visitFuncReturnNone(FuncReturnNone obj, StreamWriter param) {
    return null;
  }

  @Override
  protected Void visitFunction(Function obj, StreamWriter param) {
    name(obj, param);
    param.wr(" ");
    super.visitFunction(obj, param);
    wrId(obj, param);
    param.wr("(");
    list(obj.param, "; ", param);
    param.wr(")");
    visit(obj.ret, param);
    if (obj.properties().get(Property.Extern) == Boolean.TRUE) {
      param.wr(" extern");
    }
    if (obj.properties().get(Property.Public) == Boolean.TRUE) {
      param.wr(" public");
    }
    param.nl();

    param.incIndent();
    visit(obj.body, param);
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
    param.wr(obj.name);
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
    param.wr(obj.name);
    wrId(obj, param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitUnionType(UnionType obj, StreamWriter param) {
    param.wr("Union ");
    param.wr(obj.name);
    param.wr("(");
    visit(obj.tag, param);
    param.wr(")");
    wrId(obj, param);
    param.nl();
    param.incIndent();
    visitListNl(obj.element, param);
    param.decIndent();
    param.wr("end");
    param.nl();
    param.nl();
    return null;
  }

  @Override
  protected Void visitUnsafeUnionType(UnsafeUnionType obj, StreamWriter param) {
    param.wr("UnsafeUnion ");
    param.wr(obj.name);
    wrId(obj, param);
    param.nl();
    param.incIndent();
    visitListNl(obj.element, param);
    param.decIndent();
    param.wr("end");
    param.nl();
    param.nl();
    return null;
  }

  @Override
  protected Void visitRecordType(RecordType obj, StreamWriter param) {
    param.wr("Record ");
    param.wr(obj.name);
    wrId(obj, param);
    param.nl();
    param.incIndent();
    visitListNl(obj.element, param);
    param.decIndent();
    param.wr("end");
    param.nl();
    param.nl();
    return null;
  }

  @Override
  protected Void visitNamedElement(NamedElement obj, StreamWriter param) {
    param.wr(obj.name);
    param.wr(": ");
    visit(obj.ref, param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitAliasType(AliasType obj, StreamWriter param) {
    param.wr(obj.name);
    wrId(obj, param);
    param.wr(" = ");
    visit(obj.ref, param);
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
  protected Void visitRangeType(RangeType obj, StreamWriter param) {
    param.wr(obj.range.getLow().toString());
    param.wr("..");
    param.wr(obj.range.getHigh().toString());
    wrId(obj, param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitSIntType(SIntType obj, StreamWriter param) {
    param.wr("sint" + obj.bytes * 8);
    wrId(obj, param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitUIntType(UIntType obj, StreamWriter param) {
    param.wr("uint" + obj.bytes * 8);
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
    param.wr(obj.size.toString());
    param.wr(",");
    visit(obj.type, param);
    param.wr("}");
    wrId(obj, param);
    param.wr("\'");
    param.wr(obj.name);
    param.wr("\'");
    param.nl();
    return null;
  }

  // ---- Statement -----------------------------------------------------------

  @Override
  protected Void visitBlock(Block obj, StreamWriter param) {
    visitList(obj.statements, param);
    return null;
  }

  @Override
  protected Void visitAssignmentMulti(AssignmentMulti obj, StreamWriter param) {
    list(obj.left, ", ", param);
    param.wr(" := ");
    visit(obj.right, param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitAssignmentSingle(AssignmentSingle obj, StreamWriter param) {
    visit(obj.left, param);
    param.wr(" := ");
    visit(obj.right, param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitVarDef(VarDefStmt obj, StreamWriter param) {
    visit(obj.variable, param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitCallStmt(CallStmt obj, StreamWriter param) {
    visit(obj.call, param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitReturnExpr(ReturnExpr obj, StreamWriter param) {
    param.wr("return ");
    visit(obj.expr, param);
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
    visit(obj.condition, param);
    param.wr(" do");
    param.nl();
    param.incIndent();
    visit(obj.body, param);
    param.decIndent();
    param.wr("end");
    param.nl();
    return null;
  }

  @Override
  protected Void visitForStmt(ForStmt obj, StreamWriter param) {
    param.wr("for ");
    param.wr(obj.iterator.name);
    wrId(obj.iterator, param);
    param.wr(" in ");
    visit(obj.iterator.type, param);
    param.wr(" do");
    param.nl();
    param.incIndent();
    visit(obj.block, param);
    param.decIndent();
    param.wr("end");
    param.nl();
    return null;
  }

  @Override
  protected Void visitIfStmt(IfStmt obj, StreamWriter param) {
    assert (!obj.option.isEmpty());

    boolean first = true;
    for (IfOption itr : obj.option) {
      if (first) {
        param.wr("if ");
        first = false;
      } else {
        param.wr("ef ");
      }
      visit(itr.condition, param);
      param.wr(" then");
      param.nl();
      param.incIndent();
      visit(itr.code, param);
      param.decIndent();
    }

    param.wr("else");
    param.nl();
    param.incIndent();
    visit(obj.defblock, param);
    param.decIndent();
    param.wr("end");
    param.nl();

    return null;
  }

  @Override
  protected Void visitCaseStmt(CaseStmt obj, StreamWriter param) {
    param.wr("case ");
    visit(obj.condition, param);
    param.wr(" of");
    param.nl();
    param.incIndent();
    visitList(obj.option, param);
    if (!obj.otherwise.statements.isEmpty()) {
      param.wr("else");
      param.nl();
      param.incIndent();
      visit(obj.otherwise, param);
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
    list(obj.value, ",", param);
    param.wr(":");
    param.nl();
    param.incIndent();
    visit(obj.code, param);
    param.decIndent();
    param.wr("end");
    param.nl();
    return null;
  }

  @Override
  protected Void visitCaseOptRange(CaseOptRange obj, StreamWriter param) {
    visit(obj.start, param);
    param.wr("..");
    visit(obj.end, param);
    return null;
  }

  @Override
  protected Void visitCaseOptValue(CaseOptValue obj, StreamWriter param) {
    visit(obj.value, param);
    return null;
  }

  // ---- Expression ----------------------------------------------------------
  @Override
  protected Void visitBinaryExp(BinaryExp obj, StreamWriter param) {
    param.wr("(");
    visit(obj.left, param);
    param.wr(" ");
    param.wr(obj.getOpName());
    param.wr(" ");
    visit(obj.right, param);
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
    param.wr(obj.value ? "True" : "False");
    return null;
  }

  @Override
  protected Void visitNumber(Number obj, StreamWriter param) {
    param.wr(obj.value.toString());
    return null;
  }

  @Override
  protected Void visitStringValue(StringValue obj, StreamWriter param) {
    param.wr("'");
    param.wr(obj.value);
    param.wr("'");
    return null;
  }

  @Override
  protected Void visitArrayValue(ArrayValue obj, StreamWriter param) {
    param.wr("Array(");
    list(obj.value, ", ", param);
    param.wr(")");
    return null;
  }

  @Override
  protected Void visitNamedElementsValue(NamedElementsValue obj, StreamWriter param) {
    param.wr("[");
    list(obj.value, ", ", param);
    param.wr("]");
    return null;
  }

  @Override
  protected Void visitTupleValue(TupleValue obj, StreamWriter param) {
    param.wr("(");
    list(obj.value, ", ", param);
    param.wr(")");
    return null;
  }

  @Override
  protected Void visitNamedValue(NamedValue obj, StreamWriter param) {
    param.wr(obj.name);
    param.wr(" := ");
    visit(obj.value, param);
    return null;
  }

  @Override
  protected Void visitUnsafeUnionValue(UnsafeUnionValue obj, StreamWriter param) {
    param.wr("(");
    visit(obj.contentValue, param);
    param.wr(")");
    return null;
  }

  @Override
  protected Void visitUnionValue(UnionValue obj, StreamWriter param) {
    param.wr("(");
    visit(obj.tagValue, param);
    param.wr(" := ");
    visit(obj.contentValue, param);
    param.wr(")");
    return null;
  }

  @Override
  protected Void visitRecordValue(RecordValue obj, StreamWriter param) {
    visit(obj.type, param);
    param.wr("[");
    list(obj.value, ", ", param);
    param.wr("]");
    return null;
  }

  @Override
  protected Void visitNot(Not obj, StreamWriter param) {
    param.wr("not ");
    visit(obj.expr, param);
    return null;
  }

  @Override
  protected Void visitLogicNot(LogicNot obj, StreamWriter param) {
    param.wr("lnot ");
    visit(obj.expr, param);
    return null;
  }

  @Override
  protected Void visitBitNot(BitNot obj, StreamWriter param) {
    param.wr("bnot ");
    visit(obj.expr, param);
    return null;
  }

  @Override
  protected Void visitUminus(Uminus obj, StreamWriter param) {
    param.wr("- ");
    visit(obj.expr, param);
    return null;
  }

  private void visitBinop(String op, BinaryExp obj, StreamWriter param) {
    param.wr("(");
    visit(obj.left, param);
    param.wr(" ");
    param.wr(op);
    param.wr(" ");
    visit(obj.right, param);
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
    visit(obj.type, param);
    super.visitVariable(obj, param);
    return null;
  }

  @Override
  protected Void visitDefVariable(DefVariable obj, StreamWriter param) {
    param.wr(" = ");
    visit(obj.def, param);
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
  protected Void visitSimpleRef(SimpleRef obj, StreamWriter param) {
    name(obj.link, param);
    wrId(obj.link, param);
    return null;
  }

  @Override
  protected Void visitReference(Reference obj, StreamWriter param) {
    name(obj.link, param);
    wrId(obj.link, param);
    visitList(obj.offset, param);
    return null;
  }

  @Override
  protected Void visitRefName(RefName obj, StreamWriter param) {
    param.wr(".");
    param.wr(obj.name);
    return null;
  }

  @Override
  protected Void visitRefCall(RefCall obj, StreamWriter param) {
    visit(obj.actualParameter, param);
    return null;
  }

  @Override
  protected Void visitRefIndex(RefIndex obj, StreamWriter param) {
    param.wr("[");
    visit(obj.index, param);
    param.wr("]");
    return null;
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, StreamWriter param) {
    param.wr("implementation hfsm ");
    name(obj, param);
    wrId(obj, param);
    param.nl();
    param.nl();

    visit(obj.topstate, param);

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
    name(obj.initial.link, param);
    wrId(obj.initial.link, param);
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
    visit(obj.entryFunc, param);
    param.wr(";");
    param.nl();
    param.wr("entry ");
    visit(obj.exitFunc, param);
    param.wr(";");
    param.nl();
    param.nl();

    visitListNl(obj.item, param);
  }

  @Override
  protected Void visitTransition(Transition obj, StreamWriter param) {
    name(obj.src.link, param);
    wrId(obj.src.link, param);
    param.wr(" to ");
    name(obj.dst.link, param);
    wrId(obj.dst.link, param);
    param.wr(" by ");
    visit(obj.eventFunc, param);
    param.wr("(");
    list(obj.param, "; ", param);
    param.wr(")");
    param.wr(" if ");
    visit(obj.guard, param);
    param.wr(" do");
    param.nl();
    param.incIndent();
    visit(obj.body, param);
    param.decIndent();
    param.wr("end");
    param.nl();
    param.nl();

    return null;
  }

  @Override
  protected Void visitEndpointSelf(EndpointSelf obj, StreamWriter param) {
    name(obj.link, param);
    wrId(obj.link, param);
    return null;
  }

  @Override
  protected Void visitEndpointSub(EndpointSub obj, StreamWriter param) {
    name(obj.link, param);
    wrId(obj.link, param);
    param.wr(".");
    param.wr(obj.function);
    return null;
  }

  @Override
  protected Void visitTypeCast(TypeCast obj, StreamWriter param) {
    visit(obj.cast, param);
    param.wr("(");
    visit(obj.value, param);
    param.wr(")");
    return null;
  }

  @Override
  protected Void visitMsgPush(MsgPush obj, StreamWriter param) {
    param.wr("!push(");
    visit(obj.queue, param);
    param.wr(",");
    visit(obj.func, param);
    param.wr(",[");
    list(obj.data, ",", param);
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
