package pir.traverser;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import pir.NullTraverser;
import pir.Pir;
import pir.PirObject;
import pir.cfg.BasicBlock;
import pir.cfg.BasicBlockList;
import pir.cfg.Goto;
import pir.cfg.IfGoto;
import pir.cfg.PhiStmt;
import pir.cfg.ReturnExpr;
import pir.cfg.ReturnVoid;
import pir.expression.ArrayValue;
import pir.expression.BoolValue;
import pir.expression.Number;
import pir.expression.PExpression;
import pir.expression.StringValue;
import pir.expression.UnaryExpr;
import pir.expression.reference.RefIndex;
import pir.expression.reference.RefName;
import pir.expression.reference.VarRef;
import pir.expression.reference.VarRefSimple;
import pir.function.FuncWithBody;
import pir.function.Function;
import pir.other.Constant;
import pir.other.FuncVariable;
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
import pir.statement.StoreStmt;
import pir.statement.VarDefStmt;
import pir.statement.VariableGeneratorStmt;
import pir.statement.convert.ConvertValue;
import pir.statement.convert.SignExtendValue;
import pir.statement.convert.TruncValue;
import pir.statement.convert.ZeroExtendValue;
import pir.type.Array;
import pir.type.BooleanType;
import pir.type.EnumElement;
import pir.type.EnumType;
import pir.type.NamedElement;
import pir.type.RangeType;
import pir.type.SignedType;
import pir.type.StringType;
import pir.type.StructType;
import pir.type.Type;
import pir.type.TypeAlias;
import pir.type.UnionType;
import pir.type.UnsignedType;
import pir.type.VoidType;

import common.FuncAttr;

import error.ErrorType;
import error.RError;
import evl.doc.StreamWriter;
import evl.expression.RelOp;

public class LlvmWriter extends NullTraverser<Void, StreamWriter> {

  static public void print(PirObject obj, String filename) {
    LlvmWriter printer = new LlvmWriter();
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
    // wr.wr("[" + obj.hashCode() % 1000 + "]"); // TODO remove debug code
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
    wrTypeRef(obj.getRetType(), param);

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
    wrTypeRef(obj.getType(), param);
    param.wr(" ");
    param.wr("%" + obj.getName());
    return null;
  }

  @Override
  protected Void visitSsaVariable(SsaVariable obj, StreamWriter param) {
    wrTypeRef(obj.getType(), param);
    param.wr(" ");
    param.wr("%" + obj.getName());
    return null;
  }

