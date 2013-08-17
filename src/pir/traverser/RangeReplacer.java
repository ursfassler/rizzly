package pir.traverser;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import pir.other.Program;
import pir.type.Array;
import pir.type.BooleanType;
import pir.type.EnumType;
import pir.type.RangeType;
import pir.type.SignedType;
import pir.type.StringType;
import pir.type.StructType;
import pir.type.Type;
import pir.type.TypeAlias;
import pir.type.UnionType;
import pir.type.UnsignedType;
import pir.type.VoidType;

/**
 * Replaces range types with integer types
 *
 * @author urs
 *
 */
public class RangeReplacer extends TypeReplacer<Void> {
  private final Map<Integer, SignedType> inttypes = new HashMap<Integer, SignedType>();

  public RangeReplacer(List<SignedType> uint) {
    for( SignedType type : uint ){
      inttypes.put(type.getBits(), type);
    }
  }

  private SignedType getInt( int bits ){
    SignedType ret = inttypes.get(bits);
    if( ret == null ){
      ret = new SignedType(bits);
      inttypes.put(bits, ret);
    }
    assert( ret != null );
    return ret;
  }
  
  public static void process(Program obj) {
    List<SignedType> uint = (new Getter<SignedType, Void>() {
      @Override
      protected Void visitSignedType(SignedType obj, Void param) {
        return add(obj);
      }
    }).get(obj, null);
    assert (uint.isEmpty()); // TODO if so, remove code above
    RangeReplacer changer = new RangeReplacer(uint);
    changer.traverse(obj, null);
  }

  @Override
  protected Type visitProgram(Program obj, Void param) {
    HashSet<Type> ns = new HashSet<Type>();
    for (Type type : obj.getType()) {
      Type nt = visit(type, param);
      assert (nt != null);
      ns.add(nt);
    }
    obj.getType().clear();
    obj.getType().addAll(ns);

    visitList(obj.getConstant(), param);
    visitList(obj.getFunction(), param);
    visitList(obj.getVariable(), param);
    return null;
  }

  @Override
  protected Type visitRangeType(RangeType obj, Void param) {
    // TODO implement also for signed
    // TODO add range offset movement (i.e. move R{10,20} to R{0,10})
    BigInteger low = obj.getLow();
    boolean hasNeg = low.compareTo(BigInteger.ZERO) < 0; //TODO ok?
    if( hasNeg ){
      low = low.add(BigInteger.ONE).abs();
    }
    BigInteger max = low.max(obj.getHigh());
    int bits = getBits( max );
    bits++;
    
    SignedType ret = getInt(bits);
    return ret;
  }

  private int getBits(BigInteger val) {
    assert( val.compareTo(BigInteger.ZERO) >= 0 );
    int bits = 0;
    while( !val.equals(BigInteger.ZERO) ){
      val = val.shiftRight(1);
      bits++;
    }
    return bits;
  }

  //TODO move this functions to TypeReplacer
  @Override
  protected Type visitVoidType(VoidType obj, Void param) {
    return obj;
  }

  @Override
  protected Type visitStructType(StructType obj, Void param) {
    visitList(obj.getElements(), param);
    return obj;
  }

  @Override
  protected Type visitUnionType(UnionType obj, Void param) {
    visitList(obj.getElements(), param);
    return obj;
  }

  @Override
  protected Type visitArray(Array obj, Void param) {
    Type typ = visit(obj.getType(), param);
    obj.setType(typ);
    return obj;
  }

  @Override
  protected Type visitTypeAlias(TypeAlias obj, Void param) {
    Type typ = visit(obj.getRef(), param);
    obj.setRef(typ);
    return typ;
  }

  @Override
  protected Type visitEnumType(EnumType obj, Void param) {
    return obj;
  }

  @Override
  protected Type visitBooleanType(BooleanType obj, Void param) {
    return obj;
  }

  @Override
  protected Type visitStringType(StringType obj, Void param) {
    return obj;
  }

  @Override
  protected Type visitSignedType(SignedType obj, Void param) {
    return obj;
  }

  @Override
  protected Type visitUnsignedType(UnsignedType obj, Void param) {
    throw new RuntimeException("there should be no UnsignedType in PIR");
  }

}
