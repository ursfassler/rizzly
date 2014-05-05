package fun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.Direction;

import fun.composition.Connection;
import fun.composition.ImplComposition;
import fun.expression.ArithmeticOp;
import fun.expression.ArrayValue;
import fun.expression.BoolValue;
import fun.expression.Number;
import fun.expression.Relation;
import fun.expression.StringValue;
import fun.expression.UnaryExpression;
import fun.expression.reference.RefCall;
import fun.expression.reference.RefIndex;
import fun.expression.reference.RefName;
import fun.expression.reference.RefTemplCall;
import fun.expression.reference.Reference;
import fun.function.FuncWithBody;
import fun.function.FuncWithReturn;
import fun.function.FunctionFactory;
import fun.function.FunctionHeader;
import fun.function.impl.FuncEntryExit;
import fun.function.impl.FuncGlobal;
import fun.function.impl.FuncPrivateRet;
import fun.function.impl.FuncPrivateVoid;
import fun.function.impl.FuncProtRet;
import fun.function.impl.FuncProtVoid;
import fun.hfsm.ImplHfsm;
import fun.hfsm.StateComposite;
import fun.hfsm.StateSimple;
import fun.hfsm.Transition;
import fun.other.ImplElementary;
import fun.other.Named;
import fun.other.Namespace;
import fun.other.RizzlyFile;
import fun.statement.Assignment;
import fun.statement.Block;
import fun.statement.CallStmt;
import fun.statement.CaseOpt;
import fun.statement.CaseOptRange;
import fun.statement.CaseOptValue;
import fun.statement.CaseStmt;
import fun.statement.IfOption;
import fun.statement.IfStmt;
import fun.statement.ReturnExpr;
import fun.statement.ReturnVoid;
import fun.statement.VarDefStmt;
import fun.statement.While;
import fun.traverser.ReLinker;
import fun.type.base.AnyType;
import fun.type.base.BooleanType;
import fun.type.base.EnumElement;
import fun.type.base.EnumType;
import fun.type.base.IntegerType;
import fun.type.base.NaturalType;
import fun.type.base.StringType;
import fun.type.base.TypeAlias;
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

public class Copy {
  public static <T extends Fun> T copy(T obj) {
    CopyFun copier = new CopyFun();
    T nobj = copier.copy(obj);
    ReLinker.process(nobj, copier.getCopied());
    return nobj;
  }

}

class CopyFun extends Traverser<Fun, Void> {
  // / keeps the old -> new Named objects in order to relink references
  private Map<Named, Named> copied = new HashMap<Named, Named>();

  public Map<Named, Named> getCopied() {
    return copied;
  }

  @SuppressWarnings("unchecked")
  public <T extends Fun> T copy(T obj) {
    return (T) visit(obj, null);
  }

  public <T extends Fun> List<T> copy(List<T> obj) {
    List<T> ret = new ArrayList<T>();
    for (T itr : obj) {
      ret.add(copy(itr));
    }
    return ret;
  }

  @Override
  protected Fun visit(Fun obj, Void param) {
    Fun nobj = copied.get(obj);
    if (nobj == null) {
      nobj = super.visit(obj, param);
      if (obj instanceof Named) {
        assert (nobj instanceof Named);
        getCopied().put((Named) obj, (Named) nobj);
      }
    }
    return nobj;
  }

  @Override
  protected Fun visitReference(Reference obj, Void param) {
    Reference ret = new Reference(obj.getInfo(), obj.getLink());
    ret.getOffset().addAll(copy(obj.getOffset()));
    return ret;
  }

  @Override
  protected Fun visitRefName(RefName obj, Void param) {
    return new RefName(obj.getInfo(), obj.getName());
  }

  @Override
  protected Fun visitRefCall(RefCall obj, Void param) {
    return new RefCall(obj.getInfo(), copy(obj.getActualParameter()));
  }

