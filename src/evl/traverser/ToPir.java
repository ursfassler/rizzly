package evl.traverser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pir.PirObject;
import pir.expression.ArOp;
import pir.expression.PExpression;
import pir.expression.UnOp;
import pir.expression.reference.RefHead;
import pir.expression.reference.RefItem;
import pir.expression.reference.Referencable;
import pir.function.FuncWithRet;
import pir.function.impl.FuncImplRet;
import pir.function.impl.FuncImplVoid;
import pir.function.impl.FuncProtoRet;
import pir.function.impl.FuncProtoVoid;
import pir.other.Program;
import pir.statement.IfStmtEntry;
import pir.type.NamedElement;
import evl.Evl;
import evl.Traverser;
import evl.composition.Connection;
import evl.composition.EndpointSelf;
import evl.composition.EndpointSub;
import evl.composition.ImplComposition;
import evl.expression.ArithmeticOp;
import evl.expression.ArrayValue;
import evl.expression.BoolValue;
import evl.expression.ExpOp;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.RelOp;
import evl.expression.Relation;
import evl.expression.StringValue;
import evl.expression.UnaryExpression;
import evl.expression.UnaryOp;
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
import evl.function.impl.FuncSubHandlerEvent;
import evl.function.impl.FuncSubHandlerQuery;
import evl.hfsm.HfsmQueryFunction;
import evl.hfsm.ImplHfsm;
import evl.hfsm.QueryItem;
import evl.hfsm.State;
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
import evl.statement.Block;
import evl.statement.CallStmt;
import evl.statement.CaseOpt;
import evl.statement.CaseOptEntry;
import evl.statement.CaseOptRange;
import evl.statement.CaseOptValue;
import evl.statement.CaseStmt;
import evl.statement.IfOption;
import evl.statement.IfStmt;
import evl.statement.ReturnExpr;
import evl.statement.ReturnVoid;
import evl.statement.Statement;
import evl.statement.VarDefStmt;
import evl.statement.While;
import evl.type.Type;
import evl.type.base.Array;
import evl.type.base.BooleanType;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.type.base.FunctionTypeRet;
import evl.type.base.FunctionTypeVoid;
import evl.type.base.Range;
import evl.type.base.StringType;
import evl.type.base.TypeAlias;
import evl.type.base.Unsigned;
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
import evl.variable.StateVariable;

public class ToPir extends Traverser<PirObject, PirObject> {
  private Map<Evl, PirObject> map = new HashMap<Evl, PirObject>();

  static public PirObject process(Evl ast) {
    ToPir toC = new ToPir();
    return toC.traverse(ast, null);
  }

  private pir.type.Type getType(pir.expression.Reference obj) {
    pir.expression.reference.RefItem item = obj.getRef();
    return getType(item);
  }

  private pir.type.Type getType(pir.expression.reference.RefItem item) {
    assert (item instanceof pir.expression.reference.RefHead);
    Referencable ref = ((pir.expression.reference.RefHead) item).getRef();
    assert (ref instanceof pir.type.Type);
    return (pir.type.Type) ref;
  }

  @Override
  protected PirObject visit(Evl obj, PirObject param) {
    PirObject cobj = map.get(obj);
    if (cobj == null) {
      cobj = super.visit(obj, param);
      assert (cobj != null);
      map.put(obj, cobj);
    }
    return cobj;
  }

  @Override
  protected PirObject visitReference(Reference obj, PirObject param) {
    PirObject ref = visit(obj.getLink(), null);
    RefHead head = new RefHead((Referencable) ref);

    RefItem last = head;
    for (evl.expression.reference.RefItem itr : obj.getOffset()) {
      last = (RefItem) visit(itr, last);
    }
    return new pir.expression.Reference(last);
  }

  @Override
  protected pir.expression.reference.RefCall visitRefCall(RefCall obj, PirObject param) {
    assert (param != null);
    assert (param instanceof RefItem);
    pir.expression.reference.RefCall ret = new pir.expression.reference.RefCall((RefItem) param);
    for (Expression itr : obj.getActualParameter()) {
      PirObject arg = visit(itr, param);
      assert (arg instanceof PExpression);
      ret.getParameter().add((PExpression) arg);
    }
    return ret;
  }

