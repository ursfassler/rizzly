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

import ast.data.Ast;
import ast.data.Namespace;
import ast.data.component.ComponentReference;
import ast.data.component.composition.AsynchroniusConnection;
import ast.data.component.composition.CompUseRef;
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
import ast.data.component.hfsm.StateRef;
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
import ast.data.function.FunctionReference;
import ast.data.function.header.FuncFunction;
import ast.data.function.header.FuncInterrupt;
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
import ast.data.reference.LinkTarget;
import ast.data.reference.LinkedReferenceWithOffset_Implementation;
import ast.data.reference.RefCall;
import ast.data.reference.RefIndex;
import ast.data.reference.RefName;
import ast.data.reference.RefTemplCall;
import ast.data.reference.UnlinkedReferenceWithOffset_Implementation;
import ast.data.statement.AssignmentSingle;
import ast.data.statement.Block;
import ast.data.statement.CallStmt;
import ast.data.statement.CaseOpt;
import ast.data.statement.CaseOptRange;
import ast.data.statement.CaseOptSimple;
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
import ast.data.type.TypeReference;
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
import ast.data.variable.ConstPrivate;
import ast.data.variable.FunctionVariable;
import ast.data.variable.GlobalConstant;
import ast.data.variable.StateVariable;
import ast.data.variable.TemplateParameter;
import ast.meta.SourcePosition;

public class DefaultVisitor implements Visitor {

  private final DefaultHandler handler;

  public DefaultVisitor(DefaultHandler handler) {
    super();
    this.handler = handler;
  }

  private void defaultHandler(Ast ast) {
    handler.visit(ast);
  }

  @Override
  public void visit(AliasType aliasType) {
    defaultHandler(aliasType);
  }

  @Override
  public void visit(And and) {
    defaultHandler(and);
  }

  @Override
  public void visit(AnyType anyType) {
    defaultHandler(anyType);
  }

  @Override
  public void visit(AnyValue anyValue) {
    defaultHandler(anyValue);
  }

  @Override
  public void visit(ArrayTemplate arrayTemplate) {
    defaultHandler(arrayTemplate);
  }

  @Override
  public void visit(ArrayType arrayType) {
    defaultHandler(arrayType);
  }

  @Override
  public void visit(ArrayValue arrayValue) {
    defaultHandler(arrayValue);
  }

  @Override
  public void visit(MultiAssignment assignmentMulti) {
    defaultHandler(assignmentMulti);
  }

  @Override
  public void visit(AssignmentSingle assignmentSingle) {
    defaultHandler(assignmentSingle);
  }

  @Override
  public void visit(AsynchroniusConnection asynchroniusConnection) {
    defaultHandler(asynchroniusConnection);
  }

  @Override
  public void visit(BitAnd bitAnd) {
    defaultHandler(bitAnd);
  }

  @Override
  public void visit(BitNot bitNot) {
    defaultHandler(bitNot);
  }

  @Override
  public void visit(BitOr bitOr) {
    defaultHandler(bitOr);
  }

  @Override
  public void visit(BitXor bitXor) {
    defaultHandler(bitXor);
  }

  @Override
  public void visit(Block block) {
    defaultHandler(block);
  }

  @Override
  public void visit(BooleanType booleanType) {
    defaultHandler(booleanType);
  }

  @Override
  public void visit(BooleanValue boolValue) {
    defaultHandler(boolValue);
  }

  @Override
  public void visit(CallStmt callStmt) {
    defaultHandler(callStmt);
  }

  @Override
  public void visit(CaseOpt caseOpt) {
    defaultHandler(caseOpt);
  }

  @Override
  public void visit(CaseOptRange caseOptRange) {
    defaultHandler(caseOptRange);
  }

  @Override
  public void visit(CaseOptSimple caseOptSimple) {
    defaultHandler(caseOptSimple);
  }

  @Override
  public void visit(CaseOptValue caseOptValue) {
    defaultHandler(caseOptValue);
  }

  @Override
  public void visit(CaseStmt caseStmt) {
    defaultHandler(caseStmt);
  }

  @Override
  public void visit(ComponentType componentType) {
    defaultHandler(componentType);
  }

  @Override
  public void visit(ComponentReference compRef) {
    defaultHandler(compRef);
  }

  @Override
  public void visit(ComponentUse compUse) {
    defaultHandler(compUse);
  }

  @Override
  public void visit(CompUseRef compUseRef) {
    defaultHandler(compUseRef);
  }

  @Override
  public void visit(GlobalConstant constGlobal) {
    defaultHandler(constGlobal);
  }

  @Override
  public void visit(ConstPrivate constPrivate) {
    defaultHandler(constPrivate);
  }

  @Override
  public void visit(DefaultValueTemplate defaultValueTemplate) {
    defaultHandler(defaultValueTemplate);
  }

  @Override
  public void visit(Division div) {
    defaultHandler(div);
  }

  @Override
  public void visit(LinkTarget dummyLinkTarget) {
    defaultHandler(dummyLinkTarget);
  }

  @Override
  public void visit(EndpointRaw endpointRaw) {
    defaultHandler(endpointRaw);
  }

