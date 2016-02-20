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

import java.util.Collection;

import ast.data.Ast;
import ast.data.AstBase;
import ast.data.AstList;
import ast.data.Named;
import ast.data.Namespace;
import ast.data.component.Component;
import ast.data.component.composition.AsynchroniusConnection;
import ast.data.component.composition.CompUseRef;
import ast.data.component.composition.ComponentUse;
import ast.data.component.composition.Connection;
import ast.data.component.composition.Endpoint;
import ast.data.component.composition.EndpointRaw;
import ast.data.component.composition.EndpointSelf;
import ast.data.component.composition.EndpointSub;
import ast.data.component.composition.ImplComposition;
import ast.data.component.composition.Queue;
import ast.data.component.composition.SubCallbacks;
import ast.data.component.composition.SynchroniusConnection;
import ast.data.component.elementary.ImplElementary;
import ast.data.component.hfsm.ImplHfsm;
import ast.data.component.hfsm.State;
import ast.data.component.hfsm.StateComposite;
import ast.data.component.hfsm.StateSimple;
import ast.data.component.hfsm.Transition;
import ast.data.expression.Expression;
import ast.data.expression.ReferenceExpression;
import ast.data.expression.TypeCast;
import ast.data.expression.binop.And;
import ast.data.expression.binop.ArithmeticOp;
import ast.data.expression.binop.BinaryExpression;
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
import ast.data.expression.binop.Logical;
import ast.data.expression.binop.Minus;
import ast.data.expression.binop.Modulo;
import ast.data.expression.binop.Multiplication;
import ast.data.expression.binop.NotEqual;
import ast.data.expression.binop.Or;
import ast.data.expression.binop.Plus;
import ast.data.expression.binop.Relation;
import ast.data.expression.binop.Shl;
import ast.data.expression.binop.Shr;
import ast.data.expression.unop.BitNot;
import ast.data.expression.unop.LogicNot;
import ast.data.expression.unop.Not;
import ast.data.expression.unop.Uminus;
import ast.data.expression.unop.UnaryExp;
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
import ast.data.expression.value.ValueExpr;
import ast.data.file.RizzlyFile;
import ast.data.function.Function;
import ast.data.function.FunctionReference;
import ast.data.function.InterfaceFunction;
import ast.data.function.header.FuncFunction;
import ast.data.function.header.FuncQuery;
import ast.data.function.header.FuncSubHandlerEvent;
import ast.data.function.header.FuncSubHandlerQuery;
import ast.data.function.header.Procedure;
import ast.data.function.header.Response;
import ast.data.function.header.Signal;
import ast.data.function.header.Slot;
import ast.data.function.ret.FuncReturn;
import ast.data.function.ret.FuncReturnNone;
import ast.data.function.ret.FuncReturnTuple;
import ast.data.function.ret.FunctionReturnType;
import ast.data.function.template.DefaultValueTemplate;
import ast.data.function.template.FunctionTemplate;
import ast.data.raw.RawComponent;
import ast.data.raw.RawComposition;
import ast.data.raw.RawElementary;
import ast.data.raw.RawHfsm;
import ast.data.reference.LinkTarget;
import ast.data.reference.LinkedAnchor;
import ast.data.reference.LinkedReferenceWithOffset_Implementation;
import ast.data.reference.OffsetReference;
import ast.data.reference.RefCall;
import ast.data.reference.RefIndex;
import ast.data.reference.RefItem;
import ast.data.reference.RefName;
import ast.data.reference.RefTemplCall;
import ast.data.reference.SimpleReference;
import ast.data.reference.TypedReference;
import ast.data.reference.UnlinkedAnchor;
import ast.data.statement.Assignment;
import ast.data.statement.AssignmentSingle;
import ast.data.statement.Block;
import ast.data.statement.CallStmt;
import ast.data.statement.CaseOpt;
import ast.data.statement.CaseOptEntry;
import ast.data.statement.CaseOptRange;
import ast.data.statement.CaseOptValue;
import ast.data.statement.CaseStmt;
import ast.data.statement.ExpressionReturn;
import ast.data.statement.ForStmt;
import ast.data.statement.IfOption;
import ast.data.statement.IfStatement;
import ast.data.statement.MsgPush;
import ast.data.statement.MultiAssignment;
import ast.data.statement.Return;
import ast.data.statement.Statement;
import ast.data.statement.VarDefInitStmt;
import ast.data.statement.VarDefStmt;
import ast.data.statement.VoidReturn;
import ast.data.statement.WhileStmt;
import ast.data.template.Template;
import ast.data.type.Type;
import ast.data.type.TypeReference;
import ast.data.type.base.ArrayType;
import ast.data.type.base.BaseType;
import ast.data.type.base.BooleanType;
import ast.data.type.base.EnumElement;
import ast.data.type.base.EnumType;
import ast.data.type.base.FunctionType;
import ast.data.type.base.RangeType;
import ast.data.type.base.StringType;
import ast.data.type.base.TupleType;
import ast.data.type.composed.NamedElement;
import ast.data.type.composed.NamedElementType;
import ast.data.type.composed.RecordType;
import ast.data.type.composed.UnionType;
import ast.data.type.composed.UnsafeUnionType;
import ast.data.type.out.AliasType;
import ast.data.type.out.IntType;
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
import ast.data.type.template.TypeTemplate;
import ast.data.type.template.TypeTypeTemplate;
import ast.data.variable.ConstPrivate;
import ast.data.variable.Constant;
import ast.data.variable.DefaultVariable;
import ast.data.variable.FunctionVariable;
import ast.data.variable.GlobalConstant;
import ast.data.variable.StateVariable;
import ast.data.variable.TemplateParameter;
import ast.data.variable.Variable;

