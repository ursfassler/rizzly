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

package ast.traverser;

import ast.data.Namespace;
import ast.data.component.composition.AsynchroniusConnection;
import ast.data.component.composition.CompUse;
import ast.data.component.composition.Direction;
import ast.data.component.composition.EndpointRaw;
import ast.data.component.composition.EndpointSelf;
import ast.data.component.composition.EndpointSub;
import ast.data.component.composition.ImplComposition;
import ast.data.component.composition.Queue;
import ast.data.component.composition.SubCallbacks;
import ast.data.component.composition.SynchroniusConnection;
import ast.data.component.elementary.ImplElementary;
import ast.data.component.hfsm.ImplHfsm;
import ast.data.component.hfsm.StateComposite;
import ast.data.component.hfsm.StateSimple;
import ast.data.component.hfsm.Transition;
import ast.data.expression.AnyValue;
import ast.data.expression.ArrayValue;
import ast.data.expression.BoolValue;
import ast.data.expression.NamedElementsValue;
import ast.data.expression.NamedValue;
import ast.data.expression.Number;
import ast.data.expression.RecordValue;
import ast.data.expression.StringValue;
import ast.data.expression.TupleValue;
import ast.data.expression.TypeCast;
import ast.data.expression.UnionValue;
import ast.data.expression.UnsafeUnionValue;
import ast.data.expression.binop.And;
import ast.data.expression.binop.BitAnd;
import ast.data.expression.binop.BitOr;
import ast.data.expression.binop.BitXor;
import ast.data.expression.binop.Div;
import ast.data.expression.binop.Equal;
import ast.data.expression.binop.Greater;
import ast.data.expression.binop.Greaterequal;
import ast.data.expression.binop.Is;
import ast.data.expression.binop.Less;
import ast.data.expression.binop.Lessequal;
import ast.data.expression.binop.LogicAnd;
import ast.data.expression.binop.LogicOr;
import ast.data.expression.binop.Minus;
import ast.data.expression.binop.Mod;
import ast.data.expression.binop.Mul;
import ast.data.expression.binop.Notequal;
import ast.data.expression.binop.Or;
import ast.data.expression.binop.Plus;
import ast.data.expression.binop.Shl;
import ast.data.expression.binop.Shr;
import ast.data.expression.reference.DummyLinkTarget;
import ast.data.expression.reference.RefCall;
import ast.data.expression.reference.RefIndex;
import ast.data.expression.reference.RefName;
import ast.data.expression.reference.RefTemplCall;
import ast.data.expression.reference.Reference;
import ast.data.expression.reference.SimpleRef;
import ast.data.expression.unop.BitNot;
import ast.data.expression.unop.LogicNot;
import ast.data.expression.unop.Not;
import ast.data.expression.unop.Uminus;
import ast.data.file.RizzlyFile;
import ast.data.function.Function;
import ast.data.function.header.FuncFunction;
import ast.data.function.header.FuncProcedure;
import ast.data.function.header.FuncQuery;
import ast.data.function.header.FuncResponse;
import ast.data.function.header.FuncSignal;
import ast.data.function.header.FuncSlot;
import ast.data.function.header.FuncSubHandlerEvent;
import ast.data.function.header.FuncSubHandlerQuery;
import ast.data.function.ret.FuncReturnNone;
import ast.data.function.ret.FuncReturnTuple;
import ast.data.function.ret.FuncReturnType;
import ast.data.function.template.DefaultValueTemplate;
import ast.data.raw.RawComposition;
import ast.data.raw.RawElementary;
import ast.data.raw.RawHfsm;
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
import ast.data.statement.VarDefInitStmt;
import ast.data.statement.VarDefStmt;
import ast.data.statement.WhileStmt;
import ast.data.template.Template;
import ast.data.type.base.ArrayType;
import ast.data.type.base.BooleanType;
import ast.data.type.base.EnumElement;
import ast.data.type.base.EnumType;
import ast.data.type.base.FunctionType;
import ast.data.type.base.RangeType;
import ast.data.type.base.StringType;
import ast.data.type.base.TupleType;
import ast.data.type.composed.NamedElement;
import ast.data.type.composed.RecordType;
import ast.data.type.composed.UnionType;
import ast.data.type.composed.UnsafeUnionType;
import ast.data.type.out.AliasType;
import ast.data.type.out.SIntType;
import ast.data.type.out.UIntType;
import ast.data.type.special.AnyType;
import ast.data.type.special.ComponentType;
import ast.data.type.special.IntegerType;
import ast.data.type.special.NaturalType;
import ast.data.type.special.VoidType;
import ast.data.type.template.ArrayTemplate;
import ast.data.type.template.RangeTemplate;
import ast.data.type.template.TypeType;
import ast.data.type.template.TypeTypeTemplate;
import ast.data.variable.ConstGlobal;
import ast.data.variable.ConstPrivate;
import ast.data.variable.FuncVariable;
import ast.data.variable.StateVariable;
import ast.data.variable.TemplateParameter;

