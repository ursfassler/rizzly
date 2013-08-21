package pir.traverser;

import java.math.BigInteger;

import pir.NullTraverser;
import pir.Pir;
import pir.PirObject;
import pir.expression.BoolValue;
import pir.expression.Number;
import pir.expression.UnaryExpr;
import pir.expression.reference.RefIndex;
import pir.expression.reference.RefItem;
import pir.expression.reference.RefName;
import pir.expression.reference.VarRef;
import pir.expression.reference.VarRefSimple;
import pir.other.Variable;
import pir.statement.ArithmeticOp;
import pir.statement.Relation;
import pir.type.ArrayType;
import pir.type.BooleanType;
import pir.type.EnumType;
import pir.type.NamedElement;
import pir.type.RangeType;
import pir.type.SignedType;
import pir.type.StructType;
import pir.type.Type;
import pir.type.TypeRef;

public class ExprTypeGetter extends NullTraverser<Type, Void> {
  public final static boolean NUMBER_AS_RANGE = true;
  public final static boolean NUMBER_AS_INT = false;

  private RefTypeGetter rtg = new RefTypeGetter();
  final private boolean numAsRange;

  public ExprTypeGetter(boolean numAsRange) {
    super();
    this.numAsRange = numAsRange;
  }

  static public Type process(Pir ast, boolean numAsRange) {
    ExprTypeGetter adder = new ExprTypeGetter(numAsRange);
    return adder.traverse(ast, null);
  }

  @Override
  protected Type doDefault(PirObject obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Type visitVarRef(VarRef obj, Void param) {
    Type type = visit(obj.getRef(), param);
    for (RefItem itm : obj.getOffset()) {
      type = rtg.traverse(itm, type);
    }
    return type;
  }

  @Override
  protected Type visitTypeRef(TypeRef obj, Void param) {
    return obj.getRef();
  }

  @Override
  protected Type visitVarRefSimple(VarRefSimple obj, Void param) {
    return visit(obj.getRef(), param);
  }

  @Override
  protected Type visitRefIndex(RefIndex obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitRefName(RefName obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitUnaryExpr(UnaryExpr obj, Void param) {
    Type expt = visit(obj.getExpr(), param);
    return expt;
  }

  @Override
  protected Type visitRelation(Relation obj, Void sym) {
    return new BooleanType();
  }

  @Override
  protected Type visitArithmeticOp(ArithmeticOp obj, Void sym) {
    Type lhs = visit(obj.getLeft(), sym);
    Type rhs = visit(obj.getRight(), sym);
    Type bigger;
    // TODO implement correct test, may depend on operator
    if ((lhs instanceof EnumType) || (rhs instanceof EnumType)) {
      bigger = null;
    } else if (TypeContainer.leftIsContainer(lhs, rhs)) {
      bigger = lhs;
    } else if (TypeContainer.leftIsContainer(rhs, lhs)) {
      bigger = rhs;
    } else {
      bigger = null;
    }
    return bigger;
  }

  static private int getBits(BigInteger value) {
    return value.bitCount();    //TODO does it return the correct bit number?
//    int bit = 0;
//    while (value != 0) {
//      value >>= 1;
//      bit++;
//    }
//    return bit;
  }

  @Override
  protected Type visitNumber(Number obj, Void param) {
    if (numAsRange) {
      return new RangeType(obj.getValue(), obj.getValue());
    } else {
      if ( obj.getValue().compareTo(BigInteger.ZERO) >= 0) {
        int bits = getBits(obj.getValue());
        return new SignedType(bits);
        // return new UnsignedType(bits);  //TODO reimplement this
      } else {
        throw new RuntimeException("not yet implemented");
      }
    }
  }

  @Override
  protected Type visitBoolValue(BoolValue obj, Void param) {
    return new BooleanType();
  }

  @Override
  protected Type visitVariable(Variable obj, Void param) {
    return visit( obj.getType(), param );
  }

}

class RefTypeGetter extends NullTraverser<Type, Type> {

  @Override
  protected Type doDefault(PirObject obj, Type param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Type visitRefIndex(RefIndex obj, Type param) {
    assert (param instanceof ArrayType);
    ArrayType array = (ArrayType) param;
    return array.getType().getRef();
  }

  @Override
  protected Type visitRefName(RefName obj, Type param) {
    assert (param instanceof StructType);
    StructType struct = (StructType) param;
    NamedElement elem = struct.find(obj.getName());
    assert (elem != null);
    return elem.getType().getRef();
  }

}
