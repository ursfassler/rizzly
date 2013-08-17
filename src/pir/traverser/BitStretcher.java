package pir.traverser;

import pir.PirObject;
import pir.expression.ArrayValue;
import pir.expression.BoolValue;
import pir.expression.Number;
import pir.expression.PExpression;
import pir.expression.Relation;
import pir.expression.StringValue;
import pir.expression.UnaryExpr;
import pir.expression.reference.Reference;
import pir.know.KnowPirType;
import pir.statement.ArOp;
import pir.statement.ArithmeticOp;
import pir.type.IntType;
import pir.type.Type;

/**
 * Add modulo operations on data types where size is not a multiple of 8
 * 
 * @author urs
 * 
 */
public class BitStretcher extends ExprReplacer<Void> {

  public static void process(PirObject obj) {
    BitStretcher stretcher = new BitStretcher();
    stretcher.traverse(obj, null);
  }

  @Override
  protected PExpression visitArithmeticOp(ArithmeticOp obj, Void param) {
    PExpression le = (PExpression) visit(obj.getLeft(), null);
    PExpression re = (PExpression) visit(obj.getRight(), null);

    obj = new ArithmeticOp(le, re, obj.getOp());

    Type restype = KnowPirType.get(obj);

    if (restype instanceof IntType) { // FIXME what with type alias to integer type?
      int bits = ((IntType) restype).getBits();
      if (!isBigPowerOfTwo(bits)) {
        obj = mask(obj, bits);
      }
    }
    return obj;
  }

  @Override
  protected PExpression visitUnaryExpr(UnaryExpr obj, Void param) {
    PExpression expr = (PExpression) visit(obj.getExpr(), null);

    obj = new UnaryExpr(obj.getOp(), expr);

    Type restype = KnowPirType.get(obj);

    if (restype instanceof IntType) { // FIXME what with type alias to integer type?
      int bits = ((IntType) restype).getBits();
      if (!isBigPowerOfTwo(bits)) {
        return mask(obj, bits);
      }
    }
    return obj;
  }

  @Override
  protected PExpression visitRelation(Relation obj, Void param) {
    visit(obj.getLeft(), null);
    visit(obj.getRight(), null);
    return obj;
  }

  private boolean isBigPowerOfTwo(int number) {
    int bits = Integer.bitCount(number);
    return (bits <= 1) && (number >= 8);
  }

  private ArithmeticOp mask(PExpression obj, int bits) {
    int bitmask = Integer.MAX_VALUE << bits;
    bitmask = ~bitmask;
    Number mask = new Number(bitmask);
    return new ArithmeticOp(obj, mask, ArOp.AND);
  }

  @Override
  protected PExpression visitReference(Reference obj, Void param) {
    return obj;
  }

  @Override
  protected PExpression visitNumber(Number obj, Void param) {
    return obj;
  }

  @Override
  protected PExpression visitBoolValue(BoolValue obj, Void param) {
    return obj;
  }

  @Override
  protected PExpression visitStringValue(StringValue obj, Void param) {
    return obj;
  }

  @Override
  protected PExpression visitArrayValue(ArrayValue obj, Void param) {
    visitExprList(obj.getValue(), param);
    return obj;
  }

}
