/**
 *  This file is part of Rizzly.
 *
 *  Rizzly is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Rizzly is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Rizzly.  If not, see <http://www.gnu.org/licenses/>.
 */

package ast.dispatcher.other;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

import ast.data.Ast;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.expression.value.BooleanValue;
import ast.data.expression.value.NumberValue;
import ast.data.function.Function;
import ast.data.function.FunctionProperty;
import ast.data.function.ret.FuncReturnNone;
import ast.data.function.ret.FunctionReturnType;
import ast.data.reference.LinkedAnchor;
import ast.data.reference.OffsetReference;
import ast.data.type.Type;
import ast.data.type.base.ArrayType;
import ast.data.type.base.BooleanType;
import ast.data.type.base.EnumElement;
import ast.data.type.base.EnumType;
import ast.data.type.base.RangeType;
import ast.data.type.base.StringType;
import ast.data.type.composed.NamedElement;
import ast.data.type.composed.RecordType;
import ast.data.type.special.VoidType;
import ast.data.variable.PrivateConstant;
import ast.data.variable.FunctionVariable;
import ast.data.variable.GlobalConstant;
import ast.data.variable.Variable;
import ast.dispatcher.NullDispatcher;
import ast.doc.StreamWriter;
import ast.knowledge.KnowledgeBase;
import ast.meta.MetaList;
import ast.pass.check.type.ExpressionTypecheck;
import ast.pass.others.CWriter;
import ast.repository.query.TypeFilter;
import ast.specification.ExternalFunction;
import ast.specification.PublicFunction;
import error.ErrorType;
import error.RError;
import util.SeparatorIterator;

public class FpcHeaderWriter extends NullDispatcher<Void, StreamWriter> {
  private final static String LibName = "LIB_NAME";
  private final List<String> debugNames; // hacky hacky

  public FpcHeaderWriter(List<String> debugNames, KnowledgeBase kb) {
    this.debugNames = debugNames;
  }

