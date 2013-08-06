package evl;

import evl.cfg.BasicBlock;
import evl.cfg.BasicBlockList;
import evl.cfg.CaseGoto;
import evl.cfg.CaseGotoOpt;
import evl.cfg.Goto;
import evl.cfg.IfGoto;
import evl.cfg.PhiStmt;
import evl.composition.Connection;
import evl.composition.Endpoint;
import evl.composition.EndpointSelf;
import evl.composition.EndpointSub;
import evl.composition.ImplComposition;
import evl.expression.ArithmeticOp;
import evl.expression.ArrayValue;
import evl.expression.BoolValue;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.Relation;
import evl.expression.StringValue;
import evl.expression.UnaryExpression;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefIndex;
import evl.expression.reference.RefItem;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
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
import evl.hfsm.State;
import evl.hfsm.StateComposite;
import evl.hfsm.StateItem;
import evl.hfsm.StateSimple;
import evl.hfsm.Transition;
import evl.other.CompUse;
import evl.other.Component;
import evl.other.IfaceUse;
import evl.other.ImplElementary;
import evl.other.Interface;
import evl.other.Named;
import evl.other.NamedList;
import evl.other.Namespace;
import evl.other.RizzlyProgram;
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
import evl.statement.While;
import evl.type.Type;
import evl.type.base.Array;
import evl.type.base.BaseType;
import evl.type.base.BooleanType;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.type.base.FunctionType;
import evl.type.base.FunctionTypeRet;
import evl.type.base.FunctionTypeVoid;
import evl.type.base.Range;
import evl.type.base.StringType;
import evl.type.base.TypeAlias;
import evl.type.composed.NamedElement;
import evl.type.composed.NamedElementType;
import evl.type.composed.RecordType;
import evl.type.composed.UnionType;
import evl.type.special.ComponentType;
import evl.type.special.IntegerType;
import evl.type.special.InterfaceType;
import evl.type.special.NaturalType;
import evl.type.special.VoidType;
import evl.variable.ConstGlobal;
import evl.variable.ConstPrivate;
import evl.variable.Constant;
import evl.variable.FuncVariable;
import evl.variable.SsaVariable;
import evl.variable.StateVariable;
import evl.variable.Variable;

public abstract class Traverser<R, P> {

  public R traverse(Evl obj, P param) {
    return visit(obj, param);
  }

  protected R visitItr(Iterable<? extends Evl> list, P param) {
    for (Evl ast : list) {
      visit(ast, param);
    }
    return null;
  }

