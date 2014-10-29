package fun.traverser.spezializer;

import java.math.BigInteger;
import java.util.List;

import fun.Fun;
import fun.NullTraverser;
import fun.expression.Number;
import fun.knowledge.KnowBaseItem;
import fun.knowledge.KnowInstance;
import fun.knowledge.KnowledgeBase;
import fun.other.ActualTemplateArgument;
import fun.type.Type;
import fun.type.base.AnyType;
import fun.type.base.IntegerType;
import fun.type.base.NaturalType;
import fun.type.template.ArrayTemplate;
import fun.type.template.RangeTemplate;
import fun.type.template.TypeTemplate;
import fun.type.template.TypeTypeTemplate;

public class GenericSpecializer extends NullTraverser<Type, List<ActualTemplateArgument>> {
  private final KnowledgeBase kb;
  private final KnowInstance ki;
  private final KnowBaseItem kbi;

  public GenericSpecializer(KnowledgeBase kb) {
    this.kb = kb;
    ki = kb.getEntry(KnowInstance.class);
    kbi = kb.getEntry(KnowBaseItem.class);
  }

  @Override
  protected Type visitDefault(Fun obj, List<ActualTemplateArgument> param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  public static Type process(TypeTemplate type, List<ActualTemplateArgument> genspec, KnowledgeBase kb) {
    GenericSpecializer specializer = new GenericSpecializer(kb);
    return specializer.traverse(type, genspec);
  }

  @Override
  protected Type visitRangeTemplate(RangeTemplate obj, List<ActualTemplateArgument> param) {
    assert (param.size() == 2);
    Type ret = (Type) ki.find(obj, param);
    if (ret == null) {
      ActualTemplateArgument low = ArgEvaluator.process(new IntegerType(), param.get(0), kb);
      assert (low instanceof Number);
      ActualTemplateArgument high = ArgEvaluator.process(new IntegerType(), param.get(1), kb);
      assert (high instanceof Number);
      ret = kbi.getRangeType(((Number) low).getValue(), ((Number) high).getValue());
      ki.add(obj, param, ret);
    }
    return ret;
  }

  @Override
  protected Type visitArrayTemplate(ArrayTemplate obj, List<ActualTemplateArgument> param) {
    assert (param.size() == 2);
    Type ret = (Type) ki.find(obj, param);
    if (ret == null) {
      ActualTemplateArgument size = ArgEvaluator.process(new NaturalType(), param.get(0), kb);
      ActualTemplateArgument type = ArgEvaluator.process(new AnyType(), param.get(1), kb);
      assert (type instanceof Type);
      assert (size instanceof Number);
      BigInteger count = ((Number) size).getValue();
      ret = kbi.getArray(count, (Type) type);
      ki.add(obj, param, ret);
    }
    return ret;
  }

  @Override
  protected Type visitTypeTypeTemplate(TypeTypeTemplate obj, List<ActualTemplateArgument> param) {
    Type ret = (Type) ki.find(obj, param);
    if (ret == null) {
      assert (param.size() == 1);
      ActualTemplateArgument type = ArgEvaluator.process(new AnyType(), param.get(0), kb);
      assert (type instanceof Type);
      ret = kbi.getTypeType((Type) type);
      ki.add(obj, param, ret);
    }
    return ret;
  }
}