public abstract class Dispatcher<R, P> {

  public R traverse(Collection<? extends Ast> list, P param) {
    return visitList(list, param);
  }

  public R traverse(Ast obj, P param) {
    return visit(obj, param);
  }

  protected R visitList(Collection<? extends Ast> list, P param) {
    for (Ast itr : new AstList<Ast>(list)) {
      visit(itr, param);
    }
    return null;
  }

  protected R visit(Ast obj, P param) {
    if (obj == null) {
      throw new RuntimeException("object is null");
    } else if (obj instanceof AstBase) {
      return visitAstBase((AstBase) obj, param);
    } else {
      throwUnknownObjectError(obj);
      return null;
    }
  }

  protected R visitAstBase(AstBase obj, P param) {
    if (obj instanceof Named) {
      return visitNamed((Named) obj, param);
    } else if (obj instanceof Expression) {
      return visitExpression((Expression) obj, param);
    } else if (obj instanceof SubCallbacks) {
      return visitSubCallbacks((SubCallbacks) obj, param);
    } else if (obj instanceof RefItem) {
      return visitRefItem((RefItem) obj, param);
    } else if (obj instanceof Connection) {
      return visitConnection((Connection) obj, param);
    } else if (obj instanceof Transition) {
      return visitTransition((Transition) obj, param);
    } else if (obj instanceof Statement) {
      return visitStatement((Statement) obj, param);
    } else if (obj instanceof IfOption) {
      return visitIfOption((IfOption) obj, param);
    } else if (obj instanceof CaseOpt) {
      return visitCaseOpt((CaseOpt) obj, param);
    } else if (obj instanceof CaseOptEntry) {
      return visitCaseOptEntry((CaseOptEntry) obj, param);
    } else if (obj instanceof FuncReturn) {
      return visitFuncReturn((FuncReturn) obj, param);
    } else if (obj instanceof NamedValue) {
      return visitNamedValue((NamedValue) obj, param);
    } else if (obj instanceof Endpoint) {
      return visitEndpoint((Endpoint) obj, param);
    } else if (obj instanceof TypedReference) {
      return visitTypedRef((TypedReference) obj, param);
    } else if (obj instanceof LinkedReferenceWithOffset_Implementation) {
      return visitReference((LinkedReferenceWithOffset_Implementation) obj, param);
    } else if (obj instanceof SimpleReference) {
      return visitSimpleReference((SimpleReference) obj, param);
    } else if (obj instanceof OffsetReference) {
      return visitOffsetReference((OffsetReference) obj, param);
    } else if (obj instanceof LinkedAnchor) {
      return visitLinkedAnchor((LinkedAnchor) obj, param);
    } else if (obj instanceof UnlinkedAnchor) {
      return visitUnlinkedAnchor((UnlinkedAnchor) obj, param);
    } else {
      throwUnknownObjectError(obj);
      return null;
    }
  }

  protected R visitTypedRef(TypedReference obj, P param) {
    if (obj instanceof TypeReference) {
      return visitTypeRef((TypeReference) obj, param);
    } else if (obj instanceof FunctionReference) {
      return visitFuncRef((FunctionReference) obj, param);
    } else if (obj instanceof CompUseRef) {
      return visitCompUseRef((CompUseRef) obj, param);
    } else {
      throwUnknownObjectError(obj);
      return null;
    }
  }

