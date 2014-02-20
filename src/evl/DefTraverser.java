package evl;

import java.util.ArrayList;
import java.util.Collection;

import common.Direction;

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
import evl.statement.Statement;
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
import evl.type.composed.UnionSelector;
import evl.type.composed.UnionType;
import evl.type.special.AnyType;
import evl.type.special.ComponentType;
import evl.type.special.IntegerType;
import evl.type.special.NaturalType;
import evl.type.special.VoidType;
import evl.variable.ConstGlobal;
import evl.variable.ConstPrivate;
import evl.variable.FuncVariable;
import evl.variable.StateVariable;

public class DefTraverser<R, P> extends Traverser<R, P> {

  protected R visitList(Collection<? extends Evl> list, P param) {
    for (Evl itr : new ArrayList<Evl>(list)) {
      visit(itr, param);
    }
    return null;
  }

  @Override
  protected R visitImplElementary(ImplElementary obj, P param) {
    visitList(obj.getInput().getList(), param);
    visitList(obj.getOutput().getList(), param);

    visitList(obj.getConstant().getList(), param);
    visitList(obj.getVariable().getList(), param);
    visitList(obj.getComponent().getList(), param);
    visitList(obj.getFunction().getList(), param);
    visitList(obj.getSubCallback().getList(), param);
    visit(obj.getEntryFunc(), param);
    visit(obj.getExitFunc(), param);
    return null;
  }

  @Override
  protected R visitImplComposition(ImplComposition obj, P param) {
    visitList(obj.getInput().getList(), param);
    visitList(obj.getOutput().getList(), param);

    visitList(obj.getComponent().getList(), param);
    visitList(obj.getConnection(), param);
    return null;
  }

  @Override
  protected R visitImplHfsm(ImplHfsm obj, P param) {
    visitList(obj.getInput().getList(), param);
    visitList(obj.getOutput().getList(), param);

    visit(obj.getTopstate(), param);
    return null;
  }

  @Override
  protected R visitNumber(Number obj, P param) {
    return null;
  }

  @Override
  protected R visitVarDef(VarDefStmt obj, P param) {
    visit(obj.getVariable(), param);
    return null;
  }

  @Override
  protected R visitCallStmt(CallStmt obj, P param) {
    visit(obj.getCall(), param);
    return null;
  }

  @Override
  protected R visitAssignment(Assignment obj, P param) {
    visit(obj.getRight(), param);
    visit(obj.getLeft(), param);
    return null;
  }

  @Override
  protected R visitUnionType(UnionType obj, P param) {
    visitList(obj.getElement().getList(), param);
    return null;
  }

  @Override
  protected R visitRecordType(RecordType obj, P param) {
    visitList(obj.getElement().getList(), param);
    return null;
  }

  @Override
  protected R visitComponentType(ComponentType obj, P param) {
    visitList(obj.getElement().getList(), param);
    return null;
  }

  @Override
  protected R visitRizzlyProgram(RizzlyProgram obj, P param) {
    visitList(obj.getType().getList(), param);
    visitList(obj.getConstant().getList(), param);
    visitList(obj.getVariable().getList(), param);
    visitList(obj.getFunction().getList(), param);
    return null;
  }

  @Override
  protected R visitRefName(RefName obj, P param) {
    return null;
  }

  @Override
  protected R visitRefIndex(RefIndex obj, P param) {
    visit(obj.getIndex(), param);
    return null;
  }

  @Override
  protected R visitRefCall(RefCall obj, P param) {
    visitItr(obj.getActualParameter(), param);
    return null;
  }

  @Override
  protected R visitArrayType(ArrayType obj, P param) {
    visit(obj.getType(), param);
    return null;
  }

  @Override
  protected R visitBooleanType(BooleanType obj, P param) {
    return null;
  }

  @Override
  protected R visitEnumType(EnumType obj, P param) {
    visitItr(obj.getElement(), param);
    return null;
  }

  @Override
  protected R visitEnumElement(EnumElement obj, P param) {
    visit(obj.getType(), param);
    visit(obj.getDef(), param);
    return null;
  }

  @Override
  protected R visitReturnExpr(ReturnExpr obj, P param) {
    visit(obj.getExpr(), param);
    return null;
  }

  @Override
  protected R visitReturnVoid(ReturnVoid obj, P param) {
    return null;
  }

  @Override
  protected R visitVoidType(VoidType obj, P param) {
    return null;
  }

