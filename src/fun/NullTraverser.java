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

package fun;

import fun.composition.Connection;
import fun.composition.ImplComposition;
import fun.expression.AnyValue;
import fun.expression.ArithmeticOp;
import fun.expression.BoolValue;
import fun.expression.NamedElementsValue;
import fun.expression.NamedValue;
import fun.expression.Number;
import fun.expression.Relation;
import fun.expression.StringValue;
import fun.expression.TupleValue;
import fun.expression.UnaryExpression;
import fun.expression.reference.DummyLinkTarget;
import fun.expression.reference.RefCall;
import fun.expression.reference.RefIndex;
import fun.expression.reference.RefName;
import fun.expression.reference.RefTemplCall;
import fun.expression.reference.Reference;
import fun.expression.reference.SimpleRef;
import fun.function.FuncFunction;
import fun.function.FuncProcedure;
import fun.function.FuncQuery;
import fun.function.FuncResponse;
import fun.function.FuncReturnNone;
import fun.function.FuncReturnTuple;
import fun.function.FuncReturnType;
import fun.function.FuncSignal;
import fun.function.FuncSlot;
import fun.function.template.DefaultValueTemplate;
import fun.hfsm.ImplHfsm;
import fun.hfsm.StateComposite;
import fun.hfsm.StateSimple;
import fun.hfsm.Transition;
import fun.other.ImplElementary;
import fun.other.Namespace;
import fun.other.RizzlyFile;
import fun.other.Template;
import fun.statement.Assignment;
import fun.statement.Block;
import fun.statement.CallStmt;
import fun.statement.CaseOpt;
import fun.statement.CaseOptRange;
import fun.statement.CaseOptValue;
import fun.statement.CaseStmt;
import fun.statement.ForStmt;
import fun.statement.IfOption;
import fun.statement.IfStmt;
import fun.statement.ReturnExpr;
import fun.statement.ReturnVoid;
import fun.statement.VarDefStmt;
import fun.statement.While;
import fun.type.base.AnyType;
import fun.type.base.BooleanType;
import fun.type.base.EnumElement;
import fun.type.base.EnumType;
import fun.type.base.IntegerType;
import fun.type.base.NaturalType;
import fun.type.base.StringType;
import fun.type.base.VoidType;
import fun.type.composed.NamedElement;
import fun.type.composed.RecordType;
import fun.type.composed.UnionType;
import fun.type.template.Array;
import fun.type.template.ArrayTemplate;
import fun.type.template.Range;
import fun.type.template.RangeTemplate;
import fun.type.template.TypeType;
import fun.type.template.TypeTypeTemplate;
import fun.variable.CompUse;
import fun.variable.ConstGlobal;
import fun.variable.ConstPrivate;
import fun.variable.FuncVariable;
import fun.variable.StateVariable;
import fun.variable.TemplateParameter;

abstract public class NullTraverser<R, P> extends Traverser<R, P> {

  protected abstract R visitDefault(Fun obj, P param);

  @Override
  protected R visitForStmt(ForStmt obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitDefaultValueTemplate(DefaultValueTemplate obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncReturnTuple(FuncReturnTuple obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncReturnType(FuncReturnType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncReturnNone(FuncReturnNone obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitDummyLinkTarget(DummyLinkTarget obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitSimpleRef(SimpleRef obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncFunction(FuncFunction obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncProcedure(FuncProcedure obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncSlot(FuncSlot obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncSignal(FuncSignal obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncQuery(FuncQuery obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncResponse(FuncResponse obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitTupleValue(TupleValue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitNamedValue(NamedValue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitNamedElementsValue(NamedElementsValue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitAnyValue(AnyValue obj, P param) {
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
  protected R visitCompUse(CompUse obj, P param) {
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
  protected R visitEnumType(EnumType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitEnumElement(EnumElement obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitWhile(While obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitVarDefStmt(VarDefStmt obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitIfStmt(IfStmt obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitCallStmt(CallStmt obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitAssignment(Assignment obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitReturnExpr(ReturnExpr obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitReturnVoid(ReturnVoid obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitBlock(Block obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitUnaryExpression(UnaryExpression obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitRelation(Relation obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitArithmeticOp(ArithmeticOp obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitNumber(Number obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitStringValue(StringValue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitRefTemplCall(RefTemplCall obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitStateVariable(StateVariable obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitNamespace(Namespace obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitConstPrivate(ConstPrivate obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitConstGlobal(ConstGlobal obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitTemplateParameter(TemplateParameter obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitIntegerType(IntegerType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitNaturalType(NaturalType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitTypeTypeTemplate(TypeTypeTemplate obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitNamedElement(NamedElement obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitArrayTemplate(ArrayTemplate obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitArray(Array obj, P param) {
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
  protected R visitAnyType(AnyType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitCaseStmt(CaseStmt obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitCaseOpt(CaseOpt obj, P param) {
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
  protected R visitFuncVariable(FuncVariable obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitBoolValue(BoolValue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitImplComposition(ImplComposition obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitConnection(Connection obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitReference(Reference obj, P param) {
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
  protected R visitIfOption(IfOption obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitRange(Range obj, P param) {
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

}
