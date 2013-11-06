package cir.traverser;

import java.util.ArrayList;
import java.util.List;

import cir.CirBase;
import cir.NullTraverser;
import cir.expression.BoolValue;
import cir.expression.Number;
import cir.function.Function;
import cir.function.FunctionImpl;
import cir.function.FunctionPrototype;
import cir.other.Constant;
import cir.other.FuncVariable;
import cir.other.Program;
import cir.other.Variable;
import cir.type.ArrayType;
import cir.type.EnumElement;
import cir.type.EnumType;
import cir.type.NamedElement;
import cir.type.SIntType;
import cir.type.StringType;
import cir.type.StructType;
import cir.type.Type;
import cir.type.TypeAlias;
import cir.type.UIntType;
import cir.type.UnionType;
import cir.type.VoidType;

import common.FuncAttr;

import evl.doc.StreamWriter;

//FIXME do not name boolean elements true and false; they are defined by the language

public class FpcHeaderWriter extends NullTraverser<Void, StreamWriter> {
  private final static String LibName = "LIB_NAME";

  @Override
  protected Void visitDefault(CirBase obj, StreamWriter param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitProgram(Program obj, StreamWriter param) {
    List<Function> funcProvided = new ArrayList<Function>();
    List<Function> funcRequired = new ArrayList<Function>();
    List<Type> types = new ArrayList<Type>();

    FpcTypeCollector collector = new FpcTypeCollector();
    for (Function func : obj.getFunction()) {
      if (func.getAttributes().contains(FuncAttr.Public)) {
        if (func.getAttributes().contains(FuncAttr.Extern)) {
          funcRequired.add(func);
        } else {
          funcProvided.add(func);
        }
        collector.traverse(func.getRetType(), types);
        for (Variable var : func.getArgument()) {
          collector.traverse(var.getType(), types);
        }
      }
    }

    param.wr("unit ");
    param.wr(obj.getName());
    param.wr(";");
    param.nl();
    param.nl();

    param.wr("interface");
    param.nl();
    param.nl();

    // TODO write public constants

    if (!types.isEmpty()) {
      param.wr("type");
      param.nl();

      param.incIndent();
      for (Type type : types) {
        visit(type, param);
      }
      param.decIndent();

      param.nl();
    }

    param.wr("const ");
    param.nl();
    param.incIndent();
    param.wr(LibName);
    param.wr(" = '");
    param.wr(obj.getName());
    param.wr("';");
    param.decIndent();
    param.nl();
    param.nl();

    visitList(funcProvided, param);
    param.nl();

    param.wr("{");
    param.nl();
    param.wr("please provide the following functions:");
    param.nl();
    param.nl();
    visitList(funcRequired, param);
    param.wr("}");
    param.nl();
    param.nl();

    param.wr("implementation");
    param.nl();
    param.nl();

    param.wr("end.");
    param.nl();
    param.nl();

    return null;
  }

  @Override
  protected Void visitFunction(Function obj, StreamWriter param) {
    writeFuncHeader(obj, param);
    return null;
  }

  @Override
  protected Void visitNumber(Number obj, StreamWriter param) {
    param.wr(obj.getValue().toString());
    return null;
  }

  protected void writeFuncHeader(Function obj, StreamWriter param) {
    assert (obj.getAttributes().contains(FuncAttr.Public));
    if (obj.getRetType().getRef() instanceof VoidType) {
      param.wr("procedure");
    } else {
      param.wr("function");
    }
    param.wr(" ");
    param.wr(obj.getName());
    param.wr("(");
    for (int i = 0; i < obj.getArgument().size(); i++) {
      if (i > 0) {
        param.wr("; ");
      }
      Variable var = obj.getArgument().get(i);
      visit(var, param);
    }
    param.wr(")");

    if (!(obj.getRetType().getRef() instanceof VoidType)) {
      param.wr(":");
      visit(obj.getRetType(), param);
    }

    param.wr(";");
    param.wr(" cdecl;");

    if (!obj.getAttributes().contains(FuncAttr.Extern)) {
      assert (obj.getAttributes().contains(FuncAttr.Public));
      param.wr(" external ");
      param.wr(LibName);
      param.wr(";");
    } else {
      param.wr(" public;");
    }

    param.nl();
  }

  @Override
  protected Void visitFunctionPrototype(FunctionPrototype obj, StreamWriter param) {
    assert (false);
    param.wr("/* ");
    param.wr(obj.getName());
    param.wr(" */");
    param.nl();
    param.nl();
    return null;
  }

  @Override
  protected Void visitFunctionImpl(FunctionImpl obj, StreamWriter param) {
    assert (false);
    writeFuncHeader(obj, param);
    param.nl();
    visit(obj.getBody(), param);
    param.nl();
    return null;
  }

  @Override
  protected Void visitConstant(Constant obj, StreamWriter param) {
    assert (false);
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
  protected Void visitFuncVariable(FuncVariable obj, StreamWriter param) {
    param.wr(obj.getName());
    param.wr(": ");
    visit(obj.getType(), param);
    return null;
  }

  @Override
  protected Void visitSIntType(SIntType obj, StreamWriter param) {
    String name;
    switch (obj.getBytes()) {
    case 1:
      name = "Shortint";
      break;
    case 2:
      name = "Smallint";
      break;
    case 4:
      name = "Longint";
      break;
    case 8:
      name = "Int64";
      break;
    default:
      throw new RuntimeException("Can not handle size of signed int: " + obj.getBytes());
    }
    param.wr(obj.getName());
    param.wr(" = ");
    param.wr(name);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitUIntType(UIntType obj, StreamWriter param) {
    String name;
    switch (obj.getBytes()) {
    case 1:
      name = "Byte";
      break;
    case 2:
      name = "Word";
      break;
    case 4:
      name = "Cardinal";
      break;
    case 8:
      name = "QWord";
      break;
    default:
      throw new RuntimeException("Can not handle size of signed int: " + obj.getBytes());
    }
    param.wr(obj.getName());
    param.wr(" = ");
    param.wr(name);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitStringType(StringType obj, StreamWriter param) {
    param.wr(obj.getName());
    param.wr(" = ");
    param.wr("PChar");
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitArrayType(ArrayType obj, StreamWriter param) {
    param.wr(obj.getName());
    param.wr(" = Array[0..");
    param.wr(Integer.toString(obj.getSize() - 1));
    param.wr("] of ");
    visit(obj.getType(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitTypeAlias(TypeAlias obj, StreamWriter param) {
    param.wr(obj.getName());
    param.wr(" = ");
    param.wr(obj.getRef().getName());
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitUnionType(UnionType obj, StreamWriter param) {
    assert (false);
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
    param.wr(obj.getName());
    param.wr(" = Record");
    param.nl();
    param.incIndent();
    visitList(obj.getElements(), param);
    param.decIndent();
    param.wr("end;");
    param.nl();
    return null;
  }

  @Override
  protected Void visitEnumType(EnumType obj, StreamWriter param) {
    param.wr(obj.getName());
    param.wr(" = (");
    param.nl();
    param.incIndent();

    for (int i = 0; i < obj.getElements().size(); i++) {
      EnumElement elem = obj.getElements().get(i);
      visit(elem, param);
      if (i + 1 < obj.getElements().size()) {
        param.wr(",");
      }
      param.nl();
    }

    param.decIndent();
    param.wr(");");
    param.nl();
    return null;
  }

  @Override
  protected Void visitEnumElement(EnumElement obj, StreamWriter param) {
    param.wr(obj.getName());
    param.wr(" = ");
    param.wr(Integer.toString(obj.getValue()));
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
  protected Void visitBoolValue(BoolValue obj, StreamWriter param) {
    throw new RuntimeException("not yet implemented");
  }

}
