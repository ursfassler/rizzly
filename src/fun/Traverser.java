package fun;

import fun.composition.Connection;
import fun.composition.ImplComposition;
import fun.expression.ArithmeticOp;
import fun.expression.ArrayValue;
import fun.expression.BoolValue;
import fun.expression.Expression;
import fun.expression.Number;
import fun.expression.Relation;
import fun.expression.StringValue;
import fun.expression.UnaryExpression;
import fun.expression.reference.RefCall;
import fun.expression.reference.RefIndex;
import fun.expression.reference.RefItem;
import fun.expression.reference.RefName;
import fun.expression.reference.RefTemplCall;
import fun.expression.reference.Reference;
import fun.expression.reference.ReferenceLinked;
import fun.expression.reference.ReferenceUnlinked;
import fun.function.FunctionHeader;
import fun.function.impl.FuncEntryExit;
import fun.function.impl.FuncGlobal;
import fun.function.impl.FuncPrivateRet;
import fun.function.impl.FuncPrivateVoid;
import fun.function.impl.FuncProtRet;
import fun.function.impl.FuncProtVoid;
import fun.hfsm.ImplHfsm;
import fun.hfsm.QueryItem;
import fun.hfsm.State;
import fun.hfsm.StateComposite;
import fun.hfsm.StateItem;
import fun.hfsm.StateSimple;
import fun.hfsm.Transition;
import fun.other.Component;
import fun.other.Generator;
import fun.other.ImplElementary;
import fun.other.Interface;
import fun.other.Namespace;
import fun.other.RizzlyFile;
import fun.statement.Assignment;
import fun.statement.Block;
import fun.statement.CallStmt;
import fun.statement.CaseOpt;
import fun.statement.CaseOptEntry;
import fun.statement.CaseOptRange;
import fun.statement.CaseOptValue;
import fun.statement.CaseStmt;
import fun.statement.IfOption;
import fun.statement.IfStmt;
import fun.statement.Return;
import fun.statement.ReturnExpr;
import fun.statement.ReturnVoid;
import fun.statement.Statement;
import fun.statement.VarDefStmt;
import fun.statement.While;
import fun.type.Type;
import fun.type.base.AnyType;
import fun.type.base.BaseType;
import fun.type.base.BooleanType;
import fun.type.base.EnumElement;
import fun.type.base.EnumType;
import fun.type.base.FunctionType;
import fun.type.base.IntegerType;
import fun.type.base.NaturalType;
import fun.type.base.StringType;
import fun.type.base.TypeAlias;
import fun.type.base.VoidType;
import fun.type.composed.NamedElement;
import fun.type.composed.NamedElementType;
import fun.type.composed.RecordType;
import fun.type.composed.UnionSelector;
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
import fun.variable.Constant;
import fun.variable.FuncVariable;
import fun.variable.IfaceUse;
import fun.variable.StateVariable;
import fun.variable.TemplateParameter;
import fun.variable.Variable;

public abstract class Traverser<R, P> {

  public R traverse(Fun obj, P param) {
    return visit(obj, param);
  }

  protected R visitItr(Iterable<? extends Fun> list, P param) {
    for (Fun ast : list) {
      visit(ast, param);
    }
    return null;
  }

