package evl;

import evl.composition.Connection;
import evl.composition.EndpointSelf;
import evl.composition.EndpointSub;
import evl.composition.ImplComposition;
import evl.expression.ArrayValue;
import evl.expression.BoolValue;
import evl.expression.Number;
import evl.expression.RangeValue;
import evl.expression.StringValue;
import evl.expression.TypeCast;
import evl.expression.binop.And;
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
import evl.expression.binop.Minus;
import evl.expression.binop.Mod;
import evl.expression.binop.Mul;
import evl.expression.binop.Notequal;
import evl.expression.binop.Or;
import evl.expression.binop.Plus;
import evl.expression.binop.Shl;
import evl.expression.binop.Shr;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefIndex;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.expression.unop.BitNot;
import evl.expression.unop.LogicNot;
import evl.expression.unop.Not;
import evl.expression.unop.Uminus;
import evl.function.impl.FuncGlobal;
import evl.function.impl.FuncIfaceInRet;
import evl.function.impl.FuncIfaceInVoid;
import evl.function.impl.FuncIfaceOutRet;
import evl.function.impl.FuncIfaceOutVoid;
import evl.function.impl.FuncInputHandlerEvent;
import evl.function.impl.FuncInputHandlerQuery;
import evl.function.impl.FuncPrivateRet;
import evl.function.impl.FuncPrivateVoid;
import evl.function.impl.FuncProtoRet;
import evl.function.impl.FuncProtoVoid;
import evl.function.impl.FuncSubHandlerEvent;
import evl.function.impl.FuncSubHandlerQuery;
import evl.hfsm.HfsmQueryFunction;
import evl.hfsm.ImplHfsm;
import evl.hfsm.StateComposite;
import evl.hfsm.StateSimple;
import evl.hfsm.Transition;
import evl.other.CompUse;
import evl.other.ImplElementary;
import evl.other.Namespace;
import evl.other.RizzlyProgram;
import evl.other.SubCallbacks;
import evl.statement.Assignment;
import evl.statement.Block;
import evl.statement.CallStmt;
import evl.statement.CaseOpt;
import evl.statement.CaseOptRange;
import evl.statement.CaseOptValue;
import evl.statement.CaseStmt;
import evl.statement.IfOption;
import evl.statement.IfStmt;
import evl.statement.ReturnExpr;
import evl.statement.ReturnVoid;
import evl.statement.VarDefStmt;
import evl.statement.WhileStmt;
import evl.type.TypeRef;
import evl.type.base.ArrayType;
import evl.type.base.BooleanType;
import evl.type.base.EnumDefRef;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.type.base.FunctionTypeRet;
import evl.type.base.FunctionTypeVoid;
import evl.type.base.RangeType;
import evl.type.base.StringType;
import evl.type.composed.NamedElement;
import evl.type.composed.RecordType;
import evl.type.composed.UnionType;
import evl.type.composed.UnsafeUnionType;
import evl.type.special.AnyType;
import evl.type.special.ComponentType;
import evl.type.special.IntegerType;
import evl.type.special.NaturalType;
import evl.type.special.VoidType;
import evl.variable.ConstGlobal;
import evl.variable.ConstPrivate;
import evl.variable.FuncVariable;
import evl.variable.StateVariable;

abstract public class NullTraverser<R, P> extends Traverser<R, P> {

  abstract protected R visitDefault(Evl obj, P param);

  @Override
  protected R visitAnyType(AnyType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncIfaceOutVoid(FuncIfaceOutVoid obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncIfaceOutRet(FuncIfaceOutRet obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncIfaceInVoid(FuncIfaceInVoid obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncIfaceInRet(FuncIfaceInRet obj, P param) {
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
  protected R visitCompUse(CompUse obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitRizzlyProgram(RizzlyProgram obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitImplElementary(ImplElementary obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncProtoRet(FuncProtoRet obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncGlobal(FuncGlobal obj, P param) {
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
  protected R visitNumber(Number obj, P param) {
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
  protected R visitConstPrivate(ConstPrivate obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitConstGlobal(ConstGlobal obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitNamedElement(NamedElement obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitArrayType(ArrayType obj, P param) {
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
  protected R visitHfsmQueryFunction(HfsmQueryFunction obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitReference(Reference obj, P param) {
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
  protected R visitFuncInputHandlerEvent(FuncInputHandlerEvent obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncInputHandlerQuery(FuncInputHandlerQuery obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncPrivateVoid(FuncPrivateVoid obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncPrivateRet(FuncPrivateRet obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncProtoVoid(FuncProtoVoid obj, P param) {
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
  protected R visitFunctionTypeVoid(FunctionTypeVoid obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFunctionTypeRet(FunctionTypeRet obj, P param) {
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
  protected R visitTypeRef(TypeRef obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitAnd(And obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitDiv(Div obj, P param) {
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
  protected R visitGreaterequal(Greaterequal obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitLess(Less obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitLessequal(Lessequal obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitMinus(Minus obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitMod(Mod obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitMul(Mul obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitNot(Not obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitNotequal(Notequal obj, P param) {
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
  protected R visitRangeValue(RangeValue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitEnumDefRef(EnumDefRef obj, P param) {
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
}
