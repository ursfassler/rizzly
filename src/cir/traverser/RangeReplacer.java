package cir.traverser;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import cir.CirBase;
import cir.NullTraverser;
import cir.other.Program;
import cir.type.IntType;
import cir.type.RangeType;
import cir.type.SIntType;
import cir.type.Type;
import cir.type.UIntType;
import error.ErrorType;
import error.RError;
import evl.traverser.typecheck.specific.ExpressionTypeChecker;

/**
 * Replaces range types with integer types
 * 
 * @author urs
 * 
 */
public class RangeReplacer extends NullTraverser<Void, Void> {
  private final Map<Integer, SIntType> signed = new HashMap<Integer, SIntType>();
  private final Map<Integer, UIntType> unsigned = new HashMap<Integer, UIntType>();
  private final Map<RangeType, Type> map = new HashMap<RangeType, Type>();
  private final int allowedByteSizes[] = { 1, 2, 4, 8 }; // TODO make a parameter (is probably target specific)

  public static void process(Program obj) {
    RangeReplacer changer = new RangeReplacer();
    for (Type old : obj.getType()) {
      changer.traverse(old, null);
    }
    obj.getType().addAll(changer.getSigned().values());
    obj.getType().addAll(changer.getUnsigned().values());

    Relinker.process(obj, changer.getMap());

    obj.getType().removeAll(changer.map.keySet()); // TODO remove all unused, but at a different location
  }

  public Map<Integer, SIntType> getSigned() {
    return signed;
  }

  public Map<Integer, UIntType> getUnsigned() {
    return unsigned;
  }

  public Map<RangeType, Type> getMap() {
    return map;
  }

  @Override
  protected Void visitDefault(CirBase obj, Void param) {
    return null;
  }

  @Override
  protected Void visitUIntType(UIntType obj, Void param) {
    throw new RuntimeException("should not exist");
  }

  @Override
  protected Void visitSIntType(SIntType obj, Void param) {
    throw new RuntimeException("should not exist");
  }

  @Override
  protected Void visitRangeType(RangeType obj, Void param) {
    // TODO implement also for signed
    // TODO add range offset movement (i.e. move R{10,20} to R{0,10})
    BigInteger low = obj.getLow();
    boolean hasNeg = low.compareTo(BigInteger.ZERO) < 0; // TODO ok?
    if (hasNeg) {
      low = low.add(BigInteger.ONE).abs();
    }
    BigInteger max = low.max(obj.getHigh());
    int bits = ExpressionTypeChecker.bitCount(max);
    if (hasNeg) {
      bits++;
    }

    int bytes = (bits + 7) / 8;

    if (bytes > allowedByteSizes[allowedByteSizes.length - 1]) {
      RError.err(ErrorType.Fatal, "Found type with too many bits: " + obj.toString());
    }

    for (int i = 0; i < allowedByteSizes.length; i++) {
      if (bytes <= allowedByteSizes[i]) {
        bytes = allowedByteSizes[i];
        break;
      }
    }

    IntType ret;
    if (hasNeg) {
      ret = getSint(bytes);
    } else {
      ret = getUint(bytes);
    }
    map.put(obj, ret);

    return null;
  }

  private UIntType getUint(int bytes) {
    UIntType ret = unsigned.get(bytes);
    if (ret == null) {
      ret = new UIntType(bytes);
      unsigned.put(bytes, ret);
    }
    assert (ret != null);
    return ret;
  }

  private SIntType getSint(int bytes) {
    SIntType ret = signed.get(bytes);
    if (ret == null) {
      ret = new SIntType(bytes);
      signed.put(bytes, ret);
    }
    assert (ret != null);
    return ret;
  }
}
