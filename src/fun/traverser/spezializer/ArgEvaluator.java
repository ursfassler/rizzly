package fun.traverser.spezializer;

import java.math.BigInteger;

import error.ErrorType;
import error.RError;
import fun.Fun;
import fun.NullTraverser;
import fun.expression.Expression;
import fun.expression.Number;
import fun.expression.reference.Reference;
import fun.knowledge.KnowledgeBase;
import fun.other.ActualTemplateArgument;
import fun.traverser.Memory;
import fun.type.Type;
import fun.type.base.AnyType;
import fun.type.base.IntegerType;
import fun.type.base.NaturalType;
import fun.type.template.Range;
import fun.type.template.TypeType;

public class ArgEvaluator extends NullTraverser<ActualTemplateArgument, ActualTemplateArgument> {
  final private KnowledgeBase kb;

  public ArgEvaluator(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  public static ActualTemplateArgument process(Type type, ActualTemplateArgument acarg, KnowledgeBase kb) {
    ArgEvaluator argEvaluator = new ArgEvaluator(kb);
    return argEvaluator.traverse(type, acarg);
  }

  @Override
  protected ActualTemplateArgument visitDefault(Fun obj, ActualTemplateArgument param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected ActualTemplateArgument visitIntegerType(IntegerType obj, ActualTemplateArgument param) {
    Number num = (Number) ExprEvaluator.evaluate((Expression) param, new Memory(), kb);
    return num;
  }

  @Override
  protected ActualTemplateArgument visitNaturalType(NaturalType obj, ActualTemplateArgument param) {
    Number num = (Number) ExprEvaluator.evaluate((Expression) param, new Memory(), kb);
    if (num.getValue().compareTo(BigInteger.ZERO) < 0) {
      RError.err(ErrorType.Error, param.getInfo(), "Value for Natural type has to be >= 0");
    }
    return num;
  }

  @Override
  protected ActualTemplateArgument visitRange(Range obj, ActualTemplateArgument param) {
    Number num = (Number) ExprEvaluator.evaluate((Expression) param, new Memory(), kb);
    // TODO check type
    return num;
  }

  @Override
  protected ActualTemplateArgument visitAnyType(AnyType obj, ActualTemplateArgument param) {
    if (!(param instanceof Type)) {
      param = Specializer.evalType((Reference) param, kb);
    }
    assert (param instanceof Type);
    return param;
  }

  @Override
  protected ActualTemplateArgument visitTypeType(TypeType obj, ActualTemplateArgument param) {
    Fun evald = Specializer.eval((Reference) param, kb);
    // TODO check type
    return (ActualTemplateArgument) evald;
  }

}
