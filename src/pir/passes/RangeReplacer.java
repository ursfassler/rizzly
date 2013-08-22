package pir.passes;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import pir.NullTraverser;
import pir.PirObject;
import pir.other.Program;
import pir.traverser.Relinker;
import pir.type.RangeType;
import pir.type.SignedType;
import pir.type.Type;
import pir.type.UnsignedType;

/**
 * Replaces range types with integer types
 * 
 * @author urs
 * 
 */
public class RangeReplacer extends NullTraverser<Void, Void> {
  private final Map<Integer, SignedType> signed = new HashMap<Integer, SignedType>();
  private final Map<Integer, UnsignedType> unsigned = new HashMap<Integer, UnsignedType>();
  private final Map<RangeType, Type> map = new HashMap<RangeType, Type>();

  public static void process(Program obj) {
    RangeReplacer changer = new RangeReplacer();
    for (Type old : obj.getType()) {
      changer.traverse(old, null);
    }
    obj.getType().addAll(changer.getSigned().values());
    obj.getType().addAll(changer.getUnsigned().values());
    
    Relinker.process( obj, changer.getMap() );
  }

  public Map<Integer, SignedType> getSigned() {
    return signed;
  }

  public Map<Integer, UnsignedType> getUnsigned() {
    return unsigned;
  }

  public Map<RangeType, Type> getMap() {
    return map;
  }

  @Override
  protected Void doDefault(PirObject obj, Void param) {
    return null;
  }

  @Override
  protected Void visitUnsignedType(UnsignedType obj, Void param) {
    throw new RuntimeException("should not exist");
  }

  @Override
  protected Void visitSignedType(SignedType obj, Void param) {
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
    int bits = getBits(max);
    if (hasNeg) {
      bits++;
    }

    Type ret;
    if (hasNeg) {
      ret = getSint(bits);
    } else {
      ret = getUint(bits);
    }
    map.put(obj, ret);

    return null;
  }

  private int getBits(BigInteger val) {
    assert (val.compareTo(BigInteger.ZERO) >= 0);
    int bits = 0;
    while (!val.equals(BigInteger.ZERO)) {
      val = val.shiftRight(1);
      bits++;
    }
    return bits;
  }

  private UnsignedType getUint(int bits) {
    UnsignedType ret = unsigned.get(bits);
    if (ret == null) {
      ret = new UnsignedType(bits);
      unsigned.put(bits, ret);
    }
    assert (ret != null);
    return ret;
  }

  private SignedType getSint(int bits) {
    SignedType ret = signed.get(bits);
    if (ret == null) {
      ret = new SignedType(bits);
      signed.put(bits, ret);
    }
    assert (ret != null);
    return ret;
  }
}
