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

package evl.traverser.other;

import java.math.BigInteger;
import java.util.List;

import util.StreamWriter;

import common.Designator;

import evl.data.Evl;
import evl.data.EvlList;
import evl.data.Namespace;
import evl.data.expression.Number;
import evl.data.expression.reference.SimpleRef;
import evl.data.function.Function;
import evl.data.function.header.FuncCtrlInDataIn;
import evl.data.function.header.FuncCtrlInDataOut;
import evl.data.function.header.FuncCtrlOutDataIn;
import evl.data.function.header.FuncCtrlOutDataOut;
import evl.data.function.header.FuncSubHandlerEvent;
import evl.data.function.header.FuncSubHandlerQuery;
import evl.data.function.ret.FuncReturnNone;
import evl.data.function.ret.FuncReturnType;
import evl.data.type.base.ArrayType;
import evl.data.type.base.BooleanType;
import evl.data.type.base.EnumElement;
import evl.data.type.base.EnumType;
import evl.data.type.base.RangeType;
import evl.data.type.base.StringType;
import evl.data.type.composed.NamedElement;
import evl.data.type.composed.RecordType;
import evl.data.type.composed.UnionType;
import evl.data.type.special.VoidType;
import evl.data.variable.Variable;
import evl.knowledge.KnowledgeBase;
import evl.pass.CWriter;
import evl.pass.check.type.specific.ExpressionTypeChecker;
import evl.traverser.NullTraverser;

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
  protected Void visitDefault(Evl obj, StreamWriter param) {
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

    visitList(obj.getChildren(), param);

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
    visit(obj.ref, param);
    param.wr(" ");
    param.wr(obj.name);
    param.wr(";");
    param.nl();
    return null;
  }

  // FIXME we can not guarantee that the order is still true after PIR
  @Override
  protected Void visitEnumType(EnumType obj, StreamWriter param) {
    param.wr("typedef enum {");
    param.nl();
    param.incIndent();
    for (EnumElement elem : obj.getElement()) {
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
    boolean isNeg = obj.range.getLow().compareTo(BigInteger.ZERO) < 0;
    BigInteger max = getPos(obj.range.getHigh()).max(getPos(obj.range.getLow()));
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

  private void wrList(EvlList<? extends Evl> list, String sep, StreamWriter param) {
    boolean first = true;
    for (Evl itr : list) {
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
  protected Void visitFuncIfaceOutVoid(FuncCtrlOutDataOut obj, StreamWriter param) {
    param.wr("// ");
    wrPrototype(obj, param);
    return null;
  }

  @Override
  protected Void visitFuncIfaceOutRet(FuncCtrlOutDataIn obj, StreamWriter param) {
    param.wr("// ");
    wrPrototype(obj, param);
    return null;
  }

  @Override
  protected Void visitFuncIfaceInVoid(FuncCtrlInDataIn obj, StreamWriter param) {
    param.wr("extern ");
    wrPrototype(obj, param);
    return null;
  }

  @Override
  protected Void visitFuncIfaceInRet(FuncCtrlInDataOut obj, StreamWriter param) {
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