public class DefTraverser<R, P> extends Traverser<R, P> {

  @Override
  protected R visitNamedElementsValue(NamedElementsValue obj, P param) {
    visitList(obj.value, param);
    return null;
  }

  @Override
  protected R visitTupleType(TupleType obj, P param) {
    visitList(obj.types, param);
    return null;
  }

  @Override
  protected R visitFuncReturnTuple(FuncReturnTuple obj, P param) {
    visitList(obj.param, param);
    return null;
  }

  @Override
  protected R visitFuncReturnType(FuncReturnType obj, P param) {
    visit(obj.type, param);
    return null;
  }

  @Override
  protected R visitFuncReturnNone(FuncReturnNone obj, P param) {
    return null;
  }

  @Override
  protected R visitImplElementary(ImplElementary obj, P param) {
    visitList(obj.function, param);
    visitList(obj.iface, param);
    visit(obj.queue, param);
    visitList(obj.type, param);
    visitList(obj.constant, param);
    visitList(obj.variable, param);
    visitList(obj.component, param);
    visitList(obj.subCallback, param);
    visit(obj.entryFunc, param);
    visit(obj.exitFunc, param);
    return null;
  }

  @Override
  protected R visitImplComposition(ImplComposition obj, P param) {
    visitList(obj.function, param);
    visitList(obj.iface, param);
    visit(obj.queue, param);
    visitList(obj.component, param);
    visitList(obj.connection, param);
    return null;
  }

  @Override
  protected R visitImplHfsm(ImplHfsm obj, P param) {
    visitList(obj.function, param);
    visitList(obj.iface, param);
    visit(obj.queue, param);
    visit(obj.topstate, param);
    return null;
  }

  @Override
  protected R visitNumber(Number obj, P param) {
    return null;
  }

  @Override
  protected R visitVarDef(VarDefStmt obj, P param) {
    visit(obj.variable, param);
    return null;
  }

  @Override
  protected R visitCallStmt(CallStmt obj, P param) {
    visit(obj.call, param);
    return null;
  }