  protected R visitNamed(Named obj, P param) {
    if (obj instanceof Type) {
      return visitType((Type) obj, param);
    } else if (obj instanceof Function) {
      return visitFunction((Function) obj, param);
    } else if (obj instanceof Variable) {
      return visitVariable((Variable) obj, param);
    } else if (obj instanceof Namespace) {
      return visitNamespace((Namespace) obj, param);
    } else if (obj instanceof State) {
      return visitState((State) obj, param);
    } else if (obj instanceof Queue) {
      return visitQueue((Queue) obj, param);
    } else if (obj instanceof EnumElement) {
      return visitEnumElement((EnumElement) obj, param);
    } else if (obj instanceof Component) {
      return visitComponent((Component) obj, param);
    } else if (obj instanceof NamedElement) {
      return visitNamedElement((NamedElement) obj, param);
    } else if (obj instanceof ComponentUse) {
      return visitCompUse((ComponentUse) obj, param);
    } else if (obj instanceof RizzlyFile) {
      return visitRizzlyFile((RizzlyFile) obj, param);
    } else if (obj instanceof Template) {
      return visitTemplate((Template) obj, param);
    } else if (obj instanceof RawComponent) {
      return visitRawComponent((RawComponent) obj, param);
    } else if (obj instanceof FunctionTemplate) {
      return visitFunctionTemplate((FunctionTemplate) obj, param);
    } else {
      throwUnknownObjectError(obj);
      return null;
    }
  }

  protected R visitConnection(Connection obj, P param) {
    if (obj instanceof AsynchroniusConnection) {
      return visitAsynchroniusConnection((AsynchroniusConnection) obj, param);
    } else if (obj instanceof SynchroniusConnection) {
      return visitSynchroniusConnection((SynchroniusConnection) obj, param);
    } else {
      throwUnknownObjectError(obj);
      return null;
    }
  }

  protected R visitFuncReturn(FuncReturn obj, P param) {
    if (obj instanceof FuncReturnNone) {
      return visitFuncReturnNone((FuncReturnNone) obj, param);
    } else if (obj instanceof FunctionReturnType) {
      return visitFuncReturnType((FunctionReturnType) obj, param);
    } else if (obj instanceof FuncReturnTuple) {
      return visitFuncReturnTuple((FuncReturnTuple) obj, param);
    } else {
      throwUnknownObjectError(obj);
      return null;
    }
  }

  protected R visitCaseOptEntry(CaseOptEntry obj, P param) {
    if (obj instanceof CaseOptValue) {
      return visitCaseOptValue((CaseOptValue) obj, param);
    } else if (obj instanceof CaseOptRange) {
      return visitCaseOptRange((CaseOptRange) obj, param);
    } else {
      throwUnknownObjectError(obj);
      return null;
    }
  }

  protected R visitStatement(Statement obj, P param) {
    if (obj instanceof Block) {
      return visitBlock((Block) obj, param);
    } else if (obj instanceof Assignment) {
      return visitAssignment((Assignment) obj, param);
    } else if (obj instanceof CallStmt) {
      return visitCallStmt((CallStmt) obj, param);
    } else if (obj instanceof IfStatement) {
      return visitIfStmt((IfStatement) obj, param);
    } else if (obj instanceof Return) {
      return visitReturn((Return) obj, param);
    } else if (obj instanceof VarDefStmt) {
      return visitVarDef((VarDefStmt) obj, param);
    } else if (obj instanceof WhileStmt) {
      return visitWhileStmt((WhileStmt) obj, param);
    } else if (obj instanceof CaseStmt) {
      return visitCaseStmt((CaseStmt) obj, param);
    } else if (obj instanceof MsgPush) {
      return visitMsgPush((MsgPush) obj, param);
    } else if (obj instanceof ForStmt) {
      return visitForStmt((ForStmt) obj, param);
    } else if (obj instanceof VarDefInitStmt) {
      return visitVarDefInitStmt((VarDefInitStmt) obj, param);
    } else {
      throwUnknownObjectError(obj);
      return null;
    }
  }

  protected R visitAssignment(Assignment obj, P param) {
    if (obj instanceof AssignmentSingle) {
      return visitAssignmentSingle((AssignmentSingle) obj, param);
    } else if (obj instanceof MultiAssignment) {
      return visitAssignmentMulti((MultiAssignment) obj, param);
    } else {
      throwUnknownObjectError(obj);
      return null;
    }
  }

  protected R visitReturn(Return obj, P param) {
    if (obj instanceof VoidReturn) {
      return visitReturnVoid((VoidReturn) obj, param);
    } else if (obj instanceof ExpressionReturn) {
      return visitReturnExpr((ExpressionReturn) obj, param);
    } else {
      throwUnknownObjectError(obj);
      return null;
    }
  }

