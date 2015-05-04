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

package ast.traverser.other;

import java.math.BigInteger;
import java.util.List;

import ast.Designator;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.expression.Number;
import ast.data.expression.reference.SimpleRef;
import ast.data.function.Function;
import ast.data.function.header.FuncQuery;
import ast.data.function.header.FuncResponse;
import ast.data.function.header.FuncSignal;
import ast.data.function.header.FuncSlot;
import ast.data.function.header.FuncSubHandlerEvent;
import ast.data.function.header.FuncSubHandlerQuery;
import ast.data.function.ret.FuncReturnNone;
import ast.data.function.ret.FuncReturnType;
import ast.data.type.base.ArrayType;
import ast.data.type.base.BooleanType;
import ast.data.type.base.EnumElement;
import ast.data.type.base.EnumType;
import ast.data.type.base.RangeType;
import ast.data.type.base.StringType;
import ast.data.type.composed.NamedElement;
import ast.data.type.composed.RecordType;
import ast.data.type.composed.UnionType;
import ast.data.type.special.VoidType;
import ast.data.variable.Variable;
import ast.doc.StreamWriter;
import ast.knowledge.KnowledgeBase;
import ast.pass.check.type.ExpressionTypecheck;
import ast.pass.others.CWriter;
import ast.traverser.NullTraverser;

/**
 *
 * @author urs
 */
public class CHeaderWriter extends NullTraverser<Void, StreamWriter> {

  private final List<String> debugNames; // hacky hacky

  public CHeaderWriter(List<String> debugNames, KnowledgeBase kb) {
    this.debugNames = debugNames;
  }

  @Override
  protected Void visitDefault(Ast obj, StreamWriter param) {
    throw new UnsupportedOperationException("Not supported yet: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitNamespace(Namespace obj, StreamWriter param) {
    String protname = obj.name.toUpperCase() + "_" + "H";

    param.wr("#ifndef " + protname);
    param.nl();
    param.wr("#define " + protname);
    param.nl();
    param.nl();

    param.wr("#include <stdint.h>");
    param.nl();
    param.wr("#include <stdbool.h>");
    param.nl();
    param.nl();

    if (!debugNames.isEmpty()) {
      param.wr("const char* DEBUG_NAMES[] = { ");
      boolean first = true;
      for (String name : debugNames) {
        if (first) {
          first = false;
        } else {
          param.wr(", ");
        }
        param.wr("\"" + name + "\"");
      }
      param.wr(" };");
      param.nl();
      param.nl();
    }

    visitList(obj.children, param);

    param.nl();
    param.wr("#endif /* " + protname + " */");
    param.nl();
    param.nl();
    return null;
  }

  @Override
  protected Void visitRecordType(RecordType obj, StreamWriter param) {
    param.wr("typedef struct {");
    param.nl();
    param.incIndent();
    visitList(obj.element, param);
    param.decIndent();
    param.wr("} ");
    param.wr(obj.name);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitNamedElement(NamedElement obj, StreamWriter param) {
    visit(obj.typeref, param);
    param.wr(" ");
    param.wr(obj.name);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitEnumType(EnumType obj, StreamWriter param) {
    param.wr("typedef enum {");
    param.nl();
    param.incIndent();
    for (EnumElement elem : obj.element) {
      param.wr(obj.name + Designator.NAME_SEP + elem.name);
      param.wr(",");
      param.nl();
    }
    param.decIndent();
    param.wr("} ");
    param.wr(obj.name);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitNumber(Number obj, StreamWriter param) {
    param.wr(obj.value.toString());
    return null;
  }

  @Override
  protected Void visitSimpleRef(SimpleRef obj, StreamWriter param) {
    param.wr(obj.link.name);
    return null;
  }

  private static BigInteger getPos(BigInteger value) {
    if (value.compareTo(BigInteger.ZERO) < 0) {
      return value.negate().add(BigInteger.ONE);
    } else {
      return value;
    }
  }

  @Override
  protected Void visitBooleanType(BooleanType obj, StreamWriter param) {
    param.wr("typedef ");
    param.wr("bool");
    param.wr(" ");
    param.wr(obj.name);
    param.wr(";");
    param.nl();
    return null;
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
    bits = bits * 8;

    param.wr("typedef ");
    param.wr((isNeg ? "" : "u") + "int" + bits + "_t");
    param.wr(" ");
    param.wr(obj.name);
    param.wr(";");
    param.nl();

    return null;
  }

  @Override
  protected Void visitArrayType(ArrayType obj, StreamWriter param) {
    param.wr("typedef struct {");
    param.nl();
    param.incIndent();
    visit(obj.type, param);
    param.wr(" ");
    param.wr(CWriter.ARRAY_DATA_NAME);
    param.wr("[");
    param.wr(obj.size.toString());
    param.wr("]");
    param.wr(";");
    param.decIndent();
    param.nl();
    param.wr("} ");
    param.wr(obj.name);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitStringType(StringType obj, StreamWriter param) {
    param.wr("typedef ");
    param.wr("char*");
    param.wr(" ");
    param.wr(obj.name);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitVoidType(VoidType obj, StreamWriter param) {
    param.wr("typedef ");
    param.wr("void");
    param.wr(" ");
    param.wr(obj.name);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitUnionType(UnionType obj, StreamWriter param) {
    throw new UnsupportedOperationException("Not supported yet");
  }

  @Override
  protected Void visitVariable(Variable obj, StreamWriter param) {
    visit(obj.type, param);
    param.wr(" ");
    param.wr(obj.name);
    return null;
  }

  private void wrList(AstList<? extends Ast> list, String sep, StreamWriter param) {
    boolean first = true;
    for (Ast itr : list) {
      if (first) {
        first = false;
      } else {
        param.wr(sep);
      }
      visit(itr, param);
    }
  }

  @Override
  protected Void visitFuncReturnType(FuncReturnType obj, StreamWriter param) {
    visit(obj.type, param);
    return null;
  }

  @Override
  protected Void visitFuncReturnNone(FuncReturnNone obj, StreamWriter param) {
    param.wr("void");
    return null;
  }

  private void wrPrototype(Function obj, StreamWriter param) {
    visit(obj.ret, param);
    param.wr(" ");
    param.wr(obj.name);
    param.wr("(");
    wrList(obj.param, ", ", param);
    param.wr(");");
    param.nl();
  }

  @Override
  protected Void visitFuncSignal(FuncSignal obj, StreamWriter param) {
    param.wr("// ");
    wrPrototype(obj, param);
    return null;
  }

  @Override
  protected Void visitFuncQuery(FuncQuery obj, StreamWriter param) {
    param.wr("// ");
    wrPrototype(obj, param);
    return null;
  }

  @Override
  protected Void visitFuncSlot(FuncSlot obj, StreamWriter param) {
    param.wr("extern ");
    wrPrototype(obj, param);
    return null;
  }

  @Override
  protected Void visitFuncResponse(FuncResponse obj, StreamWriter param) {
    param.wr("extern ");
    wrPrototype(obj, param);
    return null;
  }

  @Override
  protected Void visitFuncSubHandlerQuery(FuncSubHandlerQuery obj, StreamWriter param) {
    param.wr("// ");
    wrPrototype(obj, param);
    return null;
  }

  @Override
  protected Void visitFuncSubHandlerEvent(FuncSubHandlerEvent obj, StreamWriter param) {
    param.wr("// ");
    wrPrototype(obj, param);
    return null;
  }

}