  @Override
  protected R visitCompUse(CompUse obj, P param) {
    return null;
  }

  @Override
  protected R visitFuncVariable(FuncVariable obj, P param) {
    visit(obj.getType(), param);
    return null;
  }

  @Override
  protected R visitStateVariable(StateVariable obj, P param) {
    visit(obj.getType(), param);
    return null;
  }

  @Override
  protected R visitConstPrivate(ConstPrivate obj, P param) {
    visit(obj.getType(), param);
    visit(obj.getDef(), param);
    return null;
  }

  @Override
  protected R visitConstGlobal(ConstGlobal obj, P param) {
    visit(obj.getType(), param);
    visit(obj.getDef(), param);
    return null;
  }

  @Override
  protected R visitSubCallbacks(SubCallbacks obj, P param) {
    visitItr(obj.getList(), param);
    return null;
  }

  @Override
  protected R visitNamespace(Namespace obj, P param) {
    visitList(obj.getList(), param);
    return null;
  }

  @Override
  protected R visitNamedElement(NamedElement obj, P param) {
    visit(obj.getType(), param);
    return null;
  }

  @Override
  protected R visitCaseOptRange(CaseOptRange obj, P param) {
    visit(obj.getStart(), param);
    visit(obj.getEnd(), param);
    return null;
  }

  @Override
  protected R visitCaseOptValue(CaseOptValue obj, P param) {
    visit(obj.getValue(), param);
    return null;
  }

  @Override
  protected R visitBoolValue(BoolValue obj, P param) {
    return null;
  }

  @Override
  protected R visitConnection(Connection obj, P param) {
    visit(obj.getEndpoint(Direction.in), param);
    visit(obj.getEndpoint(Direction.out), param);
    return null;
  }

  @Override
  protected R visitStringValue(StringValue obj, P param) {
    return null;
  }

  @Override
  protected R visitStringType(StringType obj, P param) {
    return null;
  }

  @Override
  protected R visitArrayValue(ArrayValue obj, P param) {
    visitItr(obj.getValue(), param);
    return null;
  }

  @Override
  protected R visitStateSimple(StateSimple obj, P param) {
    visit(obj.getEntryFunc(), param);
    visit(obj.getExitFunc(), param);
    visitItr(obj.getVariable(), param);
    visitItr(obj.getFunction(), param);
    visitItr(obj.getItem(), param);
    return null;
  }

  @Override
  protected R visitStateComposite(StateComposite obj, P param) {
    visit(obj.getEntryFunc(), param);
    visit(obj.getExitFunc(), param);
    visitItr(obj.getVariable(), param);
    visitItr(obj.getFunction(), param);
    visitItr(obj.getItem(), param);
    return null;
  }