  protected R visitEndpoint(Endpoint obj, P param) {
    if (obj instanceof EndpointSub) {
      return visitEndpointSub((EndpointSub) obj, param);
    } else if (obj instanceof EndpointSelf) {
      return visitEndpointSelf((EndpointSelf) obj, param);
    } else if (obj instanceof EndpointRaw) {
      return visitEndpointRaw((EndpointRaw) obj, param);
    } else {
      throwUnknownObjectError(obj);
      return null;
    }
  }

  protected R visitState(State obj, P param) {
    if (obj instanceof StateComposite) {
      return visitStateComposite((StateComposite) obj, param);
    } else if (obj instanceof StateSimple) {
      return visitStateSimple((StateSimple) obj, param);
    } else {
      throwUnknownObjectError(obj);
      return null;
    }
  }

  protected R visitVariable(Variable obj, P param) {
    if (obj instanceof DefaultVariable) {
      return visitDefVariable((DefaultVariable) obj, param);
    } else if (obj instanceof FunctionVariable) {
      return visitFuncVariable((FunctionVariable) obj, param);
    } else if (obj instanceof TemplateParameter) {
      return visitTemplateParameter((TemplateParameter) obj, param);
    } else {
      throwUnknownObjectError(obj);
      return null;
    }
  }

  protected R visitDefVariable(DefaultVariable obj, P param) {
    if (obj instanceof StateVariable) {
      return visitStateVariable((StateVariable) obj, param);
    } else if (obj instanceof Constant) {
      return visitConstant((Constant) obj, param);
    } else {
      throwUnknownObjectError(obj);
      return null;
    }
  }

  protected R visitConstant(Constant obj, P param) {
    if (obj instanceof ConstPrivate) {
      return visitConstPrivate((ConstPrivate) obj, param);
    } else if (obj instanceof GlobalConstant) {
      return visitConstGlobal((GlobalConstant) obj, param);
    } else {
      throwUnknownObjectError(obj);
      return null;
    }
  }

  protected R visitFunction(Function obj, P param) {
    if (obj instanceof InterfaceFunction) {
      return visitInterfaceFunction((InterfaceFunction) obj, param);
    } else if (obj instanceof Procedure) {
      return visitFuncProcedure((Procedure) obj, param);
    } else if (obj instanceof FuncFunction) {
      return visitFuncFunction((FuncFunction) obj, param);
    } else if (obj instanceof FuncSubHandlerEvent) {
      return visitFuncSubHandlerEvent((FuncSubHandlerEvent) obj, param);
    } else if (obj instanceof FuncSubHandlerQuery) {
      return visitFuncSubHandlerQuery((FuncSubHandlerQuery) obj, param);
    } else if (obj instanceof FuncQuery) {
      return visitFuncQuery((FuncQuery) obj, param);
    } else if (obj instanceof Signal) {
      return visitFuncSignal((Signal) obj, param);
    } else if (obj instanceof Procedure) {
      return visitFuncProcedure((Procedure) obj, param);
    } else if (obj instanceof FuncFunction) {
      return visitFuncFunction((FuncFunction) obj, param);
    } else if (obj instanceof Response) {
      return visitFuncResponse((Response) obj, param);
    } else if (obj instanceof Slot) {
      return visitFuncSlot((Slot) obj, param);
    } else {
      throwUnknownObjectError(obj);
      return null;
    }
  }

  protected R visitInterfaceFunction(InterfaceFunction obj, P param) {
    if (obj instanceof Slot) {
      return visitFuncSlot((Slot) obj, param);
    } else if (obj instanceof Response) {
      return visitFuncResponse((Response) obj, param);
    } else if (obj instanceof FuncQuery) {
      return visitFuncQuery((FuncQuery) obj, param);
    } else if (obj instanceof Signal) {
      return visitFuncSignal((Signal) obj, param);
    } else {
      throwUnknownObjectError(obj);
      return null;
    }
  }

  protected R visitExpression(Expression obj, P param) {
    if (obj instanceof ValueExpr) {
      return visitValueExpr((ValueExpr) obj, param);
    } else if (obj instanceof BinaryExpression) {
      return visitBinaryExp((BinaryExpression) obj, param);
    } else if (obj instanceof UnaryExp) {
      return visitUnaryExp((UnaryExp) obj, param);
    } else if (obj instanceof ReferenceExpression) {
      return visitRefExpr((ReferenceExpression) obj, param);
    } else if (obj instanceof TypeCast) {
      return visitTypeCast((TypeCast) obj, param);
    } else if (obj instanceof ArithmeticOp) {
      return visitArithmeticOp((ArithmeticOp) obj, param);
    } else if (obj instanceof Relation) {
      return visitRelation((Relation) obj, param);
    } else {
      throwUnknownObjectError(obj);
      return null;
    }
  }

