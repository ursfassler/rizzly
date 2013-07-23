package pir.traverser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import pir.PirObject;
import pir.Traverser;
import pir.expression.ArOp;
import pir.expression.ArithmeticOp;
import pir.expression.ArrayValue;
import pir.expression.BoolValue;
import pir.expression.Number;
import pir.expression.PExpression;
import pir.expression.Reference;
import pir.expression.RelOp;
import pir.expression.Relation;
import pir.expression.StringValue;
import pir.expression.UnOp;
import pir.expression.UnaryExpr;
import pir.expression.reference.RefCall;
import pir.expression.reference.RefHead;
import pir.expression.reference.RefIndex;
import pir.expression.reference.RefName;
import pir.function.FuncWithBody;
import pir.function.FuncWithRet;
import pir.function.Function;
import pir.function.impl.FuncImplRet;
import pir.function.impl.FuncImplVoid;
import pir.function.impl.FuncProtoRet;
import pir.function.impl.FuncProtoVoid;
import pir.know.KnowPirType;
import pir.other.Constant;
import pir.other.FuncVariable;
import pir.other.Program;
import pir.other.StateVariable;
import pir.statement.Assignment;
import pir.statement.Block;
import pir.statement.CallStmt;
import pir.statement.CaseEntry;
import pir.statement.CaseOptEntry;
import pir.statement.CaseOptRange;
import pir.statement.CaseOptValue;
import pir.statement.CaseStmt;
import pir.statement.IfStmt;
import pir.statement.IfStmtEntry;
import pir.statement.ReturnValue;
import pir.statement.ReturnVoid;
import pir.statement.Statement;
import pir.statement.VarDefStmt;
import pir.statement.WhileStmt;
import pir.type.Array;
import pir.type.BooleanType;
import pir.type.EnumElement;
import pir.type.EnumType;
import pir.type.NamedElement;
import pir.type.StringType;
import pir.type.StructType;
import pir.type.Type;
import pir.type.TypeAlias;
import pir.type.UnionType;
import pir.type.UnsignedType;
import pir.type.VoidType;
import cir.CirBase;
import cir.expression.BinaryOp;
import cir.expression.Expression;
import cir.expression.Op;
import cir.expression.UnaryOp;
import cir.expression.reference.RefItem;
import cir.expression.reference.Referencable;
import cir.function.FunctionImpl;
import cir.function.FunctionPrototype;
import cir.other.Variable;
import cir.type.ArrayType;
import cir.type.IntType;

import common.FuncAttr;

import error.ErrorType;
import error.RError;

public class ToC extends Traverser<CirBase, Void> {
  private Map<PirObject, CirBase> map = new HashMap<PirObject, CirBase>();

  public static CirBase process(PirObject obj) {
    ToC toC = new ToC();
    return toC.traverse(obj, null);
  }