  @Override
  protected Fun visitRefIndex(RefIndex obj, Void param) {
    return new RefIndex(obj.getInfo(), copy(obj.getIndex()));
  }

  @Override
  protected Fun visitRefCompcall(RefTemplCall obj, Void param) {
    return new RefTemplCall(obj.getInfo(), copy(obj.getActualParameter()));
  }

  @Override
  protected Fun visitNumber(Number obj, Void param) {
    return new Number(obj.getInfo(), obj.getValue());
  }

  @Override
  protected Fun visitStateSimple(StateSimple obj, Void param) {
    StateSimple ret = new StateSimple(obj.getInfo(), obj.getName());

    ret.getVariable().addAll(copy(obj.getVariable().getList()));
    ret.getItemList().addAll(copy(obj.getItemList().getList()));
    ret.setEntryFuncRef(copy(obj.getEntryFuncRef()));
    ret.setExitFuncRef(copy(obj.getExitFuncRef()));

    return ret;
  }

  @Override
  protected Fun visitStateComposite(StateComposite obj, Void param) {
    StateComposite ret = new StateComposite(obj.getInfo(), obj.getName(), "");

    ret.getVariable().addAll(copy(obj.getVariable().getList()));
    ret.getItemList().addAll(copy(obj.getItemList().getList()));
    ret.setEntryFuncRef(copy(obj.getEntryFuncRef()));
    ret.setExitFuncRef(copy(obj.getExitFuncRef()));
    ret.setInitial(copy(obj.getInitial()));

    return ret;
  }

  @Override
  protected Fun visitNamespace(Namespace obj, Void param) {
    Namespace ret = new Namespace(obj.getInfo(), obj.getName());
    ret.addAll(copy(obj.getList()));
    return ret;
  }

