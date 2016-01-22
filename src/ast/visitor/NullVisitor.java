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

public class NullVisitor implements Visitor {

  @Override
  public void visit(AliasType object) {

  }

  @Override
  public void visit(And object) {

  }

  @Override
  public void visit(AnyType object) {

  }

  @Override
  public void visit(AnyValue object) {

  }

  @Override
  public void visit(ArrayTemplate object) {

  }

  @Override
  public void visit(ArrayType object) {

  }

  @Override
  public void visit(ArrayValue object) {

  }

  @Override
  public void visit(AssignmentSingle object) {

  }

  @Override
  public void visit(AsynchroniusConnection object) {

  }

  @Override
  public void visit(BitAnd object) {

  }

  @Override
  public void visit(BitNot object) {

  }

  @Override
  public void visit(BitOr object) {

  }

  @Override
  public void visit(BitXor object) {

  }

  @Override
  public void visit(Block object) {

  }

  @Override
  public void visit(BooleanType object) {

  }

  @Override
  public void visit(BooleanValue object) {

  }

  @Override
  public void visit(CallStmt object) {

  }

  @Override
  public void visit(CaseOpt object) {

  }

  @Override
  public void visit(CaseOptRange object) {

  }

  @Override
  public void visit(CaseOptSimple object) {

  }

  @Override
  public void visit(CaseOptValue object) {

  }

  @Override
  public void visit(CaseStmt object) {

  }

  @Override
  public void visit(ComponentReference object) {

  }

  @Override
  public void visit(ComponentType object) {

  }

  @Override
  public void visit(ComponentUse object) {

  }

  @Override
  public void visit(CompUseRef object) {

  }

  @Override
  public void visit(ConstPrivate object) {

  }

  @Override
  public void visit(DefaultValueTemplate object) {

  }

  @Override
  public void visit(Division object) {

  }

  @Override
  public void visit(EndpointRaw object) {

  }

  @Override
  public void visit(EndpointSelf object) {

  }

  @Override
  public void visit(EndpointSub object) {

  }

  @Override
  public void visit(EnumElement object) {

  }

  @Override
  public void visit(EnumType object) {

  }

  @Override
  public void visit(Equal object) {

  }

  @Override
  public void visit(ExpressionReturn object) {

  }

  @Override
  public void visit(ForStmt object) {

  }

  @Override
  public void visit(FuncFunction object) {

  }

  @Override
  public void visit(FuncInterrupt object) {

  }

  @Override
  public void visit(FuncQuery object) {

  }

  @Override
  public void visit(FuncReturnNone object) {

  }

  @Override
  public void visit(FuncReturnTuple object) {

  }

  @Override
  public void visit(FuncSubHandlerEvent object) {

  }

  @Override
  public void visit(FuncSubHandlerQuery object) {

  }

  @Override
  public void visit(FunctionReference object) {

  }

  @Override
  public void visit(FunctionReturnType object) {

  }

  @Override
  public void visit(FunctionType object) {

  }

  @Override
  public void visit(FunctionVariable object) {

  }

  @Override
  public void visit(GlobalConstant object) {

  }

  @Override
  public void visit(Greater object) {

  }

  @Override
  public void visit(GreaterEqual object) {

  }

  @Override
  public void visit(IfOption object) {

  }

  @Override
  public void visit(IfStatement object) {

  }

  @Override
  public void visit(ImplComposition object) {

  }

  @Override
  public void visit(ImplElementary object) {

  }

  @Override
  public void visit(ImplHfsm object) {

  }

  @Override
  public void visit(IntegerType object) {

  }

  @Override
  public void visit(Is object) {

  }

  @Override
  public void visit(Less object) {

  }

  @Override
  public void visit(LessEqual object) {

  }

  @Override
  public void visit(LinkedReferenceWithOffset_Implementation object) {

  }

  @Override
  public void visit(LinkTarget object) {

  }

  @Override
  public void visit(LogicAnd object) {

  }

  @Override
  public void visit(LogicNot object) {

  }

