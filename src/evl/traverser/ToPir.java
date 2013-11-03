package evl.traverser;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pir.PirObject;
import pir.expression.Expression;
import pir.expression.reference.RefItem;
import pir.expression.reference.Referencable;
import pir.function.FuncWithRet;
import pir.function.impl.FuncImplRet;
import pir.function.impl.FuncImplVoid;
import pir.other.Program;
import pir.type.NamedElement;
import util.Range;
import evl.Evl;
import evl.NullTraverser;
import evl.expression.ArrayValue;
import evl.expression.BoolValue;
import evl.expression.Number;
import evl.expression.StringValue;
import evl.expression.TypeCast;
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
import evl.expression.binop.Plus;
import evl.expression.binop.Shl;
import evl.expression.binop.Shr;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefIndex;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.expression.unop.Not;
import evl.function.FuncWithBody;
import evl.function.FuncWithReturn;
import evl.function.FunctionBase;
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
import evl.type.TypeRef;
import evl.type.base.ArrayType;
import evl.type.base.BooleanType;
import evl.type.base.RangeType;
import evl.type.base.StringType;
import evl.type.composed.RecordType;
import evl.type.composed.UnionSelector;
import evl.type.composed.UnionType;
import evl.type.special.PointerType;
import evl.type.special.VoidType;
import evl.variable.Constant;
import evl.variable.FuncVariable;
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

  private pir.type.RangeType getRange(BigInteger low, BigInteger high) {
    // TODO Check if type exists and use that one
    System.err.println("Check if type exists and use that one");
    return new pir.type.RangeType(low, high);
  }

  @Override
  protected PirObject visitPointerType(PointerType obj, PirObject param) {
    return new pir.type.PointerType((pir.type.TypeRef) visit(obj.getType(), null));
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
    Referencable ref = (Referencable) visit(obj.getLink(), null);
    pir.expression.reference.Reference ret = new pir.expression.reference.Reference(ref);

    for (evl.expression.reference.RefItem itr : obj.getOffset()) {
      RefItem itm = (RefItem) visit(itr, null);
      ret.getOffset().add(itm);
    }

    return ret;
  }

  @Override
  protected pir.expression.reference.RefCall visitRefCall(RefCall obj, PirObject param) {
    pir.expression.reference.RefCall ret = new pir.expression.reference.RefCall();
    for (evl.expression.Expression itr : obj.getActualParameter()) {
      PirObject arg = visit(itr, param);
      assert (arg instanceof Expression);
      ret.getParameter().add((Expression) arg);
    }
    return ret;
  }

  @Override
  protected PirObject visitRefName(RefName obj, PirObject param) {
    return new pir.expression.reference.RefName(obj.getName());
  }

  @Override
  protected PirObject visitRefIndex(RefIndex obj, PirObject param) {
    Expression index = (Expression) visit(obj.getIndex(), param);
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
        func = new pir.function.impl.FuncProtoRet(name, arg);
      } else {
        func = new pir.function.impl.FuncProtoVoid(name, arg);
      }
    }
    func.getAttributes().addAll(obj.getAttributes());

    map.put(obj, func); // otherwise the compiler follows recursive calls

    if (obj instanceof FuncWithReturn) {
      pir.type.TypeRef rettype = (pir.type.TypeRef) visit(((FuncWithReturn) obj).getRet(), param);
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
    pir.type.UnionType struct = new pir.type.UnionType(obj.getName(), (pir.type.UnionSelector) visit(obj.getSelector(), null));
    for (evl.type.composed.NamedElement elem : obj.getElement()) {
      NamedElement celem = (NamedElement) visit(elem, param);
      struct.getElements().add(celem);
    }
    return struct;
  }

  @Override
  protected pir.type.StructType visitRecordType(RecordType obj, PirObject param) {
    pir.type.StructType struct = new pir.type.StructType(obj.getName());
    for (evl.type.composed.NamedElement elem : obj.getElement()) {
      NamedElement celem = new NamedElement(elem.getName(), (pir.type.TypeRef) visit(elem.getType(), param));
      struct.getElements().add(celem);
    }
    return struct;
  }

  @Override
  protected PirObject visitWhile(While obj, PirObject param) {
    Expression cond = (Expression) visit(obj.getCondition(), param);
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
  protected PirObject visitIfStmt(IfStmt obj, PirObject param) {
    assert (obj.getOption().size() == 1);
    IfOption opt = obj.getOption().get(0);
    pir.statement.Block elseBlock = (pir.statement.Block) visit(obj.getDefblock(), param);
    pir.statement.Block thenBlock = (pir.statement.Block) visit(opt.getCode(), param);
    Expression cond = (Expression) visit(opt.getCondition(), null);

    return new pir.statement.IfStmt(cond, thenBlock, elseBlock);
  }

  @Override
  protected pir.statement.CallStmt visitCallStmt(CallStmt obj, PirObject param) {
    PirObject call = visit(obj.getCall(), param);
    assert (call instanceof pir.expression.reference.Reference);
    return new pir.statement.CallStmt((pir.expression.reference.Reference) call);
  }

  @Override
  protected pir.statement.Assignment visitAssignment(Assignment obj, PirObject param) {
    PirObject dst = visit(obj.getLeft(), param);
    PirObject src = visit(obj.getRight(), param);
    assert (dst instanceof pir.expression.reference.Reference);
    assert (src instanceof Expression);
    return new pir.statement.Assignment((pir.expression.reference.Reference) dst, (Expression) src);
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
  protected PirObject visitBoolValue(BoolValue obj, PirObject param) {
    return new pir.expression.BoolValue(obj.isValue());
  }

  @Override
  protected pir.expression.Number visitNumber(Number obj, PirObject param) {
    pir.type.RangeType type = getRange(obj.getValue(), obj.getValue());
    return new pir.expression.Number(obj.getValue(), new pir.type.TypeRef(type));
  }

  @Override
  protected PirObject visitStringValue(StringValue obj, PirObject param) {
    return new pir.expression.StringValue(obj.getValue());
  }

  @Override
  protected PirObject visitArrayValue(ArrayValue obj, PirObject param) {
    pir.expression.ArrayValue ret = new pir.expression.ArrayValue();
    for (evl.expression.Expression itr : obj.getValue()) {
      PirObject arg = visit(itr, param);
      assert (arg instanceof Expression);
      ret.getValue().add((Expression) arg);
    }
    return ret;
  }

  @Override
  protected PirObject visitNumSet(RangeType obj, PirObject param) {
    pir.type.RangeType ret = new pir.type.RangeType(obj.getNumbers().getLow(), obj.getNumbers().getHigh());
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
    assert (expr instanceof Expression);
    return new pir.statement.ReturnExpr((Expression) expr);
  }

  @Override
  protected PirObject visitReturnVoid(ReturnVoid obj, PirObject param) {
    return new pir.statement.ReturnVoid();
  }

  @Override
  protected PirObject visitVoidType(VoidType obj, PirObject param) {
    pir.type.VoidType ret = new pir.type.VoidType();
    return ret;
  }

  @Override
  protected PirObject visitStringType(StringType obj, PirObject param) {
    pir.type.StringType ret = new pir.type.StringType();
    return ret;
  }

  @Override
  protected PirObject visitFuncVariable(FuncVariable obj, PirObject param) {
    pir.type.TypeRef type = (pir.type.TypeRef) visit(obj.getType(), null);
    return new pir.other.FuncVariable(obj.getName(), type);
  }

  @Override
  protected PirObject visitStateVariable(StateVariable obj, PirObject param) {
    pir.type.TypeRef type = (pir.type.TypeRef) visit(obj.getType(), null);
    return new pir.other.StateVariable(obj.getName(), type);
  }

  @Override
  protected PirObject visitCaseStmt(CaseStmt obj, PirObject param) {
    Expression cond = (Expression) visit(obj.getCondition(), param);
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
    List<Range> value = new ArrayList<Range>();
    for (CaseOptEntry entry : obj.getValue()) {
      if (entry instanceof CaseOptRange) {
        pir.expression.Number start = (pir.expression.Number) visit(((CaseOptRange) entry).getStart(), param);
        pir.expression.Number end = (pir.expression.Number) visit(((CaseOptRange) entry).getEnd(), param);
        value.add(new Range(start.getValue(), end.getValue()));
      } else if (entry instanceof CaseOptValue) {
        pir.expression.Number num = (pir.expression.Number) visit(((CaseOptValue) entry).getValue(), param);
        value.add(new Range(num.getValue(), num.getValue()));
      } else {
        throw new RuntimeException("not yet implemented");
      }
    }
    pir.statement.Block code = (pir.statement.Block) visit(obj.getCode(), param);
    return new pir.statement.CaseEntry(value, code);
  }

  @Override
  protected PirObject visitNamedElement(evl.type.composed.NamedElement obj, PirObject param) {
    return new NamedElement(obj.getName(), (pir.type.TypeRef) visit(obj.getType(), null));
  }

  @Override
  protected PirObject visitTypeRef(TypeRef obj, PirObject param) {
    return new pir.type.TypeRef((pir.type.Type) visit(obj.getRef(), null));
  }

  @Override
  protected PirObject visitArrayType(ArrayType obj, PirObject param) {
    return new pir.type.ArrayType(obj.getSize(), (pir.type.TypeRef) visit(obj.getType(), null));
  }

  @Override
  protected PirObject visitTypeCast(TypeCast obj, PirObject param) {
    return new pir.expression.TypeCast((pir.type.TypeRef) visit(obj.getCast(), null), (Expression) visit(obj.getValue(), null));
  }

  @Override
  protected PirObject visitUnionSelector(UnionSelector obj, PirObject param) {
    return new pir.type.UnionSelector(obj.getName());
  }

  @Override
  protected PirObject visitPlus(Plus obj, PirObject param) {
    return new pir.expression.binop.Plus((Expression) visit(obj.getLeft(), null), (Expression) visit(obj.getRight(), null));
  }

  @Override
  protected PirObject visitMinus(Minus obj, PirObject param) {
    return new pir.expression.binop.Minus((Expression) visit(obj.getLeft(), null), (Expression) visit(obj.getRight(), null));
  }

  @Override
  protected PirObject visitMul(Mul obj, PirObject param) {
    return new pir.expression.binop.Mul((Expression) visit(obj.getLeft(), null), (Expression) visit(obj.getRight(), null));
  }

  @Override
  protected PirObject visitDiv(Div obj, PirObject param) {
    return new pir.expression.binop.Div((Expression) visit(obj.getLeft(), null), (Expression) visit(obj.getRight(), null));
  }

  @Override
  protected PirObject visitMod(Mod obj, PirObject param) {
    return new pir.expression.binop.Mod((Expression) visit(obj.getLeft(), null), (Expression) visit(obj.getRight(), null));
  }

  @Override
  protected PirObject visitNot(Not obj, PirObject param) {
    return new pir.expression.unop.Not((Expression) visit(obj.getExpr(), null));
  }

  @Override
  protected PirObject visitEqual(Equal obj, PirObject param) {
    return new pir.expression.binop.Equal((Expression) visit(obj.getLeft(), null), (Expression) visit(obj.getRight(), null));
  }

  @Override
  protected PirObject visitNotequal(Notequal obj, PirObject param) {
    return new pir.expression.binop.Notequal((Expression) visit(obj.getLeft(), null), (Expression) visit(obj.getRight(), null));
  }

  @Override
  protected PirObject visitLess(Less obj, PirObject param) {
    return new pir.expression.binop.Less((Expression) visit(obj.getLeft(), null), (Expression) visit(obj.getRight(), null));
  }

  @Override
  protected PirObject visitLessequal(Lessequal obj, PirObject param) {
    return new pir.expression.binop.Lessequal((Expression) visit(obj.getLeft(), null), (Expression) visit(obj.getRight(), null));
  }

  @Override
  protected PirObject visitGreater(Greater obj, PirObject param) {
    return new pir.expression.binop.Greater((Expression) visit(obj.getLeft(), null), (Expression) visit(obj.getRight(), null));
  }

  @Override
  protected PirObject visitGreaterequal(Greaterequal obj, PirObject param) {
    return new pir.expression.binop.Greaterequal((Expression) visit(obj.getLeft(), null), (Expression) visit(obj.getRight(), null));
  }

  @Override
  protected PirObject visitBitAnd(BitAnd obj, PirObject param) {
    return new pir.expression.binop.BitAnd((Expression) visit(obj.getLeft(), null), (Expression) visit(obj.getRight(), null));
  }

  @Override
  protected PirObject visitBitOr(BitOr obj, PirObject param) {
    return new pir.expression.binop.BitOr((Expression) visit(obj.getLeft(), null), (Expression) visit(obj.getRight(), null));
  }

  @Override
  protected PirObject visitLogicOr(LogicOr obj, PirObject param) {
    return new pir.expression.binop.LogicOr((Expression) visit(obj.getLeft(), null), (Expression) visit(obj.getRight(), null));
  }

  @Override
  protected PirObject visitLogicAnd(LogicAnd obj, PirObject param) {
    return new pir.expression.binop.LogicAnd((Expression) visit(obj.getLeft(), null), (Expression) visit(obj.getRight(), null));
  }

  @Override
  protected PirObject visitShl(Shl obj, PirObject param) {
    return new pir.expression.binop.Shl((Expression) visit(obj.getLeft(), null), (Expression) visit(obj.getRight(), null));
  }

  @Override
  protected PirObject visitShr(Shr obj, PirObject param) {
    return new pir.expression.binop.Shr((Expression) visit(obj.getLeft(), null), (Expression) visit(obj.getRight(), null));
  }

}
