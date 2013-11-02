package cir.traverser;

import java.util.ArrayList;
import java.util.List;

import cir.function.Function;
import cir.other.Program;
import cir.other.Variable;
import cir.type.Type;

import common.FuncAttr;

import evl.doc.StreamWriter;

public class CHeaderWriter extends CWriter {

  @Override
  protected Void visitProgram(Program obj, StreamWriter param) {
    List<Function> funcProvided = new ArrayList<Function>();
    List<Function> funcRequired = new ArrayList<Function>();
    List<Type> types = new ArrayList<Type>();

    TypeCollector collector = new TypeCollector();
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

    param.wr("#include <stdint.h>");
    param.nl();
    param.nl();

    for (Type type : types) {
      visit(type, param);
    }
    param.nl();

    visitList(funcProvided, param);
    param.nl();

    param.wr("/*");
    param.nl();
    param.wr("please provide the following functions:");
    param.nl();
    param.nl();
    visitList(funcRequired, param);
    param.wr("*/");
    param.nl();

    return null;
  }

  @Override
  protected Void visitFunction(Function obj, StreamWriter param) {
    writeFuncHeader(obj, param);
    param.wr(";");
    param.nl();
    return null;
  }

}
