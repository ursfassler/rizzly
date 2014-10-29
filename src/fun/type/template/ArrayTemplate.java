package fun.type.template;

import common.ElementInfo;

import fun.expression.reference.RefTemplCall;
import fun.expression.reference.Reference;
import fun.other.ActualTemplateArgument;
import fun.other.FunList;
import fun.type.base.AnyType;
import fun.type.base.IntegerType;
import fun.variable.TemplateParameter;

final public class ArrayTemplate extends TypeTemplate {
  public static final String NAME = "Array";
  public static final String[] PARAM = { "S", "T" };

  public ArrayTemplate() {
    super(ElementInfo.NO);
  }

  static public FunList<TemplateParameter> makeParam() {
    FunList<TemplateParameter> ret = new FunList<TemplateParameter>();

    ret.add(inst(PARAM[0], IntegerType.NAME));

    Reference type = new Reference(ElementInfo.NO, TypeTypeTemplate.NAME);
    FunList<ActualTemplateArgument> typeparam = new FunList<ActualTemplateArgument>();
    typeparam.add(new Reference(ElementInfo.NO, AnyType.NAME));
    type.getOffset().add(new RefTemplCall(ElementInfo.NO, typeparam));
    ret.add(inst(PARAM[1], type));

    return ret;
  }

}
