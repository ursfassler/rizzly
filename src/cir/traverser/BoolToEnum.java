package cir.traverser;

import cir.DefTraverser;
import cir.expression.BoolValue;
import cir.expression.Expression;
import cir.expression.reference.Reference;
import cir.other.Program;
import cir.type.BooleanType;
import cir.type.EnumElement;
import cir.type.EnumType;
import cir.type.TypeRef;

/**
 * Converts the boolean data type into an Enum data type (in case our backend does not know boolean types)
 * 
 * @author urs
 * 
 */
public class BoolToEnum {
  public static void process(Program obj) {
    EnumType boolEnum = new EnumType("_Boolean");
    EnumElement beeFalse = new EnumElement("False", boolEnum, 0);
    boolEnum.getElements().add(beeFalse);
    EnumElement beeTrue = new EnumElement("True", boolEnum, 1);
    boolEnum.getElements().add(beeTrue);
    obj.getType().add(boolEnum);

    BoolToEnumTypeReplacer typerep = new BoolToEnumTypeReplacer(boolEnum);
    typerep.traverse(obj, null);
    BoolToEnumValueReplacer valrep = new BoolToEnumValueReplacer(beeFalse, beeTrue);
    valrep.traverse(obj, null);
  }

}

class BoolToEnumValueReplacer extends ExprReplacer<Void> {
  final private EnumElement beeFalse;
  final private EnumElement beeTrue;

  public BoolToEnumValueReplacer(EnumElement beeFalse, EnumElement beeTrue) {
    super();
    this.beeFalse = beeFalse;
    this.beeTrue = beeTrue;
  }

  @Override
  protected Expression visitBoolValue(BoolValue obj, Void param) {
    if (obj.getValue()) {
      return new Reference(beeTrue);
    } else {
      return new Reference(beeFalse);
    }
  }

}

class BoolToEnumTypeReplacer extends DefTraverser<Void, Void> {
  final private EnumType boolEnum;

  public BoolToEnumTypeReplacer(EnumType boolEnum) {
    super();
    this.boolEnum = boolEnum;
  }

  @Override
  protected Void visitTypeRef(TypeRef obj, Void param) {
    if (obj.getRef() instanceof BooleanType) {
      obj.setRef(boolEnum);
    }
    return super.visitTypeRef(obj, param);
  }

}