  @Override
  protected CirBase visit(PirObject obj, Void param) {
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
      Variable ct = (Variable) visitVariable(itr, param);
      prog.getVariable().add(ct);
    }
    for (Constant itr : obj.getConstant()) {
      cir.other.Constant ct = (cir.other.Constant) visitVariable(itr, param);
      prog.getVariable().add(ct);
    }
    for (Function itr : obj.getFunction()) {
      cir.function.Function ct = (cir.function.Function) visit(itr, param);
      prog.getFunction().add(ct);
    }
    return prog;
  }

  @Override
  protected CirBase visitRefHead(RefHead obj, Void param) {
    return new cir.expression.reference.RefHead((Referencable) visit((PirObject) obj.getRef(), param));
  }

  @Override
  protected CirBase visitRefCall(RefCall obj, Void param) {
    cir.expression.reference.RefCall call = new cir.expression.reference.RefCall((RefItem) visit(obj.getPrevious(), param));
    for (PExpression expr : obj.getParameter()) {
      Expression actarg = (Expression) visit(expr, param);
      call.getParameter().add(actarg);
    }
    return call;
  }

  @Override
  protected CirBase visitRefIndex(RefIndex obj, Void param) {
    return new cir.expression.reference.RefIndex((RefItem) visit(obj.getPrevious(), param), (Expression) visit(obj.getIndex(), param));
  }

  @Override
  protected CirBase visitRefName(RefName obj, Void param) {
    return new cir.expression.reference.RefName((RefItem) visit(obj.getPrevious(), param), obj.getName());
  }

  @Override
  protected CirBase visitReturnVoid(ReturnVoid obj, Void param) {
    return new cir.statement.ReturnVoid();
  }

  @Override
  protected CirBase visitReturnValue(ReturnValue obj, Void param) {
    return new cir.statement.ReturnValue((Expression) visit(obj.getValue(), param));
  }

  @Override
  protected CirBase visitIfStmtEntry(IfStmtEntry obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected CirBase visitCaseEntry(CaseEntry obj, Void param) {
    cir.statement.CaseEntry ret = new cir.statement.CaseEntry((cir.statement.Block) visit(obj.getCode(), param));
    for (CaseOptEntry entry : obj.getValues()) {
      if (entry instanceof CaseOptValue) {
        PExpression expr = ((CaseOptValue) entry).getValue();
        if (expr instanceof Number) {
          int value = ((Number) expr).getValue();
          ret.getValues().add(value);
        } else {
          RError.err(ErrorType.Fatal, "Case entry should be evaluated");
        }
      } else {
        RError.err(ErrorType.Fatal, "There should be no more range entries");
      }
    }
    return ret;
  }

  @Override
  protected CirBase visitCaseOptValue(CaseOptValue obj, Void param) {
    assert (false);
    return null;
  }

  @Override
  protected CirBase visitCaseOptRange(CaseOptRange obj, Void param) {
    assert (false);
    return null;
  }

  @Override
  protected CirBase visitConstant(Constant obj, Void param) {
    return new cir.other.Constant(obj.getName(), (cir.type.Type) visit(obj.getType(), param), (Expression) visit(obj.getDef(), param));
  }

  @Override
  protected CirBase visitStateVariable(StateVariable obj, Void param) {
    return new cir.other.StateVariable(obj.getName(), (cir.type.Type) visit(obj.getType(), param));
  }

  @Override
  protected CirBase visitFuncVariable(FuncVariable obj, Void param) {
    cir.other.FuncVariable var = new cir.other.FuncVariable(obj.getName(), (cir.type.Type) visit(obj.getType(), param));
    return var;
  }

  @Override
  protected CirBase visitTypeAlias(TypeAlias obj, Void param) {
    return new cir.type.TypeAlias(obj.getName(), (cir.type.Type) visit(obj.getRef(), param));
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
  protected CirBase visitRelation(Relation obj, Void param) {
    return new BinaryOp((Expression) visit(obj.getLeft(), param), (Expression) visit(obj.getRight(), param), toCOp(obj.getOp()));
  }

  @Override
  protected CirBase visitUnaryExpr(UnaryExpr obj, Void param) {
    Type typ = KnowPirType.get(obj);
    boolean isBool = typ instanceof BooleanType;
    return new UnaryOp(toCOp(obj.getOp(), isBool), (Expression) visit(obj.getExpr(), param));
  }

  private Op toCOp(UnOp op, boolean isBool) {
    switch (op) {
    case MINUS:
      return Op.MINUS;
    case NOT:
      if (isBool) {
        return Op.LOCNOT;
      } else {
        return Op.BITNOT;
      }
    default:
      throw new RuntimeException("not yet implemented: " + op);
    }
  }

  @Override
  protected CirBase visitArithmeticOp(ArithmeticOp obj, Void param) {
    Type typ = KnowPirType.get(obj);
    boolean isBool = typ instanceof BooleanType;
    return new BinaryOp((Expression) visit(obj.getLeft(), param), (Expression) visit(obj.getRight(), param), toCOp(obj.getOp(), isBool));
  }

  private Op toCOp(ArOp op, boolean isBool) {
    switch (op) {
    case PLUS:
      return Op.PLUS;
    case MINUS:
      return Op.MINUS;
    case MUL:
      return Op.MUL;
    case DIV:
      return Op.DIV;
    case MOD:
      return Op.MOD;
    case SHL:
      return Op.SHL;
    case SHR:
      return Op.SHR;
    case AND:
      if (isBool) {
        return Op.LOCAND;
      } else {
        return Op.BITAND;
      }
    case OR:
      if (isBool) {
        return Op.LOCOR;
      } else {
        return Op.BITOR;
      }
    default:
      throw new RuntimeException("not yet implemented: " + op);
    }
  }

  private Op toCOp(RelOp op) {
    switch (op) {
    case EQUAL:
      return Op.EQUAL;
    case NOT_EQUAL:
      return Op.NOT_EQUAL;
    case LESS:
      return Op.LESS;
    case LESS_EQUAL:
      return Op.LESS_EQUAL;
    case GREATER:
      return Op.GREATER;
    case GREATER_EQUEAL:
      return Op.GREATER_EQUEAL;
    default:
      throw new RuntimeException("not yet implemented: " + op);
    }
  }

  @Override
  protected CirBase visitReference(Reference obj, Void param) {
    return new cir.expression.Reference((RefItem) visit(obj.getRef(), param));
  }

  @Override
  protected CirBase visitFunction(Function obj, Void param) {
    String name = obj.getName();

    List<cir.other.FuncVariable> arg = new ArrayList<cir.other.FuncVariable>();
    for (FuncVariable var : obj.getArgument()) {
      arg.add((cir.other.FuncVariable) visitVariable(var, param));
    }

    cir.type.Type rettype;
    if (obj instanceof FuncWithRet) {
      rettype = (cir.type.Type) visit(((FuncWithRet) obj).getRetType(), param);
    } else {
      rettype = new cir.type.VoidType(); // FIXME ok to create it here?
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
    return new cir.statement.CallStmt((cir.expression.Reference) visit(obj.getRef(), param));
  }

  @Override
  protected CirBase visitAssignment(Assignment obj, Void param) {
    return new cir.statement.Assignment((cir.expression.Reference) visit(obj.getDst(), param), (Expression) visit(obj.getSrc(), param));
  }

  @Override
  protected CirBase visitCaseStmt(CaseStmt obj, Void param) {
    cir.statement.CaseStmt stmt = new cir.statement.CaseStmt((Expression) visit(obj.getCondition(), param), (cir.statement.Block) visit(obj.getOtherwise(), param));
    for (CaseEntry entry : obj.getEntries()) {
      cir.statement.CaseEntry centry = (cir.statement.CaseEntry) visit(entry, param);
      stmt.getEntries().add(centry);
    }
    return stmt;
  }

  @Override
  protected CirBase visitWhile(WhileStmt obj, Void param) {
    return new cir.statement.WhileStmt((Expression) visit(obj.getCondition(), param), (cir.statement.Block) visit(obj.getBlock(), param));
  }

  @Override
  protected CirBase visitIfStmt(IfStmt obj, Void param) {
    assert (!obj.getOption().isEmpty());

    IfStmtEntry last = obj.getOption().get(obj.getOption().size() - 1);
    Expression cond = (Expression) visit(last.getCondition(), param);
    cir.statement.Statement thenb = (cir.statement.Statement) visit(last.getCode(), param);
    cir.statement.Statement elseb = (cir.statement.Statement) visit(obj.getDef(), param);
    cir.statement.IfStmt act = new cir.statement.IfStmt(cond, thenb, elseb);

    for (int i = obj.getOption().size() - 2; i >= 0; i--) {
      IfStmtEntry itr = obj.getOption().get(i);
      Expression mcond = (Expression) visit(itr.getCondition(), param);
      cir.statement.Statement mthen = (cir.statement.Statement) visit(itr.getCode(), param);
      act = new cir.statement.IfStmt(mcond, mthen, act);
    }

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
  protected CirBase visitEnumType(EnumType obj, Void param) {
    cir.type.EnumType type = new cir.type.EnumType(obj.getName());
    map.put(obj, type); // otherwise the compiler follows recursive construction
    for (EnumElement elem : obj.getElements()) {
      type.getElements().add((cir.type.EnumElement) visit(elem, param));
    }
    return type;
  }

  @Override
  protected cir.type.EnumElement visitEnumElement(EnumElement obj, Void param) {
    cir.type.Type type = (cir.type.Type) visit(obj.getType(), param);
    return new cir.type.EnumElement(obj.getName(), type, obj.getIntValue());
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
    return new cir.type.NamedElement(obj.getName(), (cir.type.Type) visit(obj.getType(), param));
  }

  @Override
  protected CirBase visitArray(Array obj, Void param) {
    return new ArrayType(obj.getName(), (cir.type.Type) visit(obj.getType(), param), obj.getSize());
  }

  @Override
  protected IntType visitUnsignedType(UnsignedType obj, Void param) {
    if ((obj.getBits() % 8) != 0) {
      RError.err(ErrorType.Fatal, "Can only convert int types with multiple of 8 bits, got " + obj.getBits());
    }
    return new IntType(false, obj.getBits() / 8);
  }

  @Override
  protected CirBase visitStringValue(StringValue obj, Void param) {
    return new cir.expression.StringValue(obj.getValue());
  }

  @Override
  protected CirBase visitArrayValue(ArrayValue obj, Void param) {
    cir.expression.ArrayValue ret = new cir.expression.ArrayValue();
    for (PExpression expr : obj.getValue()) {
      Expression actarg = (Expression) visit(expr, param);
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
  protected CirBase visitFuncImplVoid(FuncImplVoid obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected CirBase visitFuncImplRet(FuncImplRet obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected CirBase visitFuncProtoVoid(FuncProtoVoid obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected CirBase visitFuncProtoRet(FuncProtoRet obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

}