  protected R visitValueExpr(ValueExpr obj, P param) {
    if (obj instanceof NumberValue) {
      return visitNumber((NumberValue) obj, param);
    } else if (obj instanceof StringValue) {
      return visitStringValue((StringValue) obj, param);
    } else if (obj instanceof ArrayValue) {
      return visitArrayValue((ArrayValue) obj, param);
    } else if (obj instanceof TupleValue) {
      return visitTupleValue((TupleValue) obj, param);
    } else if (obj instanceof BooleanValue) {
      return visitBoolValue((BooleanValue) obj, param);
    } else if (obj instanceof AnyValue) {
      return visitAnyValue((AnyValue) obj, param);
    } else if (obj instanceof UnionValue) {
      return visitUnionValue((UnionValue) obj, param);
    } else if (obj instanceof UnsafeUnionValue) {
      return visitUnsafeUnionValue((UnsafeUnionValue) obj, param);
    } else if (obj instanceof RecordValue) {
      return visitRecordValue((RecordValue) obj, param);
    } else if (obj instanceof NamedElementsValue) {
      return visitNamedElementsValue((NamedElementsValue) obj, param);
    } else {
      throwUnknownObjectError(obj);
      return null;
    }
  }

  protected R visitUnaryExp(UnaryExp obj, P param) {
    if (obj instanceof Uminus) {
      return visitUminus((Uminus) obj, param);
    } else if (obj instanceof Not) {
      return visitNot((Not) obj, param);
    } else if (obj instanceof BitNot) {
      return visitBitNot((BitNot) obj, param);
    } else if (obj instanceof LogicNot) {
      return visitLogicNot((LogicNot) obj, param);
    } else {
      throwUnknownObjectError(obj);
      return null;
    }
  }

  protected R visitBinaryExp(BinaryExpression obj, P param) {
    if (obj instanceof ArithmeticOp) {
      return visitArithmeticOp((ArithmeticOp) obj, param);
    } else if (obj instanceof Logical) {
      return visitLogical((Logical) obj, param);
    } else if (obj instanceof Relation) {
      return visitRelation((Relation) obj, param);
    } else if (obj instanceof And) {
      return visitAnd((And) obj, param);
    } else if (obj instanceof Or) {
      return visitOr((Or) obj, param);
    } else {
      throwUnknownObjectError(obj);
      return null;
    }
  }

  protected R visitLogical(Logical obj, P param) {
    if (obj instanceof LogicAnd) {
      return visitLogicAnd((LogicAnd) obj, param);
    } else if (obj instanceof LogicOr) {
      return visitLogicOr((LogicOr) obj, param);
    } else {
      throwUnknownObjectError(obj);
      return null;
    }
  }

  protected R visitArithmeticOp(ArithmeticOp obj, P param) {
    if (obj instanceof Plus) {
      return visitPlus((Plus) obj, param);
    } else if (obj instanceof Minus) {
      return visitMinus((Minus) obj, param);
    } else if (obj instanceof Multiplication) {
      return visitMul((Multiplication) obj, param);
    } else if (obj instanceof Division) {
      return visitDiv((Division) obj, param);
    } else if (obj instanceof Modulo) {
      return visitMod((Modulo) obj, param);
    } else if (obj instanceof Shl) {
      return visitShl((Shl) obj, param);
    } else if (obj instanceof Shr) {
      return visitShr((Shr) obj, param);
    } else if (obj instanceof BitAnd) {
      return visitBitAnd((BitAnd) obj, param);
    } else if (obj instanceof BitOr) {
      return visitBitOr((BitOr) obj, param);
    } else if (obj instanceof BitXor) {
      return visitBitXor((BitXor) obj, param);
    } else {
      throwUnknownObjectError(obj);
      return null;
    }
  }

  protected R visitRelation(Relation obj, P param) {
    if (obj instanceof Equal) {
      return visitEqual((Equal) obj, param);
    } else if (obj instanceof NotEqual) {
      return visitNotequal((NotEqual) obj, param);
    } else if (obj instanceof Greater) {
      return visitGreater((Greater) obj, param);
    } else if (obj instanceof GreaterEqual) {
      return visitGreaterequal((GreaterEqual) obj, param);
    } else if (obj instanceof Less) {
      return visitLess((Less) obj, param);
    } else if (obj instanceof LessEqual) {
      return visitLessequal((LessEqual) obj, param);
    } else if (obj instanceof Is) {
      return visitIs((Is) obj, param);
    } else {
      throwUnknownObjectError(obj);
      return null;
    }
  }