  @Override
  public void visit(EndpointSelf endpointSelf) {
    defaultHandler(endpointSelf);
  }

  @Override
  public void visit(EndpointSub endpointSub) {
    defaultHandler(endpointSub);
  }

  @Override
  public void visit(EnumElement enumElement) {
    defaultHandler(enumElement);
  }

  @Override
  public void visit(EnumType enumType) {
    defaultHandler(enumType);
  }

  @Override
  public void visit(Equal equal) {
    defaultHandler(equal);
  }

  @Override
  public void visit(ForStmt forStmt) {
    defaultHandler(forStmt);
  }

  @Override
  public void visit(FuncFunction funcFunction) {
    defaultHandler(funcFunction);
  }

  @Override
  public void visit(FuncInterrupt funcInterrupt) {
    defaultHandler(funcInterrupt);
  }

  @Override
  public void visit(Procedure funcProcedure) {
    defaultHandler(funcProcedure);
  }

  @Override
  public void visit(FuncQuery funcQuery) {
    defaultHandler(funcQuery);
  }

  @Override
  public void visit(FunctionReference funcRef) {
    defaultHandler(funcRef);
  }

  @Override
  public void visit(Response funcResponse) {
    defaultHandler(funcResponse);
  }

  @Override
  public void visit(FuncReturnNone funcReturnNone) {
    defaultHandler(funcReturnNone);
  }

  @Override
  public void visit(FuncReturnTuple funcReturnTuple) {
    defaultHandler(funcReturnTuple);
  }

  @Override
  public void visit(FunctionReturnType funcReturnType) {
    defaultHandler(funcReturnType);
  }

  @Override
  public void visit(Signal funcSignal) {
    defaultHandler(funcSignal);
  }

  @Override
  public void visit(Slot funcSlot) {
    defaultHandler(funcSlot);
  }

  @Override
  public void visit(FuncSubHandlerEvent funcSubHandlerEvent) {
    defaultHandler(funcSubHandlerEvent);
  }

  @Override
  public void visit(FuncSubHandlerQuery funcSubHandlerQuery) {
    defaultHandler(funcSubHandlerQuery);
  }

  @Override
  public void visit(FunctionType functionType) {
    defaultHandler(functionType);
  }

  @Override
  public void visit(FunctionVariable funcVariable) {
    defaultHandler(funcVariable);
  }

  @Override
  public void visit(Greater greater) {
    defaultHandler(greater);
  }

  @Override
  public void visit(GreaterEqual greaterequal) {
    defaultHandler(greaterequal);
  }

  @Override
  public void visit(IfOption ifOption) {
    defaultHandler(ifOption);
  }

  @Override
  public void visit(IfStatement ifStmt) {
    defaultHandler(ifStmt);
  }

  @Override
  public void visit(ImplComposition implComposition) {
    defaultHandler(implComposition);
  }

  @Override
  public void visit(ImplElementary implElementary) {
    defaultHandler(implElementary);
  }

  @Override
  public void visit(ImplHfsm implHfsm) {
    defaultHandler(implHfsm);
  }

  @Override
  public void visit(IntegerType integerType) {
    defaultHandler(integerType);
  }

  @Override
  public void visit(Is is) {
    defaultHandler(is);
  }

  @Override
  public void visit(Less less) {
    defaultHandler(less);
  }

  @Override
  public void visit(LessEqual lessequal) {
    defaultHandler(lessequal);
  }

  @Override
  public void visit(LogicAnd logicAnd) {
    defaultHandler(logicAnd);
  }

  @Override
  public void visit(LogicNot logicNot) {
    defaultHandler(logicNot);
  }

  @Override
  public void visit(LogicOr logicOr) {
    defaultHandler(logicOr);
  }

  @Override
  public void visit(Minus minus) {
    defaultHandler(minus);
  }

  @Override
  public void visit(Modulo mod) {
    defaultHandler(mod);
  }

  @Override
  public void visit(MsgPush msgPush) {
    defaultHandler(msgPush);
  }

  @Override
  public void visit(Multiplication mul) {
    defaultHandler(mul);
  }

  @Override
  public void visit(NamedElement namedElement) {
    defaultHandler(namedElement);
  }

  @Override
  public void visit(NamedElementsValue namedElementsValue) {
    defaultHandler(namedElementsValue);
  }

  @Override
  public void visit(NamedValue namedValue) {
    defaultHandler(namedValue);
  }

  @Override
  public void visit(Namespace namespace) {
    defaultHandler(namespace);
  }

  @Override
  public void visit(NaturalType naturalType) {
    defaultHandler(naturalType);
  }

  @Override
  public void visit(Not not) {
    defaultHandler(not);
  }

  @Override
  public void visit(NotEqual notequal) {
    defaultHandler(notequal);
  }

  @Override
  public void visit(NumberValue numberValue) {
    defaultHandler(numberValue);
  }

  @Override
  public void visit(Or or) {
    defaultHandler(or);
  }

  @Override
  public void visit(Plus plus) {
    defaultHandler(plus);
  }

