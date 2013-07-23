package fun.type.genfunc;

import java.util.ArrayList;
import java.util.List;

import common.Designator;
import common.ElementInfo;

import fun.expression.Expression;
import fun.expression.reference.RefCompcall;
import fun.expression.reference.ReferenceUnlinked;
import fun.other.ListOfNamed;
import fun.type.base.AnyType;
import fun.type.base.BaseType;
import fun.type.base.IntegerType;
import fun.variable.CompfuncParameter;

final public class GenericArray extends BaseType {
  public static final String NAME = "Array";
  public static final String[] PARAM = { "S", "T" };

  public GenericArray() {
    super(new ElementInfo());
  }

  public ListOfNamed<CompfuncParameter> getParamList() {
    ListOfNamed<CompfuncParameter> ret = new ListOfNamed<CompfuncParameter>();
    ret.add(new CompfuncParameter(new ElementInfo(), PARAM[0], new ReferenceUnlinked(new ElementInfo(), new Designator(IntegerType.NAME))));
    ReferenceUnlinked type = new ReferenceUnlinked(new ElementInfo(), new Designator(GenericTypeType.NAME));
    List<Expression> typeparam = new ArrayList<Expression>();
    typeparam.add(new ReferenceUnlinked(new ElementInfo(), new Designator(AnyType.NAME)));
    type.getOffset().add(new RefCompcall(new ElementInfo(),typeparam));
    ret.add(new CompfuncParameter(new ElementInfo(), PARAM[1], type));
    return ret;
  }

  @Override
  public String getName() {
    return NAME;
  }

}
