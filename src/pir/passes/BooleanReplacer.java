package pir.passes;

import java.util.List;

import pir.other.Program;
import pir.traverser.Getter;
import pir.traverser.TypeReplacer;
import pir.type.ArrayType;
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
 * Replaces boolean type with integer type
 * 
 * @author urs
 * 
 */
public class BooleanReplacer extends TypeReplacer<Void> {
  private final SignedType i1;

  public BooleanReplacer(SignedType i1) {
    assert (i1.getBits() == 1);
    this.i1 = i1;
  }

  public static void process(Program obj) {
    List<SignedType> uint = (new Getter<SignedType, Void>() {
      @Override
      protected Void visitSignedType(SignedType obj, Void param) {
        if (obj.getBits() == 1) {
          add(obj);
        }
        return null;
      }
    }).get(obj, null);
    assert (uint.size() <= 1);
    SignedType i1type;
    if (uint.isEmpty()) {
      i1type = new SignedType(1);
    } else {
      i1type = uint.get(0);
    }
    BooleanReplacer changer = new BooleanReplacer(i1type);
    changer.traverse(obj, null);
  }

  //TODO move this functions to TypeReplacer
  @Override
  protected Type visitRangeType(RangeType obj, Void param) {
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
  protected Type visitArray(ArrayType obj, Void param) {
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
    return i1;
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
