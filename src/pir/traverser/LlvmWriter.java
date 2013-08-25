package pir.traverser;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import pir.NullTraverser;
import pir.PirObject;
import pir.cfg.BasicBlock;
import pir.cfg.BasicBlockList;
import pir.cfg.CaseGoto;
import pir.cfg.CaseGotoOpt;
import pir.cfg.CaseOptRange;
import pir.cfg.CaseOptValue;
import pir.cfg.Goto;
import pir.cfg.IfGoto;
import pir.cfg.PhiStmt;
import pir.cfg.ReturnExpr;
import pir.cfg.ReturnVoid;
import pir.expression.ArrayValue;
import pir.expression.BoolValue;
import pir.expression.Number;
import pir.expression.StringValue;
import pir.expression.reference.RefIndex;
import pir.expression.reference.RefName;
import pir.expression.reference.VarRef;
import pir.expression.reference.VarRefSimple;
import pir.function.FuncWithBody;
import pir.function.Function;
import pir.other.Constant;
import pir.other.FuncVariable;
import pir.other.PirValue;
import pir.other.Program;
import pir.other.SsaVariable;
import pir.other.StateVariable;
import pir.other.Variable;
import pir.statement.ArOp;
import pir.statement.ArithmeticOp;
import pir.statement.Assignment;
import pir.statement.CallAssignment;
import pir.statement.CallStmt;
import pir.statement.ComplexWriter;
import pir.statement.GetElementPtr;
import pir.statement.LoadStmt;
import pir.statement.Relation;
import pir.statement.StmtSignes;
import pir.statement.StoreStmt;
import pir.statement.UnaryOp;
import pir.statement.VarDefStmt;
import pir.statement.VariableGeneratorStmt;
import pir.statement.convert.ConvertValue;
import pir.statement.convert.SignExtendValue;
import pir.statement.convert.TruncValue;
import pir.statement.convert.TypeCast;
import pir.statement.convert.ZeroExtendValue;
import pir.type.ArrayType;
import pir.type.BooleanType;
import pir.type.EnumElement;
import pir.type.EnumType;
import pir.type.IntType;
import pir.type.NamedElement;
import pir.type.NoSignType;
import pir.type.RangeType;
import pir.type.SignedType;
import pir.type.StringType;
import pir.type.StructType;
import pir.type.Type;
import pir.type.TypeAlias;
import pir.type.TypeRef;
import pir.type.UnionType;
import pir.type.UnsignedType;
import pir.type.VoidType;

import common.FuncAttr;