  protected R visitRefItem(RefItem obj, P param) {
    if (obj instanceof RefIndex) {
      return visitRefIndex((RefIndex) obj, param);
    } else if (obj instanceof RefName) {
      return visitRefName((RefName) obj, param);
    } else if (obj instanceof RefCall) {
      return visitRefCall((RefCall) obj, param);
    } else if (obj instanceof RefTemplCall) {
      return visitRefTemplCall((RefTemplCall) obj, param);
    } else {
      throwUnknownObjectError(obj);
      return null;
    }
  }

  protected R visitType(Type obj, P param) {
    if (obj instanceof BaseType) {
      return visitBaseType((BaseType) obj, param);
    } else if (obj instanceof FunctionType) {
      return visitFunctionType((FunctionType) obj, param);
    } else if (obj instanceof NamedElementType) {
      return visitNamedElementType((NamedElementType) obj, param);
    } else if (obj instanceof EnumType) {
      return visitEnumType((EnumType) obj, param);
    } else if (obj instanceof ComponentType) {
      return visitComponentType((ComponentType) obj, param);
    } else if (obj instanceof TypeType) {
      return visitTypeType((TypeType) obj, param);
    } else if (obj instanceof TypeTemplate) {
      return visitTypeTemplate((TypeTemplate) obj, param);
    } else {
      throwUnknownObjectError(obj);
      return null;
    }
  }

  protected R visitComponent(Component obj, P param) {
    if (obj instanceof ImplElementary) {
      return visitImplElementary((ImplElementary) obj, param);
    } else if (obj instanceof ImplComposition) {
      return visitImplComposition((ImplComposition) obj, param);
    } else if (obj instanceof ImplHfsm) {
      return visitImplHfsm((ImplHfsm) obj, param);
    } else {
      throwUnknownObjectError(obj);
      return null;
    }
  }

  protected R visitNamedElementType(NamedElementType obj, P param) {
    if (obj instanceof RecordType) {
      return visitRecordType((RecordType) obj, param);
    } else if (obj instanceof UnionType) {
      return visitUnionType((UnionType) obj, param);
    } else if (obj instanceof UnsafeUnionType) {
      return visitUnsafeUnionType((UnsafeUnionType) obj, param);
    } else {
      throwUnknownObjectError(obj);
      return null;
    }
  }

  protected R visitBaseType(BaseType obj, P param) {
    if (obj instanceof BooleanType) {
      return visitBooleanType((BooleanType) obj, param);
    } else if (obj instanceof RangeType) {
      return visitRangeType((RangeType) obj, param);
    } else if (obj instanceof ArrayType) {
      return visitArrayType((ArrayType) obj, param);
    } else if (obj instanceof StringType) {
      return visitStringType((StringType) obj, param);
    } else if (obj instanceof VoidType) {
      return visitVoidType((VoidType) obj, param);
    } else if (obj instanceof NaturalType) {
      return visitNaturalType((NaturalType) obj, param);
    } else if (obj instanceof IntegerType) {
      return visitIntegerType((IntegerType) obj, param);
    } else if (obj instanceof AnyType) {
      return visitAnyType((AnyType) obj, param);
    } else if (obj instanceof IntType) {
      return visitIntType((IntType) obj, param);
    } else if (obj instanceof AliasType) {
      return visitAliasType((AliasType) obj, param);
    } else if (obj instanceof TupleType) {
      return visitTupleType((TupleType) obj, param);
    } else {
      throwUnknownObjectError(obj);
      return null;
    }
  }

  protected R visitIntType(IntType obj, P param) {
    if (obj instanceof SIntType) {
      return visitSIntType((SIntType) obj, param);
    } else if (obj instanceof UIntType) {
      return visitUIntType((UIntType) obj, param);
    } else {
      throwUnknownObjectError(obj);
      return null;
    }
  }

  protected R visitFunctionTemplate(FunctionTemplate obj, P param) {
    if (obj instanceof DefaultValueTemplate) {
      return visitDefaultValueTemplate((DefaultValueTemplate) obj, param);
    } else {
      throwUnknownObjectError(obj);
      return null;
    }
  }

  protected R visitTypeTemplate(TypeTemplate obj, P param) {
    if (obj instanceof ArrayTemplate) {
      return visitArrayTemplate((ArrayTemplate) obj, param);
    } else if (obj instanceof TypeTypeTemplate) {
      return visitTypeTypeTemplate((TypeTypeTemplate) obj, param);
    } else if (obj instanceof RangeTemplate) {
      return visitRangeTemplate((RangeTemplate) obj, param);
    } else {
      throwUnknownObjectError(obj);
      return null;
    }
  }

