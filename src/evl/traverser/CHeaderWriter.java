package evl.traverser;

import common.FuncAttr;
import evl.Evl;
import evl.NullTraverser;
import evl.doc.StreamWriter;
import evl.function.FunctionBase;
import evl.function.impl.FuncProtoRet;
import evl.function.impl.FuncProtoVoid;
import evl.other.RizzlyProgram;
import evl.traverser.typecheck.specific.ExpressionTypeChecker;
import evl.type.TypeRef;
import evl.type.base.Range;
import evl.type.base.StringType;
import evl.type.composed.NamedElement;
import evl.type.composed.RecordType;
import evl.type.composed.UnionType;
import evl.variable.Variable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Set;

/**
 *
 * @author urs
 */
public class CHeaderWriter extends NullTraverser<Void, StreamWriter> {

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

    visitItr(obj.getType(), param);
    visitItr(obj.getConstant(), param);
    assert ( obj.getVariable().isEmpty() );
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

  @Override
  protected Void visitTypeRef(TypeRef obj, StreamWriter param) {
    param.wr(obj.getRef().getName());
    return null;
  }

  private static BigInteger getPos(BigInteger value) {
    if( value.compareTo(BigInteger.ZERO) < 0 ) {
      return value.negate().add(BigInteger.ONE);
    } else {
      return value;
    }
  }

  @Override
  protected Void visitRange(Range obj, StreamWriter param) {
    boolean isNeg = obj.getLow().compareTo(BigInteger.ZERO) < 0;
    BigInteger max = getPos(obj.getHigh()).max(getPos(obj.getLow()));
    int bits = ExpressionTypeChecker.bitCount(max);
    assert ( bits >= 0 );
    if( isNeg ){
      bits++;
    }
    bits = ( bits + 7 ) / 8;
    bits = bits == 0 ? 1 : bits;
    if( Integer.highestOneBit(bits) != Integer.lowestOneBit(bits) ) {
      bits = Integer.highestOneBit(bits) * 2;
    }
    bits = bits * 8;

    param.wr("typedef ");
    param.wr( (isNeg?"":"u") + "int" + bits + "_t");
    param.wr(" ");
    param.wr(obj.getName());
    param.wr(";");
    param.nl();

    return null;
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
    for( Evl itr : list ) {
      if( first ) {
        first = false;
      } else {
        param.wr(sep);
      }
      visit(itr, param);
    }
  }

  private void wrAttr(Set<FuncAttr> attr, StreamWriter param) {
    if( attr.contains(FuncAttr.Extern) ) {
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

  @Override
  protected Void visitStringType(StringType obj, StreamWriter param) {
    throw new UnsupportedOperationException("Not supported yet");
  }

  @Override
  protected Void visitUnionType(UnionType obj, StreamWriter param) {
    throw new UnsupportedOperationException("Not supported yet");
  }
}