  @Override
  protected Fun visitRizzlyFile(RizzlyFile obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Fun visitConstPrivate(ConstPrivate obj, Void param) {
    ConstPrivate var = new ConstPrivate(obj.getInfo(), obj.getName(), copy(obj.getType()));
    var.setDef(copy(obj.getDef()));
    return var;
  }

  @Override
  protected Fun visitConstGlobal(ConstGlobal obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Fun visitFuncVariable(FuncVariable obj, Void param) {
    return new FuncVariable(obj.getInfo(), obj.getName(), copy(obj.getType()));
  }

  @Override
  protected Fun visitStateVariable(StateVariable obj, Void param) {
    return new StateVariable(obj.getInfo(), obj.getName(), copy(obj.getType()));
  }

  @Override
  protected Fun visitTypeAlias(TypeAlias obj, Void param) {
    return new TypeAlias(obj.getInfo(), obj.getName(), copy(obj.getRef()));
  }

  @Override
  protected Fun visitBooleanType(BooleanType obj, Void param) {
    return new BooleanType();
  }

  @Override
  protected Fun visitVoidType(VoidType obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Fun visitStringType(StringType obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Fun visitArray(Array obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Fun visitTemplateParameter(TemplateParameter obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Fun visitCompUse(CompUse obj, Void param) {
    return new CompUse(obj.getInfo(), obj.getName(), copy(obj.getType()));
  }

  @Override
  protected Fun visitImplHfsm(ImplHfsm obj, Void param) {
    ImplHfsm ret = new ImplHfsm(obj.getInfo(), obj.getName(), copy(obj.getTopstate()));

    ret.getIface(Direction.in).addAll(copy(obj.getIface(Direction.in).getList()));
    ret.getIface(Direction.out).addAll(copy(obj.getIface(Direction.out).getList()));

    return ret;
  }

  @Override
  protected Fun visitImplComposition(ImplComposition obj, Void param) {
    ImplComposition ret = new ImplComposition(obj.getInfo(), obj.getName());

    ret.getIface(Direction.in).addAll(copy(obj.getIface(Direction.in).getList()));
    ret.getIface(Direction.out).addAll(copy(obj.getIface(Direction.out).getList()));
    ret.getConnection().addAll(copy(obj.getConnection()));
    ret.getComponent().addAll(copy(obj.getComponent().getList()));

    return ret;
  }

  @Override
  protected Fun visitImplElementary(ImplElementary obj, Void param) {
    ImplElementary ret = new ImplElementary(obj.getInfo(), obj.getName());

    ret.getIface(Direction.in).addAll(copy(obj.getIface(Direction.in).getList()));
    ret.getIface(Direction.out).addAll(copy(obj.getIface(Direction.out).getList()));
    ret.getVariable().addAll(copy(obj.getVariable().getList()));
    ret.getConstant().addAll(copy(obj.getConstant().getList()));
    ret.getFunction().addAll(copy(obj.getFunction().getList()));
    ret.setEntryFunc(copy(obj.getEntryFunc()));
    ret.setExitFunc(copy(obj.getExitFunc()));

    return ret;
  }

  @Override
  protected Fun visitTransition(Transition obj, Void param) {
    Transition ret = new Transition(obj.getInfo(), obj.getName());
    ret.setSrc(copy(obj.getSrc()));
    ret.setDst(copy(obj.getDst()));
    ret.setEvent(copy(obj.getEvent()));
    ret.setGuard(copy(obj.getGuard()));
    ret.getParam().addAll(copy(obj.getParam().getList()));
    ret.setBody(copy(obj.getBody()));
    return ret;
  }

  @Override
  protected Fun visitConnection(Connection obj, Void param) {
    return new Connection(obj.getInfo(), copy(obj.getEndpoint(Direction.in)), copy(obj.getEndpoint(Direction.out)), obj.getType());
  }

  @Override
  protected Fun visitNamedElement(NamedElement obj, Void param) {
    return new NamedElement(obj.getInfo(), obj.getName(), copy(obj.getType()));
  }

  @Override
  protected Fun visitIntegerType(IntegerType obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Fun visitTypeType(TypeType obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Fun visitTypeTypeTemplate(TypeTypeTemplate obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Fun visitArrayTemplate(ArrayTemplate obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Fun visitNaturalType(NaturalType obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Fun visitAnyType(AnyType obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Fun visitRecordType(RecordType obj, Void param) {
    RecordType ret = new RecordType(obj.getInfo(), obj.getName());
    ret.getElement().addAll(copy(obj.getElement().getList()));
    return ret;
  }

  @Override
  protected Fun visitUnionType(UnionType obj, Void param) {
    UnionType ret = new UnionType(obj.getInfo(), obj.getName());
    ret.getElement().addAll(copy(obj.getElement().getList()));
    return ret;
  }

  @Override
  protected Fun visitEnumType(EnumType obj, Void param) {
    EnumType type = new EnumType(obj.getInfo(), obj.getName());
    copied.put(obj, type);
    type.getElement().addAll(copy(obj.getElement()));
    return type;
  }

  @Override
  protected Fun visitEnumElement(EnumElement obj, Void param) {
    Reference type = copy(obj.getType());
    if (copied.containsKey(obj)) {
      return copied.get(obj);
    } else {
      EnumElement elem = new EnumElement(obj.getInfo(), obj.getName(), type);
      elem.setDef(copy(obj.getDef()));
      return elem;
    }
  }

  @Override
  protected Fun visitWhile(While obj, Void param) {
    return new While(obj.getInfo(), copy(obj.getCondition()), copy(obj.getBody()));
  }

  @Override
  protected Fun visitCaseStmt(CaseStmt obj, Void param) {
    return new CaseStmt(obj.getInfo(), copy(obj.getCondition()), copy(obj.getOption()), copy(obj.getOtherwise()));
  }

  @Override
  protected Fun visitCaseOptRange(CaseOptRange obj, Void param) {
    return new CaseOptRange(obj.getInfo(), copy(obj.getStart()), copy(obj.getEnd()));
  }

  @Override
  protected Fun visitCaseOptValue(CaseOptValue obj, Void param) {
    return new CaseOptValue(obj.getInfo(), copy(obj.getValue()));
  }

  @Override
  protected Fun visitCaseOpt(CaseOpt obj, Void param) {
    return new CaseOpt(obj.getInfo(), copy(obj.getValue()), copy(obj.getCode()));
  }

  @Override
  protected Fun visitVarDef(VarDefStmt obj, Void param) {
    return new VarDefStmt(obj.getInfo(), copy(obj.getVariable()));
  }

  @Override
  protected Fun visitIfStmt(IfStmt obj, Void param) {
    IfStmt ret = new IfStmt(obj.getInfo());
    ret.getOption().addAll(copy(obj.getOption()));
    ret.setDefblock(copy(obj.getDefblock()));
    return ret;
  }

  @Override
  protected Fun visitIfOption(IfOption obj, Void param) {
    return new IfOption(obj.getInfo(), copy(obj.getCondition()), copy(obj.getCode()));
  }

  @Override
  protected Fun visitCallStmt(CallStmt obj, Void param) {
    return new CallStmt(obj.getInfo(), copy(obj.getCall()));
  }

  @Override
  protected Fun visitAssignment(Assignment obj, Void param) {
    return new Assignment(obj.getInfo(), copy(obj.getLeft()), copy(obj.getRight()));
  }

  @Override
  protected Fun visitReturnExpr(ReturnExpr obj, Void param) {
    return new ReturnExpr(obj.getInfo(), copy(obj.getExpr()));
  }

  @Override
  protected Fun visitReturnVoid(ReturnVoid obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Fun visitBlock(Block obj, Void param) {
    Block ret = new Block(obj.getInfo());
    ret.getStatements().addAll(copy(obj.getStatements()));
    return ret;
  }

  @Override
  protected Fun visitUnaryExpression(UnaryExpression obj, Void param) {
    return new UnaryExpression(obj.getInfo(), copy(obj.getExpr()), obj.getOp());
  }

  @Override
  protected Fun visitRelation(Relation obj, Void param) {
    return new Relation(obj.getInfo(), copy(obj.getLeft()), copy(obj.getRight()), obj.getOp());
  }

  @Override
  protected Fun visitArithmeticOp(ArithmeticOp obj, Void param) {
    return new ArithmeticOp(obj.getInfo(), copy(obj.getLeft()), copy(obj.getRight()), obj.getOp());
  }

  @Override
  protected Fun visitBoolValue(BoolValue obj, Void param) {
    return new BoolValue(obj.getInfo(), obj.isValue());
  }

  @Override
  protected Fun visitArrayValue(ArrayValue obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Fun visitStringValue(StringValue obj, Void param) {
    return new StringValue(obj.getInfo(), obj.getValue());
  }

  @Override
  protected Fun visitFunctionHeader(FunctionHeader obj, Void param) {
    FunctionHeader ret = FunctionFactory.create(obj.getClass(), obj.getInfo());
    ret.setName(obj.getName());
    ret.getParam().addAll(copy(obj.getParam().getList()));
    if (obj instanceof FuncWithReturn) {
      ((FuncWithReturn) ret).setRet(copy(((FuncWithReturn) obj).getRet()));
    }
    if (obj instanceof FuncWithBody) {
      ((FuncWithBody) ret).setBody(copy(((FuncWithBody) obj).getBody()));
    }
    return ret;
  }

  @Override
  protected Fun visitFuncPrivateRet(FuncPrivateRet obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Fun visitFuncPrivateVoid(FuncPrivateVoid obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Fun visitFuncProtRet(FuncProtRet obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Fun visitFuncProtVoid(FuncProtVoid obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Fun visitFuncGlobal(FuncGlobal obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Fun visitFuncPrivate(FuncPrivateVoid obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Fun visitFuncEntryExit(FuncEntryExit obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Fun visitRange(Range obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Fun visitRangeTemplate(RangeTemplate obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

}
