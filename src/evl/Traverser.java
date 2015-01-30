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

package evl;

import evl.composition.Connection;
import evl.composition.Endpoint;
import evl.composition.EndpointSelf;
import evl.composition.EndpointSub;
import evl.composition.ImplComposition;
import evl.expression.AnyValue;
import evl.expression.ArrayValue;
import evl.expression.BoolValue;
import evl.expression.ExprList;
import evl.expression.Expression;
import evl.expression.NamedElementValue;
import evl.expression.Number;
import evl.expression.RangeValue;
import evl.expression.RecordValue;
import evl.expression.StringValue;
import evl.expression.TypeCast;
import evl.expression.UnionValue;
import evl.expression.UnsafeUnionValue;
import evl.expression.binop.And;
import evl.expression.binop.ArithmeticOp;
import evl.expression.binop.BinaryExp;
import evl.expression.binop.BitAnd;
import evl.expression.binop.BitOr;
import evl.expression.binop.Div;
import evl.expression.binop.Equal;
import evl.expression.binop.Greater;
import evl.expression.binop.Greaterequal;
import evl.expression.binop.Is;
import evl.expression.binop.Less;
import evl.expression.binop.Lessequal;
import evl.expression.binop.LogicAnd;
import evl.expression.binop.LogicOr;
import evl.expression.binop.Logical;
import evl.expression.binop.Minus;
import evl.expression.binop.Mod;
import evl.expression.binop.Mul;
import evl.expression.binop.Notequal;
import evl.expression.binop.Or;
import evl.expression.binop.Plus;
import evl.expression.binop.Relation;
import evl.expression.binop.Shl;
import evl.expression.binop.Shr;
import evl.expression.reference.BaseRef;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefIndex;
import evl.expression.reference.RefItem;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.expression.reference.SimpleRef;
import evl.expression.unop.BitNot;
import evl.expression.unop.LogicNot;
import evl.expression.unop.Not;
import evl.expression.unop.Uminus;
import evl.expression.unop.UnaryExp;
import evl.function.Function;
import evl.function.header.FuncCtrlInDataIn;
import evl.function.header.FuncCtrlInDataOut;
import evl.function.header.FuncCtrlOutDataIn;
import evl.function.header.FuncCtrlOutDataOut;
import evl.function.header.FuncGlobal;
import evl.function.header.FuncPrivateRet;
import evl.function.header.FuncPrivateVoid;
import evl.function.header.FuncSubHandlerEvent;
import evl.function.header.FuncSubHandlerQuery;
import evl.hfsm.ImplHfsm;
import evl.hfsm.State;
import evl.hfsm.StateComposite;
import evl.hfsm.StateSimple;
import evl.hfsm.Transition;
import evl.other.CompUse;
import evl.other.Component;
import evl.other.EvlList;
import evl.other.ImplElementary;
import evl.other.Namespace;
import evl.other.Queue;
import evl.other.SubCallbacks;
import evl.statement.Assignment;
import evl.statement.Block;
import evl.statement.CallStmt;
import evl.statement.CaseOpt;
import evl.statement.CaseOptEntry;
import evl.statement.CaseOptRange;
import evl.statement.CaseOptValue;
import evl.statement.CaseStmt;
import evl.statement.IfOption;
import evl.statement.IfStmt;
import evl.statement.Return;
import evl.statement.ReturnExpr;
import evl.statement.ReturnVoid;
import evl.statement.Statement;
import evl.statement.VarDefStmt;
import evl.statement.WhileStmt;
import evl.statement.intern.MsgPush;
import evl.type.Type;
import evl.type.base.ArrayType;
import evl.type.base.BaseType;
import evl.type.base.BooleanType;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.type.base.FunctionType;
import evl.type.base.RangeType;
import evl.type.base.StringType;
import evl.type.composed.NamedElement;
import evl.type.composed.NamedElementType;
import evl.type.composed.RecordType;
import evl.type.composed.UnionType;
import evl.type.composed.UnsafeUnionType;
import evl.type.out.AliasType;
import evl.type.out.IntType;
import evl.type.out.SIntType;
import evl.type.out.UIntType;
import evl.type.special.AnyType;
import evl.type.special.ComponentType;
import evl.type.special.IntegerType;
import evl.type.special.NaturalType;
import evl.type.special.VoidType;
import evl.variable.ConstGlobal;
import evl.variable.ConstPrivate;
import evl.variable.Constant;
import evl.variable.DefVariable;
import evl.variable.FuncVariable;
import evl.variable.StateVariable;
import evl.variable.Variable;

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
      return visitFunctionImpl((Function) obj, param);
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
    } else if (obj instanceof Expression) {
      return visitExpression((Expression) obj, param);
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

  protected R visitFunctionImpl(Function obj, P param) {
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
    } else if (obj instanceof Function) {
      return visitFunctionImpl(obj, param);
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
    } else if (obj instanceof ExprList) {
      return visitExprList((ExprList) obj, param);
    } else if (obj instanceof BoolValue) {
      return visitBoolValue((BoolValue) obj, param);
    } else if (obj instanceof BinaryExp) {
      return visitBinaryExp((BinaryExp) obj, param);
    } else if (obj instanceof UnaryExp) {
      return visitUnaryExp((UnaryExp) obj, param);
    } else if (obj instanceof BaseRef) {
      return visitBaseRef((BaseRef) obj, param);
    } else if (obj instanceof RangeValue) {
      return visitRangeValue((RangeValue) obj, param);
    } else if (obj instanceof TypeCast) {
      return visitTypeCast((TypeCast) obj, param);
    } else if (obj instanceof AnyValue) {
      return visitAnyValue((AnyValue) obj, param);
    } else if (obj instanceof NamedElementValue) {
      return visitNamedElementValue((NamedElementValue) obj, param);
    } else if (obj instanceof UnionValue) {
      return visitUnionValue((UnionValue) obj, param);
    } else if (obj instanceof UnsafeUnionValue) {
      return visitUnsafeUnionValue((UnsafeUnionValue) obj, param);
    } else if (obj instanceof RecordValue) {
      return visitRecordValue((RecordValue) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitBaseRef(BaseRef obj, P param) {
    if (obj instanceof SimpleRef) {
      return visitTypeRef((SimpleRef) obj, param);
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

  abstract protected R visitAliasType(AliasType obj, P param);

  abstract protected R visitUIntType(UIntType obj, P param);

  abstract protected R visitSIntType(SIntType obj, P param);

  abstract protected R visitCompUse(CompUse obj, P param);

  abstract protected R visitQueue(Queue obj, P param);

  abstract protected R visitMsgPush(MsgPush obj, P param);

  abstract protected R visitAnyType(AnyType obj, P param);

  abstract protected R visitWhileStmt(WhileStmt obj, P param);

  abstract protected R visitCaseStmt(CaseStmt obj, P param);

  abstract protected R visitCaseOptRange(CaseOptRange obj, P param);

  abstract protected R visitCaseOptValue(CaseOptValue obj, P param);

  abstract protected R visitCaseOpt(CaseOpt obj, P param);

  abstract protected R visitIfOption(IfOption obj, P param);

  abstract protected R visitVarDef(VarDefStmt obj, P param);

  abstract protected R visitIfStmt(IfStmt obj, P param);

  abstract protected R visitCallStmt(CallStmt obj, P param);

  abstract protected R visitAssignment(Assignment obj, P param);

  abstract protected R visitReturnExpr(ReturnExpr obj, P param);

  abstract protected R visitReturnVoid(ReturnVoid obj, P param);

  abstract protected R visitBlock(Block obj, P param);

  abstract protected R visitTypeRef(SimpleRef obj, P param);

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

  abstract protected R visitExprList(ExprList obj, P param);

  abstract protected R visitStringValue(StringValue obj, P param);

  abstract protected R visitNumber(Number obj, P param);

  abstract protected R visitAnyValue(AnyValue obj, P param);

  abstract protected R visitNamedElementValue(NamedElementValue obj, P param);

  abstract protected R visitUnsafeUnionValue(UnsafeUnionValue obj, P param);

  abstract protected R visitUnionValue(UnionValue obj, P param);

  abstract protected R visitRecordValue(RecordValue obj, P param);

  abstract protected R visitRangeValue(RangeValue obj, P param);

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
