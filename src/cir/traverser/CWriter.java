package cir.traverser;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.math.BigInteger;

import util.Range;
import util.StreamWriter;
import cir.Cir;
import cir.CirBase;
import cir.NullTraverser;
import cir.expression.ArrayValue;
import cir.expression.BinaryOp;
import cir.expression.BoolValue;
import cir.expression.ElementValue;
import cir.expression.NoValue;
import cir.expression.Number;
import cir.expression.StringValue;
import cir.expression.StructValue;
import cir.expression.TypeCast;
import cir.expression.UnaryOp;
import cir.expression.UnionValue;
import cir.expression.UnsafeUnionValue;
import cir.expression.reference.RefCall;
import cir.expression.reference.RefIndex;
import cir.expression.reference.RefName;
import cir.expression.reference.Reference;
import cir.function.Function;
import cir.function.FunctionPrivate;
import cir.function.FunctionPrototype;
import cir.function.FunctionPublic;
import cir.other.Named;
import cir.other.Program;
import cir.statement.Assignment;
import cir.statement.Block;
import cir.statement.CallStmt;
import cir.statement.CaseEntry;
import cir.statement.CaseStmt;
import cir.statement.IfStmt;
import cir.statement.ReturnExpr;
import cir.statement.ReturnVoid;
import cir.statement.VarDefStmt;
import cir.statement.WhileStmt;
import cir.type.ArrayType;
import cir.type.BooleanType;
import cir.type.EnumElement;
import cir.type.EnumType;
import cir.type.NamedElement;
import cir.type.RangeType;
import cir.type.SIntType;
import cir.type.StringType;
import cir.type.StructType;
import cir.type.TypeAlias;
import cir.type.TypeRef;
import cir.type.UIntType;
import cir.type.UnionType;
import cir.type.UnsafeUnionType;
import cir.type.VoidType;
import cir.variable.Constant;
import cir.variable.FuncVariable;
import cir.variable.StateVariable;
import cir.variable.Variable;

public class CWriter extends NullTraverser<Void, Boolean> {
  public static final String ARRAY_DATA_NAME = "data";
  final private boolean printReferenceId;
  final private StreamWriter sw;

  public CWriter(StreamWriter sw, boolean printReferenceId) {
    super();
    this.sw = sw;
    this.printReferenceId = printReferenceId;
  }

