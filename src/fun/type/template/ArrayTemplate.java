package fun.type.template;

import java.util.ArrayList;
import java.util.List;

import common.Designator;
import common.ElementInfo;

import fun.expression.Expression;
import fun.expression.reference.RefTemplCall;
import fun.expression.reference.ReferenceUnlinked;
import fun.other.ListOfNamed;
import fun.type.base.AnyType;
import fun.type.base.BaseType;
import fun.type.base.IntegerType;
import fun.variable.TemplateParameter;

final public class ArrayTemplate extends BaseType {
  public static final String NAME = "Array";
  public static final String[] PARAM = { "S", "T" };

  public ArrayTemplate() {
    super(new ElementInfo());
  }

  public ListOfNamed<TemplateParameter> getParamList() {
    ListOfNamed<TemplateParameter> ret = new ListOfNamed<TemplateParameter>();
    
    ret.add(new TemplateParameter(new ElementInfo(), PARAM[0], new ReferenceUnlinked(new ElementInfo(), new Designator(IntegerType.NAME))));
    
    ReferenceUnlinked type = new ReferenceUnlinked(new ElementInfo(), new Designator(TypeTypeTemplate.NAME));
    List<Expression> typeparam = new ArrayList<Expression>();
    typeparam.add(new ReferenceUnlinked(new ElementInfo(), new Designator(AnyType.NAME)));
    type.getOffset().add(new RefTemplCall(new ElementInfo(),typeparam));
    ret.add(new TemplateParameter(new ElementInfo(), PARAM[1], type));
    
    return ret;
  }

  @Override
  public String getName() {
    return NAME;
  }

}
