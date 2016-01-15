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

import ast.ElementInfo;
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

public interface Visitor {

  void visit(AliasType aliasType);

  void visit(And and);

  void visit(AnyType anyType);

  void visit(AnyValue anyValue);

  void visit(ArrayTemplate arrayTemplate);

  void visit(ArrayType arrayType);

  void visit(ArrayValue arrayValue);

  void visit(AssignmentMulti assignmentMulti);

  void visit(AssignmentSingle assignmentSingle);

  void visit(AsynchroniusConnection asynchroniusConnection);

  void visit(BitAnd bitAnd);

  void visit(BitNot bitNot);

  void visit(BitOr bitOr);

  void visit(BitXor bitXor);

  void visit(Block block);

  void visit(BooleanType booleanType);

  void visit(BooleanValue boolValue);

  void visit(CallStmt callStmt);

  void visit(CaseOpt caseOpt);

  void visit(CaseOptRange caseOptRange);

  void visit(CaseOptSimple caseOptSimple);

  void visit(CaseOptValue caseOptValue);

  void visit(CaseStmt caseStmt);

  void visit(ComponentType componentType);

  void visit(CompRef compRef);

  void visit(CompUse compUse);

  void visit(CompUseRef compUseRef);

  void visit(ConstGlobal constGlobal);

  void visit(ConstPrivate constPrivate);

  void visit(DefaultValueTemplate defaultValueTemplate);

  void visit(Division div);

  void visit(DummyLinkTarget dummyLinkTarget);

  void visit(EndpointRaw endpointRaw);

  void visit(EndpointSelf endpointSelf);

  void visit(EndpointSub endpointSub);

  void visit(EnumElement enumElement);

  void visit(EnumType enumType);

  void visit(Equal equal);

  void visit(ForStmt forStmt);

  void visit(FuncFunction funcFunction);

  void visit(FuncInterrupt funcInterrupt);

  void visit(FuncProcedure funcProcedure);

  void visit(FuncQuery funcQuery);

  void visit(FuncRef funcRef);

  void visit(FuncResponse funcResponse);

  void visit(FuncReturnNone funcReturnNone);

  void visit(FuncReturnTuple funcReturnTuple);

  void visit(FuncReturnType funcReturnType);

  void visit(FuncSignal funcSignal);

  void visit(FuncSlot funcSlot);

  void visit(FuncSubHandlerEvent funcSubHandlerEvent);

  void visit(FuncSubHandlerQuery funcSubHandlerQuery);

  void visit(FunctionType functionType);

  void visit(FuncVariable funcVariable);

  void visit(Greater greater);

  void visit(GreaterEqual greaterequal);

  void visit(IfOption ifOption);

  void visit(IfStmt ifStmt);

  void visit(ImplComposition implComposition);

  void visit(ImplElementary implElementary);

  void visit(ImplHfsm implHfsm);

  void visit(IntegerType integerType);

  void visit(Is is);

  void visit(Less less);

  void visit(LessEqual lessequal);

  void visit(LogicAnd logicAnd);

  void visit(LogicNot logicNot);

  void visit(LogicOr logicOr);

  void visit(Minus minus);

  void visit(Modulo mod);

  void visit(MsgPush msgPush);

  void visit(Multiplication mul);

  void visit(NamedElement namedElement);

  void visit(NamedElementsValue namedElementsValue);

  void visit(NamedValue namedValue);

  void visit(Namespace namespace);

  void visit(NaturalType naturalType);

  void visit(Not not);

  void visit(NotEqual notequal);

  void visit(NumberValue numberValue);

  void visit(Or or);

  void visit(Plus plus);

  void visit(PointerType pointerType);

  void visit(Queue queue);

  void visit(RangeTemplate rangeTemplate);

  void visit(RangeType rangeType);

  void visit(RawComposition rawComposition);

  void visit(RawElementary rawElementary);

  void visit(RawHfsm rawHfsm);

  void visit(RecordType recordType);

  void visit(RecordValue recordValue);

  void visit(RefCall refCall);

  void visit(Reference reference);

  void visit(RefExp refExp);

  void visit(RefIndex refIndex);

  void visit(RefName refName);

  void visit(RefTemplCall refTemplCall);

  void visit(ReturnExpr returnExpr);

  void visit(ReturnVoid returnVoid);

  void visit(RizzlyFile rizzlyFile);

  void visit(Shl shl);

  void visit(Shr shr);

  void visit(SIntType sIntType);

  void visit(StateComposite stateComposite);

  void visit(StateRef stateRef);

  void visit(StateSimple stateSimple);

  void visit(StateVariable stateVariable);

  void visit(StringType stringType);

  void visit(StringValue stringValue);

  void visit(SubCallbacks subCallbacks);

  void visit(SynchroniusConnection synchroniusConnection);

  void visit(Template template);

  void visit(TemplateParameter templateParameter);

  void visit(Transition transition);

  void visit(TupleType tupleType);

  void visit(TupleValue tupleValue);

  void visit(TypeCast typeCast);

  void visit(TypeRef typeRef);

  void visit(TypeType typeType);

  void visit(TypeTypeTemplate typeTypeTemplate);

  void visit(UIntType uIntType);

  void visit(Uminus uminus);

  void visit(UnionType unionType);

  void visit(UnionValue unionValue);

  void visit(UnsafeUnionType unsafeUnionType);

  void visit(UnsafeUnionValue unsafeUnionValue);

  void visit(VarDefInitStmt varDefInitStmt);

  void visit(VarDefStmt varDefStmt);

  void visit(VoidType voidType);

  void visit(WhileStmt whileStmt);

  void visit(ElementInfo elementInfo);

}