  @Override
  protected Void visitConstant(Constant obj, StreamWriter param) {
    param.wr(obj.getName());
    param.wr(": ");
    param.wr(obj.getType().getName());
    wrId(obj.getType(), param);
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
    wrTypeRef(obj.getType(), param);
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
  protected Void visitUnsignedType(UnsignedType obj, StreamWriter param) {
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(" = ");
    param.wr("U{");
    param.wr(Integer.toString(obj.getBits()));
    param.wr("};");
    param.nl();
    return null;
  }

  @Override
  protected Void visitArray(Array obj, StreamWriter param) {
    param.wr("%" + obj.getName());
    wrId(obj, param);
    param.wr(" = type [");
    param.wr(Integer.toString(obj.getSize()));
    param.wr(" x ");
    wrTypeRef(obj.getType(), param);
    param.wr("]");
    param.nl();
    return null;
  }

  @Override
  protected Void visitVoidType(VoidType obj, StreamWriter param) {
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(";");
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
    param.wr(obj.getType().getName());
    wrId(obj.getType(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitTypeAlias(TypeAlias obj, StreamWriter param) {
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(": ");
    param.wr(obj.getRef().getName());
    param.wr(";");
    param.nl();
    return null;
  }

  // ---- expression --------------------------------------------------------------------

  @Override
  protected Void visitCallExpr(CallAssignment obj, StreamWriter param) {
    wrVarDef(obj, param);
    param.wr("call ");
    wrTypeRef(obj.getRef().getRetType(), param);
    param.wr(" ");
    wrFuncRef(obj.getRef(), param);
    param.wr("(");

    {
      boolean first = true;
      for (PExpression itr : obj.getParameter()) {
        if (first) {
          first = false;
        } else {
          param.wr(", ");
        }
        Type type = getType(itr);
        wrTypeRef(type, param);
        param.wr(" ");
        visit(itr, param);
      }
    }

    param.wr(")");
    return null;
  }

  @Override
  protected Void visitNumber(Number obj, StreamWriter param) {
    param.wr(Integer.toString(obj.getValue()));
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
    wrVarRef(obj.getRef(), param);
    return null;
  }

  @Override
  protected Void visitVarRef(VarRef obj, StreamWriter param) {
    // TODO not used?
    param.wr("%" + obj.getRef().getName());
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

  private void wrTypeRef(Type type, StreamWriter param) {
    if (type instanceof SignedType) {
      param.wr("i" + ((SignedType) type).getBits());
    } else if (type instanceof VoidType) {
      param.wr("void");
    } else {
      param.wr("%" + type.getName());
    }
  }

  private void wrCall(Function func, List<PExpression> arg, StreamWriter param) {
    param.wr("call ");
    wrTypeRef(func.getRetType(), param);
    param.wr(" ");
    wrFuncRef(func, param);
    param.wr("(");

    {
      boolean first = true;
      for (PExpression itr : arg) {
        if (first) {
          first = false;
        } else {
          param.wr(", ");
        }
        Type type = getType(itr);
        wrTypeRef(type, param);
        param.wr(" ");
        visit(itr, param);
      }
    }

    param.wr(")");
  }

  private void wrVarRef(Variable variable, StreamWriter param) {
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
  }

  @Deprecated
  private void wrVarRef(PExpression expr, StreamWriter param) {
    if (expr instanceof VarRef) {
      Variable target = ((VarRef) expr).getRef();
      // TODO offset?
      wrVarRef(target, param);
    } else if (expr instanceof VarRefSimple) {
      SsaVariable target = ((VarRefSimple) expr).getRef();
      wrVarRef(target, param);
    } else if (expr instanceof Number) {
      param.wr(expr.toString());
    } else {
      RError.err(ErrorType.Fatal, "Unhandled class: " + expr.getClass().getCanonicalName());
    }
  }

  private void wrVarDef(VariableGeneratorStmt obj, StreamWriter param) {
    param.wr("%" + obj.getVariable().getName());
    param.wr(" = ");
  }

  @Override
  protected Void visitRelation(Relation obj, StreamWriter param) {
    wrVarDef(obj, param);
    param.wr("icmp ");
    printUnsignedRelop(obj.getOp(), param);// FIXME check if type is unsigned
    param.wr(" ");

    Type lt = getType(obj.getLeft());
    Type rt = getType(obj.getRight());
    // assert (lt == rt); //TODO reimplement

    wrTypeRef(lt, param);
    param.wr(" ");

    wrVarRef(obj.getLeft(), param);
    param.wr(", ");
    wrVarRef(obj.getRight(), param);
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
    wrTypeRef(getType(obj.getBase()), param);
    param.wr("*"); // FIXME ok?
    param.wr(" ");
    wrVarRef(obj.getBase(), param);
    for (PExpression ofs : obj.getOffset()) {
      param.wr(", ");
      wrTypeRef(getType(ofs), param);
      param.wr(" ");
      visit(ofs, param);
    }
    param.nl();
    return null;
  }

  @Override
  protected Void visitArithmeticOp(ArithmeticOp obj, StreamWriter param) {
    wrVarDef(obj, param);
    wrArop(obj.getOp(), param);
    param.wr(" ");

    Type lt = getType(obj.getLeft());
    Type rt = getType(obj.getRight());
    // assert (lt == rt); //TODO reimplement

    wrTypeRef(lt, param);

    param.wr(" ");

    wrVarRef(obj.getLeft(), param);
    param.wr(", ");
    wrVarRef(obj.getRight(), param);
    param.nl();
    return null;
  }

  private void wrArop(ArOp op, StreamWriter param) {
    String code;
    switch (op) {
    case MINUS:
      code = "sub nsw nuw"; // TODO make nsw nuw depended on signes
      break;
    case PLUS:
      code = "add nsw nuw";// TODO make nsw nuw depended on signes
      break;
    case AND:
      code = "and";
      break;
    default:
      RError.err(ErrorType.Fatal, "Operand not handled: " + op);
      return;
    }
    param.wr(code);
  }

  @Override
  protected Void visitUnaryExpr(UnaryExpr obj, StreamWriter param) {
    throw new RuntimeException("not yet implemented");
    // param.wr("(");
    // param.wr(obj.getOp().toString());
    // param.wr(" ");
    // visit(obj.getExpr(), param);
    // param.wr(")");
    // return null;
  }

  private Type getType(Pir left) {
    return ExprTypeGetter.process(left, ExprTypeGetter.NUMBER_AS_INT); // FIXME change IR that this is no longer needed
  }

  private void printUnsignedRelop(RelOp op, StreamWriter param) {
    String code;
    switch (op) {
    case EQUAL:
      code = "eq";
      break;
    case NOT_EQUAL:
      code = "ne";
      break;
    case GREATER:
      code = "ugt";
      break;
    case GREATER_EQUEAL:
      code = "uge";
      break;
    case LESS:
      code = "ult";
      break;
    case LESS_EQUAL:
      code = "ule";
      break;
    default:
      RError.err(ErrorType.Fatal, "Operand not handled: " + op);
      return;
    }
    param.wr(code);
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
    param.wr("%" + obj.getVariable().getName());
    param.wr(" = add ");
    wrTypeRef(getType(obj.getSrc()), param);
    param.wr(" ");
    wrVarRef(obj.getSrc(), param);
    param.wr(", 0");
    param.nl();
    return null;
  }

  @Override
  protected Void visitLoadStmt(LoadStmt obj, StreamWriter param) {
    wrVarDef(obj, param);
    param.wr("load ");
    wrTypeRef(getType(obj.getSrc()), param);
    param.wr("* ");
    wrVarRef(obj.getSrc(), param);
    param.nl();
    return null;
  }

  @Override
  protected Void visitStoreStmt(StoreStmt obj, StreamWriter param) {
    Type type = getType(obj.getSrc());
    // assert( type == obj.getDst().getType() ); //FIXME add it again

    param.wr("store ");
    wrTypeRef(type, param);
    param.wr(" ");
    wrVarRef(obj.getSrc(), param);
    param.wr(", ");
    wrTypeRef(getType(obj.getDst()), param);
    param.wr("* ");
    wrVarRef(obj.getDst(), param);
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
    wrTypeRef(getType(obj.getValue()), param);
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
  protected Void visitIfGoto(IfGoto obj, StreamWriter param) {
    param.wr("br ");
    wrTypeRef(getType(obj.getCondition()), param);
    param.wr(" ");
    wrVarRef(obj.getCondition(), param);

    param.wr(", label ");
    param.wr("%" + obj.getThenBlock().getName());

    param.wr(", label ");
    param.wr("%" + obj.getElseBlock().getName());

    param.nl();

    return null;
  }

  @Override
  protected Void visitSignedType(SignedType obj, StreamWriter param) {
    // param.wr("i" + obj.getBits());
    return null;
  }

  @Override
  protected Void visitPhiStmt(PhiStmt obj, StreamWriter param) {
    param.wr("%" + obj.getVariable().getName());
    param.wr(" = phi ");
    param.wr("%" + obj.getVariable().getType().getName());
    param.wr(" ");
    boolean first = true;
    for (BasicBlock in : obj.getInBB()) {
      if (first) {
        first = false;
      } else {
        param.wr(", ");
      }
      param.wr("[");
      wrVarRef(obj.getArg(in), param);
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
    visit(obj.getEntry(),param);
    visit(obj.getExit(),param);
    
    LinkedList<BasicBlock> bbs = new LinkedList<BasicBlock>(obj.getBasicBlocks());
    Collections.sort(bbs, new Comparator<BasicBlock>(){
      @Override
      public int compare(BasicBlock o1, BasicBlock o2) {
        return o1.getName().compareTo(o2.getName());
      }});
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
    param.wr(obj.toString()); // TODO remove, should not exist anymore
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

  private void wrConvertValue(ConvertValue obj, String op, StreamWriter param) {
    wrVarDef(obj, param);
    param.wr(op);
    param.wr(" ");
    wrTypeRef(getType(obj.getOriginal()), param);
    param.wr(" ");
    visit(obj.getOriginal(), param);
    param.wr(" to ");
    wrTypeRef(obj.getVariable().getType(), param);
    param.nl();
  }

}
