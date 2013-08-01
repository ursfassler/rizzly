package pir.traverser;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import pir.other.Program;
import pir.type.Array;
import pir.type.BooleanType;
import pir.type.EnumType;
import pir.type.RangeType;
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
  private final Map<Integer, UnsignedType> uint = new HashMap<Integer, UnsignedType>();

  public RangeReplacer(Collection<UnsignedType> uint) {
    for (UnsignedType type : uint) {
      this.uint.put(type.getHigh(), type);
    }
  }

  public static void process(Program obj) {
    List<UnsignedType> uint = (new Getter<UnsignedType, Void>() {
      @Override
      protected Void visitUnsignedType(UnsignedType obj, Void param) {
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
    assert (obj.getLow().compareTo(BigInteger.ZERO) >= 0);

    int bits;

    if (isLEQ(obj, 255)) {
      bits = 8;
    } else if (isLEQ(obj, 65535)) {
      bits = 16;
    } else if (isLEQ(obj, 4294967295l)) {
      bits = 16;
    } else {
      throw new RuntimeException("not yet implemented");
    }

    UnsignedType ret = uint.get(bits);
    if (ret == null) {
      ret = new UnsignedType(bits);
      uint.put(bits, ret);
    }
    return ret;
  }

  private boolean isLEQ(RangeType left, long right) {
    return left.getHigh().compareTo(BigInteger.valueOf(right)) <= 0; // TODO ok?
  }

  @Override
  protected Type visitUnsignedType(UnsignedType obj, Void param) {
    return obj;
  }

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

}
