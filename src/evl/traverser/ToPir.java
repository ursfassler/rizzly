package evl.traverser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pir.PirObject;
import pir.cfg.BasicBlockEnd;
import pir.expression.PExpression;
import pir.expression.UnOp;
import pir.expression.reference.RefItem;
import pir.expression.reference.Referencable;
import pir.expression.reference.VarRef;
import pir.expression.reference.VarRefSimple;
import pir.function.FuncImpl;
import pir.function.FuncProto;
import pir.function.Function;
import pir.other.PirValue;
import pir.other.Program;
import pir.other.Variable;
import pir.statement.ArOp;
import pir.statement.CallAssignment;
import pir.statement.ComplexWriter;
import pir.statement.VariableGeneratorStmt;
import pir.type.NamedElement;
import pir.type.TypeRef;
import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.cfg.BasicBlock;
import evl.cfg.BasicBlockList;
import evl.cfg.CaseOptRange;
import evl.cfg.CaseOptValue;
import evl.cfg.Goto;
import evl.cfg.IfGoto;
import evl.cfg.PhiStmt;
import evl.cfg.ReturnExpr;
import evl.cfg.ReturnVoid;
import evl.expression.ArithmeticOp;
import evl.expression.ArrayValue;
import evl.expression.BoolValue;
import evl.expression.ExpOp;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.RelOp;
import evl.expression.Relation;
import evl.expression.StringValue;
import evl.expression.TypeCast;
import evl.expression.UnaryExpression;
import evl.expression.UnaryOp;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefIndex;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.function.FuncWithBody;
import evl.function.FuncWithReturn;
import evl.function.FunctionBase;
import evl.function.FunctionHeader;
import evl.other.RizzlyProgram;
import evl.statement.Assignment;
import evl.statement.CallStmt;
import evl.statement.Statement;
import evl.statement.VarDefInitStmt;
import evl.statement.VarDefStmt;
import evl.type.Type;
import evl.type.base.ArrayType;
import evl.type.base.BooleanType;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.type.base.Range;
import evl.type.base.StringType;
import evl.type.base.TypeAlias;
import evl.type.composed.RecordType;
import evl.type.composed.UnionType;
import evl.type.special.IntegerType;
import evl.type.special.VoidType;
import evl.variable.Constant;
import evl.variable.FuncVariable;
import evl.variable.SsaVariable;
import evl.variable.StateVariable;

public class ToPir extends NullTraverser<PirObject, PirObject> {
  private Map<Evl, PirObject> map = new HashMap<Evl, PirObject>();

  static public PirObject process(Evl ast) {
    ToPir toC = new ToPir();
    return toC.traverse(ast, null);
  }

