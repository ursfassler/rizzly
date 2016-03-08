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

package ast.dispatcher;

import ast.data.Ast;
import ast.data.Namespace;
import ast.data.component.composition.AsynchroniusConnection;
import ast.data.component.composition.ComponentUse;
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
import ast.data.expression.ReferenceExpression;
import ast.data.expression.TypeCast;
import ast.data.expression.binop.And;
import ast.data.expression.binop.BitAnd;
import ast.data.expression.binop.BitOr;
import ast.data.expression.binop.BitXor;
import ast.data.expression.binop.Division;
import ast.data.expression.binop.Equal;
import ast.data.expression.binop.Greater;
import ast.data.expression.binop.GreaterEqual;
import ast.data.expression.binop.Is;
import ast.data.expression.binop.Less;
import ast.data.expression.binop.LessEqual;
import ast.data.expression.binop.LogicAnd;
import ast.data.expression.binop.LogicOr;
import ast.data.expression.binop.Minus;
import ast.data.expression.binop.Modulo;
import ast.data.expression.binop.Multiplication;
import ast.data.expression.binop.NotEqual;
import ast.data.expression.binop.Or;
import ast.data.expression.binop.Plus;
import ast.data.expression.binop.Shl;
import ast.data.expression.binop.Shr;
import ast.data.expression.unop.BitNot;
import ast.data.expression.unop.LogicNot;
import ast.data.expression.unop.Not;
import ast.data.expression.unop.Uminus;
import ast.data.expression.value.AnyValue;
import ast.data.expression.value.ArrayValue;
import ast.data.expression.value.BooleanValue;
import ast.data.expression.value.NamedElementsValue;
import ast.data.expression.value.NamedValue;
import ast.data.expression.value.NumberValue;
import ast.data.expression.value.RecordValue;
import ast.data.expression.value.StringValue;
import ast.data.expression.value.TupleValue;
import ast.data.expression.value.UnionValue;
import ast.data.expression.value.UnsafeUnionValue;
import ast.data.file.RizzlyFile;
import ast.data.function.header.FuncFunction;
import ast.data.function.header.FuncQuery;
import ast.data.function.header.FuncSubHandlerEvent;
import ast.data.function.header.FuncSubHandlerQuery;
import ast.data.function.header.Procedure;
import ast.data.function.header.Response;
import ast.data.function.header.Signal;
import ast.data.function.header.Slot;
import ast.data.function.ret.FuncReturnNone;
import ast.data.function.ret.FuncReturnTuple;
import ast.data.function.ret.FunctionReturnType;
import ast.data.function.template.DefaultValueTemplate;
import ast.data.raw.RawComposition;
import ast.data.raw.RawElementary;
import ast.data.raw.RawHfsm;
import ast.data.reference.LinkedAnchor;
import ast.data.reference.OffsetReference;
import ast.data.reference.RefCall;
import ast.data.reference.RefIndex;
import ast.data.reference.RefName;
import ast.data.reference.RefTemplCall;
import ast.data.reference.UnlinkedAnchor;
import ast.data.statement.AssignmentSingle;
import ast.data.statement.Block;
import ast.data.statement.CallStmt;
import ast.data.statement.CaseOpt;
import ast.data.statement.CaseOptRange;
import ast.data.statement.CaseOptValue;
import ast.data.statement.CaseStmt;
import ast.data.statement.ExpressionReturn;
import ast.data.statement.ForStmt;
import ast.data.statement.IfOption;
import ast.data.statement.IfStatement;
import ast.data.statement.MsgPush;
import ast.data.statement.MultiAssignment;
import ast.data.statement.VarDefInitStmt;
import ast.data.statement.VarDefStmt;
import ast.data.statement.VoidReturn;
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
import ast.data.type.special.TypeType;
import ast.data.type.special.VoidType;
import ast.data.type.template.ArrayTemplate;
import ast.data.type.template.RangeTemplate;
import ast.data.type.template.TypeTypeTemplate;
import ast.data.variable.PrivateConstant;
import ast.data.variable.FunctionVariable;
import ast.data.variable.GlobalConstant;
import ast.data.variable.StateVariable;
import ast.data.variable.TemplateParameter;

abstract public class NullDispatcher<R, P> extends Dispatcher<R, P> {

  abstract protected R visitDefault(Ast obj, P param);