  @Override
  protected R visitTransition(Transition obj, P param) {
    visitItr(obj.getParam(), param);
    visit(obj.getGuard(), param);
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected R visitReference(Reference obj, P param) {
    visitItr(obj.getOffset(), param);
    return null;
  }

  @Override
  protected R visitNaturalType(NaturalType obj, P param) {
    return null;
  }

  @Override
  protected R visitIntegerType(IntegerType obj, P param) {
    return null;
  }

  @Override
  protected R visitFuncInputHandlerEvent(FuncInputHandlerEvent obj, P param) {
    visitItr(obj.getParam(), param);
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected R visitFuncInputHandlerQuery(FuncInputHandlerQuery obj, P param) {
    visitItr(obj.getParam(), param);
    visit(obj.getRet(), param);
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected R visitFuncPrivateVoid(FuncPrivateVoid obj, P param) {
    visitItr(obj.getParam(), param);
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected R visitFuncPrivateRet(FuncPrivateRet obj, P param) {
    visitItr(obj.getParam(), param);
    visit(obj.getRet(), param);
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected R visitFuncProtoVoid(FuncProtoVoid obj, P param) {
    visitItr(obj.getParam(), param);
    return null;
  }

  @Override
  protected R visitHfsmQueryFunction(HfsmQueryFunction obj, P param) {
    visitItr(obj.getParam(), param);
    visit(obj.getRet(), param);
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected R visitFuncProtoRet(FuncProtoRet obj, P param) {
    visitItr(obj.getParam(), param);
    visit(obj.getRet(), param);
    return null;
  }

  @Override
  protected R visitFuncGlobal(FuncGlobal obj, P param) {
    visitItr(obj.getParam(), param);
    visit(obj.getRet(), param);
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected R visitFuncSubHandlerQuery(FuncSubHandlerQuery obj, P param) {
    visitItr(obj.getParam(), param);
    visit(obj.getRet(), param);
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected R visitFuncSubHandlerEvent(FuncSubHandlerEvent obj, P param) {
    visitItr(obj.getParam(), param);
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected R visitFunctionTypeVoid(FunctionTypeVoid obj, P param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected R visitFunctionTypeRet(FunctionTypeRet obj, P param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected R visitEndpointSelf(EndpointSelf obj, P param) {
    return null;
  }

  @Override
  protected R visitEndpointSub(EndpointSub obj, P param) {
    return null;
  }

  @Override
  protected R visitTypeRef(TypeRef obj, P param) {
    return null;
  }

  @Override
  protected R visitUnionSelector(UnionSelector obj, P param) {
    return null;
  }

  @Override
  protected R visitAnd(And obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitDiv(Div obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitEqual(Equal obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitGreater(Greater obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitGreaterequal(Greaterequal obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitLess(Less obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitLessequal(Lessequal obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitMinus(Minus obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitMod(Mod obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitMul(Mul obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitNot(Not obj, P param) {
    visit(obj.getExpr(), param);
    return null;
  }

  @Override
  protected R visitLogicNot(LogicNot obj, P param) {
    visit(obj.getExpr(), param);
    return null;
  }

  @Override
  protected R visitBitNot(BitNot obj, P param) {
    visit(obj.getExpr(), param);
    return null;
  }

  @Override
  protected R visitNotequal(Notequal obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitOr(Or obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitPlus(Plus obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitShl(Shl obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitShr(Shr obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitUminus(Uminus obj, P param) {
    visit(obj.getExpr(), param);
    return null;
  }

  @Override
  protected R visitRangeType(RangeType obj, P param) {
    return null;
  }

  @Override
  protected R visitRangeValue(RangeValue obj, P param) {
    return null;
  }

  @Override
  protected R visitEnumDefRef(EnumDefRef obj, P param) {
    return null;
  }

  @Override
  protected R visitWhileStmt(WhileStmt obj, P param) {
    visit(obj.getCondition(), param);
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected R visitIfStmt(IfStmt obj, P param) {
    visitItr(obj.getOption(), param);
    visit(obj.getDefblock(), param);
    return null;
  }

  @Override
  protected R visitIfOption(IfOption obj, P param) {
    visit(obj.getCondition(), param);
    visit(obj.getCode(), param);
    return null;
  }

  @Override
  protected R visitBlock(Block obj, P param) {
    for (Statement stmt : obj.getStatements()) {
      visit(stmt, param);
    }
    return null;
  }

  @Override
  protected R visitCaseStmt(CaseStmt obj, P param) {
    visit(obj.getCondition(), param);
    visitItr(obj.getOption(), param);
    visit(obj.getOtherwise(), param);
    return null;
  }

  @Override
  protected R visitCaseOpt(CaseOpt obj, P param) {
    visitItr(obj.getValue(), param);
    visit(obj.getCode(), param);
    return null;
  }

  @Override
  protected R visitTypeCast(TypeCast obj, P param) {
    visit(obj.getCast(), param);
    visit(obj.getValue(), param);
    return null;
  }

  @Override
  protected R visitBitAnd(BitAnd obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitBitOr(BitOr obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitLogicOr(LogicOr obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitLogicAnd(LogicAnd obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitFuncIfaceOutVoid(FuncIfaceOutVoid obj, P param) {
    visitItr(obj.getParam(), param);
    return null;
  }

  @Override
  protected R visitFuncIfaceOutRet(FuncIfaceOutRet obj, P param) {
    visitItr(obj.getParam(), param);
    visit(obj.getRet(), param);
    return null;
  }

  @Override
  protected R visitFuncIfaceInVoid(FuncIfaceInVoid obj, P param) {
    visitItr(obj.getParam(), param);
    return null;
  }

  @Override
  protected R visitFuncIfaceInRet(FuncIfaceInRet obj, P param) {
    visitItr(obj.getParam(), param);
    visit(obj.getRet(), param);
    return null;
  }

  @Override
  protected R visitAnyType(AnyType obj, P param) {
    return null;
  }

}
