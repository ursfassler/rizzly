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

package evl.traverser;

import evl.data.Evl;
import evl.data.EvlList;
import evl.data.Namespace;
import evl.data.component.Component;
import evl.data.component.composition.CompUse;
import evl.data.component.composition.Connection;
import evl.data.component.composition.Endpoint;
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
import evl.data.expression.Expression;
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
import evl.data.expression.binop.ArithmeticOp;
import evl.data.expression.binop.BinaryExp;
import evl.data.expression.binop.BitAnd;
import evl.data.expression.binop.BitOr;
import evl.data.expression.binop.BitXor;
import evl.data.expression.binop.Div;
import evl.data.expression.binop.Equal;
import evl.data.expression.binop.Greater;
import evl.data.expression.binop.Greaterequal;
import evl.data.expression.binop.Is;
import evl.data.expression.binop.Less;
import evl.data.expression.binop.Lessequal;
import evl.data.expression.binop.LogicAnd;
import evl.data.expression.binop.LogicOr;
import evl.data.expression.binop.Logical;
import evl.data.expression.binop.Minus;
import evl.data.expression.binop.Mod;
import evl.data.expression.binop.Mul;
import evl.data.expression.binop.Notequal;
import evl.data.expression.binop.Or;
import evl.data.expression.binop.Plus;
import evl.data.expression.binop.Relation;
import evl.data.expression.binop.Shl;
import evl.data.expression.binop.Shr;
import evl.data.expression.reference.BaseRef;
import evl.data.expression.reference.RefCall;
import evl.data.expression.reference.RefIndex;
import evl.data.expression.reference.RefItem;
import evl.data.expression.reference.RefName;
import evl.data.expression.reference.Reference;
import evl.data.expression.reference.SimpleRef;
import evl.data.expression.unop.BitNot;
import evl.data.expression.unop.LogicNot;
import evl.data.expression.unop.Not;
import evl.data.expression.unop.Uminus;
import evl.data.expression.unop.UnaryExp;
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
import evl.data.function.ret.FuncReturn;
import evl.data.function.ret.FuncReturnNone;
import evl.data.function.ret.FuncReturnTuple;
import evl.data.function.ret.FuncReturnType;
import evl.data.statement.Assignment;
import evl.data.statement.AssignmentMulti;
import evl.data.statement.AssignmentSingle;
import evl.data.statement.Block;
import evl.data.statement.CallStmt;
import evl.data.statement.CaseOpt;
import evl.data.statement.CaseOptEntry;
import evl.data.statement.CaseOptRange;
import evl.data.statement.CaseOptValue;
import evl.data.statement.CaseStmt;
import evl.data.statement.ForStmt;
import evl.data.statement.IfOption;
import evl.data.statement.IfStmt;
import evl.data.statement.Return;
import evl.data.statement.ReturnExpr;
import evl.data.statement.ReturnVoid;
import evl.data.statement.Statement;
import evl.data.statement.VarDefStmt;
import evl.data.statement.WhileStmt;
import evl.data.statement.intern.MsgPush;
import evl.data.type.Type;
import evl.data.type.base.ArrayType;
import evl.data.type.base.BaseType;
import evl.data.type.base.BooleanType;
import evl.data.type.base.EnumElement;
import evl.data.type.base.EnumType;
import evl.data.type.base.FunctionType;
import evl.data.type.base.RangeType;
import evl.data.type.base.StringType;
import evl.data.type.base.TupleType;
import evl.data.type.composed.NamedElement;
import evl.data.type.composed.NamedElementType;
import evl.data.type.composed.RecordType;
import evl.data.type.composed.UnionType;
import evl.data.type.composed.UnsafeUnionType;
import evl.data.type.out.AliasType;
import evl.data.type.out.IntType;
import evl.data.type.out.SIntType;
import evl.data.type.out.UIntType;
import evl.data.type.special.AnyType;
import evl.data.type.special.ComponentType;
import evl.data.type.special.IntegerType;
import evl.data.type.special.NaturalType;
import evl.data.type.special.VoidType;
import evl.data.variable.ConstGlobal;
import evl.data.variable.ConstPrivate;
import evl.data.variable.Constant;
import evl.data.variable.DefVariable;
import evl.data.variable.FuncVariable;
import evl.data.variable.StateVariable;
import evl.data.variable.Variable;

public abstract class Traverser<R, P> {

  public R traverse(Evl obj, P param) {
    return visit(obj, param);
  }

  protected R visitList(EvlList<? extends Evl> list, P param) {
    for (Evl itr : new EvlList<Evl>(list)) {
      visit(itr, param);
    }
    return null;
  }

