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

package ast.visitor;

import ast.data.Namespace;
import ast.data.component.CompRef;
import ast.data.component.composition.AsynchroniusConnection;
import ast.data.component.composition.CompUse;
import ast.data.component.composition.CompUseRef;
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
import ast.data.component.hfsm.StateRef;
import ast.data.component.hfsm.StateSimple;
import ast.data.component.hfsm.Transition;
import ast.data.expression.RefExp;
import ast.data.expression.TypeCast;
import ast.data.expression.binop.And;
import ast.data.expression.binop.BitAnd;
import ast.data.expression.binop.BitOr;
import ast.data.expression.binop.BitXor;
import ast.data.expression.binop.Div;
import ast.data.expression.binop.Equal;
import ast.data.expression.binop.Greater;
import ast.data.expression.binop.GreaterEqual;
import ast.data.expression.binop.Is;
import ast.data.expression.binop.Less;
import ast.data.expression.binop.LessEqual;
import ast.data.expression.binop.LogicAnd;
import ast.data.expression.binop.LogicOr;
import ast.data.expression.binop.Minus;
import ast.data.expression.binop.Mod;
import ast.data.expression.binop.Mul;
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
import ast.data.expression.value.BoolValue;
import ast.data.expression.value.NamedElementsValue;
import ast.data.expression.value.NamedValue;
import ast.data.expression.value.NumberValue;
import ast.data.expression.value.RecordValue;
import ast.data.expression.value.StringValue;
import ast.data.expression.value.TupleValue;
import ast.data.expression.value.UnionValue;
import ast.data.expression.value.UnsafeUnionValue;
import ast.data.file.RizzlyFile;
import ast.data.function.FuncRef;
import ast.data.function.header.FuncFunction;
import ast.data.function.header.FuncInterrupt;
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
import ast.data.reference.DummyLinkTarget;
import ast.data.reference.RefCall;
import ast.data.reference.RefIndex;
import ast.data.reference.RefName;
import ast.data.reference.RefTemplCall;
import ast.data.reference.Reference;
import ast.data.statement.AssignmentMulti;
import ast.data.statement.AssignmentSingle;
import ast.data.statement.Block;
import ast.data.statement.CallStmt;
import ast.data.statement.CaseOpt;
import ast.data.statement.CaseOptRange;
import ast.data.statement.CaseOptSimple;
import ast.data.statement.CaseOptValue;
import ast.data.statement.CaseStmt;
import ast.data.statement.ForStmt;
import ast.data.statement.IfOption;
import ast.data.statement.IfStmt;
import ast.data.statement.MsgPush;
import ast.data.statement.ReturnExpr;
import ast.data.statement.ReturnVoid;
import ast.data.statement.VarDefInitStmt;
import ast.data.statement.VarDefStmt;
import ast.data.statement.WhileStmt;
import ast.data.template.Template;
import ast.data.type.TypeRef;
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
import ast.data.type.out.PointerType;
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
import ast.data.variable.ConstGlobal;
import ast.data.variable.ConstPrivate;
import ast.data.variable.FuncVariable;
import ast.data.variable.StateVariable;
import ast.data.variable.TemplateParameter;

public class NullVisitor implements Visitor {

  @Override
  public void visit(AliasType aliasType) {

  }

  @Override
  public void visit(And and) {

  }

  @Override
  public void visit(AnyType anyType) {

  }

  @Override
  public void visit(AnyValue anyValue) {

  }

  @Override
  public void visit(ArrayTemplate arrayTemplate) {

  }

  @Override
  public void visit(ArrayType arrayType) {

  }

  @Override
  public void visit(ArrayValue arrayValue) {

  }

  @Override
  public void visit(AssignmentMulti assignmentMulti) {

  }

  @Override
  public void visit(AssignmentSingle assignmentSingle) {

  }

  @Override
  public void visit(AsynchroniusConnection asynchroniusConnection) {

  }

  @Override
  public void visit(BitAnd bitAnd) {

  }

  @Override
  public void visit(BitNot bitNot) {

  }

