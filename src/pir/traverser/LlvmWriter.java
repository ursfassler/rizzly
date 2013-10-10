package pir.traverser;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import pir.NullTraverser;
import pir.PirObject;
import pir.cfg.BasicBlock;
import pir.cfg.BasicBlockList;
import pir.expression.ArrayValue;
import pir.expression.BoolValue;
import pir.expression.Number;
import pir.expression.StringValue;
import pir.expression.reference.RefIndex;
import pir.expression.reference.RefName;
import pir.expression.reference.VarRef;
import pir.expression.reference.VarRefConst;
import pir.expression.reference.VarRefSimple;
import pir.expression.reference.VarRefStatevar;
import pir.function.FuncWithBody;
import pir.function.Function;
import pir.other.Constant;
import pir.other.FuncVariable;
import pir.other.PirValue;
import pir.other.Program;
import pir.other.SsaVariable;
import pir.other.StateVariable;
import pir.statement.bbend.CaseGoto;
import pir.statement.bbend.CaseGotoOpt;
import pir.statement.bbend.Goto;
import pir.statement.bbend.IfGoto;
import pir.statement.bbend.ReturnExpr;
import pir.statement.bbend.ReturnVoid;
import pir.statement.bbend.Unreachable;
import pir.statement.normal.Assignment;
import pir.statement.normal.CallAssignment;
import pir.statement.normal.CallStmt;
import pir.statement.normal.GetElementPtr;
import pir.statement.normal.LoadStmt;
import pir.statement.normal.StackMemoryAlloc;
import pir.statement.normal.StmtSignes;
import pir.statement.normal.StoreStmt;
import pir.statement.normal.VariableGeneratorStmt;
import pir.statement.normal.binop.BinaryOp;
import pir.statement.normal.binop.BitAnd;
import pir.statement.normal.binop.Div;
import pir.statement.normal.binop.Equal;
import pir.statement.normal.binop.Greater;
import pir.statement.normal.binop.Greaterequal;
import pir.statement.normal.binop.Less;
import pir.statement.normal.binop.Lessequal;
import pir.statement.normal.binop.LogicAnd;
import pir.statement.normal.binop.LogicOr;
import pir.statement.normal.binop.LogicXand;
import pir.statement.normal.binop.LogicXor;
import pir.statement.normal.binop.Minus;
import pir.statement.normal.binop.Mod;
import pir.statement.normal.binop.Mul;
import pir.statement.normal.binop.Notequal;
import pir.statement.normal.binop.Plus;
import pir.statement.normal.binop.Shl;
import pir.statement.normal.binop.Shr;
import pir.statement.normal.convert.ConvertValue;
import pir.statement.normal.convert.SignExtendValue;
import pir.statement.normal.convert.TruncValue;
import pir.statement.normal.convert.TypeCast;
import pir.statement.normal.convert.ZeroExtendValue;
import pir.statement.normal.unop.Not;
import pir.statement.normal.unop.Uminus;
import pir.statement.phi.PhiStmt;
import pir.type.ArrayType;
import pir.type.BooleanType;
import pir.type.IntType;
import pir.type.NamedElement;
import pir.type.NoSignType;
import pir.type.PointerType;
import pir.type.RangeType;
import pir.type.SignedType;
import pir.type.StringType;
import pir.type.StructType;
import pir.type.Type;
import pir.type.TypeRef;
import pir.type.UnionType;
import pir.type.UnsignedType;
import pir.type.VoidType;

import common.FuncAttr;

import error.ErrorType;
import error.RError;
import evl.doc.StreamWriter;

//TODO mark private objects with "internal"
public class LlvmWriter extends NullTraverser<Void, StreamWriter> {

  private final boolean wrId;

  public LlvmWriter(boolean wrId) {
    super();
    this.wrId = wrId;
  }