  @Override
  public void visit(LogicOr object) {

  }

  @Override
  public void visit(Minus object) {

  }

  @Override
  public void visit(Modulo object) {

  }

  @Override
  public void visit(MsgPush object) {

  }

  @Override
  public void visit(MultiAssignment object) {

  }

  @Override
  public void visit(Multiplication object) {

  }

  @Override
  public void visit(NamedElement object) {

  }

  @Override
  public void visit(NamedElementsValue object) {

  }

  @Override
  public void visit(NamedValue object) {

  }

  @Override
  public void visit(Namespace object) {

  }

  @Override
  public void visit(NaturalType object) {

  }

  @Override
  public void visit(Not object) {

  }

  @Override
  public void visit(NotEqual object) {

  }

  @Override
  public void visit(NumberValue object) {

  }

  @Override
  public void visit(Or object) {

  }

  @Override
  public void visit(Plus object) {

  }

  @Override
  public void visit(PointerType object) {

  }

  @Override
  public void visit(Procedure object) {

  }

  @Override
  public void visit(Queue object) {

  }

  @Override
  public void visit(RangeTemplate object) {

  }

  @Override
  public void visit(RangeType object) {

  }

  @Override
  public void visit(RawComposition object) {

  }

  @Override
  public void visit(RawElementary object) {

  }

  @Override
  public void visit(RawHfsm object) {

  }

  @Override
  public void visit(RecordType object) {

  }

  @Override
  public void visit(RecordValue object) {

  }

  @Override
  public void visit(RefCall object) {

  }

  @Override
  public void visit(ReferenceExpression object) {

  }

  @Override
  public void visit(RefIndex object) {

  }

  @Override
  public void visit(RefName object) {

  }

  @Override
  public void visit(RefTemplCall object) {

  }

  @Override
  public void visit(Response object) {

  }

  @Override
  public void visit(RizzlyFile object) {

  }

  @Override
  public void visit(Shl object) {

  }

  @Override
  public void visit(Shr object) {

  }

  @Override
  public void visit(Signal object) {

  }

  @Override
  public void visit(SIntType object) {

  }

  @Override
  public void visit(Slot object) {

  }

  @Override
  public void visit(SourcePosition object) {
  }

  @Override
  public void visit(StateComposite object) {

  }

  @Override
  public void visit(StateRef object) {

  }

  @Override
  public void visit(StateSimple object) {

  }

  @Override
  public void visit(StateVariable object) {

  }

  @Override
  public void visit(StringType object) {

  }

  @Override
  public void visit(StringValue object) {

  }

  @Override
  public void visit(SubCallbacks object) {

  }

  @Override
  public void visit(SynchroniusConnection object) {

  }

  @Override
  public void visit(Template object) {

  }

  @Override
  public void visit(TemplateParameter object) {

  }

  @Override
  public void visit(Transition object) {

  }

  @Override
  public void visit(TupleType object) {

  }

  @Override
  public void visit(TupleValue object) {

  }

  @Override
  public void visit(TypeCast object) {

  }

  @Override
  public void visit(TypeReference object) {

  }

  @Override
  public void visit(TypeType object) {

  }

  @Override
  public void visit(TypeTypeTemplate object) {

  }

  @Override
  public void visit(UIntType object) {

  }

  @Override
  public void visit(Uminus object) {

  }

  @Override
  public void visit(UnionType object) {

  }

  @Override
  public void visit(UnionValue object) {

  }

  @Override
  public void visit(UnlinkedReferenceWithOffset_Implementation object) {
  }

  @Override
  public void visit(UnsafeUnionType object) {

  }

  @Override
  public void visit(UnsafeUnionValue object) {

  }

  @Override
  public void visit(VarDefInitStmt object) {

  }

  @Override
  public void visit(VarDefStmt object) {

  }

  @Override
  public void visit(VoidReturn object) {

  }

  @Override
  public void visit(VoidType object) {

  }

  @Override
  public void visit(WhileStmt object) {

  }

}