  @Override
  protected Void visitDefault(Ast obj, StreamWriter param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitNamespace(Namespace obj, StreamWriter param) {
    param.wr("unit ");
    param.wr(obj.getName());
    param.wr(";");
    param.nl();
    param.nl();

    param.wr("interface");
    param.nl();
    param.nl();

    List<Type> types = TypeFilter.select(obj.children, Type.class);

    if (!types.isEmpty() && !((types.size() == 1) && (types.get(0) instanceof VoidType))) {
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

    if (!debugNames.isEmpty()) {
      param.wr("  DEBUG_NAMES : array[0.." + (debugNames.size() - 1) + "] of string = ( ");
      SeparatorIterator.iterate(debugNames, () -> param.wr(", "), (name) -> param.wr("'" + name + "'"));
      param.wr(" );");
      param.nl();
      param.nl();
    }

    param.nl();

    AstList<Ast> funcProvided = ast.repository.query.List.select(obj.children, new PublicFunction());
    visitList(funcProvided, param);
    param.nl();

    param.wr("{");
    param.nl();
    param.wr("please provide the following functions:");
    param.nl();
    param.nl();
    AstList<Ast> funcRequired = ast.repository.query.List.select(obj.children, new ExternalFunction());
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
  protected Void visitNumber(NumberValue obj, StreamWriter param) {
    param.wr(obj.value.toString());
    return null;
  }

  @Override
  protected Void visitFuncReturnType(FunctionReturnType obj, StreamWriter param) {
    param.wr(":");
    visit(obj.type, param);
    return null;
  }

  @Override
  protected Void visitFuncReturnNone(FuncReturnNone obj, StreamWriter param) {
    return null;
  }

  protected void writeFuncHeader(Function obj, StreamWriter param) {
    if (obj.ret instanceof FuncReturnNone) {
      param.wr("procedure");
    } else {
      param.wr("function");
    }
    param.wr(" ");
    param.wr(obj.getName());
    param.wr("(");
    for (int i = 0; i < obj.param.size(); i++) {
      if (i > 0) {
        param.wr("; ");
      }
      Variable var = obj.param.get(i);
      visit(var, param);
    }
    param.wr(")");

    visit(obj.ret, param);

    param.wr(";");
    param.wr(" cdecl;");

    if (obj.property == FunctionProperty.External) {
      param.wr(" public;");
    } else {
      param.wr(" external ");
      param.wr(LibName);
      param.wr(";");
    }

    param.nl();

    // if (!obj.getBody().getStatements().isEmpty()) {
    // visit(obj.getBody(), param);
    // param.nl();
    // }
  }

  @Override
  protected Void visitBooleanType(BooleanType obj, StreamWriter param) {
    return null;
  }

  private static BigInteger getPos(BigInteger value) {
    if (value.compareTo(BigInteger.ZERO) < 0) {
      return value.negate().add(BigInteger.ONE);
    } else {
      return value;
    }
  }

  private String getName(boolean isNeg, int bytes, MetaList info) {
    switch (bytes) {
      case 1:
        return isNeg ? "Shortint" : "Byte";
      case 2:
        return isNeg ? "Smallint" : "Word";
      case 4:
        return isNeg ? "Longint" : "Cardinal";
      case 8:
        return isNeg ? "Int64" : "QWord";
      default:
        RError.err(ErrorType.Error, "Too many bytes for fpc backend: " + bytes, info);
        return null;
    }
  }

  @Override
  protected Void visitRangeType(RangeType obj, StreamWriter param) {
    boolean isNeg = obj.range.low.compareTo(BigInteger.ZERO) < 0;
    BigInteger max = getPos(obj.range.high).max(getPos(obj.range.low));
    int bits = ExpressionTypecheck.bitCount(max);
    assert (bits >= 0);
    if (isNeg) {
      bits++;
    }
    bits = (bits + 7) / 8;
    bits = bits == 0 ? 1 : bits;
    if (Integer.highestOneBit(bits) != Integer.lowestOneBit(bits)) {
      bits = Integer.highestOneBit(bits) * 2;
    }

    String tname = getName(isNeg, bits, obj.metadata());

    param.wr(obj.getName());
    param.wr(" = ");
    param.wr(tname);
    param.wr(";");
    param.nl();

    return null;
  }

  @Override
  protected Void visitOffsetReference(OffsetReference obj, StreamWriter param) {
    assert (obj.getOffset().isEmpty());
    visit(obj.getAnchor(), param);
    return null;
  }

  @Override
  protected Void visitLinkedAnchor(LinkedAnchor obj, StreamWriter param) {
    param.wr(obj.getLink().getName());
    return null;
  }

  @Override
  protected Void visitConstPrivate(PrivateConstant obj, StreamWriter param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Void visitConstGlobal(GlobalConstant obj, StreamWriter param) {
    assert (false);
    param.wr("static const ");
    visit(obj.type, param);
    param.wr(" ");
    param.wr(obj.getName());
    param.wr(" = ");
    visit(obj.def, param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitFuncVariable(FunctionVariable obj, StreamWriter param) {
    param.wr(obj.getName());
    param.wr(": ");
    visit(obj.type, param);
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
    param.wr(" = Record");
    param.nl();
    param.incIndent();
    param.wr(CWriter.ARRAY_DATA_NAME);
    param.wr(": Array[0..");
    param.wr(obj.size.subtract(BigInteger.ONE).toString());
    param.wr("] of ");
    visit(obj.type, param);
    param.wr(";");
    param.nl();
    param.decIndent();
    param.wr("end;");
    param.nl();
    return null;
  }

  @Override
  protected Void visitRecordType(RecordType obj, StreamWriter param) {
    param.wr(obj.getName());
    param.wr(" = Record");
    param.nl();
    param.incIndent();
    visitList(obj.element, param);
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

    Iterator<EnumElement> itr = obj.element.iterator();
    while (itr.hasNext()) {
      param.wr(itr.next().getName());
      if (itr.hasNext()) {
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
  protected Void visitNamedElement(NamedElement obj, StreamWriter param) {
    param.wr(obj.getName());
    param.wr(": ");
    visit(obj.typeref, param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitBoolValue(BooleanValue obj, StreamWriter param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Void visitVoidType(VoidType obj, StreamWriter param) {
    param.wr("{" + obj.getName() + "}");
    param.nl();
    return null;
  }

}
