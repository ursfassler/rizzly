package pir.traverser;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import pir.NullTraverser;
import pir.Pir;
import pir.PirObject;
import pir.expression.ArrayValue;
import pir.expression.BoolValue;
import pir.expression.Expression;
import pir.expression.Number;
import pir.expression.StringValue;
import pir.expression.TypeCast;
import pir.expression.binop.BinaryExp;
import pir.expression.binop.BitAnd;
import pir.expression.binop.BitOr;
import pir.expression.binop.Div;
import pir.expression.binop.Equal;
import pir.expression.binop.Greater;
import pir.expression.binop.Greaterequal;
import pir.expression.binop.Less;
import pir.expression.binop.Lessequal;
import pir.expression.binop.LogicAnd;
import pir.expression.binop.LogicOr;
import pir.expression.binop.Minus;
import pir.expression.binop.Mod;
import pir.expression.binop.Mul;
import pir.expression.binop.Notequal;
import pir.expression.binop.Plus;
import pir.expression.binop.Shl;
import pir.expression.binop.Shr;
import pir.expression.reference.RefCall;
import pir.expression.reference.RefIndex;
import pir.expression.reference.RefName;
import pir.expression.reference.Reference;
import pir.expression.unop.Not;
import pir.function.FuncWithBody;
import pir.function.FuncWithRet;
import pir.function.Function;
import pir.other.Constant;
import pir.other.FuncVariable;
import pir.other.Program;
import pir.other.StateVariable;
import pir.statement.Assignment;
import pir.statement.Block;
import pir.statement.CallStmt;
import pir.statement.CaseEntry;
import pir.statement.CaseStmt;
import pir.statement.IfStmt;
import pir.statement.ReturnExpr;
import pir.statement.ReturnVoid;
import pir.statement.Statement;
import pir.statement.VarDefStmt;
import pir.statement.WhileStmt;
import pir.type.ArrayType;
import pir.type.BooleanType;
import pir.type.NamedElement;
import pir.type.PointerType;
import pir.type.SignedType;
import pir.type.StringType;
import pir.type.StructType;
import pir.type.Type;
import pir.type.TypeRef;
import pir.type.UnionType;
import pir.type.UnsignedType;
import pir.type.VoidType;
import cir.CirBase;
import cir.expression.BinaryOp;
import cir.expression.Op;
import cir.expression.reference.RefItem;
import cir.function.FunctionImpl;
import cir.function.FunctionPrototype;
import cir.other.Variable;
import cir.type.SIntType;
import cir.type.UIntType;

import common.FuncAttr;

import error.ErrorType;
import error.RError;

public class ToC extends NullTraverser<CirBase, Void> {
  private Map<Pir, CirBase> map = new HashMap<Pir, CirBase>();

