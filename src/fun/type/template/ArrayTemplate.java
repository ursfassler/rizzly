package fun.type.template;

import java.util.ArrayList;
import java.util.List;

import common.ElementInfo;

import fun.expression.Expression;
import fun.expression.reference.RefTemplCall;
import fun.expression.reference.Reference;
import fun.type.base.AnyType;
import fun.type.base.IntegerType;
import fun.variable.TemplateParameter;

final public class ArrayTemplate extends BuiltinTemplate {
  public static final String NAME = "Array";
  public static final String[] PARAM = { "S", "T" };

  public ArrayTemplate() {
    super(new ElementInfo(), NAME);
    getTemplateParam().addAll(makeParam());
  }

  static public List<TemplateParameter> makeParam() {
    ArrayList<TemplateParameter> ret = new ArrayList<TemplateParameter>();

    ret.add(new TemplateParameter(new ElementInfo(), PARAM[0], new Reference(new ElementInfo(), IntegerType.NAME)));

    Reference type = new Reference(new ElementInfo(), TypeTypeTemplate.NAME);
    List<Expression> typeparam = new ArrayList<Expression>();
    typeparam.add(new Reference(new ElementInfo(), AnyType.NAME));
    type.getOffset().add(new RefTemplCall(new ElementInfo(), typeparam));
    ret.add(new TemplateParameter(new ElementInfo(), PARAM[1], type));

    return ret;
  }

}
