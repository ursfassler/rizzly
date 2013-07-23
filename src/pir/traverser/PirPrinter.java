package pir.traverser;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.List;

import pir.PirObject;
import pir.Traverser;
import pir.expression.ArithmeticOp;
import pir.expression.ArrayValue;
import pir.expression.BoolValue;
import pir.expression.Number;
import pir.expression.Reference;
import pir.expression.Relation;
import pir.expression.StringValue;
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
import pir.other.Constant;
import pir.other.FuncVariable;
import pir.other.Program;
import pir.other.StateVariable;
import pir.statement.Assignment;
import pir.statement.Block;
import pir.statement.CallStmt;
import pir.statement.CaseEntry;
import pir.statement.CaseOptRange;
import pir.statement.CaseOptValue;
import pir.statement.CaseStmt;
import pir.statement.IfStmt;
import pir.statement.IfStmtEntry;
import pir.statement.ReturnValue;
import pir.statement.ReturnVoid;
import pir.statement.VarDefStmt;
import pir.statement.WhileStmt;
import pir.type.Array;
import pir.type.BooleanType;
import pir.type.EnumElement;
import pir.type.EnumType;
import pir.type.NamedElement;
import pir.type.StringType;
import pir.type.StructType;
import pir.type.TypeAlias;
import pir.type.UnionType;
import pir.type.UnsignedType;
import pir.type.VoidType;
import evl.doc.StreamWriter;

public class PirPrinter extends Traverser<Void, StreamWriter> {

  static public void print(PirObject obj, String filename) {
    PirPrinter printer = new PirPrinter();
    try {
      printer.traverse(obj, new StreamWriter(new PrintStream(filename)));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  private void wrId(PirObject obj, StreamWriter wr) {
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
    param.wr("(");
    visitSepList(",", obj.getArgument(), param);
    param.wr(")");
    if (obj instanceof FuncWithRet) {
      param.wr(":");
      param.wr(((FuncWithRet) obj).getRetType().getName());
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
    param.wr(obj.getType().getName());
    wrId(obj.getType(), param);
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
    param.wr(obj.getName());
    param.wr(": ");
    param.wr(obj.getType().getName());
    wrId(obj.getType(), param);
    param.wr(";");
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
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(" = ");
    param.wr("Array{");
    param.wr(Integer.toString(obj.getSize()));
    param.wr(",");
    param.wr(obj.getType().getName());
    wrId(obj.getType(), param);
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
  protected Void visitReference(Reference obj, StreamWriter param) {
    visit(obj.getRef(), param);
    return null;
  }

  @Override
  protected Void visitRefCall(RefCall obj, StreamWriter param) {
    visit(obj.getPrevious(), param);
    param.wr("(");
    visitSepList(",", obj.getParameter(), param);
    param.wr(")");
    return null;
  }

  @Override
  protected Void visitRefHead(RefHead obj, StreamWriter param) {
    param.wr(obj.getRef().getName());
    wrId((PirObject) obj.getRef(), param);
    return null;
  }

  @Override
  protected Void visitArithmeticOp(ArithmeticOp obj, StreamWriter param) {
    param.wr("(");
    visit(obj.getLeft(), param);
    param.wr(" ");
    param.wr(obj.getOp().toString());
    param.wr(" ");
    visit(obj.getRight(), param);
    param.wr(")");
    return null;
  }

  @Override
  protected Void visitUnaryExpr(UnaryExpr obj, StreamWriter param) {
    param.wr("(");
    param.wr(obj.getOp().toString());
    param.wr(" ");
    visit(obj.getExpr(), param);
    param.wr(")");
    return null;
  }

  @Override
  protected Void visitRelation(Relation obj, StreamWriter param) {
    param.wr("(");
    visit(obj.getLeft(), param);
    param.wr(obj.getOp().toString());
    visit(obj.getRight(), param);
    param.wr(")");
    return null;
  }

  @Override
  protected Void visitRefIndex(RefIndex obj, StreamWriter param) {
    visit(obj.getPrevious(), param);
    param.wr("[");
    visit(obj.getIndex(), param);
    param.wr("]");
    return null;
  }

  @Override
  protected Void visitRefName(RefName obj, StreamWriter param) {
    visit(obj.getPrevious(), param);
    param.wr(".");
    param.wr(obj.getName());
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
    assert (!obj.getOption().isEmpty());
    boolean first = true;
    for (IfStmtEntry opt : obj.getOption()) {
      if (first) {
        first = false;
        param.wr("if");
      } else {
        param.wr("ef");
      }
      param.wr(" ");
      visit(opt.getCondition(), param);
      param.wr(" then");
      param.nl();
      param.incIndent();
      visit(opt.getCode(), param);
      param.decIndent();
    }

    param.wr("else");
    param.nl();
    param.incIndent();
    visit(obj.getDef(), param);
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
    visitSepList(",", obj.getValues(), param);
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
  protected Void visitCaseOptValue(CaseOptValue obj, StreamWriter param) {
    visit(obj.getValue(), param);
    return null;
  }

  @Override
  protected Void visitCaseOptRange(CaseOptRange obj, StreamWriter param) {
    visit(obj.getStart(), param);
    param.wr("..");
    visit(obj.getEnd(), param);
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
  protected Void visitReturnValue(ReturnValue obj, StreamWriter param) {
    param.wr("return ");
    visit(obj.getValue(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitWhile(WhileStmt obj, StreamWriter param) {
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
  protected Void visitIfStmtEntry(IfStmtEntry obj, StreamWriter param) {
    throw new RuntimeException("not yet implemented");
  }

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
  protected Void visitFuncImplVoid(FuncImplVoid obj, StreamWriter param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Void visitFuncImplRet(FuncImplRet obj, StreamWriter param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Void visitFuncProtoVoid(FuncProtoVoid obj, StreamWriter param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Void visitFuncProtoRet(FuncProtoRet obj, StreamWriter param) {
    throw new RuntimeException("not yet implemented");
  }

}
