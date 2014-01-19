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
import cir.expression.Number;
import cir.expression.StringValue;
import cir.expression.TypeCast;
import cir.expression.UnaryOp;
import cir.expression.reference.RefCall;
import cir.expression.reference.RefIndex;
import cir.expression.reference.RefName;
import cir.expression.reference.Reference;
import cir.function.Function;
import cir.function.FunctionImpl;
import cir.function.FunctionPrototype;
import cir.other.Constant;
import cir.other.FuncVariable;
import cir.other.Program;
import cir.other.StateVariable;
import cir.other.Variable;
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
import cir.type.SIntType;
import cir.type.StringType;
import cir.type.StructType;
import cir.type.TypeAlias;
import cir.type.TypeRef;
import cir.type.UIntType;
import cir.type.UnionType;
import cir.type.VoidType;

import common.FuncAttr;

import error.ErrorType;
import error.RError;

public class CWriter extends NullTraverser<Void, StreamWriter> {
  private boolean printReferenceId = false;

  static public void print(Cir obj, String filename, boolean printReferenceId) {
    CWriter printer = new CWriter();
    printer.printReferenceId = printReferenceId;
    try {
      printer.traverse(obj, new StreamWriter(new PrintStream(filename)));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected Void visitDefault(CirBase obj, StreamWriter param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitProgram(Program obj, StreamWriter param) {
    param.wr("#include <stdint.h>");
    param.nl();
    param.wr("#include <stdbool.h>");
    param.nl();
    param.nl();

    visitList(obj.getType(), param);
    param.nl();

    for (Function func : obj.getFunction()) {
      writeFuncHeader(func, param);
      param.wr(";");
      param.nl();
    }
    param.nl();

    visitList(obj.getVariable(), param);
    param.nl();

    visitList(obj.getFunction(), param);
    param.nl();

    return null;
  }

  @Override
  protected Void visitNumber(Number obj, StreamWriter param) {
    param.wr(obj.getValue().toString());
    return null;
  }

  @Override
  protected Void visitBinaryOp(BinaryOp obj, StreamWriter param) {
    param.wr("(");
    visit(obj.getLeft(), param);
    param.wr(obj.getOp().toString());
    visit(obj.getRight(), param);
    param.wr(")");
    return null;
  }

  @Override
  protected Void visitUnaryOp(UnaryOp obj, StreamWriter param) {
    param.wr("(");
    param.wr(obj.getOp().toString());
    visit(obj.getExpr(), param);
    param.wr(")");
    return null;
  }

  @Override
  protected Void visitReference(Reference obj, StreamWriter param) {
    param.wr(obj.getRef().getName());
    visitList(obj.getOffset(), param);
    return null;
  }

  @Override
  protected Void visitRefCall(RefCall obj, StreamWriter param) {
    param.wr("(");
    for (int i = 0; i < obj.getParameter().size(); i++) {
      if (i > 0) {
        param.wr(",");
      }
      visit(obj.getParameter().get(i), param);
    }
    param.wr(")");
    return null;
  }

  @Override
  protected Void visitRefName(RefName obj, StreamWriter param) {
    param.wr(".");
    param.wr(obj.getName());
    return null;
  }

  @Override
  protected Void visitRefIndex(RefIndex obj, StreamWriter param) {
    param.wr("[");
    visit(obj.getIndex(), param);
    param.wr("]");
    return null;
  }

  protected void writeFuncHeader(Function obj, StreamWriter param) {
    if (!obj.getAttributes().contains(FuncAttr.Public)) {
      assert (!obj.getAttributes().contains(FuncAttr.Extern));
      param.wr("static ");
    }
    if (obj.getAttributes().contains(FuncAttr.Extern)) {
      assert (obj.getAttributes().contains(FuncAttr.Public));
      param.wr("extern ");
    }
    visit(obj.getRetType(), param);
    param.wr(" ");
    param.wr(obj.getName());
    param.wr("(");
    if (obj.getArgument().isEmpty()) {
      param.wr("void");
    } else {
      for (int i = 0; i < obj.getArgument().size(); i++) {
        if (i > 0) {
          param.wr(",");
        }
        Variable var = obj.getArgument().get(i);
        visit(var, param);
      }
    }
    param.wr(")");
  }

  @Override
  protected Void visitFunctionPrototype(FunctionPrototype obj, StreamWriter param) {
    param.wr("/* ");
    param.wr(obj.getName());
    param.wr(" */");
    param.nl();
    param.nl();
    return null;
  }

  @Override
  protected Void visitFunctionImpl(FunctionImpl obj, StreamWriter param) {
    writeFuncHeader(obj, param);
    param.nl();
    visit(obj.getBody(), param);
    param.nl();
    return null;
  }

  @Override
  protected Void visitConstant(Constant obj, StreamWriter param) {
    param.wr("static const ");
    visit(obj.getType(), param);
    param.wr(" ");
    param.wr(obj.getName());
    param.wr(" = ");
    visit(obj.getDef(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitStateVariable(StateVariable obj, StreamWriter param) {
    param.wr("static ");
    visit(obj.getType(), param);
    param.wr(" ");
    param.wr(obj.getName());
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitFuncVariable(FuncVariable obj, StreamWriter param) {
    visit(obj.getType(), param);
    param.wr(" ");
    param.wr(obj.getName());
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
  protected Void visitAssignment(Assignment obj, StreamWriter param) {
    visit(obj.getDst(), param);
    param.wr(" = ");
    visit(obj.getSrc(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitBlock(Block obj, StreamWriter param) {
    param.wr("{");
    param.nl();
    param.incIndent();
    visitList(obj.getStatement(), param);
    param.decIndent();
    param.wr("}");
    param.nl();
    return null;
  }

  @Override
  protected Void visitSIntType(SIntType obj, StreamWriter param) {
    param.wr("typedef ");
    param.wr("int" + obj.getBytes() * 8 + "_t ");
    param.wr(obj.getName());
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitUIntType(UIntType obj, StreamWriter param) {
    param.wr("typedef ");
    param.wr("uint" + obj.getBytes() * 8 + "_t ");
    param.wr(obj.getName());
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitArrayType(ArrayType obj, StreamWriter param) {
    param.wr("typedef ");
    visit(obj.getType(), param);
    param.wr(" ");
    param.wr(obj.getName());
    param.wr("[");
    param.wr(Integer.toString(obj.getSize()));
    param.wr("]");
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitVoidType(VoidType obj, StreamWriter param) {
    param.wr("/* "); // built in type
    param.wr("typedef ");
    param.wr("void ");
    param.wr(obj.getName());
    param.wr(";");
    param.wr(" */");
    param.nl();
    return null;
  }

  @Override
  protected Void visitBooleanType(BooleanType obj, StreamWriter param) {
    param.wr("/* "); // built in type
    param.wr("typedef ");
    param.wr("bool");
    param.wr(" ");
    param.wr(obj.getName());
    param.wr(";");
    param.wr(" */");
    param.nl();
    return null;
  }

  @Override
  protected Void visitStringType(StringType obj, StreamWriter param) {
    param.wr("typedef ");
    param.wr("char*");
    param.wr(" ");
    param.wr(obj.getName());
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitTypeAlias(TypeAlias obj, StreamWriter param) {
    param.wr("typedef ");
    param.wr(obj.getRef().getName());
    param.wr(" ");
    param.wr(obj.getName());
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitUnionType(UnionType obj, StreamWriter param) {
    param.wr("typedef union {");
    param.nl();
    param.incIndent();
    visitList(obj.getElements(), param);
    param.decIndent();
    param.wr("} ");
    param.wr(obj.getName());
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitStructType(StructType obj, StreamWriter param) {
    param.wr("typedef struct {");
    param.nl();
    param.incIndent();
    visitList(obj.getElements(), param);
    param.decIndent();
    param.wr("} ");
    param.wr(obj.getName());
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitEnumType(EnumType obj, StreamWriter param) {
    param.wr("typedef enum {");
    param.nl();
    param.incIndent();

    for (int i = 0; i < obj.getElements().size(); i++) {
      EnumElement elem = obj.getElements().get(i);
      param.wr(elem.getName());
      param.wr(" = ");
      param.wr(Integer.toString(elem.getValue()));
      if (i + 1 < obj.getElements().size()) {
        param.wr(",");
      }
      param.nl();
    }

    param.decIndent();
    param.wr("} ");
    param.wr(obj.getName());
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitNamedElement(NamedElement obj, StreamWriter param) {
    visit(obj.getType(), param);
    param.wr(" ");
    param.wr(obj.getName());
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
  protected Void visitReturnValue(ReturnExpr obj, StreamWriter param) {
    param.wr("return ");
    visit(obj.getValue(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitIf(IfStmt obj, StreamWriter param) {
    param.wr("if( ");
    visit(obj.getCondition(), param);
    param.wr(" )");
    visit(obj.getThenBlock(), param);
    param.wr("else ");
    visit(obj.getElseBlock(), param);
    return null;
  }

  @Override
  protected Void visitWhile(WhileStmt obj, StreamWriter param) {
    param.wr("while( ");
    visit(obj.getCondition(), param);
    param.wr(" )");
    visit(obj.getBlock(), param);
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
  protected Void visitCaseStmt(CaseStmt obj, StreamWriter param) {
    param.wr("switch( ");
    visit(obj.getCondition(), param);
    param.wr(" ){");
    param.nl();
    param.incIndent();
    visitList(obj.getEntries(), param);

    param.wr("default:{");
    param.nl();
    param.incIndent();
    visit(obj.getOtherwise(), param);
    param.wr("break;");
    param.nl();
    param.decIndent();
    param.wr("}");
    param.nl();

    param.decIndent();
    param.wr("}");
    param.nl();

    return null;
  }

  @Override
  protected Void visitCaseEntry(CaseEntry obj, StreamWriter param) {
    for (Range range : obj.getValues()) {
      for (BigInteger val : range) {
        param.wr("case ");
        param.wr(val.toString());
        param.wr(": ");
      }
    }
    param.wr("{");
    param.nl();
    param.incIndent();

    visit(obj.getCode(), param);
    param.wr("break;");
    param.nl();

    param.decIndent();
    param.wr("}");
    param.nl();
    return null;
  }

  @Override
  protected Void visitStringValue(StringValue obj, StreamWriter param) {
    param.wr("\"");
    param.wr(escape(obj.getValue()));
    param.wr("\"");
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
  protected Void visitArrayValue(ArrayValue obj, StreamWriter param) {
    param.wr("{");
    for (int i = 0; i < obj.getValue().size(); i++) {
      if (i > 0) {
        param.wr(",");
      }
      visit(obj.getValue().get(i), param);
    }
    param.wr("}");
    return null;
  }

  @Override
  protected Void visitTypeRef(TypeRef obj, StreamWriter param) {
    param.wr(obj.getRef().getName());
    return null;
  }

  @Override
  protected Void visitTypeCast(TypeCast obj, StreamWriter param) {
    param.wr("((");
    visit(obj.getCast(), param);
    param.wr(")");
    visit(obj.getValue(), param);
    param.wr(")");
    return null;
  }

  @Override
  protected Void visitBoolValue(BoolValue obj, StreamWriter param) {
    if (obj.getValue()) {
      param.wr("true");
    } else {
      param.wr("false");
    }
    return null;
  }

}
