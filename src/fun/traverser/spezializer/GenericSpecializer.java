package fun.traverser.spezializer;

import java.math.BigInteger;
import java.util.List;

import common.ElementInfo;

import fun.Fun;
import fun.NullTraverser;
import fun.expression.Number;
import fun.expression.reference.Reference;
import fun.knowledge.KnowledgeBase;
import fun.other.ActualTemplateArgument;
import fun.other.Named;
import fun.type.Type;
import fun.type.template.Array;
import fun.type.template.ArrayTemplate;
import fun.type.template.BuiltinTemplate;
import fun.type.template.Range;
import fun.type.template.RangeTemplate;
import fun.type.template.TypeType;
import fun.type.template.TypeTypeTemplate;

public class GenericSpecializer extends NullTraverser<Type, List<ActualTemplateArgument>> {
  @Override
  protected Type visitDefault(Fun obj, List<ActualTemplateArgument> param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  public static Named process(BuiltinTemplate type, List<ActualTemplateArgument> genspec, KnowledgeBase kb) {
    GenericSpecializer specializer = new GenericSpecializer();
    return specializer.traverse(type, genspec);
  }

  @Override
  protected Type visitRangeTemplate(RangeTemplate obj, List<ActualTemplateArgument> param) {
    assert (param.size() == 2);
    ActualTemplateArgument low = param.get(0);
    ActualTemplateArgument high = param.get(1);
    assert (low instanceof Number);
    assert (high instanceof Number);
    return new Range(obj.getInfo(), ((Number) low).getValue(), ((Number) high).getValue());
  }

  @Override
  protected Type visitArrayTemplate(ArrayTemplate obj, List<ActualTemplateArgument> param) {
    assert (param.size() == 2);
    ActualTemplateArgument size = param.get(0);
    ActualTemplateArgument type = param.get(1);
    assert (type instanceof Type);
    assert (size instanceof Number);
    BigInteger count = ((Number) size).getValue();
    Type typ = (Type) type;
    return new Array(obj.getInfo(), count, new Reference(new ElementInfo(), typ));
  }

  @Override
  protected Type visitTypeTypeTemplate(TypeTypeTemplate obj, List<ActualTemplateArgument> param) {
    assert (param.size() == 1);
    ActualTemplateArgument type = param.get(0);
    assert (type instanceof Type);
    Type typ = (Type) type;
    return new TypeType(obj.getInfo(), new Reference(new ElementInfo(), typ));
  }

}