  protected R visit(Evl obj, P param) {
    if (obj == null)
      throw new RuntimeException("object is null");
    else if (obj instanceof RizzlyProgram)
      return visitRizzlyProgram((RizzlyProgram) obj, param);
    else if (obj instanceof Type)
      return visitType((Type) obj, param);
    else if (obj instanceof FunctionBase)
      return visitFunctionBase((FunctionBase) obj, param);
    else if (obj instanceof Expression)
      return visitExpression((Expression) obj, param);
    else if (obj instanceof Statement)
      return visitStatement((Statement) obj, param);
    else if (obj instanceof Variable)
      return visitVariable((Variable) obj, param);
    else if (obj instanceof NamedList)
      return visitNamedList((NamedList<Named>) obj, param);
    else if (obj instanceof RefItem)
      return visitRefItem((RefItem) obj, param);
    else if (obj instanceof Namespace)
      return visitNamespace((Namespace) obj, param);
    else if (obj instanceof NamedElement)
      return visitNamedElement((NamedElement) obj, param);
    else if (obj instanceof EnumElement)
      return visitEnumElement((EnumElement) obj, param);
    else if (obj instanceof CaseOpt)
      return visitCaseOpt((CaseOpt) obj, param);
    else if (obj instanceof CaseOptEntry)
      return visitCaseOptEntry((CaseOptEntry) obj, param);
    else if (obj instanceof IfOption)
      return visitIfOption((IfOption) obj, param);
    else if (obj instanceof Connection)
      return visitConnection((Connection) obj, param);
    else if (obj instanceof StateItem)
      return visitStateItem((StateItem) obj, param);
    else if (obj instanceof CompUse)
      return visitCompUse((CompUse) obj, param);
    else if (obj instanceof IfaceUse)
      return visitIfaceUse((IfaceUse) obj, param);
    else if (obj instanceof Endpoint)
      return visitEndpoint((Endpoint) obj, param);
    else if (obj instanceof Interface)
      return visitInterface((Interface) obj, param);
    else if (obj instanceof Component)
      return visitComponent((Component) obj, param);
    else if (obj instanceof BasicBlock)
      return visitBasicBlock((BasicBlock) obj, param);
    else if (obj instanceof BasicBlockList)
      return visitBasicBlockList((BasicBlockList) obj, param);
    else if (obj instanceof CaseGotoOpt)
      return visitCaseGotoOpt((CaseGotoOpt) obj, param);
    else if (obj instanceof Goto)
      return visitGoto((Goto) obj, param);
    else if (obj instanceof IfGoto)
      return visitIfGoto((IfGoto) obj, param);
    else if (obj instanceof CaseGoto)
      return visitCaseGoto((CaseGoto) obj, param);
    else if (obj instanceof PhiStmt)
      return visitPhiStmt((PhiStmt) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitEndpoint(Endpoint obj, P param) {
    if (obj instanceof EndpointSub)
      return visitEndpointSub((EndpointSub) obj, param);
    else if (obj instanceof EndpointSelf)
      return visitEndpointSelf((EndpointSelf) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitStateItem(StateItem obj, P param) {
    if (obj instanceof State)
      return visitState((State) obj, param);
    else if (obj instanceof Transition)
      return visitTransition((Transition) obj, param);
    else if (obj instanceof QueryItem)
      return visitQueryItem((QueryItem) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitState(State obj, P param) {
    if (obj instanceof StateComposite)
      return visitStateComposite((StateComposite) obj, param);
    else if (obj instanceof StateSimple)
      return visitStateSimple((StateSimple) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitCaseOptEntry(CaseOptEntry obj, P param) {
    if (obj instanceof CaseOptRange)
      return visitCaseOptRange((CaseOptRange) obj, param);
    else if (obj instanceof CaseOptValue)
      return visitCaseOptValue((CaseOptValue) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitVariable(Variable obj, P param) {
    if (obj instanceof StateVariable)
      return visitStateVariable((StateVariable) obj, param);
    else if (obj instanceof FuncVariable)
      return visitFuncVariable((FuncVariable) obj, param);
    else if (obj instanceof Constant)
      return visitConstant((Constant) obj, param);
    else if (obj instanceof SsaVariable)
      return visitSsaVariable((SsaVariable) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitConstant(Constant obj, P param) {
    if (obj instanceof ConstPrivate)
      return visitConstPrivate((ConstPrivate) obj, param);
    else if (obj instanceof ConstGlobal)
      return visitConstGlobal((ConstGlobal) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitFunctionBase(FunctionBase obj, P param) {
    if (obj instanceof FuncGlobal)
      return visitFuncGlobal((FuncGlobal) obj, param);
    else if (obj instanceof HfsmQueryFunction)
      return visitHfsmQueryFunction((HfsmQueryFunction) obj, param);
    else if (obj instanceof FuncProtoRet)
      return visitFuncProtoRet((FuncProtoRet) obj, param);
    else if (obj instanceof FuncProtoVoid)
      return visitFuncProtoVoid((FuncProtoVoid) obj, param);
    else if (obj instanceof FuncPrivateRet)
      return visitFuncPrivateRet((FuncPrivateRet) obj, param);
    else if (obj instanceof FuncPrivateVoid)
      return visitFuncPrivateVoid((FuncPrivateVoid) obj, param);
    else if (obj instanceof FuncInputHandlerQuery)
      return visitFuncInputHandlerQuery((FuncInputHandlerQuery) obj, param);
    else if (obj instanceof FuncInputHandlerEvent)
      return visitFuncInputHandlerEvent((FuncInputHandlerEvent) obj, param);
    else if (obj instanceof FuncSubHandlerEvent)
      return visitFuncSubHandlerEvent((FuncSubHandlerEvent) obj, param);
    else if (obj instanceof FuncSubHandlerQuery)
      return visitFuncSubHandlerQuery((FuncSubHandlerQuery) obj, param);
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
      return visitIf((IfStmt) obj, param);
    else if (obj instanceof Return)
      return visitReturn((Return) obj, param);
    else if (obj instanceof VarDefStmt)
      return visitVarDef((VarDefStmt) obj, param);
    else if (obj instanceof While)
      return visitWhile((While) obj, param);
    else if (obj instanceof CaseStmt)
      return visitCaseStmt((CaseStmt) obj, param);
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

  protected R visitExpression(Expression obj, P param) {
    if (obj instanceof Number)
      return visitNumber((Number) obj, param);
    else if (obj instanceof StringValue)
      return visitStringValue((StringValue) obj, param);
    else if (obj instanceof ArrayValue)
      return visitArrayValue((ArrayValue) obj, param);
    else if (obj instanceof BoolValue)
      return visitBoolValue((BoolValue) obj, param);
    else if (obj instanceof ArithmeticOp)
      return visitArithmeticOp((ArithmeticOp) obj, param);
    else if (obj instanceof Relation)
      return visitRelation((Relation) obj, param);
    else if (obj instanceof UnaryExpression)
      return visitUnaryExpression((UnaryExpression) obj, param);
    else if (obj instanceof Reference)
      return visitReference((Reference) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitRefItem(RefItem obj, P param) {
    if (obj instanceof RefIndex)
      return visitRefIndex((RefIndex) obj, param);
    else if (obj instanceof RefName)
      return visitRefName((RefName) obj, param);
    else if (obj instanceof RefCall)
      return visitRefCall((RefCall) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitType(Type obj, P param) {
    if (obj instanceof BaseType)
      return visitBaseType((BaseType) obj, param);
    else if (obj instanceof FunctionType)
      return visitFunctionType((FunctionType) obj, param);
    if (obj instanceof NamedElementType)
      return visitNamedElementType((NamedElementType) obj, param);
    else if (obj instanceof EnumType)
      return visitEnumType((EnumType) obj, param);
    else if (obj instanceof TypeAlias)
      return visitTypeAlias((TypeAlias) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  private R visitFunctionType(FunctionType obj, P param) {
    if (obj instanceof FunctionTypeRet)
      return visitFunctionTypeRet((FunctionTypeRet) obj, param);
    else if (obj instanceof FunctionTypeVoid)
      return visitFunctionTypeVoid((FunctionTypeVoid) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitComponent(Component obj, P param) {
    if (obj instanceof ImplElementary)
      return visitImplElementary((ImplElementary) obj, param);
    else if (obj instanceof ImplComposition)
      return visitImplComposition((ImplComposition) obj, param);
    else if (obj instanceof ImplHfsm)
      return visitImplHfsm((ImplHfsm) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitNamedElementType(NamedElementType obj, P param) {
    if (obj instanceof RecordType)
      return visitRecordType((RecordType) obj, param);
    else if (obj instanceof UnionType)
      return visitUnionType((UnionType) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitBaseType(BaseType obj, P param) {
    if (obj instanceof BooleanType)
      return visitBooleanType((BooleanType) obj, param);
    else if (obj instanceof Range)
      return visitRange((Range) obj, param);
    else if (obj instanceof Array)
      return visitArray((Array) obj, param);
    else if (obj instanceof StringType)
      return visitStringType((StringType) obj, param);
    else if (obj instanceof VoidType)
      return visitVoidType((VoidType) obj, param);
    else if (obj instanceof NaturalType)
      return visitNaturalType((NaturalType) obj, param);
    else if (obj instanceof IntegerType)
      return visitIntegerType((IntegerType) obj, param);
    else if (obj instanceof InterfaceType)
      return visitInterfaceType((InterfaceType) obj, param);
    else if (obj instanceof ComponentType)
      return visitComponentType((ComponentType) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  abstract protected R visitPhiStmt(PhiStmt obj, P param);

  abstract protected R visitBasicBlockList(BasicBlockList obj, P param);

  abstract protected R visitBasicBlock(BasicBlock obj, P param);

  abstract protected R visitCaseGotoOpt(CaseGotoOpt obj, P param);

  abstract protected R visitCaseGoto(CaseGoto obj, P param);

  abstract protected R visitIfGoto(IfGoto obj, P param);

  abstract protected R visitGoto(Goto obj, P param);

  abstract protected R visitEndpointSelf(EndpointSelf obj, P param);

  abstract protected R visitEndpointSub(EndpointSub obj, P param);

  abstract protected R visitReference(Reference obj, P param);

  abstract protected R visitStateSimple(StateSimple obj, P param);

  abstract protected R visitStateComposite(StateComposite obj, P param);

  abstract protected R visitNamespace(Namespace obj, P param);

  abstract protected R visitNamedList(NamedList<Named> obj, P param);

  abstract protected R visitConstPrivate(ConstPrivate obj, P param);

  abstract protected R visitConstGlobal(ConstGlobal obj, P param);

  abstract protected R visitFuncVariable(FuncVariable obj, P param);

  abstract protected R visitStateVariable(StateVariable obj, P param);

  abstract protected R visitSsaVariable(SsaVariable obj, P param);

  abstract protected R visitTypeAlias(TypeAlias obj, P param);

  abstract protected R visitVoidType(VoidType obj, P param);

  abstract protected R visitStringType(StringType obj, P param);

  abstract protected R visitArray(Array obj, P param);

  abstract protected R visitNaturalType(NaturalType obj, P param);

  abstract protected R visitComponentType(ComponentType obj, P param);

  abstract protected R visitInterfaceType(InterfaceType obj, P param);

  abstract protected R visitIntegerType(IntegerType obj, P param);

  abstract protected R visitRange(Range obj, P param);

  abstract protected R visitBooleanType(BooleanType obj, P param);

  abstract protected R visitRefCall(RefCall obj, P param);

  abstract protected R visitRefName(RefName obj, P param);

  abstract protected R visitRefIndex(RefIndex obj, P param);

  abstract protected R visitIfaceUse(IfaceUse obj, P param);

  abstract protected R visitCompUse(CompUse obj, P param);

  abstract protected R visitRizzlyProgram(RizzlyProgram obj, P param);

  abstract protected R visitImplHfsm(ImplHfsm obj, P param);

  abstract protected R visitImplComposition(ImplComposition obj, P param);

  abstract protected R visitImplElementary(ImplElementary obj, P param);

  abstract protected R visitInterface(Interface obj, P param);

  abstract protected R visitHfsmQueryFunction(HfsmQueryFunction obj, P param);

  abstract protected R visitQueryItem(QueryItem obj, P param);

  abstract protected R visitTransition(Transition obj, P param);

  abstract protected R visitConnection(Connection obj, P param);

  abstract protected R visitFuncProtoRet(FuncProtoRet obj, P param);

  abstract protected R visitFuncGlobal(FuncGlobal obj, P param);

  abstract protected R visitFuncInputHandlerEvent(FuncInputHandlerEvent obj, P param);

  abstract protected R visitFuncInputHandlerQuery(FuncInputHandlerQuery obj, P param);

  abstract protected R visitFuncSubHandlerQuery(FuncSubHandlerQuery obj, P param);

  abstract protected R visitFuncSubHandlerEvent(FuncSubHandlerEvent obj, P param);

  abstract protected R visitFuncPrivateVoid(FuncPrivateVoid obj, P param);

  abstract protected R visitFuncPrivateRet(FuncPrivateRet obj, P param);

  abstract protected R visitFuncProtoVoid(FuncProtoVoid obj, P param);

  abstract protected R visitNamedElement(NamedElement obj, P param);

  abstract protected R visitRecordType(RecordType obj, P param);

  abstract protected R visitUnionType(UnionType obj, P param);

  abstract protected R visitEnumType(EnumType obj, P param);

  abstract protected R visitEnumElement(EnumElement obj, P param);

  abstract protected R visitWhile(While obj, P param);

  abstract protected R visitCaseStmt(CaseStmt obj, P param);

  abstract protected R visitCaseOptRange(CaseOptRange obj, P param);

  abstract protected R visitCaseOptValue(CaseOptValue obj, P param);

  abstract protected R visitCaseOpt(CaseOpt obj, P param);

  abstract protected R visitIfOption(IfOption obj, P param);

  abstract protected R visitVarDef(VarDefStmt obj, P param);

  abstract protected R visitIf(IfStmt obj, P param);

  abstract protected R visitCallStmt(CallStmt obj, P param);

  abstract protected R visitAssignment(Assignment obj, P param);

  abstract protected R visitReturnExpr(ReturnExpr obj, P param);

  abstract protected R visitReturnVoid(ReturnVoid obj, P param);

  abstract protected R visitBlock(Block obj, P param);

  abstract protected R visitUnaryExpression(UnaryExpression obj, P param);

  abstract protected R visitRelation(Relation obj, P param);

  abstract protected R visitArithmeticOp(ArithmeticOp obj, P param);

  abstract protected R visitBoolValue(BoolValue obj, P param);

  abstract protected R visitArrayValue(ArrayValue obj, P param);

  abstract protected R visitStringValue(StringValue obj, P param);

  abstract protected R visitNumber(Number obj, P param);

  abstract protected R visitFunctionTypeVoid(FunctionTypeVoid obj, P param);

  abstract protected R visitFunctionTypeRet(FunctionTypeRet obj, P param);

}