  static public void print(PirObject obj, String filename, boolean wrId) {
    LlvmWriter printer = new LlvmWriter(wrId);
    try {
      printer.traverse(obj, new StreamWriter(new PrintStream(filename)));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected Void doDefault(PirObject obj, StreamWriter param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  private void wrId(PirObject obj, StreamWriter wr) {
    if (wrId) {
      wr.wr("[" + obj.hashCode() % 1000 + "]"); // TODO remove debug code
    }
  }

  protected void visitOptList(String name, Collection<? extends PirObject> type, StreamWriter param) {
    if (type.isEmpty()) {
      return;
    }
    param.wr(name);
    param.nl();
    param.incIndent();
    visitList(type, param);
    param.decIndent();
    param.emptyLine();
  }

  private void visitSepList(String sep, List<? extends PirObject> list, StreamWriter param) {
    boolean first = true;
    for (PirObject obj : list) {
      if (first) {
        first = false;
      } else {
        param.wr(sep);
      }
      visit(obj, param);
    }
  }

  // ------------------------------------------------------------------------
  @Override
  protected Void visitProgram(Program obj, StreamWriter param) {
    visitList(obj.getType(), param);
    param.nl();
    visitList(obj.getConstant(), param);
    param.nl();
    visitList(obj.getVariable(), param);
    param.nl();
    visitList(obj.getFunction(), param);
    return null;
  }

  @Override
  protected Void visitFunction(Function obj, StreamWriter param) {
    if (obj instanceof FuncWithBody) {
      param.wr("define ");
    } else {
      param.wr("declare ");
    }
    if (obj.getAttributes().contains(FuncAttr.Public)) {
      param.wr("external ccc ");
    }
    visit(obj.getRetType(), param);

    param.wr(" @");
    param.wr(obj.getName());
    param.wr("(");
    visitSepList(",", obj.getArgument(), param);
    param.wr(")");

    if (obj instanceof FuncWithBody) {
      param.wr("{");
      param.nl();
      visit(((FuncWithBody) obj).getBody(), param);
      param.wr("}");
    }
    param.nl();
    param.emptyLine();
    return null;
  }

  // ---- variables --------------------------------------------------------------------
  @Override
  protected Void visitFuncVariable(FuncVariable obj, StreamWriter param) {
    visit(obj.getType(), param);
    param.wr(" ");
    param.wr("%" + obj.getName());
    wrId(obj, param);
    return null;
  }

  @Override
  protected Void visitSsaVariable(SsaVariable obj, StreamWriter param) {
    visit(obj.getType(), param);
    param.wr(" ");
    param.wr("%" + obj.getName());
    wrId(obj, param);
    return null;
  }

  @Override
  protected Void visitConstant(Constant obj, StreamWriter param) {
    param.wr(obj.getName());
    param.wr(": ");
    visit(obj.getType(), param);
    param.wr(" = ");
    visit(obj.getDef(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitStateVariable(StateVariable obj, StreamWriter param) {
    TypeRef type = obj.getType();
    assert (type.getRef() instanceof PointerType);
    type = ((PointerType) type.getRef()).getType();
    param.wr("@" + obj.getName());
    wrId(obj, param);
    param.wr(" = global ");
    visit(type, param);
    param.wr(" undef");
    param.nl();
    return null;
  }

  // ---- types --------------------------------------------------------------------
  @Override
  protected Void visitBooleanType(BooleanType obj, StreamWriter param) {
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitNoSignType(NoSignType obj, StreamWriter param) {
    param.wr("; ");
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(" = ");
    param.wr("i");
    param.wr(Integer.toString(obj.getBits()));
    param.nl();
    return null;
  }

  @Override
  protected Void visitUnsignedType(UnsignedType obj, StreamWriter param) {
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(" = ");
    param.wr("U");
    param.wr(Integer.toString(obj.getBits()));
    param.nl();
    return null;
  }

  @Override
  protected Void visitSignedType(SignedType obj, StreamWriter param) {
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(" = ");
    param.wr("S");
    param.wr(Integer.toString(obj.getBits()));
    param.nl();
    return null;
  }

  @Override
  protected Void visitPointerType(PointerType obj, StreamWriter param) {
    param.wr("%");
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(" = type ");
    visit(obj.getType(), param);
    param.wr(" *");
    param.nl();
    return null;
  }

  @Override
  protected Void visitArray(ArrayType obj, StreamWriter param) {
    param.wr("%" + obj.getName());
    wrId(obj, param);
    param.wr(" = type [");
    param.wr(obj.getSize().toString());
    param.wr(" x ");
    visit(obj.getType(), param);
    param.wr("]");
    param.nl();
    return null;
  }

  @Override
  protected Void visitVoidType(VoidType obj, StreamWriter param) {
    param.wr("; ");
    param.wr(obj.getName());
    wrId(obj, param);
    param.nl();
    return null;
  }

  @Override
  protected Void visitStructType(StructType obj, StreamWriter param) {
    param.wr("%");
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(" = type {");
    param.nl();
    param.incIndent();
    wrNamedElem(obj.getElements(), param);
    param.decIndent();
    param.wr("}");
    param.nl();
    return null;
  }

  @Override
  protected Void visitUnionType(UnionType obj, StreamWriter param) {
    param.wr("%");
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(" = type {");
    param.nl();
    param.incIndent();
    wrNamedElem(obj.getElements(), param);
    param.decIndent();
    param.wr("}");
    param.wr("  ; sorry for wasting your memory"); // TODO implement unions as they are supposed to be
    param.nl();
    return null;
  }

  private void wrNamedElem(List<NamedElement> list, StreamWriter param) {
    for (int i = 0; i < list.size(); i++) {
      NamedElement elem = list.get(i);
      visit(elem.getType(), param);
      if (i == list.size() - 1) {
        param.wr(" ");
      } else {
        param.wr(",");
      }
      param.wr(" ; ");
      param.wr(elem.getName());
      param.nl();
    }
  }

  // ---- expression --------------------------------------------------------------------
  @Override
  protected Void visitNumber(Number obj, StreamWriter param) {
    param.wr(obj.getValue().toString());
    return null;
  }

  @Override
  protected Void visitBoolValue(BoolValue obj, StreamWriter param) {
    if (obj.isValue()) {
      param.wr("True");
    } else {
      param.wr("False");
    }
    return null;
  }

  @Override
  protected Void visitVarRefStatevar(VarRefStatevar obj, StreamWriter param) {
    StateVariable variable = obj.getRef();
    param.wr("@");
    param.wr(variable.getName());
    wrId(variable, param);
    return null;
  }

  @Override
  protected Void visitVarRefConst(VarRefConst obj, StreamWriter param) {
    Constant constant = obj.getRef();
    param.wr("@");
    param.wr(constant.getName());
    wrId(constant, param);
    return null;
  }

  @Override
  protected Void visitVarRefSimple(VarRefSimple obj, StreamWriter param) {
    SsaVariable variable = obj.getRef();
    param.wr("%");
    param.wr(variable.getName());
    wrId(variable, param);
    return null;
  }

  @Override
  protected Void visitVarRef(VarRef obj, StreamWriter param) {
    // TODO not used?
    param.wr("%" + obj.getRef().getName());
    return null;
  }

  @Override
  protected Void visitTypeRef(TypeRef obj, StreamWriter param) {
    if (obj.getRef() instanceof NoSignType) {
      param.wr("i" + ((IntType) obj.getRef()).getBits());
    } else if (obj.getRef() instanceof VoidType) {
      param.wr("void");
    } else {
      param.wr("%" + obj.getRef().getName());
      wrId(obj.getRef(), param);
    }
    return null;
  }

  private void wrList(String sep, Iterable<? extends PirObject> lst, StreamWriter param) {
    boolean first = true;
    for (PirObject itr : lst) {
      if (first) {
        first = false;
      } else {
        param.wr(sep);
      }
      visit(itr, param);
    }
  }

  private void wrFuncRef(Function ref, StreamWriter param) {
    param.wr("@" + ref.getName());
  }

  @Deprecated
  private void wrTypeRef(Type type, StreamWriter param) {
    throw new RuntimeException("not yet implemented");
  }

  private void wrCall(Function func, List<PirValue> arg, StreamWriter param) {
    param.wr("call ");
    visit(func.getRetType(), param);
    param.wr(" ");
    wrFuncRef(func, param);
    param.wr("(");

    {
      boolean first = true;
      for (PirValue itr : arg) {
        if (first) {
          first = false;
        } else {
          param.wr(", ");
        }
        visit(itr.getType(), param);
        param.wr(" ");
        visit(itr, param);
      }
    }

    param.wr(")");
  }

  private void wrVarDef(VariableGeneratorStmt obj, StreamWriter param) {
    param.wr("%" + obj.getVariable().getName());
    wrId(obj.getVariable(), param);
    param.wr(" = ");
  }

  @Override
  protected Void visitEqual(Equal obj, StreamWriter param) {
    wrVarDef(obj, param);
    param.wr("icmp eq ");

    TypeRef lt = obj.getLeft().getType();
    TypeRef rt = obj.getRight().getType();

    // assert (lt.getRef() == rt.getRef());

    visit(lt, param);
    param.wr(" ");

    visit(obj.getLeft(), param);
    param.wr(", ");
    visit(obj.getRight(), param);
    param.nl();
    return null;
  }

  @Override
  protected Void visitNotequal(Notequal obj, StreamWriter param) {
    wrVarDef(obj, param);
    param.wr("icmp ne ");

    TypeRef lt = obj.getLeft().getType();
    TypeRef rt = obj.getRight().getType();

    // assert (lt.getRef() == rt.getRef());

    visit(lt, param);
    param.wr(" ");

    visit(obj.getLeft(), param);
    param.wr(", ");
    visit(obj.getRight(), param);
    param.nl();
    return null;
  }

  @Override
  protected Void visitGreater(Greater obj, StreamWriter param) {
    wrVarDef(obj, param);
    param.wr("icmp " + getSign(obj.getSignes()) + "gt ");

    TypeRef lt = obj.getLeft().getType();
    TypeRef rt = obj.getRight().getType();

    // assert (lt.getRef() == rt.getRef());

    visit(lt, param);
    param.wr(" ");

    visit(obj.getLeft(), param);
    param.wr(", ");
    visit(obj.getRight(), param);
    param.nl();
    return null;
  }

  @Override
  protected Void visitGreaterequal(Greaterequal obj, StreamWriter param) {
    wrVarDef(obj, param);
    param.wr("icmp " + getSign(obj.getSignes()) + "ge ");

    TypeRef lt = obj.getLeft().getType();
    TypeRef rt = obj.getRight().getType();

    // assert (lt.getRef() == rt.getRef());

    visit(lt, param);
    param.wr(" ");

    visit(obj.getLeft(), param);
    param.wr(", ");
    visit(obj.getRight(), param);
    param.nl();
    return null;
  }

  @Override
  protected Void visitLess(Less obj, StreamWriter param) {
    wrVarDef(obj, param);
    param.wr("icmp " + getSign(obj.getSignes()) + "lt ");

    TypeRef lt = obj.getLeft().getType();
    TypeRef rt = obj.getRight().getType();

    // assert (lt.getRef() == rt.getRef());

    visit(lt, param);
    param.wr(" ");

    visit(obj.getLeft(), param);
    param.wr(", ");
    visit(obj.getRight(), param);
    param.nl();
    return null;
  }

  @Override
  protected Void visitLessequal(Lessequal obj, StreamWriter param) {
    wrVarDef(obj, param);
    param.wr("icmp " + getSign(obj.getSignes()) + "le ");

    TypeRef lt = obj.getLeft().getType();
    TypeRef rt = obj.getRight().getType();

    // assert (lt.getRef() == rt.getRef());

    visit(lt, param);
    param.wr(" ");

    visit(obj.getLeft(), param);
    param.wr(", ");
    visit(obj.getRight(), param);
    param.nl();
    return null;
  }

  private String getSign(StmtSignes signes) {
    String sign;
    switch (signes) {
    case unknown:
      return "";
    case signed:
      return "s";
    case unsigned:
      return "u";
    default:
      RError.err(ErrorType.Fatal, "Unknown signes value: " + signes);
      return null;
    }
  }

  @Override
  protected Void visitCallAssignment(CallAssignment obj, StreamWriter param) {
    wrVarDef(obj, param);
    wrCall(obj.getRef(), obj.getParameter(), param);
    param.nl();
    return null;
  }

  @Override
  protected Void visitGetElementPtr(GetElementPtr obj, StreamWriter param) {
    wrVarDef(obj, param);
    param.wr("getelementptr ");
    visit(obj.getBase().getType(), param);
    param.wr(" ");
    visit(obj.getBase(), param);
    for (PirValue ofs : obj.getOffset()) {
      param.wr(", ");
      visit(ofs.getType(), param);
      param.wr(" ");
      visit(ofs, param);
    }
    param.nl();
    return null;
  }

  @Override
  protected Void visitNot(Not obj, StreamWriter param) {
    wrVarDef(obj, param);
    param.wr("xor ");
    visit(obj.getVariable().getType(), param);
    param.wr(" ");
    visit(obj.getExpr(), param);
    param.wr(", -1");
    param.nl();
    return null;
  }

  @Override
  protected Void visitUminus(Uminus obj, StreamWriter param) {
    wrVarDef(obj, param);
    param.wr("sub ");
    visit(obj.getVariable().getType(), param);
    param.wr(" 0, ");
    visit(obj.getExpr(), param);
    param.nl();
    return null;
  }

  private void emitAnd(BinaryOp obj, StreamWriter param) {
    wrVarDef(obj, param);
    param.wr("and ");
    visit(obj.getVariable().getType(), param);
    param.wr(" ");
    visit(obj.getLeft(), param);
    param.wr(", ");
    visit(obj.getRight(), param);
    param.nl();
  }

  @Override
  protected Void visitAnd(BitAnd obj, StreamWriter param) {
    emitAnd(obj, param);
    return null;
  }

  @Override
  protected Void visitLogicAnd(LogicAnd obj, StreamWriter param) {
    emitAnd(obj, param);
    return null;
  }

  @Override
  protected Void visitDiv(Div obj, StreamWriter param) {
    // FIXME does only work if left and right is unsigned
    wrVarDef(obj, param);
    param.wr("udiv ");
    visit(obj.getVariable().getType(), param);
    param.wr(" ");
    visit(obj.getLeft(), param);
    param.wr(", ");
    visit(obj.getRight(), param);
    param.nl();
    return null;
  }

  @Override
  protected Void visitMinus(Minus obj, StreamWriter param) {
    wrVarDef(obj, param);
    param.wr("sub nuw nsw ");
    visit(obj.getVariable().getType(), param);
    param.wr(" ");
    visit(obj.getLeft(), param);
    param.wr(", ");
    visit(obj.getRight(), param);
    param.nl();
    return null;
  }

  @Override
  protected Void visitMod(Mod obj, StreamWriter param) {
    // FIXME does only work if left is unsigned (and right too, of course)
    wrVarDef(obj, param);
    param.wr("urem ");
    visit(obj.getVariable().getType(), param);
    param.wr(" ");
    visit(obj.getLeft(), param);
    param.wr(", ");
    visit(obj.getRight(), param);
    param.nl();
    return null;
  }

  @Override
  protected Void visitMul(Mul obj, StreamWriter param) {
    wrVarDef(obj, param);
    param.wr("mul nuw nsw ");
    visit(obj.getVariable().getType(), param);
    param.wr(" ");
    visit(obj.getLeft(), param);
    param.wr(", ");
    visit(obj.getRight(), param);
    param.nl();
    return null;
  }

  @Override
  protected Void visitPlus(Plus obj, StreamWriter param) {
    wrVarDef(obj, param);
    param.wr("add nuw nsw ");
    visit(obj.getVariable().getType(), param);
    param.wr(" ");
    visit(obj.getLeft(), param);
    param.wr(", ");
    visit(obj.getRight(), param);
    param.nl();
    return null;
  }

  @Override
  protected Void visitShl(Shl obj, StreamWriter param) {
    wrVarDef(obj, param);
    param.wr("shl nuw nsw ");
    visit(obj.getVariable().getType(), param);
    param.wr(" ");
    visit(obj.getLeft(), param);
    param.wr(", ");
    visit(obj.getRight(), param);
    param.nl();
    return null;
  }

  @Override
  protected Void visitShr(Shr obj, StreamWriter param) {
    wrVarDef(obj, param);
    param.wr("shr nuw nsw ");
    visit(obj.getVariable().getType(), param);
    param.wr(" ");
    visit(obj.getLeft(), param);
    param.wr(", ");
    visit(obj.getRight(), param);
    param.nl();
    return null;
  }

  @Override
  protected Void visitLogicOr(LogicOr obj, StreamWriter param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Void visitLogicXand(LogicXand obj, StreamWriter param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Void visitLogicXor(LogicXor obj, StreamWriter param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Void visitRefIndex(RefIndex obj, StreamWriter param) {
    param.wr("[");
    visit(obj.getIndex(), param);
    param.wr("]");
    return null;
  }

  @Override
  protected Void visitRefName(RefName obj, StreamWriter param) {
    param.wr(".");
    param.wr(obj.getName());
    return null;
  }

  // ---- statement --------------------------------------------------------------------
  @Override
  protected Void visitCallStmt(CallStmt obj, StreamWriter param) {
    wrCall(obj.getRef(), obj.getParameter(), param);
    param.nl();
    return null;
  }

  @Override
  protected Void visitAssignment(Assignment obj, StreamWriter param) {
    wrVarDef(obj, param);
    param.wr("copy ");
    visit(obj.getVariable().getType(), param);
    param.wr(" ");
    visit(obj.getSrc(), param);
    param.nl();
    return null;
  }

  @Override
  protected Void visitLoadStmt(LoadStmt obj, StreamWriter param) {
    wrVarDef(obj, param);
    param.wr("load ");
    visit(obj.getSrc().getType(), param);
    param.wr(" ");
    visit(obj.getSrc(), param);
    param.nl();
    return null;
  }

  @Override
  protected Void visitStoreStmt(StoreStmt obj, StreamWriter param) {
    param.wr("store ");
    visit(obj.getSrc().getType(), param);
    param.wr(" ");
    visit(obj.getSrc(), param);
    param.wr(", ");
    visit(obj.getDst().getType(), param);
    param.wr(" ");
    visit(obj.getDst(), param);
    param.nl();
    return null;
  }

  @Override
  protected Void visitStackMemoryAlloc(StackMemoryAlloc obj, StreamWriter param) {
    wrVarDef(obj, param);
    param.wr("alloca ");
    visit(new TypeRef(obj.getType()), param);
    param.nl();
    return null;
  }

  @Override
  protected Void visitUnreachable(Unreachable obj, StreamWriter param) {
    param.wr("unreachable");
    param.nl();
    return null;
  }

  @Override
  protected Void visitReturnVoid(ReturnVoid obj, StreamWriter param) {
    param.wr("ret void");
    param.nl();
    return null;
  }

  @Override
  protected Void visitReturnExpr(ReturnExpr obj, StreamWriter param) {
    param.wr("ret ");
    visit(obj.getValue().getType(), param);
    param.wr(" ");
    visit(obj.getValue(), param);
    param.nl();
    return null;
  }

  // ------------------------------------------------------------------------
  @Override
  protected Void visitStringValue(StringValue obj, StreamWriter param) {
    param.wr("'");
    param.wr(obj.getValue());
    param.wr("'");
    return null;
  }

  @Override
  protected Void visitStringType(StringType obj, StreamWriter param) {
    param.wr("String");
    return null;
  }

  @Override
  protected Void visitArrayValue(ArrayValue obj, StreamWriter param) {
    param.wr("[");
    visitSepList(",", obj.getValue(), param);
    param.wr("]");
    return null;
  }

  @Override
  protected Void visitCaseGoto(CaseGoto obj, StreamWriter param) {
    param.wr("switch ");
    visit(obj.getCondition().getType(), param);
    param.wr(" ");
    visit(obj.getCondition(), param);
    param.wr(", label ");
    param.wr("%" + obj.getOtherwise().getName());
    param.wr(" [ ");
    visitList(obj.getOption(), param);
    param.wr("]");
    param.nl();

    return null;
  }

  @Override
  protected Void visitCaseGotoOpt(CaseGotoOpt obj, StreamWriter param) {
    for (BigInteger val : obj.getValue()) {
      param.wr(val.toString());
      param.wr(", label ");
      param.wr("%" + obj.getDst().getName());
      param.wr(" ");
    }
    return null;
  }

  @Override
  protected Void visitIfGoto(IfGoto obj, StreamWriter param) {
    param.wr("br ");
    visit(obj.getCondition().getType(), param);
    param.wr(" ");
    visit(obj.getCondition(), param);

    param.wr(", label ");
    param.wr("%" + obj.getThenBlock().getName());

    param.wr(", label ");
    param.wr("%" + obj.getElseBlock().getName());

    param.nl();

    return null;
  }

  @Override
  protected Void visitPhiStmt(PhiStmt obj, StreamWriter param) {
    wrVarDef(obj, param);
    param.wr("phi ");
    visit(obj.getVariable().getType(), param);
    param.wr(" ");
    boolean first = true;
    for (BasicBlock in : obj.getInBB()) {
      if (first) {
        first = false;
      } else {
        param.wr(", ");
      }
      param.wr("[");
      visit(obj.getArg(in), param);
      param.wr(", ");
      param.wr("%" + in.getName());
      param.wr("]");
    }
    param.nl();
    return null;
  }

  @Override
  protected Void visitGoto(Goto obj, StreamWriter param) {
    param.wr("br label ");
    param.wr("%" + obj.getTarget().getName());
    param.nl();
    return null;
  }

  @Override
  protected Void visitBasicBlock(BasicBlock obj, StreamWriter param) {
    param.wr(obj.getName() + ":");
    param.nl();
    param.incIndent();
    param.wr("; phi");
    param.nl();
    visitList(obj.getPhi(), param);
    param.wr("; code");
    param.nl();
    visitList(obj.getCode(), param);
    param.wr("; end");
    param.nl();
    visit(obj.getEnd(), param);
    param.decIndent();
    param.nl();
    return null;
  }

  @Override
  protected Void visitBasicBlockList(BasicBlockList obj, StreamWriter param) {
    param.incIndent();
    visit(obj.getEntry(), param);
    visit(obj.getExit(), param);

    LinkedList<BasicBlock> bbs = new LinkedList<BasicBlock>(obj.getBasicBlocks());
    Collections.sort(bbs, new Comparator<BasicBlock>() {

      @Override
      public int compare(BasicBlock o1, BasicBlock o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
    visitList(bbs, param);
    param.decIndent();
    return null;
  }

  @Override
  protected Void visitRangeType(RangeType obj, StreamWriter param) {
    param.wr(obj.getName()); // TODO remove, should not exist anymore
    wrId(obj, param);
    param.wr(" := ");
    param.wr(obj.getLow().toString());
    param.wr("..");
    param.wr(obj.getHigh().toString());
    param.nl();
    return null;
  }

  @Override
  protected Void visitTruncValue(TruncValue obj, StreamWriter param) {
    wrConvertValue(obj, "trunc", param);
    return null;
  }

  @Override
  protected Void visitSignExtendValue(SignExtendValue obj, StreamWriter param) {
    wrConvertValue(obj, "sext", param);
    return null;
  }

  @Override
  protected Void visitZeroExtendValue(ZeroExtendValue obj, StreamWriter param) {
    wrConvertValue(obj, "zext", param);
    return null;
  }

  @Override
  protected Void visitTypeCast(TypeCast obj, StreamWriter param) {
    wrConvertValue(obj, "typecast", param);
    return null;
  }

  private void wrConvertValue(ConvertValue obj, String op, StreamWriter param) {
    wrVarDef(obj, param);
    param.wr(op);
    param.wr(" ");
    visit(obj.getOriginal().getType(), param);
    param.wr(" ");
    visit(obj.getOriginal(), param);
    param.wr(" to ");
    visit(obj.getVariable().getType(), param);
    param.nl();
  }
}