  protected R visit(Evl obj, P param) {
    if (obj == null) {
      throw new RuntimeException("object is null");
    } else if (obj instanceof Type) {
      return visitType((Type) obj, param);
    } else if (obj instanceof Function) {
      return visitFunction((Function) obj, param);
    } else if (obj instanceof Expression) {
      return visitExpression((Expression) obj, param);
    } else if (obj instanceof Variable) {
      return visitVariable((Variable) obj, param);
    } else if (obj instanceof SubCallbacks) {
      return visitSubCallbacks((SubCallbacks) obj, param);
    } else if (obj instanceof RefItem) {
      return visitRefItem((RefItem) obj, param);
    } else if (obj instanceof Namespace) {
      return visitNamespace((Namespace) obj, param);
    } else if (obj instanceof Connection) {
      return visitConnection((Connection) obj, param);
    } else if (obj instanceof State) {
      return visitState((State) obj, param);
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
    } else if (obj instanceof Queue) {
      return visitQueue((Queue) obj, param);
    } else if (obj instanceof EnumElement) {
      return visitEnumElement((EnumElement) obj, param);
    } else if (obj instanceof Component) {
      return visitComponent((Component) obj, param);
    } else if (obj instanceof NamedElement) {
      return visitNamedElement((NamedElement) obj, param);
    } else if (obj instanceof FuncReturn) {
      return visitFuncReturn((FuncReturn) obj, param);
    } else if (obj instanceof NamedValue) {
      return visitNamedValue((NamedValue) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitFuncReturn(FuncReturn obj, P param) {
    if (obj instanceof FuncReturnNone) {
      return visitFuncReturnNone((FuncReturnNone) obj, param);
    } else if (obj instanceof FuncReturnType) {
      return visitFuncReturnType((FuncReturnType) obj, param);
    } else if (obj instanceof FuncReturnTuple) {
      return visitFuncReturnTuple((FuncReturnTuple) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitCaseOptEntry(CaseOptEntry obj, P param) {
    if (obj instanceof CaseOptValue)
      return visitCaseOptValue((CaseOptValue) obj, param);
    else if (obj instanceof CaseOptRange)
      return visitCaseOptRange((CaseOptRange) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitStatement(Statement obj, P param) {
    if (obj instanceof Block)
      return visitBlock((Block) obj, param);
    else if (obj instanceof Assignment)
      return visitAssignment((Assignment) obj, param);
    else if (obj instanceof CallStmt)
      return visitCallStmt((CallStmt) obj, param);
    else if (obj instanceof IfStmt)
      return visitIfStmt((IfStmt) obj, param);
    else if (obj instanceof Return)
      return visitReturn((Return) obj, param);
    else if (obj instanceof VarDefStmt)
      return visitVarDef((VarDefStmt) obj, param);
    else if (obj instanceof WhileStmt)
      return visitWhileStmt((WhileStmt) obj, param);
    else if (obj instanceof CaseStmt)
      return visitCaseStmt((CaseStmt) obj, param);
    else if (obj instanceof MsgPush)
      return visitMsgPush((MsgPush) obj, param);
    else if (obj instanceof ForStmt)
      return visitForStmt((ForStmt) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitAssignment(Assignment obj, P param) {
    if (obj instanceof AssignmentSingle)
      return visitAssignmentSingle((AssignmentSingle) obj, param);
    else if (obj instanceof AssignmentMulti)
      return visitAssignmentMulti((AssignmentMulti) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitReturn(Return obj, P param) {
    if (obj instanceof ReturnVoid)
      return visitReturnVoid((ReturnVoid) obj, param);
    else if (obj instanceof ReturnExpr)
      return visitReturnExpr((ReturnExpr) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitEndpoint(Endpoint obj, P param) {
    if (obj instanceof EndpointSub) {
      return visitEndpointSub((EndpointSub) obj, param);
    } else if (obj instanceof EndpointSelf) {
      return visitEndpointSelf((EndpointSelf) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitState(State obj, P param) {
    if (obj instanceof StateComposite) {
      return visitStateComposite((StateComposite) obj, param);
    } else if (obj instanceof StateSimple) {
      return visitStateSimple((StateSimple) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitVariable(Variable obj, P param) {
    if (obj instanceof DefVariable) {
      return visitDefVariable((DefVariable) obj, param);
    } else if (obj instanceof FuncVariable) {
      return visitFuncVariable((FuncVariable) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitDefVariable(DefVariable obj, P param) {
    if (obj instanceof StateVariable) {
      return visitStateVariable((StateVariable) obj, param);
    } else if (obj instanceof Constant) {
      return visitConstant((Constant) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitConstant(Constant obj, P param) {
    if (obj instanceof ConstPrivate) {
      return visitConstPrivate((ConstPrivate) obj, param);
    } else if (obj instanceof ConstGlobal) {
      return visitConstGlobal((ConstGlobal) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitFunction(Function obj, P param) {
    if (obj instanceof FuncGlobal) {
      return visitFuncGlobal((FuncGlobal) obj, param);
    } else if (obj instanceof FuncPrivateVoid) {
      return visitFuncPrivateVoid((FuncPrivateVoid) obj, param);
    } else if (obj instanceof FuncCtrlInDataIn) {
      return visitFuncIfaceInVoid((FuncCtrlInDataIn) obj, param);
    } else if (obj instanceof FuncPrivateRet) {
      return visitFuncPrivateRet((FuncPrivateRet) obj, param);
    } else if (obj instanceof FuncSubHandlerEvent) {
      return visitFuncSubHandlerEvent((FuncSubHandlerEvent) obj, param);
    } else if (obj instanceof FuncSubHandlerQuery) {
      return visitFuncSubHandlerQuery((FuncSubHandlerQuery) obj, param);
    } else if (obj instanceof FuncCtrlInDataOut) {
      return visitFuncIfaceInRet((FuncCtrlInDataOut) obj, param);
    } else if (obj instanceof FuncCtrlOutDataIn) {
      return visitFuncIfaceOutRet((FuncCtrlOutDataIn) obj, param);
    } else if (obj instanceof FuncCtrlOutDataOut) {
      return visitFuncIfaceOutVoid((FuncCtrlOutDataOut) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitExpression(Expression obj, P param) {
    if (obj instanceof Number) {
      return visitNumber((Number) obj, param);
    } else if (obj instanceof StringValue) {
      return visitStringValue((StringValue) obj, param);
    } else if (obj instanceof ArrayValue) {
      return visitArrayValue((ArrayValue) obj, param);
    } else if (obj instanceof TupleValue) {
      return visitTupleValue((TupleValue) obj, param);
    } else if (obj instanceof BoolValue) {
      return visitBoolValue((BoolValue) obj, param);
    } else if (obj instanceof BinaryExp) {
      return visitBinaryExp((BinaryExp) obj, param);
    } else if (obj instanceof UnaryExp) {
      return visitUnaryExp((UnaryExp) obj, param);
    } else if (obj instanceof BaseRef) {
      return visitBaseRef((BaseRef) obj, param);
    } else if (obj instanceof TypeCast) {
      return visitTypeCast((TypeCast) obj, param);
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
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitBaseRef(BaseRef obj, P param) {
    if (obj instanceof SimpleRef) {
      return visitSimpleRef((SimpleRef) obj, param);
    } else if (obj instanceof Reference) {
      return visitReference((Reference) obj, param);
    } else if (obj instanceof Endpoint) {
      return visitEndpoint((Endpoint) obj, param);
    } else if (obj instanceof CompUse) {
      return visitCompUse((CompUse) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
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
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitBinaryExp(BinaryExp obj, P param) {
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
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitLogical(Logical obj, P param) {
    if (obj instanceof LogicAnd) {
      return visitLogicAnd((LogicAnd) obj, param);
    } else if (obj instanceof LogicOr) {
      return visitLogicOr((LogicOr) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitArithmeticOp(ArithmeticOp obj, P param) {
    if (obj instanceof Plus) {
      return visitPlus((Plus) obj, param);
    } else if (obj instanceof Minus) {
      return visitMinus((Minus) obj, param);
    } else if (obj instanceof Mul) {
      return visitMul((Mul) obj, param);
    } else if (obj instanceof Div) {
      return visitDiv((Div) obj, param);
    } else if (obj instanceof Mod) {
      return visitMod((Mod) obj, param);
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
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitRelation(Relation obj, P param) {
    if (obj instanceof Equal) {
      return visitEqual((Equal) obj, param);
    } else if (obj instanceof Notequal) {
      return visitNotequal((Notequal) obj, param);
    } else if (obj instanceof Greater) {
      return visitGreater((Greater) obj, param);
    } else if (obj instanceof Greaterequal) {
      return visitGreaterequal((Greaterequal) obj, param);
    } else if (obj instanceof Less) {
      return visitLess((Less) obj, param);
    } else if (obj instanceof Lessequal) {
      return visitLessequal((Lessequal) obj, param);
    } else if (obj instanceof Is) {
      return visitIs((Is) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitRefItem(RefItem obj, P param) {
    if (obj instanceof RefIndex) {
      return visitRefIndex((RefIndex) obj, param);
    } else if (obj instanceof RefName) {
      return visitRefName((RefName) obj, param);
    } else if (obj instanceof RefCall) {
      return visitRefCall((RefCall) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
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
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
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
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
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
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
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
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitIntType(IntType obj, P param) {
    if (obj instanceof SIntType) {
      return visitSIntType((SIntType) obj, param);
    } else if (obj instanceof UIntType) {
      return visitUIntType((UIntType) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  abstract protected R visitFuncReturnTuple(FuncReturnTuple obj, P param);

  abstract protected R visitFuncReturnType(FuncReturnType obj, P param);

  abstract protected R visitFuncReturnNone(FuncReturnNone obj, P param);

  abstract protected R visitAliasType(AliasType obj, P param);

  abstract protected R visitTupleType(TupleType obj, P param);

  abstract protected R visitUIntType(UIntType obj, P param);

  abstract protected R visitSIntType(SIntType obj, P param);

  abstract protected R visitCompUse(CompUse obj, P param);

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

  abstract protected R visitIfStmt(IfStmt obj, P param);

  abstract protected R visitCallStmt(CallStmt obj, P param);

  abstract protected R visitAssignmentSingle(AssignmentSingle obj, P param);

  abstract protected R visitAssignmentMulti(AssignmentMulti obj, P param);

  abstract protected R visitReturnExpr(ReturnExpr obj, P param);

  abstract protected R visitReturnVoid(ReturnVoid obj, P param);

  abstract protected R visitBlock(Block obj, P param);

  abstract protected R visitSimpleRef(SimpleRef obj, P param);

  abstract protected R visitEndpointSelf(EndpointSelf obj, P param);

  abstract protected R visitEndpointSub(EndpointSub obj, P param);

  abstract protected R visitReference(Reference obj, P param);

  abstract protected R visitStateSimple(StateSimple obj, P param);

  abstract protected R visitStateComposite(StateComposite obj, P param);

  abstract protected R visitNamespace(Namespace obj, P param);

  abstract protected R visitSubCallbacks(SubCallbacks obj, P param);

  abstract protected R visitConstPrivate(ConstPrivate obj, P param);

  abstract protected R visitConstGlobal(ConstGlobal obj, P param);

  abstract protected R visitFuncVariable(FuncVariable obj, P param);

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

  abstract protected R visitConnection(Connection obj, P param);

  abstract protected R visitFuncGlobal(FuncGlobal obj, P param);

  abstract protected R visitFuncSubHandlerQuery(FuncSubHandlerQuery obj, P param);

  abstract protected R visitFuncSubHandlerEvent(FuncSubHandlerEvent obj, P param);

  abstract protected R visitFuncPrivateVoid(FuncPrivateVoid obj, P param);

  abstract protected R visitFuncPrivateRet(FuncPrivateRet obj, P param);

  abstract protected R visitFuncIfaceOutVoid(FuncCtrlOutDataOut obj, P param);

  abstract protected R visitFuncIfaceOutRet(FuncCtrlOutDataIn obj, P param);

  abstract protected R visitFuncIfaceInVoid(FuncCtrlInDataIn obj, P param);

  abstract protected R visitFuncIfaceInRet(FuncCtrlInDataOut obj, P param);

  abstract protected R visitRecordType(RecordType obj, P param);

  abstract protected R visitUnionType(UnionType obj, P param);

  abstract protected R visitUnsafeUnionType(UnsafeUnionType obj, P param);

  abstract protected R visitEnumType(EnumType obj, P param);

  abstract protected R visitEnumElement(EnumElement obj, P param);

  abstract protected R visitBoolValue(BoolValue obj, P param);

  abstract protected R visitArrayValue(ArrayValue obj, P param);

  abstract protected R visitTupleValue(TupleValue obj, P param);

  abstract protected R visitStringValue(StringValue obj, P param);

  abstract protected R visitNumber(Number obj, P param);

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

  abstract protected R visitMul(Mul obj, P param);

  abstract protected R visitDiv(Div obj, P param);

  abstract protected R visitMod(Mod obj, P param);

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

  abstract protected R visitNotequal(Notequal obj, P param);

  abstract protected R visitLess(Less obj, P param);

  abstract protected R visitLessequal(Lessequal obj, P param);

  abstract protected R visitGreater(Greater obj, P param);

  abstract protected R visitGreaterequal(Greaterequal obj, P param);

  abstract protected R visitIs(Is obj, P param);

  abstract protected R visitNamedElement(NamedElement obj, P param);

}
