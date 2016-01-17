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
import ast.data.function.FuncRef;
import ast.data.function.header.FuncFunction;
import ast.data.function.header.FuncInterrupt;
import ast.data.function.header.FuncProcedure;
import ast.data.function.header.FuncQuery;
import ast.data.function.header.FuncResponse;
import ast.data.function.header.FuncSubHandlerEvent;
import ast.data.function.header.FuncSubHandlerQuery;
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
import ast.data.reference.RefCall;
import ast.data.reference.RefIndex;
import ast.data.reference.RefName;
import ast.data.reference.RefTemplCall;
import ast.data.reference.Reference;
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
import ast.data.statement.IfStatement;
import ast.data.statement.MsgPush;
import ast.data.statement.MultiAssignment;
import ast.data.statement.ExpressionReturn;
import ast.data.statement.VoidReturn;
import ast.data.statement.VarDefInitStmt;
import ast.data.statement.VarDefStmt;
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
import ast.data.variable.ConstGlobal;
import ast.data.variable.ConstPrivate;
import ast.data.variable.FunctionVariable;
import ast.data.variable.StateVariable;
import ast.data.variable.TemplateParameter;
import ast.meta.SourcePosition;

public interface Visitor {

  void visit(AliasType object);

  void visit(And object);

  void visit(AnyType object);

  void visit(AnyValue object);

  void visit(ArrayTemplate object);

  void visit(ArrayType object);

  void visit(ArrayValue object);

  void visit(MultiAssignment object);

  void visit(AssignmentSingle object);

  void visit(AsynchroniusConnection object);

  void visit(BitAnd object);

  void visit(BitNot object);

  void visit(BitOr object);

  void visit(BitXor object);

  void visit(Block object);

  void visit(BooleanType object);

  void visit(BooleanValue object);

  void visit(CallStmt object);

  void visit(CaseOpt object);

  void visit(CaseOptRange object);

  void visit(CaseOptSimple object);

  void visit(CaseOptValue object);

  void visit(CaseStmt object);

  void visit(ComponentType object);

  void visit(CompRef object);

  void visit(CompUse object);

  void visit(CompUseRef object);

  void visit(ConstGlobal object);

  void visit(ConstPrivate object);

  void visit(DefaultValueTemplate object);

  void visit(Division object);

  void visit(LinkTarget object);

  void visit(EndpointRaw object);

  void visit(EndpointSelf object);

  void visit(EndpointSub object);

  void visit(EnumElement object);

  void visit(EnumType object);

  void visit(Equal object);

  void visit(ForStmt object);

  void visit(FuncFunction object);

  void visit(FuncInterrupt object);

  void visit(FuncProcedure object);

  void visit(FuncQuery object);

  void visit(FuncRef object);

  void visit(FuncResponse object);

  void visit(FuncReturnNone object);

  void visit(FuncReturnTuple object);

  void visit(FunctionReturnType object);

  void visit(Signal object);

  void visit(Slot object);

  void visit(FuncSubHandlerEvent object);

  void visit(FuncSubHandlerQuery object);

  void visit(FunctionType object);

  void visit(FunctionVariable object);

  void visit(Greater object);

  void visit(GreaterEqual object);

  void visit(IfOption object);

  void visit(IfStatement object);

  void visit(ImplComposition object);

  void visit(ImplElementary object);

  void visit(ImplHfsm object);

  void visit(IntegerType object);

  void visit(Is object);

  void visit(Less object);

  void visit(LessEqual object);

  void visit(LogicAnd object);

  void visit(LogicNot object);

  void visit(LogicOr object);

  void visit(Minus minus);

  void visit(Modulo mod);

  void visit(MsgPush object);

  void visit(Multiplication object);

  void visit(NamedElement object);

  void visit(NamedElementsValue object);

  void visit(NamedValue object);

  void visit(Namespace object);

  void visit(NaturalType object);

  void visit(Not object);

  void visit(NotEqual object);

  void visit(NumberValue object);

  void visit(Or object);

  void visit(Plus object);

  void visit(PointerType object);

  void visit(Queue object);

  void visit(RangeTemplate object);

  void visit(RangeType object);

  void visit(RawComposition object);

  void visit(RawElementary object);

  void visit(RawHfsm object);

  void visit(RecordType object);

  void visit(RecordValue object);

  void visit(RefCall object);

  void visit(Reference object);

  void visit(ReferenceExpression object);

  void visit(RefIndex object);

  void visit(RefName object);

  void visit(RefTemplCall object);

  void visit(ExpressionReturn object);

  void visit(VoidReturn object);

  void visit(RizzlyFile rizzlyFile);

  void visit(Shl shl);

  void visit(Shr object);

  void visit(SIntType object);

  void visit(StateComposite object);

  void visit(StateRef object);

  void visit(StateSimple object);

  void visit(StateVariable object);

  void visit(StringType object);

  void visit(StringValue object);

  void visit(SubCallbacks object);

  void visit(SynchroniusConnection object);

  void visit(Template object);

  void visit(TemplateParameter object);

  void visit(Transition object);

  void visit(TupleType object);

  void visit(TupleValue object);

  void visit(TypeCast object);

  void visit(TypeReference object);

  void visit(TypeType object);

  void visit(TypeTypeTemplate object);

  void visit(UIntType object);

  void visit(Uminus object);

  void visit(UnionType object);

  void visit(UnionValue object);

  void visit(UnsafeUnionType object);

  void visit(UnsafeUnionValue object);

  void visit(VarDefInitStmt object);

  void visit(VarDefStmt object);

  void visit(VoidType object);

  void visit(WhileStmt object);

  void visit(SourcePosition object);

}