  @Override
  public void visit(PointerType pointerType) {
    defaultHandler(pointerType);
  }

  @Override
  public void visit(Queue queue) {
    defaultHandler(queue);
  }

  @Override
  public void visit(RangeTemplate rangeTemplate) {
    defaultHandler(rangeTemplate);
  }

  @Override
  public void visit(RangeType rangeType) {
    defaultHandler(rangeType);
  }

  @Override
  public void visit(RawComposition rawComposition) {
    defaultHandler(rawComposition);
  }

  @Override
  public void visit(RawElementary rawElementary) {
    defaultHandler(rawElementary);
  }

  @Override
  public void visit(RawHfsm rawHfsm) {
    defaultHandler(rawHfsm);
  }

  @Override
  public void visit(RecordType recordType) {
    defaultHandler(recordType);
  }

  @Override
  public void visit(RecordValue recordValue) {
    defaultHandler(recordValue);
  }

  @Override
  public void visit(RefCall refCall) {
    defaultHandler(refCall);
  }

  @Override
  public void visit(LinkedReferenceWithOffset_Implementation reference) {
    defaultHandler(reference);
  }

  @Override
  public void visit(ReferenceExpression refExp) {
    defaultHandler(refExp);
  }

  @Override
  public void visit(RefIndex refIndex) {
    defaultHandler(refIndex);
  }

  @Override
  public void visit(RefName refName) {
    defaultHandler(refName);
  }

  @Override
  public void visit(RefTemplCall refTemplCall) {
    defaultHandler(refTemplCall);
  }

  @Override
  public void visit(ExpressionReturn returnExpr) {
    defaultHandler(returnExpr);
  }

  @Override
  public void visit(VoidReturn returnVoid) {
    defaultHandler(returnVoid);
  }

  @Override
  public void visit(RizzlyFile rizzlyFile) {
    defaultHandler(rizzlyFile);
  }

  @Override
  public void visit(Shl shl) {
    defaultHandler(shl);
  }

  @Override
  public void visit(Shr shr) {
    defaultHandler(shr);
  }

  @Override
  public void visit(SIntType sIntType) {
    defaultHandler(sIntType);
  }

  @Override
  public void visit(StateComposite stateComposite) {
    defaultHandler(stateComposite);
  }

  @Override
  public void visit(StateRef stateRef) {
    defaultHandler(stateRef);
  }

  @Override
  public void visit(StateSimple stateSimple) {
    defaultHandler(stateSimple);
  }

  @Override
  public void visit(StateVariable stateVariable) {
    defaultHandler(stateVariable);
  }

  @Override
  public void visit(StringType stringType) {
    defaultHandler(stringType);
  }

  @Override
  public void visit(StringValue stringValue) {
    defaultHandler(stringValue);
  }

  @Override
  public void visit(SubCallbacks subCallbacks) {
    defaultHandler(subCallbacks);
  }

  @Override
  public void visit(SynchroniusConnection synchroniusConnection) {
    defaultHandler(synchroniusConnection);
  }

  @Override
  public void visit(Template template) {
    defaultHandler(template);
  }

  @Override
  public void visit(TemplateParameter templateParameter) {
    defaultHandler(templateParameter);
  }

  @Override
  public void visit(Transition transition) {
    defaultHandler(transition);
  }

  @Override
  public void visit(TupleType tupleType) {
    defaultHandler(tupleType);
  }

  @Override
  public void visit(TupleValue tupleValue) {
    defaultHandler(tupleValue);
  }

  @Override
  public void visit(TypeCast typeCast) {
    defaultHandler(typeCast);
  }

  @Override
  public void visit(TypeReference typeRef) {
    defaultHandler(typeRef);
  }

  @Override
  public void visit(TypeType typeType) {
    defaultHandler(typeType);
  }

  @Override
  public void visit(TypeTypeTemplate typeTypeTemplate) {
    defaultHandler(typeTypeTemplate);
  }

  @Override
  public void visit(UIntType uIntType) {
    defaultHandler(uIntType);
  }

  @Override
  public void visit(Uminus uminus) {
    defaultHandler(uminus);
  }

  @Override
  public void visit(UnionType unionType) {
    defaultHandler(unionType);
  }

  @Override
  public void visit(UnionValue unionValue) {
    defaultHandler(unionValue);
  }

  @Override
  public void visit(UnsafeUnionType unsafeUnionType) {
    defaultHandler(unsafeUnionType);
  }

  @Override
  public void visit(UnsafeUnionValue unsafeUnionValue) {
    defaultHandler(unsafeUnionValue);
  }

  @Override
  public void visit(VarDefInitStmt varDefInitStmt) {
    defaultHandler(varDefInitStmt);
  }

  @Override
  public void visit(VarDefStmt object) {
    defaultHandler(object);
  }

  @Override
  public void visit(VoidType object) {
    defaultHandler(object);
  }

  @Override
  public void visit(WhileStmt object) {
    defaultHandler(object);
  }

  @Override
  public void visit(SourcePosition object) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public void visit(UnlinkedReferenceWithOffset_Implementation object) {
    defaultHandler(object);
  }

}