  @Override
  protected PirObject visitRefName(RefName obj, PirObject param) {
    assert (param != null);
    assert (param instanceof RefItem);
    return new pir.expression.reference.RefName((RefItem) param, obj.getName());
  }

  @Override
  protected PirObject visitRefIndex(RefIndex obj, PirObject param) {
    assert (param != null);
    assert (param instanceof RefItem);
    PExpression index = (PExpression) visit(obj.getIndex(), param);
    return new pir.expression.reference.RefIndex((RefItem) param, index);
  }

  @Override
  protected PirObject visitCompUse(CompUse obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Program visitRizzlyProgram(RizzlyProgram obj, PirObject param) {
    Program prog = new Program(obj.getName());

    for (Type type : obj.getType()) {
      pir.type.Type ct = (pir.type.Type) visit(type, param);
      prog.getType().add(ct);
    }
    for (StateVariable itr : obj.getVariable()) {
      pir.other.StateVariable ct = (pir.other.StateVariable) visit(itr, param);
      prog.getVariable().add(ct);
    }
    for (Constant itr : obj.getConstant()) {
      pir.other.Constant ct = (pir.other.Constant) visit(itr, param);
      prog.getConstant().add(ct);
    }
    for (FunctionBase itr : obj.getFunction()) {
      pir.function.Function ct = (pir.function.Function) visit(itr, param);
      prog.getFunction().add(ct);
    }
    return prog;
  }

  @Override
  protected PirObject visitImplElementary(ImplElementary obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PirObject visitInterface(Interface obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PirObject visitFunctionBase(FunctionBase obj, PirObject param) {
    String name = obj.getName();

    List<pir.other.FuncVariable> arg = new ArrayList<pir.other.FuncVariable>();
    for (FuncVariable var : obj.getParam()) {
      arg.add((pir.other.FuncVariable) visit(var, param));
    }

    pir.function.Function func;
    if (obj instanceof FuncWithBody) {
      if (obj instanceof FuncWithReturn) {
        func = new FuncImplRet(name, arg);
      } else {
        func = new FuncImplVoid(name, arg);
      }
    } else {
      if (obj instanceof FuncWithReturn) {
        func = new FuncProtoRet(name, arg);
      } else {
        func = new FuncProtoVoid(name, arg);
      }
    }
    func.getAttributes().addAll(obj.getAttributes());

    map.put(obj, func); // otherwise the compiler follows recursive calls

    if (obj instanceof FuncWithReturn) {
      pir.type.Type rettype = getType((pir.expression.Reference) visit(((FuncWithReturn) obj).getRet(), param));
      ((FuncWithRet) func).setRetType(rettype);
    }

    if (obj instanceof FuncWithBody) {
      pir.statement.Block stmt = (pir.statement.Block) visit(((FuncWithBody) obj).getBody(), param);
      ((pir.function.FuncWithBody) func).setBody(stmt);
    }

    return func;
  }

  @Override
  protected PirObject visitUnionType(UnionType obj, PirObject param) {
    pir.type.UnionType struct = new pir.type.UnionType(obj.getName());
    for (evl.type.composed.NamedElement elem : obj.getElement()) {
      NamedElement celem = new NamedElement(elem.getName(), (pir.type.Type) visit(elem.getType(), param));
      struct.getElements().add(celem);
    }
    return struct;
  }

  @Override
  protected pir.type.StructType visitRecordType(RecordType obj, PirObject param) {
    pir.type.StructType struct = new pir.type.StructType(obj.getName());
    for (evl.type.composed.NamedElement elem : obj.getElement()) {
      NamedElement celem = new NamedElement(elem.getName(), (pir.type.Type) visit(elem.getType(), param));
      struct.getElements().add(celem);
    }
    return struct;
  }

  @Override
  protected pir.type.EnumType visitEnumType(EnumType obj, PirObject param) {
    pir.type.EnumType struct = new pir.type.EnumType(obj.getName());
    map.put(obj, struct);
    for (EnumElement elem : obj.getElement()) {
      pir.type.EnumElement celem = (pir.type.EnumElement) visit(elem, struct);
      struct.getElements().add(celem);
    }
    return struct;
  }

  @Override
  protected pir.type.EnumElement visitEnumElement(EnumElement obj, PirObject param) {
    pir.type.EnumType type = (pir.type.EnumType) param;
    return new pir.type.EnumElement(obj.getName(), type);
  }

  @Override
  protected PirObject visitWhile(While obj, PirObject param) {
    PExpression cond = (PExpression) visit(obj.getCondition(), param);
    pir.statement.Block block = (pir.statement.Block) visit(obj.getBody(), param);
    return new pir.statement.WhileStmt(cond, block);
  }

  @Override
  protected PirObject visitVarDef(VarDefStmt obj, PirObject param) {
    pir.other.FuncVariable cvar = (pir.other.FuncVariable) visit(obj.getVariable(), param);
    pir.statement.VarDefStmt ret = new pir.statement.VarDefStmt(cvar);
    return ret;
  }

  @Override
  protected PirObject visitIf(IfStmt obj, PirObject param) {
    assert (!obj.getOption().isEmpty());
    pir.statement.Block def = (pir.statement.Block) visit(obj.getDefblock(), param);
    pir.statement.IfStmt act = new pir.statement.IfStmt(def);
    for (IfOption itr : obj.getOption()) {
      act.getOption().add((IfStmtEntry) visit(itr, param));
    }
    return act;
  }

  @Override
  protected PirObject visitIfOption(IfOption obj, PirObject param) {
    PExpression mcond = (PExpression) visit(obj.getCondition(), param);
    pir.statement.Block mthen = (pir.statement.Block) visit(obj.getCode(), param);
    IfStmtEntry entry = new IfStmtEntry(mcond, mthen);
    return entry;
  }

  @Override
  protected pir.statement.CallStmt visitCallStmt(CallStmt obj, PirObject param) {
    PirObject call = visit(obj.getCall(), param);
    assert (call instanceof pir.expression.Reference);
    return new pir.statement.CallStmt((pir.expression.Reference) call);
  }

  @Override
  protected pir.statement.Assignment visitAssignment(Assignment obj, PirObject param) {
    PirObject dst = visit(obj.getLeft(), param);
    PirObject src = visit(obj.getRight(), param);
    assert (dst instanceof pir.expression.Reference);
    assert (src instanceof PExpression);
    return new pir.statement.Assignment((pir.expression.Reference) dst, (PExpression) src);
  }

  @Override
  protected pir.statement.Block visitBlock(Block obj, PirObject param) {
    pir.statement.Block ret = new pir.statement.Block();
    for (Statement stmt : obj.getStatements()) {
      ret.getStatement().add((pir.statement.Statement) visit(stmt, param));
    }
    return ret;
  }

  @Override
  protected PirObject visitUnaryExpression(UnaryExpression obj, PirObject param) {
    PirObject expr = visit(obj.getExpr(), param);
    assert (expr instanceof PExpression);
    return new pir.expression.UnaryExpr(toUnOp(obj.getOp()), (PExpression) expr);
  }

  private UnOp toUnOp(UnaryOp op) {
    switch (op) {
    case MINUS:
      return pir.expression.UnOp.MINUS;
    case NOT:
      return pir.expression.UnOp.NOT;
    default:
      throw new RuntimeException("not yet implemented: " + op);
    }
  }

  @Override
  protected PirObject visitRelation(Relation obj, PirObject param) {
    PirObject left = visit(obj.getLeft(), param);
    PirObject right = visit(obj.getRight(), param);
    assert (left instanceof PExpression);
    assert (right instanceof PExpression);
    return new pir.expression.Relation((PExpression) left, (PExpression) right, toRelOp(obj.getOp()));
  }

  private pir.expression.RelOp toRelOp(RelOp op) {
    switch (op) {
    case EQUAL:
      return pir.expression.RelOp.EQUAL;
    case NOT_EQUAL:
      return pir.expression.RelOp.NOT_EQUAL;
    case LESS:
      return pir.expression.RelOp.LESS;
    case LESS_EQUAL:
      return pir.expression.RelOp.LESS_EQUAL;
    case GREATER:
      return pir.expression.RelOp.GREATER;
    case GREATER_EQUEAL:
      return pir.expression.RelOp.GREATER_EQUEAL;
    default: {
      throw new RuntimeException("not yet implemented: " + op);
    }
    }
  }

  @Override
  protected PirObject visitArithmeticOp(ArithmeticOp obj, PirObject param) {
    PirObject left = visit(obj.getLeft(), param);
    PirObject right = visit(obj.getRight(), param);
    assert (left instanceof PExpression);
    assert (right instanceof PExpression);
    return new pir.expression.ArithmeticOp((PExpression) left, (PExpression) right, toCOp(obj.getOp()));
  }

  private ArOp toCOp(ExpOp op) {
    switch (op) {
    case MUL:
      return ArOp.MUL;
    case PLUS:
      return ArOp.PLUS;
    case DIV:
      return ArOp.DIV;
    case MINUS:
      return ArOp.MINUS;
    case AND:
      return ArOp.AND;
    case OR:
      return ArOp.OR;
    case MOD:
      return ArOp.MOD;
    case SHL:
      return ArOp.SHL;
    case SHR:
      return ArOp.SHR;
    default: {
      throw new RuntimeException("not yet implemented: " + op);
    }
    }
  }

  @Override
  protected PirObject visitBoolValue(BoolValue obj, PirObject param) {
    return new pir.expression.BoolValue(obj.isValue());
  }

  @Override
  protected pir.expression.Number visitNumber(Number obj, PirObject param) {
    return new pir.expression.Number(obj.getValue());
  }

  @Override
  protected PirObject visitStringValue(StringValue obj, PirObject param) {
    return new pir.expression.StringValue(obj.getValue());
  }

  @Override
  protected PirObject visitArrayValue(ArrayValue obj, PirObject param) {
    pir.expression.ArrayValue ret = new pir.expression.ArrayValue();
    for (Expression itr : obj.getValue()) {
      PirObject arg = visit(itr, param);
      assert (arg instanceof PExpression);
      ret.getValue().add((PExpression) arg);
    }
    return ret;
  }

  @Override
  protected PirObject visitUnsigned(Unsigned obj, PirObject param) {
    pir.type.UnsignedType ret = new pir.type.UnsignedType(obj.getBits());
    return ret;
  }

  @Override
  protected PirObject visitRange(Range obj, PirObject param) {
    pir.type.RangeType ret = new pir.type.RangeType(obj.getLow(), obj.getHigh());
    return ret;
  }

  @Override
  protected PirObject visitArray(Array obj, PirObject param) {
    pir.type.Type type = getType((pir.expression.Reference) visit(obj.getType(), param));
    pir.type.Array ret = new pir.type.Array(obj.getName(), type, obj.getSize());
    return ret;
  }

  @Override
  protected pir.type.Type visitBooleanType(BooleanType obj, PirObject param) {
    pir.type.BooleanType ret = new pir.type.BooleanType();
    return ret;
  }

  @Override
  protected PirObject visitReturnExpr(ReturnExpr obj, PirObject param) {
    PirObject expr = visit(obj.getExpr(), param);
    assert (expr instanceof PExpression);
    return new pir.statement.ReturnValue((PExpression) expr);
  }

  @Override
  protected PirObject visitReturnVoid(ReturnVoid obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PirObject visitVoidType(VoidType obj, PirObject param) {
    pir.type.VoidType ret = new pir.type.VoidType(obj.getName());
    return ret;
  }

  @Override
  protected PirObject visitStringType(StringType obj, PirObject param) {
    pir.type.StringType ret = new pir.type.StringType(obj.getName());
    return ret;
  }

  @Override
  protected pir.type.Type visitTypeAlias(TypeAlias obj, PirObject param) {
    pir.type.Type typ = getType((pir.expression.Reference) visit(obj.getRef(), param));
    return new pir.type.TypeAlias(obj.getName(), typ);
  }

  @Override
  protected PirObject visitFuncVariable(FuncVariable obj, PirObject param) {
    pir.type.Type type = (pir.type.Type) visit(obj.getType(), null);
    return new pir.other.FuncVariable(obj.getName(), type);
  }

  @Override
  protected PirObject visitStateVariable(StateVariable obj, PirObject param) {
    pir.type.Type type = (pir.type.Type) visit(obj.getType(), null);
    return new pir.other.StateVariable(obj.getName(), type);
  }

  @Override
  protected PirObject visitConstant(Constant obj, PirObject param) {
    pir.type.Type type = (pir.type.Type) visit(obj.getType(), null);
    PExpression def = (PExpression) visit(obj.getDef(), null);
    return new pir.other.Constant(obj.getName(), type, def);
  }

  @Override
  protected PirObject visitNamespace(Namespace obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PirObject visitNamedList(NamedList<Named> obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PirObject visitIfaceUse(IfaceUse obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PirObject visitCaseStmt(CaseStmt obj, PirObject param) {
    PExpression cond = (PExpression) visit(obj.getCondition(), param);
    pir.statement.Block otherwise = (pir.statement.Block) visit(obj.getOtherwise(), param);
    pir.statement.CaseStmt stmt = new pir.statement.CaseStmt(cond, otherwise);
    for (CaseOpt opt : obj.getOption()) {
      pir.statement.CaseEntry entry = (pir.statement.CaseEntry) visit(opt, param);
      stmt.getEntries().add(entry);
    }
    return stmt;
  }

  @Override
  protected PirObject visitCaseOpt(CaseOpt obj, PirObject param) {
    List<pir.statement.CaseOptEntry> value = new ArrayList<pir.statement.CaseOptEntry>();
    for (CaseOptEntry entry : obj.getValue()) {
      value.add((pir.statement.CaseOptEntry) visit(entry, param));
    }
    pir.statement.Block code = (pir.statement.Block) visit(obj.getCode(), param);
    return new pir.statement.CaseEntry(value, code);
  }

  @Override
  protected PirObject visitCaseOptRange(CaseOptRange obj, PirObject param) {
    // assert (obj.getStart() instanceof Number);
    // assert (obj.getEnd() instanceof Number);
    PExpression start = (PExpression) visit(obj.getStart(), param);
    PExpression end = (PExpression) visit(obj.getEnd(), param);
    return new pir.statement.CaseOptRange(start, end);
  }

  @Override
  protected PirObject visitCaseOptValue(CaseOptValue obj, PirObject param) {
    // assert (obj.getValue() instanceof Number);
    PExpression value = (PExpression) visit(obj.getValue(), param);
    return new pir.statement.CaseOptValue(value);
  }

  @Override
  protected PirObject visitImplComposition(ImplComposition obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PirObject visitConnection(Connection obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PirObject visitConstPrivate(ConstPrivate obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PirObject visitConstGlobal(ConstGlobal obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PirObject visitFuncGlobal(FuncGlobal obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PirObject visitImplHfsm(ImplHfsm obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PirObject visitState(State obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PirObject visitTransition(Transition obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PirObject visitStateSimple(StateSimple obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PirObject visitStateComposite(StateComposite obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PirObject visitHfsmQueryFunction(HfsmQueryFunction obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PirObject visitQueryItem(QueryItem obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PirObject visitNamedElement(evl.type.composed.NamedElement obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PirObject visitNaturalType(NaturalType obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PirObject visitIntegerType(IntegerType obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PirObject visitFuncProtoRet(evl.function.impl.FuncProtoRet obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PirObject visitFuncInputHandlerEvent(FuncInputHandlerEvent obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PirObject visitFuncInputHandlerQuery(FuncInputHandlerQuery obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PirObject visitFuncPrivateVoid(FuncPrivateVoid obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PirObject visitFuncPrivateRet(FuncPrivateRet obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PirObject visitFuncProtoVoid(evl.function.impl.FuncProtoVoid obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PirObject visitFuncSubHandlerQuery(FuncSubHandlerQuery obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PirObject visitFuncSubHandlerEvent(FuncSubHandlerEvent obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PirObject visitFunctionTypeVoid(FunctionTypeVoid obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PirObject visitFunctionTypeRet(FunctionTypeRet obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PirObject visitEndpointSelf(EndpointSelf obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PirObject visitEndpointSub(EndpointSub obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PirObject visitInterfaceType(InterfaceType obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected PirObject visitComponentType(ComponentType obj, PirObject param) {
    throw new RuntimeException("not yet implemented");
  }

}