  @Override
  protected R visitAssignmentMulti(AssignmentMulti obj, P param) {
    visitList(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitAssignmentSingle(AssignmentSingle obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitUnionType(UnionType obj, P param) {
    visit(obj.tag, param);
    visitList(obj.element, param);
    return null;
  }

  @Override
  protected R visitUnsafeUnionType(UnsafeUnionType obj, P param) {
    visitList(obj.element, param);
    return null;
  }

  @Override
  protected R visitRecordType(RecordType obj, P param) {
    visitList(obj.element, param);
    return null;
  }

  @Override
  protected R visitComponentType(ComponentType obj, P param) {
    visitList(obj.input, param);
    visitList(obj.output, param);
    return null;
  }

  @Override
  protected R visitRefName(RefName obj, P param) {
    return null;
  }

  @Override
  protected R visitRefIndex(RefIndex obj, P param) {
    visit(obj.index, param);
    return null;
  }

  @Override
  protected R visitRefCall(RefCall obj, P param) {
    visit(obj.actualParameter, param);
    return null;
  }

  @Override
  protected R visitArrayType(ArrayType obj, P param) {
    visit(obj.type, param);
    return null;
  }

  @Override
  protected R visitBooleanType(BooleanType obj, P param) {
    return null;
  }

  @Override
  protected R visitEnumType(EnumType obj, P param) {
    visitList(obj.getElement(), param);
    return null;
  }

  @Override
  protected R visitEnumElement(EnumElement obj, P param) {
    return null;
  }

  @Override
  protected R visitReturnExpr(ReturnExpr obj, P param) {
    visit(obj.expr, param);
    return null;
  }

  @Override
  protected R visitReturnVoid(ReturnVoid obj, P param) {
    return null;
  }

  @Override
  protected R visitVoidType(VoidType obj, P param) {
    return null;
  }

  @Override
  protected R visitFuncVariable(FuncVariable obj, P param) {
    visit(obj.type, param);
    return null;
  }

  @Override
  protected R visitStateVariable(StateVariable obj, P param) {
    visit(obj.type, param);
    visit(obj.def, param);
    return null;
  }

  @Override
  protected R visitConstPrivate(ConstPrivate obj, P param) {
    visit(obj.type, param);
    visit(obj.def, param);
    return null;
  }

  @Override
  protected R visitConstGlobal(ConstGlobal obj, P param) {
    visit(obj.type, param);
    visit(obj.def, param);
    return null;
  }

  @Override
  protected R visitSubCallbacks(SubCallbacks obj, P param) {
    visit(obj.compUse, param);
    visitList(obj.func, param);
    return null;
  }

  @Override
  protected R visitNamespace(Namespace obj, P param) {
    visitList(obj.children, param);
    return null;
  }

  @Override
  protected R visitCaseOptRange(CaseOptRange obj, P param) {
    visit(obj.start, param);
    visit(obj.end, param);
    return null;
  }

  @Override
  protected R visitCaseOptValue(CaseOptValue obj, P param) {
    visit(obj.value, param);
    return null;
  }

  @Override
  protected R visitBoolValue(BoolValue obj, P param) {
    return null;
  }

  @Override
  protected R visitSynchroniusConnection(SynchroniusConnection obj, P param) {
    visit(obj.endpoint.get(Direction.in), param);
    visit(obj.endpoint.get(Direction.out), param);
    return null;
  }

  @Override
  protected R visitAsynchroniusConnection(AsynchroniusConnection obj, P param) {
    visit(obj.endpoint.get(Direction.in), param);
    visit(obj.endpoint.get(Direction.out), param);
    return null;
  }

  @Override
  protected R visitStringValue(StringValue obj, P param) {
    return null;
  }

  @Override
  protected R visitStringType(StringType obj, P param) {
    return null;
  }

  @Override
  protected R visitArrayValue(ArrayValue obj, P param) {
    visitList(obj.value, param);
    return null;
  }

  @Override
  protected R visitTupleValue(TupleValue obj, P param) {
    visitList(obj.value, param);
    return null;
  }

  @Override
  protected R visitStateSimple(StateSimple obj, P param) {
    visit(obj.entryFunc, param);
    visit(obj.exitFunc, param);
    visitList(obj.item, param);
    return null;
  }

  @Override
  protected R visitStateComposite(StateComposite obj, P param) {
    visit(obj.entryFunc, param);
    visit(obj.exitFunc, param);
    visitList(obj.item, param);
    visit(obj.initial, param);
    return null;
  }

  @Override
  protected R visitTransition(Transition obj, P param) {
    visit(obj.src, param);
    visit(obj.dst, param);
    visit(obj.eventFunc, param);
    visitList(obj.param, param);
    visit(obj.guard, param);
    visit(obj.body, param);
    return null;
  }

  @Override
  protected R visitSimpleRef(SimpleRef obj, P param) {
    return null;
  }

  @Override
  protected R visitReference(Reference obj, P param) {
    visitList(obj.offset, param);
    return null;
  }

  @Override
  protected R visitNaturalType(NaturalType obj, P param) {
    return null;
  }

  @Override
  protected R visitIntegerType(IntegerType obj, P param) {
    return null;
  }

  @Override
  protected R visitFuncProcedure(FuncProcedure obj, P param) {
    return null;
  }

  @Override
  protected R visitFuncFunction(FuncFunction obj, P param) {
    return null;
  }

  @Override
  protected R visitFuncSubHandlerQuery(FuncSubHandlerQuery obj, P param) {
    return null;
  }

  @Override
  protected R visitFuncSubHandlerEvent(FuncSubHandlerEvent obj, P param) {
    return null;
  }

  @Override
  protected R visitFunctionType(FunctionType obj, P param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected R visitEndpointSelf(EndpointSelf obj, P param) {
    visit(obj.funcRef, param);
    return null;
  }

  @Override
  protected R visitEndpointSub(EndpointSub obj, P param) {
    visit(obj.component, param);
    return null;
  }

  @Override
  protected R visitEndpointRaw(EndpointRaw obj, P param) {
    visit(obj.ref, param);
    return null;
  }

  @Override
  protected R visitAnd(And obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitDiv(Div obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitEqual(Equal obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitGreater(Greater obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitGreaterequal(Greaterequal obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitLess(Less obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitIs(Is obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitLessequal(Lessequal obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitMinus(Minus obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitMod(Mod obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitMul(Mul obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitNot(Not obj, P param) {
    visit(obj.expr, param);
    return null;
  }

  @Override
  protected R visitLogicNot(LogicNot obj, P param) {
    visit(obj.expr, param);
    return null;
  }

  @Override
  protected R visitBitNot(BitNot obj, P param) {
    visit(obj.expr, param);
    return null;
  }

  @Override
  protected R visitNotequal(Notequal obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitOr(Or obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitPlus(Plus obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitShl(Shl obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitShr(Shr obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitUminus(Uminus obj, P param) {
    visit(obj.expr, param);
    return null;
  }

  @Override
  protected R visitRangeType(RangeType obj, P param) {
    return null;
  }

  @Override
  protected R visitWhileStmt(WhileStmt obj, P param) {
    visit(obj.condition, param);
    visit(obj.body, param);
    return null;
  }

  @Override
  protected R visitIfStmt(IfStmt obj, P param) {
    visitList(obj.option, param);
    visit(obj.defblock, param);
    return null;
  }

  @Override
  protected R visitIfOption(IfOption obj, P param) {
    visit(obj.condition, param);
    visit(obj.code, param);
    return null;
  }

  @Override
  protected R visitBlock(Block obj, P param) {
    for (Statement stmt : obj.statements) {
      visit(stmt, param);
    }
    return null;
  }

  @Override
  protected R visitCaseStmt(CaseStmt obj, P param) {
    visit(obj.condition, param);
    visitList(obj.option, param);
    visit(obj.otherwise, param);
    return null;
  }

  @Override
  protected R visitCaseOpt(CaseOpt obj, P param) {
    visitList(obj.value, param);
    visit(obj.code, param);
    return null;
  }

  @Override
  protected R visitTypeCast(TypeCast obj, P param) {
    visit(obj.cast, param);
    visit(obj.value, param);
    return null;
  }

  @Override
  protected R visitBitAnd(BitAnd obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitBitOr(BitOr obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitBitXor(BitXor obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitLogicOr(LogicOr obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitLogicAnd(LogicAnd obj, P param) {
    visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected R visitFuncSignal(FuncSignal obj, P param) {
    return null;
  }

  @Override
  protected R visitFuncQuery(FuncQuery obj, P param) {
    return null;
  }

  @Override
  protected R visitFuncSlot(FuncSlot obj, P param) {
    return null;
  }

  @Override
  protected R visitFuncResponse(FuncResponse obj, P param) {
    return null;
  }

  @Override
  protected R visitAnyType(AnyType obj, P param) {
    return null;
  }

  @Override
  protected R visitAnyValue(AnyValue obj, P param) {
    return null;
  }

  @Override
  protected R visitNamedValue(NamedValue obj, P param) {
    visit(obj.value, param);
    return null;
  }

  @Override
  protected R visitUnionValue(UnionValue obj, P param) {
    visit(obj.tagValue, param);
    visit(obj.contentValue, param);
    visit(obj.type, param);
    return null;
  }

  @Override
  protected R visitUnsafeUnionValue(UnsafeUnionValue obj, P param) {
    visit(obj.contentValue, param);
    visit(obj.type, param);
    return null;
  }

  @Override
  protected R visitRecordValue(RecordValue obj, P param) {
    visitList(obj.value, param);
    visit(obj.type, param);
    return null;
  }

  @Override
  protected R visitMsgPush(MsgPush obj, P param) {
    visit(obj.queue, param);
    visit(obj.func, param);
    visitList(obj.data, param);
    return null;
  }

  @Override
  protected R visitQueue(Queue obj, P param) {
    return null;
  }

  @Override
  protected R visitFunction(Function obj, P param) {
    super.visitFunction(obj, param);
    visitList(obj.param, param);
    visit(obj.ret, param);
    visit(obj.body, param);
    return null;
  }

  @Override
  protected R visitNamedElement(NamedElement obj, P param) {
    visit(obj.typeref, param);
    return null;
  }

  @Override
  protected R visitCompUse(CompUse obj, P param) {
    visit(obj.compRef, param);
    return null;
  }

  @Override
  protected R visitUIntType(UIntType obj, P param) {
    return null;
  }

  @Override
  protected R visitSIntType(SIntType obj, P param) {
    return null;
  }

  @Override
  protected R visitAliasType(AliasType obj, P param) {
    visit(obj.ref, param);
    return null;
  }

  @Override
  protected R visitForStmt(ForStmt obj, P param) {
    visit(obj.iterator, param);
    visit(obj.block, param);
    return null;
  }

  // /////////////////////////////////////////////

  @Override
  protected R visitDefaultValueTemplate(DefaultValueTemplate obj, P param) {
    return null;
  }

  @Override
  protected R visitRizzlyFile(RizzlyFile obj, P param) {
    visitList(obj.getObjects(), param);
    return null;
  }

  @Override
  protected R visitRawElementary(RawElementary obj, P param) {
    visit(obj.getEntryFunc(), param);
    visit(obj.getExitFunc(), param);
    visitList(obj.getIface(), param);
    visitList(obj.getDeclaration(), param);
    visitList(obj.getInstantiation(), param);
    return null;
  }

  @Override
  protected R visitRawComposition(RawComposition obj, P param) {
    visitList(obj.getIface(), param);
    visitList(obj.getInstantiation(), param);
    visitList(obj.getConnection(), param);
    return null;
  }

  @Override
  protected R visitRawHfsm(RawHfsm obj, P param) {
    visitList(obj.getIface(), param);
    visit(obj.getTopstate(), param);
    return null;
  }

  @Override
  protected R visitVarDefInitStmt(VarDefInitStmt obj, P param) {
    visitList(obj.variable, param);
    visit(obj.initial, param);
    return null;
  }

  @Override
  protected R visitRefTemplCall(RefTemplCall obj, P param) {
    visitList(obj.actualParameter, param);
    return null;
  }

  @Override
  protected R visitTemplateParameter(TemplateParameter obj, P param) {
    visit(obj.type, param);
    return null;
  }

  @Override
  protected R visitTypeTypeTemplate(TypeTypeTemplate obj, P param) {
    return null;
  }

  @Override
  protected R visitTypeType(TypeType obj, P param) {
    return null;
  }

  @Override
  protected R visitRangeTemplate(RangeTemplate obj, P param) {
    return null;
  }

  @Override
  protected R visitTemplate(Template obj, P param) {
    visitList(obj.getTempl(), param);
    visit(obj.getObject(), param);
    return null;
  }

  @Override
  protected R visitArrayTemplate(ArrayTemplate obj, P param) {
    return null;
  }

  @Override
  protected R visitDummyLinkTarget(DummyLinkTarget obj, P param) {
    return null;
  }

}