import error.ErrorType;
import error.RError;
import evl.doc.StreamWriter;
import evl.expression.RelOp;

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
    visitList(obj.getConstant(), param);
    visitList(obj.getVariable(), param);
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
    return null;
  }

  @Override
  protected Void visitSsaVariable(SsaVariable obj, StreamWriter param) {
    visit(obj.getType(), param);
    param.wr(" ");
    param.wr("%" + obj.getName());
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
    param.wr("@" + obj.getName());
    param.wr(" = global ");
    visit(obj.getType(), param);
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
  protected Void visitEnumType(EnumType obj, StreamWriter param) {
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(" = ");
    param.wr("Enum");
    param.nl();
    param.incIndent();
    visitList(obj.getElements(), param);
    param.decIndent();
    param.wr("end");
    param.nl();
    return null;
  }

  @Override
  protected Void visitEnumElement(EnumElement obj, StreamWriter param) {
    param.wr(obj.getName());
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitStructType(StructType obj, StreamWriter param) {
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(" = ");
    param.wr("Record");
    param.nl();
    param.incIndent();
    visitList(obj.getElements(), param);
    param.decIndent();
    param.wr("end");
    param.nl();
    return null;
  }

  @Override
  protected Void visitUnionType(UnionType obj, StreamWriter param) {
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(" = ");
    param.wr("Union");
    param.nl();
    param.incIndent();
    visitList(obj.getElements(), param);
    param.decIndent();
    param.wr("end");
    param.nl();
    return null;
  }

  @Override
  protected Void visitNamedElement(NamedElement obj, StreamWriter param) {
    param.wr(obj.getName());
    param.wr(": ");
    visit(obj.getType(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitTypeAlias(TypeAlias obj, StreamWriter param) {
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(": ");
    visit(obj.getRef(), param);
    param.wr(";");
    param.nl();
    return null;
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
  protected Void visitVarRefSimple(VarRefSimple obj, StreamWriter param) {
    Variable variable = obj.getRef();
    if (variable instanceof StateVariable) {
      param.wr("@");
    } else if (variable instanceof SsaVariable) {
      param.wr("%");
    } else if (variable instanceof FuncVariable) { // FIXME remove?
      param.wr("%");
    } else {
      throw new RuntimeException("not yet implemented: " + variable.getClass().getCanonicalName());
    }
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

  private String getRelop(RelOp op, StmtSignes signes) {
    String sign;
    switch (signes) {
    case unknown:
      sign = "";
      break;
    case signed:
      sign = "s";
      break;
    case unsigned:
      sign = "u";
      break;
    default:
      RError.err(ErrorType.Fatal, "Unknown signes value: " + signes);
      return null;
    }
    switch (op) {
    case EQUAL:
      return "eq";
    case NOT_EQUAL:
      return "ne";
    case GREATER:
      return sign + "gt";
    case GREATER_EQUEAL:
      return sign + "ge";
    case LESS:
      return sign + "lt";
    case LESS_EQUAL:
      return sign + "le";
    default:
      RError.err(ErrorType.Fatal, "Operand not handled: " + op);
      return null;
    }
  }

  @Override
  protected Void visitRelation(Relation obj, StreamWriter param) {
    wrVarDef(obj, param);
    param.wr("icmp ");
    param.wr(getRelop(obj.getOp(), obj.getSignes()));
    param.wr(" ");

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
    param.wr("*"); // FIXME ok?
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
  protected Void visitUnaryOp(UnaryOp obj, StreamWriter param) {
    wrVarDef(obj, param);
    param.wr(obj.getOp().toString());
    param.wr(" ");
    visit(obj.getVariable().getType(), param);
    param.wr(" ");
    visit(obj.getExpr(), param);
    param.nl();
    return null;
  }

  @Override
  protected Void visitArithmeticOp(ArithmeticOp obj, StreamWriter param) {
    wrVarDef(obj, param);
    param.wr(getOpString(obj.getOp()));
    param.wr(" ");
    param.wr("nuw nsw "); // TODO ok to set them always?

    visit(obj.getVariable().getType(), param);

    param.wr(" ");

    visit(obj.getLeft(), param);
    param.wr(", ");
    visit(obj.getRight(), param);
    param.nl();
    return null;
  }

  private String getOpString(ArOp op) {
    switch (op) {
    case MINUS:
      return "sub";
    case PLUS:
      return "add";
    case AND:
      return "and";
    default:
      RError.err(ErrorType.Warning, "Operand not handled: " + op); // TODO change to fatal error
      return op.toString();
    }
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
    param.wr("* ");
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
    param.wr("* ");
    visit(obj.getDst(), param);
    param.nl();
    return null;
  }

  @Override
  protected Void visitVarDefStmt(VarDefStmt obj, StreamWriter param) {
    visit(obj.getVariable(), param);
    param.wr(";");
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
    wrList("|", obj.getValue(), param);
    param.wr(", label ");
    param.wr("%" + obj.getDst().getName());
    param.wr(" ");
    return null;
  }

  @Override
  protected Void visitCaseOptValue(CaseOptValue obj, StreamWriter param) {
    visit(obj.getValue().getType(), param);
    param.wr(" ");
    visit(obj.getValue(), param);
    return null;
  }

  @Override
  protected Void visitCaseOptRange(CaseOptRange obj, StreamWriter param) {
    param.wr(obj.getStart().toString());
    param.wr("..");
    param.wr(obj.getEnd().toString());
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
    visitList(obj.getPhi(), param);
    visitList(obj.getCode(), param);
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
  protected Void visitComplexWriter(ComplexWriter obj, StreamWriter param) {
    param.wr(obj.toString()); // TODO remove, should not exist anymore
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