  protected R visit(Fun obj, P param) {
    if (obj == null) {
      throw new RuntimeException("object is null");
    } else if (obj instanceof RizzlyFile) {
      return visitRizzlyFile((RizzlyFile) obj, param);
    } else if (obj instanceof Type) {
      return visitType((Type) obj, param);
    } else if (obj instanceof FunctionHeader) {
      return visitFunctionHeader((FunctionHeader) obj, param);
    } else if (obj instanceof Expression) {
      return visitExpression((Expression) obj, param);
    } else if (obj instanceof Statement) {
      return visitStatement((Statement) obj, param);
    } else if (obj instanceof Variable) {
      return visitVariable((Variable) obj, param);
    } else if (obj instanceof RefItem) {
      return visitRefItem((RefItem) obj, param);
    } else if (obj instanceof Namespace) {
      return visitNamespace((Namespace) obj, param);
    } else if (obj instanceof NamedElement) {
      return visitNamedElement((NamedElement) obj, param);
    } else if (obj instanceof CaseOpt) {
      return visitCaseOpt((CaseOpt) obj, param);
    } else if (obj instanceof CaseOptEntry) {
      return visitCaseOptEntry((CaseOptEntry) obj, param);
    } else if (obj instanceof IfOption) {
      return visitIfOption((IfOption) obj, param);
    } else if (obj instanceof Connection) {
      return visitConnection((Connection) obj, param);
    } else if (obj instanceof StateItem) {
      return visitStateItem((StateItem) obj, param);
    } else if (obj instanceof State) {
      return visitState((State) obj, param);
    } else if (obj instanceof Transition) {
      return visitTransition((Transition) obj, param);
    } else if (obj instanceof QueryItem) {
      return visitQueryItem((QueryItem) obj, param);
    } else if (obj instanceof Interface) {
      return visitInterface((Interface) obj, param);
    } else if (obj instanceof Component) {
      return visitComponent((Component) obj, param);
    } else if (obj instanceof UnionSelector) {
      return visitUnionSelector((UnionSelector) obj, param);
    } else if (obj instanceof Generator) {
      return visitGenerator((Generator) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitFunctionHeader(FunctionHeader obj, P param) {
    if (obj instanceof FuncGlobal) {
      return visitFuncGlobal((FuncGlobal) obj, param);
    } else if (obj instanceof FuncProtVoid) {
      return visitFuncProtVoid((FuncProtVoid) obj, param);
    } else if (obj instanceof FuncProtRet) {
      return visitFuncProtRet((FuncProtRet) obj, param);
    } else if (obj instanceof FuncPrivateVoid) {
      return visitFuncPrivateVoid((FuncPrivateVoid) obj, param);
    } else if (obj instanceof FuncPrivateRet) {
      return visitFuncPrivateRet((FuncPrivateRet) obj, param);
    } else if (obj instanceof FuncEntryExit) {
      return visitFuncEntryExit((FuncEntryExit) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitStateItem(StateItem obj, P param) {
    if (obj instanceof State) {
      return visitState((State) obj, param);
    } else if (obj instanceof Transition) {
      return visitTransition((Transition) obj, param);
    } else if (obj instanceof QueryItem) {
      return visitQueryItem((QueryItem) obj, param);
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

  protected R visitCaseOptEntry(CaseOptEntry obj, P param) {
    if (obj instanceof CaseOptRange) {
      return visitCaseOptRange((CaseOptRange) obj, param);
    } else if (obj instanceof CaseOptValue) {
      return visitCaseOptValue((CaseOptValue) obj, param);
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
    } else if (obj instanceof TemplateParameter) {
      return visitCompfuncParameter((TemplateParameter) obj, param);
    } else if (obj instanceof CompUse) {
      return visitCompUse((CompUse) obj, param);
    } else if (obj instanceof IfaceUse) {
      return visitIfaceUse((IfaceUse) obj, param);
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

  protected R visitStatement(Statement obj, P param) {
    if (obj instanceof Block) {
      return visitBlock((Block) obj, param);
    } else if (obj instanceof Assignment) {
      return visitAssignment((Assignment) obj, param);
    } else if (obj instanceof CallStmt) {
      return visitCallStmt((CallStmt) obj, param);
    } else if (obj instanceof IfStmt) {
      return visitIfStmt((IfStmt) obj, param);
    } else if (obj instanceof Return) {
      return visitReturn((Return) obj, param);
    } else if (obj instanceof VarDefStmt) {
      return visitVarDef((VarDefStmt) obj, param);
    } else if (obj instanceof While) {
      return visitWhile((While) obj, param);
    } else if (obj instanceof CaseStmt) {
      return visitCaseStmt((CaseStmt) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitReturn(Return obj, P param) {
    if (obj instanceof ReturnVoid) {
      return visitReturnVoid((ReturnVoid) obj, param);
    } else if (obj instanceof ReturnExpr) {
      return visitReturnExpr((ReturnExpr) obj, param);
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
    } else if (obj instanceof ArithmeticOp) {
      return visitArithmeticOp((ArithmeticOp) obj, param);
    } else if (obj instanceof Relation) {
      return visitRelation((Relation) obj, param);
    } else if (obj instanceof UnaryExpression) {
      return visitUnaryExpression((UnaryExpression) obj, param);
    } else if (obj instanceof Reference) {
      return visitReference((Reference) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitReference(Reference obj, P param) {
    if (obj instanceof ReferenceUnlinked) {
      return visitReferenceUnlinked((ReferenceUnlinked) obj, param);
    } else if (obj instanceof ReferenceLinked) {
      return visitReferenceLinked((ReferenceLinked) obj, param);
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
    } else if (obj instanceof RefTemplCall) {
      return visitRefCompcall((RefTemplCall) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitType(Type obj, P param) {
    if (obj instanceof BaseType) {
      return visitBaseType((BaseType) obj, param);
    } else if (obj instanceof FunctionType) {
      return visitFunctionType((FunctionType) obj, param);
    } else if (obj instanceof TypeType) {
      return visitTypeType((TypeType) obj, param);
    } else if (obj instanceof NamedElementType) {
      return visitNamedElementType((NamedElementType) obj, param);
    } else if (obj instanceof EnumType) {
      return visitEnumType((EnumType) obj, param);
    } else if (obj instanceof TypeAlias) {
      return visitTypeAlias((TypeAlias) obj, param);
    } else if (obj instanceof ArrayTemplate) {
      return visitArrayTemplate((ArrayTemplate) obj, param);
    } else if (obj instanceof TypeTypeTemplate) {
      return visitTypeTypeTemplate((TypeTypeTemplate) obj, param);
    } else if (obj instanceof RangeTemplate) {
      return visitRangeTemplate((RangeTemplate) obj, param);
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
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitBaseType(BaseType obj, P param) {
    if (obj instanceof BooleanType) {
      return visitBooleanType((BooleanType) obj, param);
    } else if (obj instanceof Range) {
      return visitRange((Range) obj, param);
    } else if (obj instanceof Array) {
      return visitArray((Array) obj, param);
    } else if (obj instanceof StringType) {
      return visitStringType((StringType) obj, param);
    } else if (obj instanceof VoidType) {
      return visitVoidType((VoidType) obj, param);
    } else if (obj instanceof IntegerType) {
      return visitIntegerType((IntegerType) obj, param);
    } else if (obj instanceof NaturalType) {
      return visitNaturalType((NaturalType) obj, param);
    } else if (obj instanceof AnyType) {
      return visitAnyType((AnyType) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  abstract protected R visitFuncEntryExit(FuncEntryExit obj, P param);

  abstract protected R visitFuncPrivateRet(FuncPrivateRet obj, P param);

  abstract protected R visitFuncPrivateVoid(FuncPrivateVoid obj, P param);

  abstract protected R visitFuncProtRet(FuncProtRet obj, P param);

  abstract protected R visitFuncProtVoid(FuncProtVoid obj, P param);

  abstract protected R visitFuncGlobal(FuncGlobal obj, P param);

  abstract protected R visitStateSimple(StateSimple obj, P param);

  abstract protected R visitStateComposite(StateComposite obj, P param);

  abstract protected R visitNamespace(Namespace obj, P param);

  abstract protected R visitRizzlyFile(RizzlyFile obj, P param);

  abstract protected R visitConstPrivate(ConstPrivate obj, P param);

  abstract protected R visitConstGlobal(ConstGlobal obj, P param);

  abstract protected R visitFuncVariable(FuncVariable obj, P param);

  abstract protected R visitStateVariable(StateVariable obj, P param);

  abstract protected R visitTypeAlias(TypeAlias obj, P param);

  abstract protected R visitFunctionType(FunctionType obj, P param);

  abstract protected R visitVoidType(VoidType obj, P param);

  abstract protected R visitStringType(StringType obj, P param);

  abstract protected R visitArray(Array obj, P param);

  abstract protected R visitRange(Range obj, P param);

  abstract protected R visitBooleanType(BooleanType obj, P param);

  abstract protected R visitCompfuncParameter(TemplateParameter obj, P param);

  abstract protected R visitRefCall(RefCall obj, P param);

  abstract protected R visitRefName(RefName obj, P param);

  abstract protected R visitRefIndex(RefIndex obj, P param);

  abstract protected R visitRefCompcall(RefTemplCall obj, P param);

  abstract protected R visitIfaceUse(IfaceUse obj, P param);

  abstract protected R visitCompUse(CompUse obj, P param);

  abstract protected R visitImplHfsm(ImplHfsm obj, P param);

  abstract protected R visitImplComposition(ImplComposition obj, P param);

  abstract protected R visitImplElementary(ImplElementary obj, P param);

  abstract protected R visitInterface(Interface obj, P param);

  abstract protected R visitQueryItem(QueryItem obj, P param);

  abstract protected R visitTransition(Transition obj, P param);

  abstract protected R visitConnection(Connection obj, P param);

  abstract protected R visitGenerator(Generator obj, P param);

  abstract protected R visitFuncPrivate(FuncPrivateVoid obj, P param);

  abstract protected R visitNamedElement(NamedElement obj, P param);

  abstract protected R visitNaturalType(NaturalType obj, P param);

  abstract protected R visitIntegerType(IntegerType obj, P param);

  abstract protected R visitTypeType(TypeType obj, P param);

  abstract protected R visitTypeTypeTemplate(TypeTypeTemplate obj, P param);

  abstract protected R visitArrayTemplate(ArrayTemplate obj, P param);

  abstract protected R visitRangeTemplate(RangeTemplate obj, P param);

  abstract protected R visitAnyType(AnyType obj, P param);

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

  abstract protected R visitIfStmt(IfStmt obj, P param);

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

  abstract protected R visitReferenceUnlinked(ReferenceUnlinked obj, P param);

  abstract protected R visitReferenceLinked(ReferenceLinked obj, P param);

  abstract protected R visitUnionSelector(UnionSelector obj, P param);
}
