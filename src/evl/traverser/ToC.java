package evl.traverser;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import util.Range;
import cir.CirBase;
import cir.expression.BinaryOp;
import cir.expression.Op;
import cir.expression.reference.RefItem;
import cir.function.FunctionImpl;
import cir.function.FunctionPrototype;
import cir.other.Variable;

import common.FuncAttr;

import evl.Evl;
import evl.NullTraverser;
import evl.expression.ArrayValue;
import evl.expression.BoolValue;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.StringValue;
import evl.expression.TypeCast;
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
import evl.expression.unop.BitNot;
import evl.expression.unop.LogicNot;
import evl.expression.unop.Uminus;
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
import evl.statement.WhileStmt;
import evl.type.Type;
import evl.type.TypeRef;
import evl.type.base.ArrayType;
import evl.type.base.BooleanType;
import evl.type.base.RangeType;
import evl.type.base.StringType;
import evl.type.composed.NamedElement;
import evl.type.composed.RecordType;
import evl.type.composed.UnionType;
import evl.type.special.VoidType;
import evl.variable.ConstGlobal;
import evl.variable.ConstPrivate;
import evl.variable.Constant;
import evl.variable.FuncVariable;
import evl.variable.StateVariable;

public class ToC extends NullTraverser<CirBase, Void> {
  private Map<Evl, CirBase> map = new HashMap<Evl, CirBase>();

  public static CirBase process(Evl obj) {
    ToC toC = new ToC();
    return toC.traverse(obj, null);
  }

