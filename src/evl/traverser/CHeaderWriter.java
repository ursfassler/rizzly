package evl.traverser;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import util.StreamWriter;

import common.FuncAttr;

import evl.Evl;
import evl.NullTraverser;
import evl.expression.Number;
import evl.function.FunctionBase;
import evl.function.impl.FuncProtoRet;
import evl.function.impl.FuncProtoVoid;
import evl.other.RizzlyProgram;
import evl.traverser.typecheck.specific.ExpressionTypeChecker;
import evl.type.TypeRef;
import evl.type.base.ArrayType;
import evl.type.base.BooleanType;
import evl.type.base.EnumDefRef;
import evl.type.base.EnumType;
import evl.type.base.RangeType;
import evl.type.base.StringType;
import evl.type.composed.NamedElement;
import evl.type.composed.RecordType;
import evl.type.composed.UnionType;
import evl.type.special.PointerType;
import evl.variable.Variable;

/**
 * 
 * @author urs
 */
public class CHeaderWriter extends NullTraverser<Void, StreamWriter> {

  private final List<String> debugNames; // hacky hacky

  public CHeaderWriter(List<String> debugNames) {
    this.debugNames = debugNames;
  }

  @Override
  protected Void visitDefault(Evl obj, StreamWriter param) {
    throw new UnsupportedOperationException("Not supported yet: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitRizzlyProgram(RizzlyProgram obj, StreamWriter param) {
    String protname = obj.getName().toUpperCase() + "_" + "H";

    param.wr("#ifndef " + protname);
    param.nl();
    param.wr("#define " + protname);
    param.nl();
    param.nl();

    param.wr("#include <stdint.h>");
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

    visitItr(obj.getType(), param);
    visitItr(obj.getConstant(), param);
    assert (obj.getVariable().isEmpty());
    visitItr(obj.getFunction(), param);

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
    visitItr(obj.getElement(), param);
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

  // FIXME we can not guarantee that the order is still true after PIR
  @Override
  protected Void visitEnumType(EnumType obj, StreamWriter param) {
    param.wr("typedef enum {");
    param.nl();
    param.incIndent();
    visitItr(obj.getElement(), param);
    param.decIndent();
    param.wr("} ");
    param.wr(obj.getName());
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitEnumDefRef(EnumDefRef obj, StreamWriter param) {
    param.wr(obj.getElem().getName());
    param.wr(" = ");
    visit(obj.getElem().getDef(), param);
    param.wr(",");
    param.nl();
    return null;
  }

  @Override
  protected Void visitNumber(Number obj, StreamWriter param) {
    param.wr(obj.getValue().toString());
    return null;
  }

  @Override
  protected Void visitTypeRef(TypeRef obj, StreamWriter param) {
    param.wr(obj.getRef().getName());
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
    param.wr("uint8_t");
    param.wr(" ");
    param.wr(obj.getName());
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitNumSet(RangeType obj, StreamWriter param) {
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
    bits = bits * 8;

    param.wr("typedef ");
    param.wr((isNeg ? "" : "u") + "int" + bits + "_t");
    param.wr(" ");
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
    param.wr(obj.getSize().toString());
    param.wr("];");
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
  protected Void visitPointerType(PointerType obj, StreamWriter param) {
    param.wr("typedef ");
    visit(obj.getType(), param);
    param.wr(" ");
    param.wr("*");
    param.wr(obj.getName());
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
    visit(obj.getType(), param);
    param.wr(" ");
    param.wr(obj.getName());
    return null;
  }

  private void wrList(Collection<? extends Evl> list, String sep, StreamWriter param) {
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

  private void wrAttr(Set<FuncAttr> attr, StreamWriter param) {
    if (attr.contains(FuncAttr.Extern)) {
      param.wr("// ");
    } else {
      param.wr("extern ");
    }
  }

  private void wrPrototype(FunctionBase obj, StreamWriter param) {
    param.wr(obj.getName());
    param.wr("(");
    wrList(obj.getParam().getList(), ", ", param);
    param.wr(");");
    param.nl();
  }

  @Override
  protected Void visitFuncProtoVoid(FuncProtoVoid obj, StreamWriter param) {
    wrAttr(obj.getAttributes(), param);
    param.wr("void ");
    wrPrototype(obj, param);
    return null;
  }

  @Override
  protected Void visitFuncProtoRet(FuncProtoRet obj, StreamWriter param) {
    wrAttr(obj.getAttributes(), param);
    visit(obj.getRet(), param);
    param.wr(" ");
    wrPrototype(obj, param);
    return null;
  }
}