  @Override
  protected PirObject visitDefault(Evl obj, PirObject param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  private pir.type.Type getType(pir.expression.reference.Reference obj) {
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
    if (ref instanceof Variable) {
      if ((ref instanceof pir.other.SsaVariable) && (obj.getOffset().isEmpty())) {
        return new VarRefSimple((pir.other.SsaVariable) ref);
      } else {
        ArrayList<RefItem> offset = new ArrayList<RefItem>();
        for (evl.expression.reference.RefItem itm : obj.getOffset()) {
          offset.add((RefItem) visit(itm, null));
        }
        return new VarRef((Variable) ref, offset);
      }
    } else if (ref instanceof pir.type.Type) {
      assert (obj.getOffset().isEmpty());
      return new TypeRef((pir.type.Type) ref);
    } else if (ref instanceof Function) {
      assert ((obj.getOffset().size() == 1) && (obj.getOffset().get(0) instanceof RefCall));
      RefCall call = (RefCall) obj.getOffset().get(0);

      ArrayList<PirValue> acpa = new ArrayList<PirValue>();
      for (Expression pa : call.getActualParameter()) {
        acpa.add((PirValue) visit(pa, null));
      }

      return new pir.statement.CallStmt((Function) ref, acpa); // TODO correct? return value not used?
    } else {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Unhandled class: " + ref.getClass().getCanonicalName());
    }

    return null;

    // PirObject ref = visit(obj.getLink(), null);
    // RefHead head = new RefHead((Referencable) ref);
    //
    // RefItem last = head;
    // for (evl.expression.reference.RefItem itr : obj.getOffset()) {
    // last = (RefItem) visit(itr, last);
    // }
    // return new pir.expression.reference.Reference(last);
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
    return new pir.expression.reference.RefName(obj.getName());
  }

  @Override
  protected PirObject visitRefIndex(RefIndex obj, PirObject param) {
    PExpression index = (PExpression) visit(obj.getIndex(), null);
    return new pir.expression.reference.RefIndex(index);
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
  protected PirObject visitFunctionBase(FunctionBase obj, PirObject param) {
    String name = obj.getName();

    List<pir.other.SsaVariable> arg = new ArrayList<pir.other.SsaVariable>();
    for (evl.variable.Variable var : obj.getParam()) {
      assert (var instanceof SsaVariable);
      arg.add((pir.other.SsaVariable) visit(var, param));
    }

    pir.function.Function func;
    TypeRef retType;
    if (obj instanceof FuncWithReturn) {
      retType = (TypeRef) visit(((FuncWithReturn) obj).getRet(), null);
    } else {
      retType = new TypeRef(new pir.type.VoidType()); // FIXME get single void type instance
    }

    if (obj instanceof FuncWithBody) {
      func = new FuncImpl(name, arg, retType);
    } else {
      func = new FuncProto(name, arg, retType);
    }
    func.getAttributes().addAll(obj.getAttributes());

    map.put(obj, func); // otherwise the compiler follows recursive calls

    if (obj instanceof FuncWithBody) {
      pir.cfg.BasicBlockList stmt = (pir.cfg.BasicBlockList) visit(((FuncWithBody) obj).getBody(), param);
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
  protected PirObject visitVarDef(VarDefStmt obj, PirObject param) {
    pir.other.FuncVariable cvar = (pir.other.FuncVariable) visit(obj.getVariable(), param);
    pir.statement.VarDefStmt ret = new pir.statement.VarDefStmt(cvar);
    return ret;
  }

  @Override
  protected pir.statement.CallStmt visitCallStmt(CallStmt obj, PirObject param) {
    PirObject call = visit(obj.getCall(), param);
    return (pir.statement.CallStmt) call;
    // assert (call instanceof CallAssignment);
    // CallAssignment ass = (CallAssignment) call;
    // return new pir.statement.CallStmt(ass.getRef(), ass.getParameter()); // FIXME is that ok? just forget the defined
    // // variable?
  }

  @Override
  protected PirObject visitBasicBlockList(BasicBlockList obj, PirObject param) {
    pir.cfg.BasicBlockList ret = new pir.cfg.BasicBlockList((pir.cfg.BasicBlock) visit(obj.getEntry(), null), (pir.cfg.BasicBlock) visit(obj.getExit(), null));
    for (BasicBlock bb : obj.getBasicBlocks()) {
      ret.getBasicBlocks().add((pir.cfg.BasicBlock) visit(bb, param));
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
  protected PirObject visitRange(Range obj, PirObject param) {
    pir.type.RangeType ret = new pir.type.RangeType(obj.getLow(), obj.getHigh());
    return ret;
  }

  @Override
  protected PirObject visitArrayType(ArrayType obj, PirObject param) {
    TypeRef elemType = (TypeRef) visit(obj.getType(), param);
    pir.type.ArrayType ret = new pir.type.ArrayType(obj.getSize(), elemType);
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
    assert (expr instanceof PirValue);
    return new pir.cfg.ReturnExpr((PirValue) expr);
  }

  @Override
  protected PirObject visitReturnVoid(ReturnVoid obj, PirObject param) {
    return new pir.cfg.ReturnVoid();
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
    pir.type.Type typ = getType((pir.expression.reference.Reference) visit(obj.getRef(), param));
    return new pir.type.TypeAlias(obj.getName(), typ);
  }

  @Override
  protected PirObject visitFuncVariable(FuncVariable obj, PirObject param) {
    return new pir.other.FuncVariable(obj.getName(), (TypeRef) visit(obj.getType(), null));
  }

  @Override
  protected PirObject visitStateVariable(StateVariable obj, PirObject param) {
    return new pir.other.StateVariable(obj.getName(), (TypeRef) visit(obj.getType(), null));
  }

  @Override
  protected PirObject visitConstant(Constant obj, PirObject param) {
    pir.type.Type type = (pir.type.Type) visit(obj.getType(), null);
    PExpression def = (PExpression) visit(obj.getDef(), null);
    return new pir.other.Constant(obj.getName(), new TypeRef(type), def);
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
  protected PirObject visitPhiStmt(PhiStmt obj, PirObject param) {
    pir.cfg.PhiStmt ret = new pir.cfg.PhiStmt((pir.other.SsaVariable) visit(obj.getVariable(), null));
    for (BasicBlock in : obj.getInBB()) {
      pir.other.SsaVariable var = (pir.other.SsaVariable) visit(obj.getArg(in), null);
      ret.addArg((pir.cfg.BasicBlock) visit(in, null), new VarRefSimple(var));
    }
    return ret;
  }

  @Override
  protected PirObject visitBasicBlock(BasicBlock obj, PirObject param) {
    pir.cfg.BasicBlock ret = new pir.cfg.BasicBlock(obj.getName());
    map.put(obj, ret);

    for (PhiStmt phi : obj.getPhi()) {
      ret.getPhi().add((pir.cfg.PhiStmt) visit(phi, null));
    }
    for (Statement stmt : obj.getCode()) {
      ret.getCode().add((pir.statement.Statement) visit(stmt, null));
    }
    ret.setEnd((BasicBlockEnd) visit(obj.getEnd(), null));

    return ret;
  }

  @Override
  protected PirObject visitIfGoto(IfGoto obj, PirObject param) {
    PirObject cond = visit(obj.getCondition(), null);
    assert (cond instanceof VarRefSimple);
    pir.other.SsaVariable var = ((VarRefSimple) cond).getRef();
    assert (var.getType().getRef() instanceof pir.type.BooleanType);
    return new pir.cfg.IfGoto(new VarRefSimple(var), (pir.cfg.BasicBlock) visit(obj.getThenBlock(), null), (pir.cfg.BasicBlock) visit(obj.getElseBlock(), null));
  }

  @Override
  protected PirObject visitGoto(Goto obj, PirObject param) {
    return new pir.cfg.Goto((pir.cfg.BasicBlock) visit(obj.getTarget(), null));
  }

  @Override
  protected PirObject visitSsaVariable(SsaVariable obj, PirObject param) {
    return new pir.other.SsaVariable(obj.getName(), (TypeRef) visit(obj.getType(), null));
  }

  @Override
  protected PirObject visitVarDefInitStmt(VarDefInitStmt obj, PirObject param) {
    pir.other.SsaVariable var = (pir.other.SsaVariable) visit(obj.getVariable(), null);
    ToVariableGenerator converter = new ToVariableGenerator(this);
    return converter.traverse(obj.getInit(), var);
  }

  @Override
  protected PirObject visitAssignment(Assignment obj, PirObject param) {
    assert (obj.getLeft().getLink() instanceof evl.variable.Variable);
    evl.variable.Variable var = (evl.variable.Variable) obj.getLeft().getLink();

    if (isScalar(var.getType().getRef())) {
      assert (obj.getLeft().getOffset().isEmpty());

      ToVariableGenerator converter = new ToVariableGenerator(this);

      VariableGeneratorStmt vargen = converter.traverse(obj.getRight(), (pir.other.SsaVariable) visit(obj.getLeft().getLink(), null));
      return vargen;
    } else {
      VarRef dst = (VarRef) visit(obj.getLeft(), null);
      PirValue src = (PirValue) visit(obj.getRight(), null);
      ComplexWriter cw = new ComplexWriter(dst, src);
      return cw;
    }
  }

  // TODO make it better
  private boolean isScalar(Type type) {
    return (type instanceof IntegerType) || (type instanceof BooleanType) || (type instanceof Range);
  }

  @Override
  protected PirObject visitTypeRef(evl.type.TypeRef obj, PirObject param) {
    return new TypeRef((pir.type.Type) visit(obj.getRef(), null));
  }

}

class ToVariableGenerator extends NullTraverser<VariableGeneratorStmt, pir.other.SsaVariable> {
  private ToPir converter;

  public ToVariableGenerator(ToPir converter) {
    super();
    this.converter = converter;
  }

  @Override
  protected VariableGeneratorStmt visitDefault(Evl obj, pir.other.SsaVariable param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected VariableGeneratorStmt visitReference(Reference obj, pir.other.SsaVariable param) {
    // TODO make it simple
    if ((obj.getLink() instanceof evl.variable.SsaVariable) && obj.getOffset().isEmpty()) {
      pir.other.SsaVariable var = (pir.other.SsaVariable) converter.traverse(obj.getLink(), null);
      return new pir.statement.Assignment(param, new VarRefSimple(var));
    } else if (obj.getLink() instanceof FunctionHeader) {
      assert (obj.getOffset().size() == 1);
      evl.expression.reference.RefCall ofs = (RefCall) obj.getOffset().get(0);
      Function ref = (Function) converter.traverse(obj.getLink(), null);
      ArrayList<PirValue> parameter = new ArrayList<PirValue>();
      for (Expression expr : ofs.getActualParameter()) {
        parameter.add((PirValue) converter.traverse(expr, null));
      }
      return new CallAssignment(param, ref, parameter);
    }
    throw new RuntimeException("not yet implemented: " + obj);
  }

  // FIXME hack needed since assignment wants a variable as source
  @Override
  protected VariableGeneratorStmt visitNumber(Number obj, pir.other.SsaVariable param) {
    pir.expression.Number num = new pir.expression.Number(obj.getValue());
    return new pir.statement.Assignment(param, num);
  }

  @Override
  protected VariableGeneratorStmt visitTypeCast(TypeCast obj, pir.other.SsaVariable param) {
    VarRefSimple old = (VarRefSimple) converter.traverse(obj.getRef(), null);
    return new pir.statement.convert.TypeCast(param, old);
  }

  @Override
  protected VariableGeneratorStmt visitRelation(Relation obj, pir.other.SsaVariable param) {
    PirValue left = (PirValue) converter.visit(obj.getLeft(), null);
    PirValue right = (PirValue) converter.visit(obj.getRight(), null);
    return new pir.statement.Relation(param, left, right, obj.getOp());
  }

  @Override
  protected VariableGeneratorStmt visitArithmeticOp(ArithmeticOp obj, pir.other.SsaVariable param) {
    PirValue left = (PirValue) converter.visit(obj.getLeft(), null);
    PirValue right = (PirValue) converter.visit(obj.getRight(), null);
    return new pir.statement.ArithmeticOp(param, left, right, toCOp(obj.getOp()));
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

}