  @Override
  public void visit(BitOr bitOr) {

  }

  @Override
  public void visit(BitXor bitXor) {

  }

  @Override
  public void visit(Block block) {

  }

  @Override
  public void visit(BooleanType booleanType) {

  }

  @Override
  public void visit(BoolValue boolValue) {

  }

  @Override
  public void visit(CallStmt callStmt) {

  }

  @Override
  public void visit(CaseOpt caseOpt) {

  }

  @Override
  public void visit(CaseOptRange caseOptRange) {

  }

  @Override
  public void visit(CaseOptSimple caseOptSimple) {

  }

  @Override
  public void visit(CaseOptValue caseOptValue) {

  }

  @Override
  public void visit(CaseStmt caseStmt) {

  }

  @Override
  public void visit(ComponentType componentType) {

  }

  @Override
  public void visit(CompRef compRef) {

  }

  @Override
  public void visit(CompUse compUse) {

  }

  @Override
  public void visit(CompUseRef compUseRef) {

  }

  @Override
  public void visit(ConstGlobal constGlobal) {

  }

  @Override
  public void visit(ConstPrivate constPrivate) {

  }

  @Override
  public void visit(DefaultValueTemplate defaultValueTemplate) {

  }

  @Override
  public void visit(Div div) {

  }

  @Override
  public void visit(DummyLinkTarget dummyLinkTarget) {

  }

  @Override
  public void visit(EndpointRaw endpointRaw) {

  }

  @Override
  public void visit(EndpointSelf endpointSelf) {

  }

  @Override
  public void visit(EndpointSub endpointSub) {

  }

  @Override
  public void visit(EnumElement enumElement) {

  }

  @Override
  public void visit(EnumType enumType) {

  }

  @Override
  public void visit(Equal equal) {

  }

  @Override
  public void visit(ForStmt forStmt) {

  }

  @Override
  public void visit(FuncFunction funcFunction) {

  }

  @Override
  public void visit(FuncInterrupt funcInterrupt) {

  }

  @Override
  public void visit(FuncProcedure funcProcedure) {

  }

  @Override
  public void visit(FuncQuery funcQuery) {

  }

  @Override
  public void visit(FuncRef funcRef) {

  }

  @Override
  public void visit(FuncResponse funcResponse) {

  }

  @Override
  public void visit(FuncReturnNone funcReturnNone) {

  }

  @Override
  public void visit(FuncReturnTuple funcReturnTuple) {

  }

  @Override
  public void visit(FuncReturnType funcReturnType) {

  }

  @Override
  public void visit(FuncSignal funcSignal) {

  }

  @Override
  public void visit(FuncSlot funcSlot) {

  }

  @Override
  public void visit(FuncSubHandlerEvent funcSubHandlerEvent) {

  }

  @Override
  public void visit(FuncSubHandlerQuery funcSubHandlerQuery) {

  }

  @Override
  public void visit(FunctionType functionType) {

  }

  @Override
  public void visit(FuncVariable funcVariable) {

  }

  @Override
  public void visit(Greater greater) {

  }

  @Override
  public void visit(GreaterEqual greaterequal) {

  }

  @Override
  public void visit(IfOption ifOption) {

  }

  @Override
  public void visit(IfStmt ifStmt) {

  }

  @Override
  public void visit(ImplComposition implComposition) {

  }

  @Override
  public void visit(ImplElementary implElementary) {

  }

  @Override
  public void visit(ImplHfsm implHfsm) {

  }

  @Override
  public void visit(IntegerType integerType) {

  }

  @Override
  public void visit(Is is) {

  }

  @Override
  public void visit(Less less) {

  }

  @Override
  public void visit(LessEqual lessequal) {

  }

  @Override
  public void visit(LogicAnd logicAnd) {

  }

  @Override
  public void visit(LogicNot logicNot) {

  }

  @Override
  public void visit(LogicOr logicOr) {

  }

  @Override
  public void visit(Minus minus) {

  }

