package pir.traverser;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.List;

import pir.NullTraverser;
import pir.Pir;
import pir.PirObject;
import pir.expression.ArrayValue;
import pir.expression.BoolValue;
import pir.expression.Number;
import pir.expression.StringValue;
import pir.expression.TypeCast;
import pir.expression.binop.BinaryExp;
import pir.expression.reference.RefCall;
import pir.expression.reference.RefIndex;
import pir.expression.reference.RefName;
import pir.expression.reference.Reference;
import pir.expression.unop.UnaryExp;
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
import pir.statement.VarDefStmt;
import pir.statement.WhileStmt;
import pir.type.ArrayType;
import pir.type.BooleanType;
import pir.type.NamedElement;
import pir.type.PointerType;
import pir.type.RangeType;
import pir.type.SignedType;
import pir.type.StringType;
import pir.type.StructType;
import pir.type.TypeRef;
import pir.type.UnionType;
import pir.type.UnsignedType;
import pir.type.VoidType;
import util.Range;
import evl.doc.StreamWriter;

public class PirPrinter extends NullTraverser<Void, StreamWriter> {

  static public void print(PirObject obj, String filename) {
    PirPrinter printer = new PirPrinter();
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

  private void wrId(Pir obj, StreamWriter wr) {
    wr.wr("[" + obj.hashCode() % 1000 + "]"); // TODO remove debug code
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
    visitOptList("type", obj.getType(), param);
    visitOptList("const", obj.getConstant(), param);
    visitOptList("var", obj.getVariable(), param);
    visitList(obj.getFunction(), param);
    return null;
  }

  @Override
  protected Void visitFunction(Function obj, StreamWriter param) {
    param.wr("function ");
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr("(");
    visitSepList(",", obj.getArgument(), param);
    param.wr(")");
    if (obj instanceof FuncWithRet) {
      param.wr(":");
      visit(((FuncWithRet) obj).getRetType(), param);
    }
    param.nl();
    if (obj instanceof FuncWithBody) {
      param.incIndent();
      visit(((FuncWithBody) obj).getBody(), param);
      param.decIndent();
      param.wr("end");
    }
    param.emptyLine();
    return null;
  }

  // ---- variables --------------------------------------------------------------------

  @Override
  protected Void visitFuncVariable(FuncVariable obj, StreamWriter param) {
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(": ");
    visit(obj.getType(), param);
    return null;
  }

  @Override
  protected Void visitConstant(Constant obj, StreamWriter param) {
    param.wr(obj.getName());
    param.wr(": ");
    visit(obj.getType(), param);
    wrId(obj.getType(), param);
    param.wr(" = ");
    visit(obj.getDef(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitStateVariable(StateVariable obj, StreamWriter param) {
    param.wr(obj.getName());
    param.wr(": ");
    visit(obj.getType(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  // ---- types --------------------------------------------------------------------

  @Override
  protected Void visitTypeRef(TypeRef obj, StreamWriter param) {
    param.wr(obj.getRef().getName());
    wrId(obj.getRef(), param);
    return null;
  }

  @Override
  protected Void visitPointerType(PointerType obj, StreamWriter param) {
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(" = ");
    visit(obj.getType(), param);
    param.wr("^;");
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitSignedType(SignedType obj, StreamWriter param) {
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitRangeType(RangeType obj, StreamWriter param) {
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(";");
    param.nl();
    return null;
  }

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
  protected Void visitArrayType(ArrayType obj, StreamWriter param) {
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(" = ");
    param.wr("Array{");
    param.wr(obj.getSize().toString());
    param.wr(",");
    visit(obj.getType(), param);
    param.wr("};");
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
    wrId(obj.getType(), param);
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
  protected Void visitBinaryExp(BinaryExp obj, StreamWriter param) {
    param.wr("(");
    visit(obj.getLeft(), param);
    param.wr(" ");
    param.wr(obj.getOpName());
    param.wr(" ");
    visit(obj.getLeft(), param);
    param.wr(")");
    return null;
  }

  @Override
  protected Void visitReference(Reference obj, StreamWriter param) {
    param.wr(obj.getRef().getName());
    wrId(obj.getRef(), param);
    visitList(obj.getOffset(), param);
    return null;
  }

  @Override
  protected Void visitRefCall(RefCall obj, StreamWriter param) {
    param.wr("(");
    visitSepList(",", obj.getParameter(), param);
    param.wr(")");
    return null;
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

  @Override
  protected Void visitUnaryExp(UnaryExp obj, StreamWriter param) {
    param.wr("(");
    param.wr(obj.getOpName());
    param.wr(" ");
    visit(obj.getExpr(), param);
    param.wr(")");
    return null;
  }

  @Override
  protected Void visitTypeCast(TypeCast obj, StreamWriter param) {
    visit(obj.getCast(), param);
    param.wr("(");
    visit(obj.getValue(), param);
    param.wr(")");
    return null;
  }

  // ---- statement --------------------------------------------------------------------

  @Override
  protected Void visitBlock(Block obj, StreamWriter param) {
    visitList(obj.getStatement(), param);
    return null;
  }

  @Override
  protected Void visitCallStmt(CallStmt obj, StreamWriter param) {
    visit(obj.getRef(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitIfStmt(IfStmt obj, StreamWriter param) {
    param.wr("if ");
    visit(obj.getCondition(), param);
    param.wr(" then");
    param.nl();
    param.incIndent();
    visit(obj.getThenBlock(), param);
    param.decIndent();
    param.wr("else");
    param.nl();
    param.incIndent();
    visit(obj.getElseBlock(), param);
    param.decIndent();
    param.wr("end");
    param.nl();
    return null;
  }

  @Override
  protected Void visitCaseStmt(CaseStmt obj, StreamWriter param) {
    param.wr("case ");
    visit(obj.getCondition(), param);
    param.wr(" of");
    param.nl();
    param.incIndent();

    visitList(obj.getEntries(), param);

    param.wr("else");
    param.nl();
    param.incIndent();
    visit(obj.getOtherwise(), param);
    param.decIndent();
    param.wr("end");
    param.nl();

    param.decIndent();
    param.wr("end");
    param.nl();
    return null;
  }

  @Override
  protected Void visitCaseEntry(CaseEntry obj, StreamWriter param) {
    boolean first = true;
    for (Range itr : obj.getValues()) {
      if (first) {
        first = false;
      } else {
        param.wr(", ");
      }
      param.wr(itr.getLow().toString());
      param.wr("..");
      param.wr(itr.getHigh().toString());
    }
    param.wr(":");
    param.nl();
    param.incIndent();
    visit(obj.getCode(), param);
    param.decIndent();
    param.wr("end");
    param.nl();
    return null;
  }

  @Override
  protected Void visitAssignment(Assignment obj, StreamWriter param) {
    visit(obj.getDst(), param);
    param.wr(" := ");
    visit(obj.getSrc(), param);
    param.wr(";");
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
    param.wr("return;");
    param.nl();
    return null;
  }

  @Override
  protected Void visitReturnExpr(ReturnExpr obj, StreamWriter param) {
    param.wr("return ");
    visit(obj.getValue(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitWhileStmt(WhileStmt obj, StreamWriter param) {
    param.wr("while ");
    visit(obj.getCondition(), param);
    param.wr(" do");
    param.nl();
    param.incIndent();
    visit(obj.getBlock(), param);
    param.decIndent();
    param.wr("end");
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

}