  static public void print(Cir obj, String filename, boolean printReferenceId) {
    try {
      CWriter printer = new CWriter(new StreamWriter(new PrintStream(filename)), printReferenceId);
      printer.traverse(obj, null);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  @Deprecated
  private String name(Named obj) {
    return obj.getName();
  }

  @Override
  protected Void visitDefault(CirBase obj, Boolean param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitProgram(Program obj, Boolean param) {
    sw.wr("#include <stdint.h>");
    sw.nl();
    sw.wr("#include <stdbool.h>");
    sw.nl();
    sw.nl();

    visitList(obj.getType(), true);
    sw.nl();
    visitList(obj.getFunction(), false);
    sw.nl();
    visitList(obj.getVariable(), true);
    sw.nl();
    visitList(obj.getFunction(), true);
    sw.nl();

    return null;
  }

  @Override
  protected Void visitNumber(Number obj, Boolean param) {
    sw.wr(obj.getValue().toString());
    return null;
  }

  @Override
  protected Void visitNoValue(NoValue obj, Boolean param) {
    sw.wr("_");
    return null;
  }

  @Override
  protected Void visitBinaryOp(BinaryOp obj, Boolean param) {
    sw.wr("(");
    visit(obj.getLeft(), param);
    sw.wr(obj.getOp().toString());
    visit(obj.getRight(), param);
    sw.wr(")");
    return null;
  }

  @Override
  protected Void visitUnaryOp(UnaryOp obj, Boolean param) {
    sw.wr("(");
    sw.wr(obj.getOp().toString());
    visit(obj.getExpr(), param);
    sw.wr(")");
    return null;
  }

  @Override
  protected Void visitReference(Reference obj, Boolean param) {
    sw.wr(name(obj.getRef()));
    visitList(obj.getOffset(), param);
    return null;
  }

  @Override
  protected Void visitRefCall(RefCall obj, Boolean param) {
    sw.wr("(");
    for (int i = 0; i < obj.getParameter().size(); i++) {
      if (i > 0) {
        sw.wr(",");
      }
      visit(obj.getParameter().get(i), param);
    }
    sw.wr(")");
    return null;
  }

  @Override
  protected Void visitRefName(RefName obj, Boolean param) {
    sw.wr(".");
    sw.wr(obj.getName());
    return null;
  }

  @Override
  protected Void visitRefIndex(RefIndex obj, Boolean param) {
    sw.wr(".");
    sw.wr(ARRAY_DATA_NAME);
    sw.wr("[");
    visit(obj.getIndex(), param);
    sw.wr("]");
    return null;
  }

  protected void writeFuncHeader(Function obj) {
    visit(obj.getRetType(), true);
    sw.wr(" ");
    sw.wr(name(obj));
    sw.wr("(");
    if (obj.getArgument().isEmpty()) {
      sw.wr("void");
    } else {
      for (int i = 0; i < obj.getArgument().size(); i++) {
        if (i > 0) {
          sw.wr(",");
        }
        Variable var = obj.getArgument().get(i);
        visit(var, true);
      }
    }
    sw.wr(")");
  }

  @Override
  protected Void visitFunctionPrototype(FunctionPrototype obj, Boolean param) {
    if (!param) {
      sw.wr("extern ");
      writeFuncHeader(obj);
      sw.wr(";");
      sw.nl();
    }
    return null;
  }

  @Override
  protected Void visitFunctionPrivate(FunctionPrivate obj, Boolean param) {
    sw.wr("static ");
    writeFuncHeader(obj);
    if (param) {
      sw.nl();
      visit(obj.getBody(), param);
    } else {
      sw.wr(";");
    }
    sw.nl();
    return null;
  }

  @Override
  protected Void visitFunctionPublic(FunctionPublic obj, Boolean param) {
    writeFuncHeader(obj);
    if (param) {
      sw.nl();
      visit(obj.getBody(), param);
    } else {
      sw.wr(";");
    }
    sw.nl();
    return null;
  }

  @Override
  protected Void visitConstant(Constant obj, Boolean param) {
    sw.wr("static const ");
    visit(obj.getType(), param);
    sw.wr(" ");
    sw.wr(name(obj));
    sw.wr(" = ");
    visit(obj.getDef(), param);
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitStateVariable(StateVariable obj, Boolean param) {
    sw.wr("static ");
    visit(obj.getType(), param);
    sw.wr(" ");
    sw.wr(name(obj));
    sw.wr(" = ");
    visit(obj.getDef(), param);
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitFuncVariable(FuncVariable obj, Boolean param) {
    visit(obj.getType(), param);
    sw.wr(" ");
    sw.wr(name(obj));
    return null;
  }

  @Override
  protected Void visitCallStmt(CallStmt obj, Boolean param) {
    visit(obj.getRef(), param);
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitAssignment(Assignment obj, Boolean param) {
    visit(obj.getDst(), param);
    sw.wr(" = ");
    visit(obj.getSrc(), param);
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitBlock(Block obj, Boolean param) {
    sw.wr("{");
    sw.nl();
    sw.incIndent();
    visitList(obj.getStatement(), param);
    sw.decIndent();
    sw.wr("}");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitRangeType(RangeType obj, Boolean param) {
    sw.wr("\"" + obj.getName() + "\"");
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitSIntType(SIntType obj, Boolean param) {
    sw.wr("typedef ");
    sw.wr("int" + obj.getBytes() * 8 + "_t ");
    sw.wr(name(obj));
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitUIntType(UIntType obj, Boolean param) {
    sw.wr("typedef ");
    sw.wr("uint" + obj.getBytes() * 8 + "_t ");
    sw.wr(name(obj));
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitArrayType(ArrayType obj, Boolean param) {
    sw.wr("typedef struct {");
    sw.nl();
    sw.incIndent();
    visit(obj.getType(), param);
    sw.wr(" ");
    sw.wr(ARRAY_DATA_NAME);
    sw.wr("[");
    sw.wr(Integer.toString(obj.getSize()));
    sw.wr("]");
    sw.wr(";");
    sw.decIndent();
    sw.nl();
    sw.wr("} ");
    sw.wr(name(obj));
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitVoidType(VoidType obj, Boolean param) {
    sw.wr("typedef ");
    sw.wr("void ");
    sw.wr(name(obj));
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitBooleanType(BooleanType obj, Boolean param) {
    sw.wr("typedef ");
    sw.wr("bool");
    sw.wr(" ");
    sw.wr(name(obj));
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitStringType(StringType obj, Boolean param) {
    sw.wr("typedef ");
    sw.wr("char*");
    sw.wr(" ");
    sw.wr(name(obj));
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitTypeAlias(TypeAlias obj, Boolean param) {
    sw.wr("typedef ");
    sw.wr(name(obj.getRef()));
    sw.wr(" ");
    sw.wr(name(obj));
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitUnsafeUnionType(UnsafeUnionType obj, Boolean param) {
    sw.wr("typedef union {");
    sw.nl();
    sw.incIndent();
    visitList(obj.getElements(), param);
    sw.decIndent();
    sw.wr("} ");
    sw.wr(name(obj));
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitUnionType(UnionType obj, Boolean param) {
    sw.wr("typedef struct {");
    sw.nl();
    sw.incIndent();

    visit(obj.getTag(), param);

    sw.wr("union {");
    sw.nl();
    sw.incIndent();
    visitList(obj.getElements(), param);
    sw.decIndent();
    sw.wr("};");
    sw.nl();

    sw.decIndent();
    sw.wr("} ");
    sw.wr(name(obj));
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitStructType(StructType obj, Boolean param) {
    sw.wr("typedef struct {");
    sw.nl();
    sw.incIndent();
    visitList(obj.getElements(), param);
    sw.decIndent();
    sw.wr("} ");
    sw.wr(name(obj));
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitEnumType(EnumType obj, Boolean param) {
    sw.wr("typedef enum {");
    sw.nl();
    sw.incIndent();

    for (int i = 0; i < obj.getElements().size(); i++) {
      EnumElement elem = obj.getElements().get(i);
      sw.wr(elem.getName());
      sw.wr(" = ");
      sw.wr(Integer.toString(elem.getValue()));
      if (i + 1 < obj.getElements().size()) {
        sw.wr(",");
      }
      sw.nl();
    }

    sw.decIndent();
    sw.wr("} ");
    sw.wr(name(obj));
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitNamedElement(NamedElement obj, Boolean param) {
    visit(obj.getType(), param);
    sw.wr(" ");
    sw.wr(obj.getName());
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitReturnVoid(ReturnVoid obj, Boolean param) {
    sw.wr("return;");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitReturnValue(ReturnExpr obj, Boolean param) {
    sw.wr("return ");
    visit(obj.getValue(), param);
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitIf(IfStmt obj, Boolean param) {
    sw.wr("if( ");
    visit(obj.getCondition(), param);
    sw.wr(" )");
    visit(obj.getThenBlock(), param);
    sw.wr("else ");
    visit(obj.getElseBlock(), param);
    return null;
  }

  @Override
  protected Void visitWhile(WhileStmt obj, Boolean param) {
    sw.wr("while( ");
    visit(obj.getCondition(), param);
    sw.wr(" )");
    visit(obj.getBlock(), param);
    return null;
  }

  @Override
  protected Void visitVarDefStmt(VarDefStmt obj, Boolean param) {
    visit(obj.getVariable(), param);
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitCaseStmt(CaseStmt obj, Boolean param) {
    sw.wr("switch( ");
    visit(obj.getCondition(), param);
    sw.wr(" ){");
    sw.nl();
    sw.incIndent();
    visitList(obj.getEntries(), param);

    sw.wr("default:{");
    sw.nl();
    sw.incIndent();
    visit(obj.getOtherwise(), param);
    sw.wr("break;");
    sw.nl();
    sw.decIndent();
    sw.wr("}");
    sw.nl();

    sw.decIndent();
    sw.wr("}");
    sw.nl();

    return null;
  }

  @Override
  protected Void visitCaseEntry(CaseEntry obj, Boolean param) {
    for (Range range : obj.getValues()) {
      for (BigInteger val : range) {
        sw.wr("case ");
        sw.wr(val.toString());
        sw.wr(": ");
      }
    }
    sw.wr("{");
    sw.nl();
    sw.incIndent();

    visit(obj.getCode(), param);
    sw.wr("break;");
    sw.nl();

    sw.decIndent();
    sw.wr("}");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitStringValue(StringValue obj, Boolean param) {
    sw.wr("\"");
    sw.wr(escape(obj.getValue()));
    sw.wr("\"");
    return null;
  }

  private String escape(String value) {
    String ret = "";

    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      switch (c) {
        case '\"':
          ret += "\\\"";
          break;
        case '\n':
          ret += "\\n";
          break;
        case '\t':
          ret += "\\t";
          break;
        // TODO more symbols to escape?
        default:
          ret += c;
          break;
      }
    }

    return ret;
  }

  @Override
  protected Void visitArrayValue(ArrayValue obj, Boolean param) {
    sw.wr("{");
    sw.wr(" ." + ARRAY_DATA_NAME + " = {");
    for (int i = 0; i < obj.getValue().size(); i++) {
      if (i > 0) {
        sw.wr(",");
      }
      visit(obj.getValue().get(i), param);
    }
    sw.wr("} ");
    sw.wr("}");
    return null;
  }

  @Override
  protected Void visitElementValue(ElementValue obj, Boolean param) {
    sw.wr(".");
    sw.wr(obj.getName());
    sw.wr("=");
    visit(obj.getValue(), param);
    sw.wr(",");
    return null;
  }

  @Override
  protected Void visitUnsafeUnionValue(UnsafeUnionValue obj, Boolean param) {
    sw.wr("{");
    visit(obj.getContentValue(), param);
    sw.wr("}");
    return null;
  }

  @Override
  protected Void visitUnionValue(UnionValue obj, Boolean param) {
    sw.wr("{");
    visit(obj.getTagValue(), param);
    // sw.wr(",");
    visit(obj.getContentValue(), param);
    sw.wr("}");
    return null;
  }

  @Override
  protected Void visitStructValue(StructValue obj, Boolean param) {
    sw.wr("{");
    visitList(obj.getValue(), param);
    sw.wr("}");
    return null;
  }

  @Override
  protected Void visitTypeRef(TypeRef obj, Boolean param) {
    sw.wr(name(obj.getRef()));
    return null;
  }

  @Override
  protected Void visitTypeCast(TypeCast obj, Boolean param) {
    sw.wr("((");
    visit(obj.getCast(), param);
    sw.wr(")");
    visit(obj.getValue(), param);
    sw.wr(")");
    return null;
  }

  @Override
  protected Void visitBoolValue(BoolValue obj, Boolean param) {
    if (obj.getValue()) {
      sw.wr("true");
    } else {
      sw.wr("false");
    }
    return null;
  }

}