  public static CirBase process(PirObject obj) {
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
  protected CirBase doDefault(PirObject obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected CirBase visit(Pir obj, Void param) {
    CirBase cobj = map.get(obj);
    if (cobj == null) {
      cobj = super.visit(obj, param);
      assert (cobj != null);
      map.put(obj, cobj);
    }
    return cobj;
  }

  @Override
  protected cir.other.Program visitProgram(Program obj, Void param) {
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
    for (Function itr : obj.getFunction()) {
      cir.function.Function ct = (cir.function.Function) visit(itr, param);
      prog.getFunction().add(ct);
    }
    return prog;
  }

  @Override
  protected CirBase visitTypeCast(TypeCast obj, Void param) {
    return new cir.expression.TypeCast((cir.type.TypeRef) visit(obj.getCast(), null), (cir.expression.Expression) visit(obj.getValue(), null));
  }

  @Override
  protected CirBase visitRefCall(RefCall obj, Void param) {
    cir.expression.reference.RefCall call = new cir.expression.reference.RefCall();
    for (Expression expr : obj.getParameter()) {
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
    return new cir.statement.ReturnExpr((cir.expression.Expression) visit(obj.getValue(), param));
  }

  @Override
  protected CirBase visitCaseEntry(CaseEntry obj, Void param) {
    cir.statement.CaseEntry ret = new cir.statement.CaseEntry((cir.statement.Block) visit(obj.getCode(), param));
    ret.getValues().addAll(obj.getValues());
    return ret;
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
  protected CirBase visitPointerType(PointerType obj, Void param) {
    return new cir.type.PointerType(obj.getName(), (cir.type.TypeRef) visit(obj.getType(), null));
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
    return new cir.expression.Number(toInt(obj.getValue()));
  }

  @Override
  protected CirBase visitReference(Reference obj, Void param) {
    cir.expression.reference.Reference ref = new cir.expression.reference.Reference((cir.expression.reference.Referencable) visit(obj.getRef(), param));
    for (pir.expression.reference.RefItem itr : obj.getOffset()) {
      ref.getOffset().add((RefItem) visit(itr, null));
    }
    return ref;
  }

  @Override
  protected CirBase visitFunction(Function obj, Void param) {
    String name = obj.getName();

    List<cir.other.FuncVariable> arg = new ArrayList<cir.other.FuncVariable>();
    for (FuncVariable var : obj.getArgument()) {
      arg.add((cir.other.FuncVariable) visitVariable(var, param));
    }

    cir.type.TypeRef rettype;
    if (obj instanceof FuncWithRet) {
      rettype = (cir.type.TypeRef) visit(((FuncWithRet) obj).getRetType(), param);
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

    ret.getAttributes().addAll(obj.getAttributes());

    map.put(obj, ret); // otherwise the compiler follows recursive calls

    if (obj instanceof FuncWithBody) {
      ((FunctionImpl) ret).setBody((cir.statement.Block) visit(((FuncWithBody) obj).getBody(), param));
    }

    return ret;
  }

  @Override
  protected CirBase visitVarDefStmt(VarDefStmt obj, Void param) {
    cir.statement.VarDefStmt stmt = new cir.statement.VarDefStmt((cir.other.FuncVariable) visit(obj.getVariable(), param));
    return stmt;
  }

  @Override
  protected CirBase visitCallStmt(CallStmt obj, Void param) {
    return new cir.statement.CallStmt((cir.expression.reference.Reference) visit(obj.getRef(), param));
  }

  @Override
  protected CirBase visitAssignment(Assignment obj, Void param) {
    return new cir.statement.Assignment((cir.expression.reference.Reference) visit(obj.getDst(), param), (cir.expression.Expression) visit(obj.getSrc(), param));
  }

  @Override
  protected CirBase visitCaseStmt(CaseStmt obj, Void param) {
    cir.statement.CaseStmt stmt = new cir.statement.CaseStmt((cir.expression.Expression) visit(obj.getCondition(), param), (cir.statement.Block) visit(obj.getOtherwise(), param));
    for (CaseEntry entry : obj.getEntries()) {
      cir.statement.CaseEntry centry = (cir.statement.CaseEntry) visit(entry, param);
      stmt.getEntries().add(centry);
    }
    return stmt;
  }

  @Override
  protected CirBase visitWhileStmt(WhileStmt obj, Void param) {
    return new cir.statement.WhileStmt((cir.expression.Expression) visit(obj.getCondition(), param), (cir.statement.Block) visit(obj.getBlock(), param));
  }

  @Override
  protected CirBase visitIfStmt(IfStmt obj, Void param) {
    cir.expression.Expression cond = (cir.expression.Expression) visit(obj.getCondition(), param);
    cir.statement.Statement thenb = (cir.statement.Statement) visit(obj.getThenBlock(), param);
    cir.statement.Statement elseb = (cir.statement.Statement) visit(obj.getElseBlock(), param);
    cir.statement.IfStmt act = new cir.statement.IfStmt(cond, thenb, elseb);
    return act;
  }

  @Override
  protected CirBase visitBlock(Block obj, Void param) {
    cir.statement.Block ret = new cir.statement.Block();
    for (Statement stmt : obj.getStatement()) {
      cir.statement.Statement cstmt = (cir.statement.Statement) visit(stmt, param);
      ret.getStatement().add(cstmt);
    }
    return ret;
  }

  @Override
  protected CirBase visitStructType(StructType obj, Void param) {
    cir.type.StructType type = new cir.type.StructType(obj.getName());
    for (NamedElement elem : obj.getElements()) {
      cir.type.NamedElement celem = (cir.type.NamedElement) visit(elem, param);
      type.getElements().add(celem);
    }
    return type;
  }

  @Override
  protected CirBase visitUnionType(UnionType obj, Void param) {
    cir.type.UnionType type = new cir.type.UnionType(obj.getName());
    for (NamedElement elem : obj.getElements()) {
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

  private int getBytes(pir.type.IntType obj) {
    if ((obj.getBits() % 8) != 0) {
      RError.err(ErrorType.Fatal, "Can only convert int types with multiple of 8 bits, got " + obj.getBits());
    }
    int bytes = obj.getBits() / 8;
    return bytes;
  }

  @Override
  protected UIntType visitUnsignedType(UnsignedType obj, Void param) {
    return new UIntType(getBytes(obj));
  }

  @Override
  protected CirBase visitSignedType(SignedType obj, Void param) {
    return new SIntType(getBytes(obj));
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
  protected CirBase visitNot(Not obj, Void param) {
    return new cir.expression.UnaryOp(Op.BITNOT, (cir.expression.Expression) visit(obj.getExpr(), null)); // FIXME
                                                                                                          // bitnot is
                                                                                                          // correct?
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