  protected R visitRawComponent(RawComponent obj, P param) {
    if (obj instanceof RawElementary) {
      return visitRawElementary((RawElementary) obj, param);
    } else if (obj instanceof RawComposition) {
      return visitRawComposition((RawComposition) obj, param);
    } else if (obj instanceof RawHfsm) {
      return visitRawHfsm((RawHfsm) obj, param);
    } else {
      throwUnknownObjectError(obj);
      return null;
    }
  }

  private void throwUnknownObjectError(Ast obj) {
    throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  abstract protected R visitCompUseRef(CompUseRef obj, P param);

  abstract protected R visitFuncRef(FunctionReference obj, P param);

  abstract protected R visitTypeRef(TypeReference obj, P param);

  abstract protected R visitRefExpr(ReferenceExpression obj, P param);

  abstract protected R visitNamedElement(NamedElement obj, P param);

  abstract protected R visitSynchroniusConnection(SynchroniusConnection obj, P param);

  abstract protected R visitAsynchroniusConnection(AsynchroniusConnection obj, P param);

  abstract protected R visitFuncReturnTuple(FuncReturnTuple obj, P param);

  abstract protected R visitFuncReturnType(FunctionReturnType obj, P param);

  abstract protected R visitFuncReturnNone(FuncReturnNone obj, P param);

  abstract protected R visitAliasType(AliasType obj, P param);

  abstract protected R visitTupleType(TupleType obj, P param);

  abstract protected R visitUIntType(UIntType obj, P param);

  abstract protected R visitSIntType(SIntType obj, P param);

  abstract protected R visitCompUse(ComponentUse obj, P param);

  abstract protected R visitQueue(Queue obj, P param);

  abstract protected R visitMsgPush(MsgPush obj, P param);

  abstract protected R visitAnyType(AnyType obj, P param);

  abstract protected R visitWhileStmt(WhileStmt obj, P param);

  abstract protected R visitForStmt(ForStmt obj, P param);

  abstract protected R visitCaseStmt(CaseStmt obj, P param);

  abstract protected R visitCaseOptRange(CaseOptRange obj, P param);

  abstract protected R visitCaseOptValue(CaseOptValue obj, P param);

  abstract protected R visitCaseOpt(CaseOpt obj, P param);

  abstract protected R visitIfOption(IfOption obj, P param);

  abstract protected R visitVarDef(VarDefStmt obj, P param);

  abstract protected R visitIfStmt(IfStatement obj, P param);

  abstract protected R visitCallStmt(CallStmt obj, P param);

  abstract protected R visitAssignmentSingle(AssignmentSingle obj, P param);

  abstract protected R visitAssignmentMulti(MultiAssignment obj, P param);

  abstract protected R visitReturnExpr(ExpressionReturn obj, P param);

  abstract protected R visitReturnVoid(VoidReturn obj, P param);

  abstract protected R visitBlock(Block obj, P param);

  abstract protected R visitEndpointSelf(EndpointSelf obj, P param);

  abstract protected R visitEndpointSub(EndpointSub obj, P param);

  abstract protected R visitEndpointRaw(EndpointRaw obj, P param);

  abstract protected R visitReference(LinkedReferenceWithOffset_Implementation obj, P param);

  abstract protected R visitStateSimple(StateSimple obj, P param);

  abstract protected R visitStateComposite(StateComposite obj, P param);

  abstract protected R visitNamespace(Namespace obj, P param);

  abstract protected R visitSubCallbacks(SubCallbacks obj, P param);

  abstract protected R visitConstPrivate(ConstPrivate obj, P param);

  abstract protected R visitConstGlobal(GlobalConstant obj, P param);

  abstract protected R visitFuncVariable(FunctionVariable obj, P param);

  abstract protected R visitStateVariable(StateVariable obj, P param);

  abstract protected R visitVoidType(VoidType obj, P param);

  abstract protected R visitStringType(StringType obj, P param);

  abstract protected R visitArrayType(ArrayType obj, P param);

  abstract protected R visitNaturalType(NaturalType obj, P param);

  abstract protected R visitComponentType(ComponentType obj, P param);

  abstract protected R visitIntegerType(IntegerType obj, P param);

  abstract protected R visitRangeType(RangeType obj, P param);

  abstract protected R visitBooleanType(BooleanType obj, P param);

  abstract protected R visitRefCall(RefCall obj, P param);

  abstract protected R visitRefName(RefName obj, P param);

  abstract protected R visitRefIndex(RefIndex obj, P param);

  abstract protected R visitImplHfsm(ImplHfsm obj, P param);

  abstract protected R visitImplComposition(ImplComposition obj, P param);

  abstract protected R visitImplElementary(ImplElementary obj, P param);

  abstract protected R visitTransition(Transition obj, P param);

  abstract protected R visitFuncSubHandlerQuery(FuncSubHandlerQuery obj, P param);

  abstract protected R visitFuncSubHandlerEvent(FuncSubHandlerEvent obj, P param);

  abstract protected R visitFuncProcedure(Procedure obj, P param);

  abstract protected R visitFuncFunction(FuncFunction obj, P param);

  abstract protected R visitFuncSignal(Signal obj, P param);

  abstract protected R visitFuncQuery(FuncQuery obj, P param);

  abstract protected R visitFuncSlot(Slot obj, P param);

  abstract protected R visitFuncResponse(Response obj, P param);

  abstract protected R visitRecordType(RecordType obj, P param);

  abstract protected R visitUnionType(UnionType obj, P param);

  abstract protected R visitUnsafeUnionType(UnsafeUnionType obj, P param);

  abstract protected R visitEnumType(EnumType obj, P param);

  abstract protected R visitEnumElement(EnumElement obj, P param);

  abstract protected R visitBoolValue(BooleanValue obj, P param);

  abstract protected R visitArrayValue(ArrayValue obj, P param);

  abstract protected R visitTupleValue(TupleValue obj, P param);

  abstract protected R visitStringValue(StringValue obj, P param);

  abstract protected R visitNumber(NumberValue obj, P param);

  abstract protected R visitAnyValue(AnyValue obj, P param);

  abstract protected R visitNamedValue(NamedValue obj, P param);

  abstract protected R visitUnsafeUnionValue(UnsafeUnionValue obj, P param);

  abstract protected R visitUnionValue(UnionValue obj, P param);

  abstract protected R visitNamedElementsValue(NamedElementsValue obj, P param);

  abstract protected R visitRecordValue(RecordValue obj, P param);

  abstract protected R visitTypeCast(TypeCast obj, P param);

  abstract protected R visitFunctionType(FunctionType obj, P param);

  abstract protected R visitUminus(Uminus obj, P param);

  abstract protected R visitNot(Not obj, P param);

  abstract protected R visitLogicNot(LogicNot obj, P param);

  abstract protected R visitBitNot(BitNot obj, P param);

  abstract protected R visitPlus(Plus obj, P param);

  abstract protected R visitMinus(Minus obj, P param);

  abstract protected R visitMul(Multiplication obj, P param);

  abstract protected R visitDiv(Division obj, P param);

  abstract protected R visitMod(Modulo obj, P param);

  abstract protected R visitOr(Or obj, P param);

  abstract protected R visitAnd(And obj, P param);

  abstract protected R visitBitAnd(BitAnd obj, P param);

  abstract protected R visitBitOr(BitOr obj, P param);

  abstract protected R visitBitXor(BitXor obj, P param);

  abstract protected R visitLogicOr(LogicOr obj, P param);

  abstract protected R visitLogicAnd(LogicAnd obj, P param);

  abstract protected R visitShr(Shr obj, P param);

  abstract protected R visitShl(Shl obj, P param);

  abstract protected R visitEqual(Equal obj, P param);

  abstract protected R visitNotequal(NotEqual obj, P param);

  abstract protected R visitLess(Less obj, P param);

  abstract protected R visitLessequal(LessEqual obj, P param);

  abstract protected R visitGreater(Greater obj, P param);

  abstract protected R visitGreaterequal(GreaterEqual obj, P param);

  abstract protected R visitIs(Is obj, P param);

  abstract protected R visitTemplate(Template obj, P param);

  abstract protected R visitRizzlyFile(RizzlyFile obj, P param);

  abstract protected R visitTemplateParameter(TemplateParameter obj, P param);

  abstract protected R visitRefTemplCall(RefTemplCall obj, P param);

  abstract protected R visitRawHfsm(RawHfsm obj, P param);

  abstract protected R visitRawComposition(RawComposition obj, P param);

  abstract protected R visitRawElementary(RawElementary obj, P param);

  abstract protected R visitDefaultValueTemplate(DefaultValueTemplate obj, P param);

  abstract protected R visitTypeType(TypeType obj, P param);

  abstract protected R visitTypeTypeTemplate(TypeTypeTemplate obj, P param);

  abstract protected R visitArrayTemplate(ArrayTemplate obj, P param);

  abstract protected R visitRangeTemplate(RangeTemplate obj, P param);

  abstract protected R visitVarDefInitStmt(VarDefInitStmt obj, P param);

  abstract protected R visitDummyLinkTarget(LinkTarget obj, P param);

  abstract protected R visitOffsetReference(OffsetReference obj, P param);

  abstract protected R visitSimpleReference(SimpleReference obj, P param);

  abstract protected R visitUnlinkedAnchor(UnlinkedAnchor obj, P param);

  abstract protected R visitLinkedAnchor(LinkedAnchor obj, P param);

}
