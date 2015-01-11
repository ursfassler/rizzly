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

package evl.traverser;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

import util.StreamWriter;
import cir.traverser.CWriter;

import common.ElementInfo;
import common.Property;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.expression.BoolValue;
import evl.expression.Number;
import evl.expression.reference.SimpleRef;
import evl.function.Function;
import evl.knowledge.KnowledgeBase;
import evl.other.EvlList;
import evl.other.RizzlyProgram;
import evl.pass.check.type.specific.ExpressionTypeChecker;
import evl.type.Type;
import evl.type.base.ArrayType;
import evl.type.base.BooleanType;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.type.base.RangeType;
import evl.type.base.StringType;
import evl.type.composed.NamedElement;
import evl.type.composed.RecordType;
import evl.type.special.VoidType;
import evl.variable.ConstGlobal;
import evl.variable.ConstPrivate;
import evl.variable.FuncVariable;
import evl.variable.Variable;

public class FpcHeaderWriter extends NullTraverser<Void, StreamWriter> {
  private final static String LibName = "LIB_NAME";
  private final List<String> debugNames; // hacky hacky

  public FpcHeaderWriter(List<String> debugNames, KnowledgeBase kb) {
    this.debugNames = debugNames;
  }

  @Override
  protected Void visitDefault(Evl obj, StreamWriter param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitRizzlyProgram(RizzlyProgram obj, StreamWriter param) {
    EvlList<Function> funcProvided = new EvlList<Function>();
    EvlList<Function> funcRequired = new EvlList<Function>();

    for (Function func : obj.getFunction()) {
      if (func.properties().get(Property.Public) == Boolean.TRUE) {
        if (func.properties().get(Property.Extern) == Boolean.TRUE) {
          funcRequired.add(func);
        } else {
          funcProvided.add(func);
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

    if (!obj.getType().isEmpty() && !((obj.getType().size() == 1) && (obj.getType().get(0) instanceof VoidType))) {
      param.wr("type");
      param.nl();

      param.incIndent();
      for (Type type : obj.getType()) {
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
      boolean first = true;
      for (String name : debugNames) {
        if (first) {
          first = false;
        } else {
          param.wr(", ");
        }
        param.wr("'" + name + "'");
      }
      param.wr(" );");
      param.nl();
      param.nl();
    }

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
  protected Void visitFunctionImpl(Function obj, StreamWriter param) {
    writeFuncHeader(obj, param);
    return null;
  }

  @Override
  protected Void visitNumber(Number obj, StreamWriter param) {
    param.wr(obj.getValue().toString());
    return null;
  }

  protected void writeFuncHeader(Function obj, StreamWriter param) {
    assert (obj.properties().get(Property.Public) == Boolean.TRUE);
    if (obj.getRet().getLink() instanceof VoidType) {
      param.wr("procedure");
    } else {
      param.wr("function");
    }
    param.wr(" ");
    param.wr(obj.getName());
    param.wr("(");
    for (int i = 0; i < obj.getParam().size(); i++) {
      if (i > 0) {
        param.wr("; ");
      }
      Variable var = obj.getParam().get(i);
      visit(var, param);
    }
    param.wr(")");

    if (!(obj.getRet().getLink() instanceof VoidType)) {
      param.wr(":");
      visit(obj.getRet(), param);
    }

    param.wr(";");
    param.wr(" cdecl;");

    if (obj.properties().get(Property.Extern) != Boolean.TRUE) {
      assert (obj.properties().get(Property.Public) == Boolean.TRUE);
      param.wr(" external ");
      param.wr(LibName);
      param.wr(";");
    } else {
      param.wr(" public;");
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

  private String getName(boolean isNeg, int bytes, ElementInfo info) {
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
        RError.err(ErrorType.Error, info, "Too many bytes for fpc backend: " + bytes);
        return null;
    }
  }

  @Override
  protected Void visitRangeType(RangeType obj, StreamWriter param) {
    boolean isNeg = obj.getNumbers().getLow().compareTo(BigInteger.ZERO) < 0;
    BigInteger max = getPos(obj.getNumbers().getHigh()).max(getPos(obj.getNumbers().getLow()));
    int bits = ExpressionTypeChecker.bitCount(max);
    assert (bits >= 0);
    if (isNeg) {
      bits++;
    }
    bits = (bits + 7) / 8;
    bits = bits == 0 ? 1 : bits;
    if (Integer.highestOneBit(bits) != Integer.lowestOneBit(bits)) {
      bits = Integer.highestOneBit(bits) * 2;
    }

    String tname = getName(isNeg, bits, obj.getInfo());

    param.wr(obj.getName());
    param.wr(" = ");
    param.wr(tname);
    param.wr(";");
    param.nl();

    return null;
  }

  @Override
  protected Void visitTypeRef(SimpleRef obj, StreamWriter param) {
    param.wr(obj.getLink().getName());
    return null;
  }

  @Override
  protected Void visitConstPrivate(ConstPrivate obj, StreamWriter param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Void visitConstGlobal(ConstGlobal obj, StreamWriter param) {
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
    param.wr(obj.getSize().subtract(BigInteger.ONE).toString());
    param.wr("] of ");
    visit(obj.getType(), param);
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
    visitList(obj.getElement(), param);
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

    Iterator<EnumElement> itr = obj.getElement().iterator();
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
    visit(obj.getRef(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitBoolValue(BoolValue obj, StreamWriter param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Void visitVoidType(VoidType obj, StreamWriter param) {
    param.wr("{" + obj.getName() + "}");
    param.nl();
    return null;
  }

}