  private int toInt(BigInteger bigval) {
    if (bigval.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
      assert (false); // TODO better error
    }
    if (bigval.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) < 0) {
      assert (false); // TODO better error
    }
    int intValue = bigval.intValue();
    return intValue;
  }

  @Override
  protected CirBase visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected CirBase visit(Evl obj, Void param) {
    CirBase cobj = map.get(obj);
    if (cobj == null) {
      cobj = super.visit(obj, param);
      assert (cobj != null);
      map.put(obj, cobj);
    }
    return cobj;
  }

  @Override
  protected cir.other.Program visitRizzlyProgram(RizzlyProgram obj, Void param) {
    cir.other.Program prog = new cir.other.Program(obj.getName());
    for (Type type : obj.getType()) {
      cir.type.Type ct = (cir.type.Type) visit(type, param);
      prog.getType().add(ct);
    }
    for (StateVariable itr : obj.getVariable()) {
      Variable ct = (Variable) visit(itr, param);
      prog.getVariable().add(ct);
    }
    for (Constant itr : obj.getConstant()) {
      cir.other.Constant ct = (cir.other.Constant) visit(itr, param);
      prog.getVariable().add(ct);
    }
    for (FunctionBase itr : obj.getFunction()) {
      cir.function.Function ct = (cir.function.Function) visit(itr, param);
      prog.getFunction().add(ct);
    }
    return prog;
  }

  @Override
  protected CirBase visitFunctionBase(FunctionBase obj, Void param) {
    String name = obj.getName();

    List<cir.other.FuncVariable> arg = new ArrayList<cir.other.FuncVariable>();
    for (FuncVariable var : obj.getParam()) {
      arg.add((cir.other.FuncVariable) visit(var, param));
    }

    cir.type.TypeRef rettype;
    if (obj instanceof FuncWithReturn) {
      rettype = (cir.type.TypeRef) visit(((FuncWithReturn) obj).getRet(), null);
    } else {
      cir.type.VoidType ref = new cir.type.VoidType(); // FIXME ok to create it here? no, use single instance
      rettype = new cir.type.TypeRef(ref);
    }

    cir.function.Function ret;
    if (obj instanceof FuncWithBody) {
      ret = new FunctionImpl(name, rettype, arg, new HashSet<FuncAttr>(obj.getAttributes()));
    } else {
      ret = new FunctionPrototype(name, rettype, arg, new HashSet<FuncAttr>(obj.getAttributes()));
    }

    map.put(obj, ret); // otherwise the compiler follows recursive calls

    if (obj instanceof FuncWithBody) {
      ((FunctionImpl) ret).setBody((cir.statement.Block) visit(((FuncWithBody) obj).getBody(), param));
    }

    return ret;
  }

  @Override
  protected CirBase visitTypeCast(TypeCast obj, Void param) {
    return new cir.expression.TypeCast((cir.type.TypeRef) visit(obj.getCast(), null), (cir.expression.Expression) visit(obj.getValue(), null));
  }

  @Override
  protected CirBase visitRefCall(RefCall obj, Void param) {
    cir.expression.reference.RefCall call = new cir.expression.reference.RefCall();
    for (Expression expr : obj.getActualParameter()) {
      cir.expression.Expression actarg = (cir.expression.Expression) visit(expr, param);
      call.getParameter().add(actarg);
    }
    return call;
  }

  @Override
  protected CirBase visitRefIndex(RefIndex obj, Void param) {
    return new cir.expression.reference.RefIndex((cir.expression.Expression) visit(obj.getIndex(), param));
  }

  @Override
  protected CirBase visitRefName(RefName obj, Void param) {
    return new cir.expression.reference.RefName(obj.getName());
  }

  @Override
  protected CirBase visitReturnVoid(ReturnVoid obj, Void param) {
    return new cir.statement.ReturnVoid();
  }

  @Override
  protected CirBase visitReturnExpr(ReturnExpr obj, Void param) {
    return new cir.statement.ReturnExpr((cir.expression.Expression) visit(obj.getExpr(), param));
  }

  @Override
  protected CirBase visitCaseOpt(CaseOpt obj, Void param) {
    cir.statement.CaseEntry ret = new cir.statement.CaseEntry((cir.statement.Block) visit(obj.getCode(), param));
    for (CaseOptEntry entry : obj.getValue()) {
      if (entry instanceof CaseOptRange) {
        cir.expression.Number start = (cir.expression.Number) visit(((CaseOptRange) entry).getStart(), param);
        cir.expression.Number end = (cir.expression.Number) visit(((CaseOptRange) entry).getEnd(), param);
        ret.getValues().add(new Range(start.getValue(), end.getValue()));
      } else if (entry instanceof CaseOptValue) {
        cir.expression.Number num = (cir.expression.Number) visit(((CaseOptValue) entry).getValue(), param);
        ret.getValues().add(new Range(num.getValue(), num.getValue()));
      } else {
        throw new RuntimeException("not yet implemented");
      }
    }
    return ret;
  }

  @Override
  protected CirBase visitConstGlobal(ConstGlobal obj, Void param) {
    return new cir.other.Constant(obj.getName(), (cir.type.TypeRef) visit(obj.getType(), param), (cir.expression.Expression) visit(obj.getDef(), param));
  }

  @Override
  protected CirBase visitConstPrivate(ConstPrivate obj, Void param) {
    return new cir.other.Constant(obj.getName(), (cir.type.TypeRef) visit(obj.getType(), param), (cir.expression.Expression) visit(obj.getDef(), param));
  }

  @Override
  protected CirBase visitStateVariable(StateVariable obj, Void param) {
    return new cir.other.StateVariable(obj.getName(), (cir.type.TypeRef) visit(obj.getType(), param));
  }

  @Override
  protected CirBase visitFuncVariable(FuncVariable obj, Void param) {
    cir.other.FuncVariable var = new cir.other.FuncVariable(obj.getName(), (cir.type.TypeRef) visit(obj.getType(), param));
    return var;
  }

  @Override
  protected CirBase visitRangeType(RangeType obj, Void param) {
    return new cir.type.RangeType(obj.getNumbers().getLow(), obj.getNumbers().getHigh());
  }

  @Override
  protected CirBase visitTypeRef(TypeRef obj, Void param) {
    return new cir.type.TypeRef((cir.type.Type) visit(obj.getRef(), param));
  }

  @Override
  protected CirBase visitVoidType(VoidType obj, Void param) {
    return new cir.type.VoidType();
  }

  @Override
  protected CirBase visitNumber(Number obj, Void param) {
    return new cir.expression.Number(obj.getValue());
  }

  @Override
  protected CirBase visitReference(Reference obj, Void param) {
    cir.expression.reference.Reference ref = new cir.expression.reference.Reference((cir.expression.reference.Referencable) visit(obj.getLink(), param));
    for (evl.expression.reference.RefItem itr : obj.getOffset()) {
      ref.getOffset().add((RefItem) visit(itr, null));
    }
    return ref;
  }

  @Override
  protected CirBase visitVarDef(VarDefStmt obj, Void param) {
    cir.statement.VarDefStmt stmt = new cir.statement.VarDefStmt((cir.other.FuncVariable) visit(obj.getVariable(), param));
    return stmt;
  }

  @Override
  protected CirBase visitCallStmt(CallStmt obj, Void param) {
    return new cir.statement.CallStmt((cir.expression.reference.Reference) visit(obj.getCall(), param));
  }

  @Override
  protected CirBase visitAssignment(Assignment obj, Void param) {
    return new cir.statement.Assignment((cir.expression.reference.Reference) visit(obj.getLeft(), param), (cir.expression.Expression) visit(obj.getRight(), param));
  }

  @Override
  protected CirBase visitCaseStmt(CaseStmt obj, Void param) {
    cir.statement.CaseStmt stmt = new cir.statement.CaseStmt((cir.expression.Expression) visit(obj.getCondition(), param), (cir.statement.Block) visit(obj.getOtherwise(), param));
    for (CaseOpt entry : obj.getOption()) {
      cir.statement.CaseEntry centry = (cir.statement.CaseEntry) visit(entry, param);
      stmt.getEntries().add(centry);
    }
    return stmt;
  }

  @Override
  protected CirBase visitWhileStmt(WhileStmt obj, Void param) {
    return new cir.statement.WhileStmt((cir.expression.Expression) visit(obj.getCondition(), param), (cir.statement.Block) visit(obj.getBody(), param));
  }

  @Override
  protected CirBase visitIfStmt(IfStmt obj, Void param) {
    assert (obj.getOption().size() == 1);
    IfOption opt = obj.getOption().get(0);
    cir.statement.Block elseBlock = (cir.statement.Block) visit(obj.getDefblock(), param);
    cir.statement.Block thenBlock = (cir.statement.Block) visit(opt.getCode(), param);
    cir.expression.Expression cond = (cir.expression.Expression) visit(opt.getCondition(), null);

    return new cir.statement.IfStmt(cond, thenBlock, elseBlock);
  }

  @Override
  protected CirBase visitBlock(Block obj, Void param) {
    cir.statement.Block ret = new cir.statement.Block();
    for (Statement stmt : obj.getStatements()) {
      cir.statement.Statement cstmt = (cir.statement.Statement) visit(stmt, param);
      ret.getStatement().add(cstmt);
    }
    return ret;
  }

  @Override
  protected CirBase visitRecordType(RecordType obj, Void param) {
    cir.type.StructType type = new cir.type.StructType(obj.getName());
    for (NamedElement elem : obj.getElement()) {
      cir.type.NamedElement celem = (cir.type.NamedElement) visit(elem, param);
      type.getElements().add(celem);
    }
    return type;
  }

  @Override
  protected CirBase visitUnionType(UnionType obj, Void param) {
    cir.type.UnionType type = new cir.type.UnionType(obj.getName());
    for (NamedElement elem : obj.getElement()) {
      cir.type.NamedElement celem = (cir.type.NamedElement) visit(elem, param);
      type.getElements().add(celem);
    }
    return type;
  }

  @Override
  protected CirBase visitNamedElement(NamedElement obj, Void param) {
    return new cir.type.NamedElement(obj.getName(), (cir.type.TypeRef) visit(obj.getType(), param));
  }

  @Override
  protected CirBase visitArrayType(ArrayType obj, Void param) {
    return new cir.type.ArrayType(obj.getName(), (cir.type.TypeRef) visit(obj.getType(), param), toInt(obj.getSize()));
  }

  @Override
  protected CirBase visitStringValue(StringValue obj, Void param) {
    return new cir.expression.StringValue(obj.getValue());
  }

  @Override
  protected CirBase visitArrayValue(ArrayValue obj, Void param) {
    cir.expression.ArrayValue ret = new cir.expression.ArrayValue();
    for (Expression expr : obj.getValue()) {
      cir.expression.Expression actarg = (cir.expression.Expression) visit(expr, param);
      ret.getValue().add(actarg);
    }
    return ret;
  }

  @Override
  protected CirBase visitStringType(StringType obj, Void param) {
    return new cir.type.StringType();
  }

  @Override
  protected CirBase visitBooleanType(BooleanType obj, Void param) {
    return new cir.type.BooleanType();
  }

  @Override
  protected CirBase visitBoolValue(BoolValue obj, Void param) {
    return new cir.expression.BoolValue(obj.isValue());
  }

  @Override
  protected CirBase visitUminus(Uminus obj, Void param) {
    return new cir.expression.UnaryOp(Op.MINUS, (cir.expression.Expression) visit(obj.getExpr(), null));
  }

  @Override
  protected CirBase visitLogicNot(LogicNot obj, Void param) {
    return new cir.expression.UnaryOp(Op.LOCNOT, (cir.expression.Expression) visit(obj.getExpr(), null));
  }

  @Override
  protected CirBase visitBitNot(BitNot obj, Void param) {
    return new cir.expression.UnaryOp(Op.BITNOT, (cir.expression.Expression) visit(obj.getExpr(), null));
  }

  private CirBase transBinOp(BinaryExp obj, Op op) {
    return new BinaryOp((cir.expression.Expression) visit(obj.getLeft(), null), (cir.expression.Expression) visit(obj.getRight(), null), op);
  }

  @Override
  protected CirBase visitPlus(Plus obj, Void param) {
    return transBinOp(obj, Op.PLUS);
  }

  @Override
  protected CirBase visitLogicAnd(LogicAnd obj, Void param) {
    return transBinOp(obj, Op.LOCAND);
  }

  @Override
  protected CirBase visitGreaterequal(Greaterequal obj, Void param) {
    return transBinOp(obj, Op.GREATER_EQUEAL);
  }

  @Override
  protected CirBase visitNotequal(Notequal obj, Void param) {
    return transBinOp(obj, Op.NOT_EQUAL);
  }

  @Override
  protected CirBase visitLessequal(Lessequal obj, Void param) {
    return transBinOp(obj, Op.LESS_EQUAL);
  }

  @Override
  protected CirBase visitLess(Less obj, Void param) {
    return transBinOp(obj, Op.LESS);
  }

  @Override
  protected CirBase visitGreater(Greater obj, Void param) {
    return transBinOp(obj, Op.GREATER);
  }

  @Override
  protected CirBase visitMinus(Minus obj, Void param) {
    return transBinOp(obj, Op.MINUS);
  }

  @Override
  protected CirBase visitMod(Mod obj, Void param) {
    return transBinOp(obj, Op.MOD);
  }

  @Override
  protected CirBase visitMul(Mul obj, Void param) {
    return transBinOp(obj, Op.MUL);
  }

  @Override
  protected CirBase visitDiv(Div obj, Void param) {
    return transBinOp(obj, Op.DIV);
  }

  @Override
  protected CirBase visitEqual(Equal obj, Void param) {
    return transBinOp(obj, Op.EQUAL);
  }

  @Override
  protected CirBase visitBitAnd(BitAnd obj, Void param) {
    return transBinOp(obj, Op.BITAND);
  }

  @Override
  protected CirBase visitBitOr(BitOr obj, Void param) {
    return transBinOp(obj, Op.BITOR);
  }

  @Override
  protected CirBase visitLogicOr(LogicOr obj, Void param) {
    return transBinOp(obj, Op.LOCOR);
  }

  @Override
  protected CirBase visitShl(Shl obj, Void param) {
    return transBinOp(obj, Op.SHL);
  }

  @Override
  protected CirBase visitShr(Shr obj, Void param) {
    return transBinOp(obj, Op.SHR);
  }

}