  @Override
  protected R visitRefExpr(ReferenceExpression obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitEndpointRaw(EndpointRaw obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitSynchroniusConnection(SynchroniusConnection obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitAsynchroniusConnection(AsynchroniusConnection obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitForStmt(ForStmt obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitBitXor(BitXor obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitAssignmentSingle(AssignmentSingle obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitNamedElementsValue(NamedElementsValue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitTupleType(TupleType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncReturnTuple(FuncReturnTuple obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncReturnType(FunctionReturnType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncReturnNone(FuncReturnNone obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitAliasType(AliasType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitUIntType(UIntType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitSIntType(SIntType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitCompUse(ComponentUse obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitNamedElement(NamedElement obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitQueue(Queue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitMsgPush(MsgPush obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitUnsafeUnionValue(UnsafeUnionValue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitRecordValue(RecordValue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitTupleValue(TupleValue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitUnionValue(UnionValue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitNamedValue(NamedValue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitAnyType(AnyType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitAnyValue(AnyValue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncSignal(Signal obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncQuery(FuncQuery obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncSlot(Slot obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncResponse(Response obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitBitAnd(BitAnd obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitBitOr(BitOr obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitLogicOr(LogicOr obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitLogicAnd(LogicAnd obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitArrayValue(ArrayValue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitVoidType(VoidType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitBooleanType(BooleanType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitStringType(StringType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitRefCall(RefCall obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitRefName(RefName obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitRefIndex(RefIndex obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitImplElementary(ImplElementary obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitRecordType(RecordType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitUnionType(UnionType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitUnsafeUnionType(UnsafeUnionType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitEnumType(EnumType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitEnumElement(EnumElement obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitNumber(NumberValue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitStringValue(StringValue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitStateVariable(StateVariable obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitSubCallbacks(SubCallbacks obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitNamespace(Namespace obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitConstPrivate(PrivateConstant obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitConstGlobal(GlobalConstant obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitArrayType(ArrayType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncVariable(FunctionVariable obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitBoolValue(BooleanValue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitImplComposition(ImplComposition obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitImplHfsm(ImplHfsm obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitStateSimple(StateSimple obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitStateComposite(StateComposite obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitTransition(Transition obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitNaturalType(NaturalType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitIntegerType(IntegerType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncProcedure(Procedure obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncFunction(FuncFunction obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncSubHandlerQuery(FuncSubHandlerQuery obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncSubHandlerEvent(FuncSubHandlerEvent obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFunctionType(FunctionType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitEndpointSelf(EndpointSelf obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitEndpointSub(EndpointSub obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitComponentType(ComponentType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitAnd(And obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitDiv(Division obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitEqual(Equal obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitGreater(Greater obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitGreaterequal(GreaterEqual obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitLess(Less obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitLessequal(LessEqual obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitMinus(Minus obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitMod(Modulo obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitMul(Multiplication obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitNot(Not obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitNotequal(NotEqual obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitOr(Or obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitPlus(Plus obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitShl(Shl obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitShr(Shr obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitUminus(Uminus obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitRangeType(RangeType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitWhileStmt(WhileStmt obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitCaseStmt(CaseStmt obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitCaseOptRange(CaseOptRange obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitCaseOptValue(CaseOptValue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitCaseOpt(CaseOpt obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitIfOption(IfOption obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitVarDef(VarDefStmt obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitIfStmt(IfStatement obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitCallStmt(CallStmt obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitAssignmentMulti(MultiAssignment obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitReturnExpr(ExpressionReturn obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitReturnVoid(VoidReturn obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitBlock(Block obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitTypeCast(TypeCast obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitLogicNot(LogicNot obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitBitNot(BitNot obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitIs(Is obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitDefaultValueTemplate(DefaultValueTemplate obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitRawElementary(RawElementary obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitVarDefInitStmt(VarDefInitStmt obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitRefTemplCall(RefTemplCall obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitTemplateParameter(TemplateParameter obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitTypeTypeTemplate(TypeTypeTemplate obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitArrayTemplate(ArrayTemplate obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitRizzlyFile(RizzlyFile obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitTypeType(TypeType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitRawComposition(RawComposition obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitRawHfsm(RawHfsm obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitRangeTemplate(RangeTemplate obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitTemplate(Template obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitOffsetReference(OffsetReference obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitUnlinkedAnchor(UnlinkedAnchor obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitLinkedAnchor(LinkedAnchor obj, P param) {
    return visitDefault(obj, param);
  }

}
