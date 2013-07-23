package pir.traverser;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import pir.other.Program;
import pir.type.Array;
import pir.type.BooleanType;
import pir.type.EnumType;
import pir.type.StringType;
import pir.type.StructType;
import pir.type.Type;
import pir.type.TypeAlias;
import pir.type.UnionType;
import pir.type.UnsignedType;
import pir.type.VoidType;

/**
 * Changes integer types to multiple of 8 bits
 *
 * @author urs
 *
 */
public class BitChanger extends TypeReplacer<Void> {
  private final Map<Integer, UnsignedType> uint = new HashMap<Integer, UnsignedType>();

  public BitChanger(Collection<UnsignedType> uint) {
    for (UnsignedType type : uint) {
      this.uint.put(type.getBits(), type);
    }
  }

  public static void process(Program obj) {
    List<UnsignedType> uint = (new Getter<UnsignedType, Void>() {
      @Override
      protected Void visitUnsignedType(UnsignedType obj, Void param) {
        return add(obj);
      }
    }).get(obj, null);
    BitChanger changer = new BitChanger(uint);
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
  protected Type visitUnsignedType(UnsignedType obj, Void param) {
    if (isBigPowerOfTwo(obj.getBits())) {
      return obj;
    }
    int bits = nextBigPowerOf2(obj.getBits());

    UnsignedType type = uint.get(bits);
    if (type == null) {
      type = new UnsignedType(bits);
      uint.put(bits, type);
    }

    return type;
  }

  private boolean isBigPowerOfTwo(int number) {
    int bits = Integer.bitCount(number);
    return (bits <= 1) && (number >= 8);
  }

  private int nextBigPowerOf2(int number) {
    assert (!isBigPowerOfTwo(number));
    int log = 0;
    while (number != 0) {
      log++;
      number = number >> 1;
    }
    if (log < 3) {
      log = 3;
    }
    return 1 << log;
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
