package evl;

import evl.statement.GetElementPtr;
import evl.statement.LoadStmt;
import evl.statement.StoreStmt;
import evl.type.special.PointerType;
import java.util.ArrayList;
import java.util.Collection;

import common.Direction;

import evl.cfg.BasicBlock;
import evl.cfg.BasicBlockList;
import evl.cfg.CaseGoto;
import evl.cfg.CaseGotoOpt;
import evl.cfg.CaseOptRange;
import evl.cfg.CaseOptValue;
import evl.cfg.Goto;
import evl.cfg.IfGoto;
import evl.cfg.PhiStmt;
import evl.cfg.ReturnExpr;
import evl.cfg.ReturnVoid;
import evl.composition.Connection;
import evl.composition.EndpointSelf;
import evl.composition.EndpointSub;
import evl.composition.ImplComposition;
import evl.expression.ArithmeticOp;
import evl.expression.ArrayValue;
import evl.expression.BoolValue;
import evl.expression.Number;
import evl.expression.Relation;
import evl.expression.StringValue;
import evl.expression.TypeCast;
import evl.expression.UnaryExpression;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefIndex;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.function.FuncWithBody;
import evl.function.FuncWithReturn;
import evl.function.FunctionBase;
import evl.function.impl.FuncGlobal;
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
import evl.hfsm.QueryItem;
import evl.hfsm.StateComposite;
import evl.hfsm.StateSimple;
import evl.hfsm.Transition;
import evl.other.CompUse;
import evl.other.IfaceUse;
import evl.other.ImplElementary;
import evl.other.Interface;
import evl.other.Named;
import evl.other.NamedList;
import evl.other.Namespace;
import evl.other.RizzlyProgram;
import evl.statement.Assignment;
import evl.statement.CallStmt;
import evl.statement.VarDefInitStmt;
import evl.statement.VarDefStmt;
import evl.type.TypeRef;
import evl.type.base.ArrayType;
import evl.type.base.BooleanType;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.type.base.FunctionTypeRet;
import evl.type.base.FunctionTypeVoid;
import evl.type.base.Range;
import evl.type.base.StringType;
import evl.type.composed.NamedElement;
import evl.type.composed.RecordType;
import evl.type.composed.UnionType;
import evl.type.special.ComponentType;
import evl.type.special.IntegerType;
import evl.type.special.InterfaceType;
import evl.type.special.NaturalType;
import evl.type.special.VoidType;
import evl.variable.ConstGlobal;
import evl.variable.ConstPrivate;
import evl.variable.FuncVariable;
import evl.variable.SsaVariable;
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
    visitList(obj.getIface(Direction.in).getList(), param);
    visitList(obj.getIface(Direction.out).getList(), param);

    visitList(obj.getConstant().getList(), param);
    visitList(obj.getVariable().getList(), param);
    visitList(obj.getComponent().getList(), param);
    visitList(obj.getInternalFunction().getList(), param);
    visitList(obj.getInputFunc().getList(), param);
    visitList(obj.getSubComCallback().getList(), param);
    visit(obj.getEntryFunc(), param);
    visit(obj.getExitFunc(), param);
    return null;
  }

  @Override
  protected R visitImplComposition(ImplComposition obj, P param) {
    visitList(obj.getIface(Direction.in).getList(), param);
    visitList(obj.getIface(Direction.out).getList(), param);

    visitList(obj.getComponent().getList(), param);
    visitList(obj.getConnection(), param);
    return null;
  }

  @Override
  protected R visitImplHfsm(ImplHfsm obj, P param) {
    visitList(obj.getIface(Direction.in).getList(), param);
    visitList(obj.getIface(Direction.out).getList(), param);

    visit(obj.getTopstate(), param);
    return null;
  }

  @Override
  protected R visitInterface(Interface obj, P param) {
    visitList(obj.getPrototype().getList(), param);
    return null;
  }

  @Override
  protected R visitNumber(Number obj, P param) {
    return null;
  }

  @Override
  protected R visitUnaryExpression(UnaryExpression obj, P param) {
    visit(obj.getExpr(), param);
    return null;
  }

  @Override
  protected R visitRelation(Relation obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitArithmeticOp(ArithmeticOp obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitVarDef(VarDefStmt obj, P param) {
    visit(obj.getVariable(), param);
    return null;
  }

  @Override
  protected R visitVarDefInitStmt(VarDefInitStmt obj, P param) {
    visit(obj.getVariable(), param);
    visit(obj.getInit(), param);
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
  protected R visitIfaceUse(IfaceUse obj, P param) {
    return null;
  }

  @Override
  protected R visitCompUse(CompUse obj, P param) {
    return null;
  }

  @Override
  protected R visitFuncVariable(FuncVariable obj, P param) {
    return null;
  }

  @Override
  protected R visitStateVariable(StateVariable obj, P param) {
    return null;
  }

  @Override
  protected R visitConstPrivate(ConstPrivate obj, P param) {
    visit(obj.getDef(), param);
    return null;
  }

  @Override
  protected R visitConstGlobal(ConstGlobal obj, P param) {
    visit(obj.getDef(), param);
    return null;
  }

  @Override
  protected R visitNamedList(NamedList<Named> obj, P param) {
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
  protected R visitQueryItem(QueryItem obj, P param) {
    visit(obj.getFunc(), param);
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
  protected R visitFunctionBase(FunctionBase obj, P param) {
    visitItr(obj.getParam(), param);
    if (obj instanceof FuncWithReturn) {
      visit(((FuncWithReturn) obj).getRet(), param);
    }
    if (obj instanceof FuncWithBody) {
      visit(((FuncWithBody) obj).getBody(), param);
    }
    return super.visitFunctionBase(obj, param);
  }

  @Override
  protected R visitFuncInputHandlerEvent(FuncInputHandlerEvent obj, P param) {
    return null;
  }

  @Override
  protected R visitFuncInputHandlerQuery(FuncInputHandlerQuery obj, P param) {
    return null;
  }

  @Override
  protected R visitFuncPrivateVoid(FuncPrivateVoid obj, P param) {
    return null;
  }

  @Override
  protected R visitFuncPrivateRet(FuncPrivateRet obj, P param) {
    return null;
  }

  @Override
  protected R visitFuncProtoVoid(FuncProtoVoid obj, P param) {
    return null;
  }

  @Override
  protected R visitHfsmQueryFunction(HfsmQueryFunction obj, P param) {
    return null;
  }

  @Override
  protected R visitFuncProtoRet(FuncProtoRet obj, P param) {
    return null;
  }

  @Override
  protected R visitFuncGlobal(FuncGlobal obj, P param) {
    return null;
  }

  @Override
  protected R visitFuncSubHandlerQuery(FuncSubHandlerQuery obj, P param) {
    return null;
  }

  @Override
  protected R visitFuncSubHandlerEvent(FuncSubHandlerEvent obj, P param) {
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
  protected R visitInterfaceType(InterfaceType obj, P param) {
    visitItr(obj.getPrototype(), param);
    return null;
  }

  @Override
  protected R visitComponentType(ComponentType obj, P param) {
    visitItr(obj.getIface(Direction.in), param);
    visitItr(obj.getIface(Direction.out), param);
    return null;
  }

  @Override
  protected R visitRange(Range obj, P param) {
    return null;
  }

  @Override
  protected R visitPhiStmt(PhiStmt obj, P param) {
    visit(obj.getVariable(), param);
    return null;
  }

  @Override
  protected R visitBasicBlock(BasicBlock obj, P param) {
    visitItr(obj.getPhi(), param);
    visitItr(obj.getCode(), param);
    visit(obj.getEnd(), param);
    return null;
  }

  @Override
  protected R visitGoto(Goto obj, P param) {
    return null;
  }

  @Override
  protected R visitCaseGotoOpt(CaseGotoOpt obj, P param) {
    visitItr(obj.getValue(), param);
    return null;
  }

  @Override
  protected R visitCaseGoto(CaseGoto obj, P param) {
    visit(obj.getCondition(), param);
    visitItr(obj.getOption(), param);
    return null;
  }

  @Override
  protected R visitIfGoto(IfGoto obj, P param) {
    visit(obj.getCondition(), param);
    return null;
  }

  @Override
  protected R visitBasicBlockList(BasicBlockList obj, P param) {
    visit(obj.getEntry(), param);
    visitItr(obj.getBasicBlocks(), param);
    visit(obj.getExit(), param);
    return null;
  }

  @Override
  protected R visitSsaVariable(SsaVariable obj, P param) {
    return null;
  }

  @Override
  protected R visitTypeCast(TypeCast obj, P param) {
    visit(obj.getRef(),param);
    visit(obj.getCast(),param);
    return null;
  }

  @Override
  protected R visitTypeRef(TypeRef obj, P param) {
    return null;
  }

  @Override
  protected R visitStoreStmt(StoreStmt obj, P param) {
    visit(obj.getExpr(),param);
    visit(obj.getAddress(),param);
    return null;
  }

  @Override
  protected R visitLoadStmt(LoadStmt obj, P param) {
    visit(obj.getAddress(),param);
    visit(obj.getVariable(),param);
    return null;
  }

  @Override
  protected R visitGetElementPtr(GetElementPtr obj, P param) {
    visit(obj.getAddress(),param);
    visit(obj.getVariable(),param);
    return null;
  }

  @Override
  protected R visitPointerType(PointerType obj, P param) {
    visit(obj.getType(),param);
    return null;
  }

}