  @Override
  public void visit(Mod mod) {

  }

  @Override
  public void visit(MsgPush msgPush) {

  }

  @Override
  public void visit(Mul mul) {

  }

  @Override
  public void visit(NamedElement namedElement) {

  }

  @Override
  public void visit(NamedElementsValue namedElementsValue) {

  }

  @Override
  public void visit(NamedValue namedValue) {

  }

  @Override
  public void visit(Namespace namespace) {

  }

  @Override
  public void visit(NaturalType naturalType) {

  }

  @Override
  public void visit(Not not) {

  }

  @Override
  public void visit(NotEqual notequal) {

  }

  @Override
  public void visit(NumberValue numberValue) {

  }

  @Override
  public void visit(Or or) {

  }

  @Override
  public void visit(Plus plus) {

  }

  @Override
  public void visit(PointerType pointerType) {

  }

  @Override
  public void visit(Queue queue) {

  }

  @Override
  public void visit(RangeTemplate rangeTemplate) {

  }

  @Override
  public void visit(RangeType rangeType) {

  }

  @Override
  public void visit(RawComposition rawComposition) {

  }

  @Override
  public void visit(RawElementary rawElementary) {

  }

  @Override
  public void visit(RawHfsm rawHfsm) {

  }

  @Override
  public void visit(RecordType recordType) {

  }

  @Override
  public void visit(RecordValue recordValue) {

  }

  @Override
  public void visit(RefCall refCall) {

  }

  @Override
  public void visit(Reference reference) {

  }

  @Override
  public void visit(RefExp refExp) {

  }

  @Override
  public void visit(RefIndex refIndex) {

  }

  @Override
  public void visit(RefName refName) {

  }

  @Override
  public void visit(RefTemplCall refTemplCall) {

  }

  @Override
  public void visit(ReturnExpr returnExpr) {

  }

  @Override
  public void visit(ReturnVoid returnVoid) {

  }

  @Override
  public void visit(RizzlyFile rizzlyFile) {

  }

  @Override
  public void visit(Shl shl) {

  }

  @Override
  public void visit(Shr shr) {

  }

  @Override
  public void visit(SIntType sIntType) {

  }

  @Override
  public void visit(StateComposite stateComposite) {

  }

  @Override
  public void visit(StateRef stateRef) {

  }

  @Override
  public void visit(StateSimple stateSimple) {

  }

  @Override
  public void visit(StateVariable stateVariable) {

  }

  @Override
  public void visit(StringType stringType) {

  }

  @Override
  public void visit(StringValue stringValue) {

  }

  @Override
  public void visit(SubCallbacks subCallbacks) {

  }

  @Override
  public void visit(SynchroniusConnection synchroniusConnection) {

  }

  @Override
  public void visit(Template template) {

  }

  @Override
  public void visit(TemplateParameter templateParameter) {

  }

  @Override
  public void visit(Transition transition) {

  }

  @Override
  public void visit(TupleType tupleType) {

  }

  @Override
  public void visit(TupleValue tupleValue) {

  }

  @Override
  public void visit(TypeCast typeCast) {

  }

  @Override
  public void visit(TypeRef typeRef) {

  }

  @Override
  public void visit(TypeType typeType) {

  }

  @Override
  public void visit(TypeTypeTemplate typeTypeTemplate) {

  }

  @Override
  public void visit(UIntType uIntType) {

  }

  @Override
  public void visit(Uminus uminus) {

  }

  @Override
  public void visit(UnionType unionType) {

  }

  @Override
  public void visit(UnionValue unionValue) {

  }

  @Override
  public void visit(UnsafeUnionType unsafeUnionType) {

  }

  @Override
  public void visit(UnsafeUnionValue unsafeUnionValue) {

  }

  @Override
  public void visit(VarDefInitStmt varDefInitStmt) {

  }

  @Override
  public void visit(VarDefStmt varDefStmt) {

  }

  @Override
  public void visit(VoidType voidType) {

  }

  @Override
  public void visit(WhileStmt whileStmt) {

  }

}
