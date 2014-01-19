package evl;

import evl.composition.Connection;
import evl.composition.Endpoint;
import evl.composition.EndpointSelf;
import evl.composition.EndpointSub;
import evl.composition.ImplComposition;
import evl.expression.ArrayValue;
import evl.expression.BoolValue;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.RangeValue;
import evl.expression.StringValue;
import evl.expression.TypeCast;
import evl.expression.binop.And;
import evl.expression.binop.ArithmeticOp;
import evl.expression.binop.BinaryExp;
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
import evl.expression.reference.RefCall;
import evl.expression.reference.RefIndex;
import evl.expression.reference.RefItem;
import evl.expression.reference.RefName;
import evl.expression.reference.RefPtrDeref;
import evl.expression.reference.Reference;
import evl.expression.unop.Not;
import evl.expression.unop.Uminus;
import evl.expression.unop.UnaryExp;
import evl.function.FunctionBase;
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
import evl.hfsm.State;
import evl.hfsm.StateComposite;
import evl.hfsm.StateItem;
import evl.hfsm.StateSimple;
import evl.hfsm.Transition;
import evl.other.CompUse;
import evl.other.Component;
import evl.other.ImplElementary;
import evl.other.Namespace;
import evl.other.RizzlyProgram;
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
import evl.type.Type;
import evl.type.TypeRef;
import evl.type.base.ArrayType;
import evl.type.base.BaseType;
import evl.type.base.BooleanType;
import evl.type.base.EnumDefRef;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.type.base.FunctionType;
import evl.type.base.FunctionTypeRet;
import evl.type.base.FunctionTypeVoid;
import evl.type.base.RangeType;
import evl.type.base.StringType;
import evl.type.composed.NamedElement;
import evl.type.composed.NamedElementType;
import evl.type.composed.RecordType;
import evl.type.composed.UnionSelector;
import evl.type.composed.UnionType;
import evl.type.special.AnyType;
import evl.type.special.ComponentType;
import evl.type.special.IntegerType;
import evl.type.special.NaturalType;
import evl.type.special.PointerType;
import evl.type.special.VoidType;
import evl.variable.ConstGlobal;
import evl.variable.ConstPrivate;
import evl.variable.Constant;
import evl.variable.FuncVariable;
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
    if (obj == null) {
      throw new RuntimeException("object is null");
    } else if (obj instanceof RizzlyProgram) {
      return visitRizzlyProgram((RizzlyProgram) obj, param);
    } else if (obj instanceof Type) {
      return visitType((Type) obj, param);
    } else if (obj instanceof FunctionBase) {
      return visitFunctionBase((FunctionBase) obj, param);
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
    } else if (obj instanceof NamedElement) {
      return visitNamedElement((NamedElement) obj, param);
    } else if (obj instanceof Connection) {
      return visitConnection((Connection) obj, param);
    } else if (obj instanceof StateItem) {
      return visitStateItem((StateItem) obj, param);
    } else if (obj instanceof CompUse) {
      return visitCompUse((CompUse) obj, param);
    } else if (obj instanceof Endpoint) {
      return visitEndpoint((Endpoint) obj, param);
    } else if (obj instanceof Component) {
      return visitComponent((Component) obj, param);
    } else if (obj instanceof TypeRef) {
      return visitTypeRef((TypeRef) obj, param);
    } else if (obj instanceof UnionSelector) {
      return visitUnionSelector((UnionSelector) obj, param);
    } else if (obj instanceof EnumDefRef) {
      return visitEnumDefRef((EnumDefRef) obj, param);
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

  protected R visitStateItem(StateItem obj, P param) {
    if (obj instanceof State) {
      return visitState((State) obj, param);
    } else if (obj instanceof Transition) {
      return visitTransition((Transition) obj, param);
    } else if (obj instanceof HfsmQueryFunction) {
      return visitHfsmQueryFunction((HfsmQueryFunction) obj, param);
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
    if (obj instanceof StateVariable) {
      return visitStateVariable((StateVariable) obj, param);
    } else if (obj instanceof FuncVariable) {
      return visitFuncVariable((FuncVariable) obj, param);
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
    } else if (obj instanceof EnumElement) {
      return visitEnumElement((EnumElement) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitFunctionBase(FunctionBase obj, P param) {
    if (obj instanceof FuncGlobal) {
      return visitFuncGlobal((FuncGlobal) obj, param);
    } else if (obj instanceof FuncProtoRet) {
      return visitFuncProtoRet((FuncProtoRet) obj, param);
    } else if (obj instanceof FuncProtoVoid) {
      return visitFuncProtoVoid((FuncProtoVoid) obj, param);
    } else if (obj instanceof FuncPrivateRet) {
      return visitFuncPrivateRet((FuncPrivateRet) obj, param);
    } else if (obj instanceof FuncPrivateVoid) {
      return visitFuncPrivateVoid((FuncPrivateVoid) obj, param);
    } else if (obj instanceof FuncInputHandlerQuery) {
      return visitFuncInputHandlerQuery((FuncInputHandlerQuery) obj, param);
    } else if (obj instanceof FuncInputHandlerEvent) {
      return visitFuncInputHandlerEvent((FuncInputHandlerEvent) obj, param);
    } else if (obj instanceof FuncSubHandlerEvent) {
      return visitFuncSubHandlerEvent((FuncSubHandlerEvent) obj, param);
    } else if (obj instanceof FuncSubHandlerQuery) {
      return visitFuncSubHandlerQuery((FuncSubHandlerQuery) obj, param);
    } else if (obj instanceof FuncIfaceInRet) {
      return visitFuncIfaceInRet((FuncIfaceInRet) obj, param);
    } else if (obj instanceof FuncIfaceInVoid) {
      return visitFuncIfaceInVoid((FuncIfaceInVoid) obj, param);
    } else if (obj instanceof FuncIfaceOutRet) {
      return visitFuncIfaceOutRet((FuncIfaceOutRet) obj, param);
    } else if (obj instanceof FuncIfaceOutVoid) {
      return visitFuncIfaceOutVoid((FuncIfaceOutVoid) obj, param);
    } else if (obj instanceof HfsmQueryFunction) {
      return visitHfsmQueryFunction((HfsmQueryFunction) obj, param);
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
    } else if (obj instanceof BoolValue) {
      return visitBoolValue((BoolValue) obj, param);
    } else if (obj instanceof BinaryExp) {
      return visitBinaryExp((BinaryExp) obj, param);
    } else if (obj instanceof UnaryExp) {
      return visitUnaryExp((UnaryExp) obj, param);
    } else if (obj instanceof Reference) {
      return visitReference((Reference) obj, param);
    } else if (obj instanceof RangeValue) {
      return visitRangeValue((RangeValue) obj, param);
    } else if (obj instanceof TypeCast) {
      return visitTypeCast((TypeCast) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitUnaryExp(UnaryExp obj, P param) {
    if (obj instanceof Uminus) {
      return visitUminus((Uminus) obj, param);
    } else if (obj instanceof Not) {
      return visitNot((Not) obj, param);
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
    } else if (obj instanceof RefPtrDeref) {
      return visitRefPtrDeref((RefPtrDeref) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitType(Type obj, P param) {
    if (obj instanceof BaseType) {
      return visitBaseType((BaseType) obj, param);
    } else if (obj instanceof FunctionType) {
      return visitFunctionType((FunctionType) obj, param);
    }
    if (obj instanceof NamedElementType) {
      return visitNamedElementType((NamedElementType) obj, param);
    } else if (obj instanceof EnumType) {
      return visitEnumType((EnumType) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  private R visitFunctionType(FunctionType obj, P param) {
    if (obj instanceof FunctionTypeRet) {
      return visitFunctionTypeRet((FunctionTypeRet) obj, param);
    } else if (obj instanceof FunctionTypeVoid) {
      return visitFunctionTypeVoid((FunctionTypeVoid) obj, param);
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
    } else if (obj instanceof ComponentType) {
      return visitComponentType((ComponentType) obj, param);
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
    } else if (obj instanceof PointerType) {
      return visitPointerType((PointerType) obj, param);
    } else if (obj instanceof AnyType) {
      return visitAnyType((AnyType) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

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

  abstract protected R visitTypeRef(TypeRef obj, P param);

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

  abstract protected R visitRefPtrDeref(RefPtrDeref obj, P param);

  abstract protected R visitEnumDefRef(EnumDefRef obj, P param);

  abstract protected R visitCompUse(CompUse obj, P param);

  abstract protected R visitRizzlyProgram(RizzlyProgram obj, P param);

  abstract protected R visitImplHfsm(ImplHfsm obj, P param);

  abstract protected R visitImplComposition(ImplComposition obj, P param);

  abstract protected R visitImplElementary(ImplElementary obj, P param);

  abstract protected R visitHfsmQueryFunction(HfsmQueryFunction obj, P param);

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

  abstract protected R visitFuncIfaceOutVoid(FuncIfaceOutVoid obj, P param);

  abstract protected R visitFuncIfaceOutRet(FuncIfaceOutRet obj, P param);

  abstract protected R visitFuncIfaceInVoid(FuncIfaceInVoid obj, P param);

  abstract protected R visitFuncIfaceInRet(FuncIfaceInRet obj, P param);

  abstract protected R visitNamedElement(NamedElement obj, P param);

  abstract protected R visitRecordType(RecordType obj, P param);

  abstract protected R visitUnionType(UnionType obj, P param);

  abstract protected R visitEnumType(EnumType obj, P param);

  abstract protected R visitEnumElement(EnumElement obj, P param);

  abstract protected R visitBoolValue(BoolValue obj, P param);

  abstract protected R visitArrayValue(ArrayValue obj, P param);

  abstract protected R visitStringValue(StringValue obj, P param);

  abstract protected R visitNumber(Number obj, P param);

  abstract protected R visitRangeValue(RangeValue obj, P param);

  abstract protected R visitTypeCast(TypeCast obj, P param);

  abstract protected R visitFunctionTypeVoid(FunctionTypeVoid obj, P param);

  abstract protected R visitFunctionTypeRet(FunctionTypeRet obj, P param);

  abstract protected R visitPointerType(PointerType obj, P param);

  abstract protected R visitUnionSelector(UnionSelector obj, P param);

  abstract protected R visitUminus(Uminus obj, P param);

  abstract protected R visitNot(Not obj, P param);

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
}
