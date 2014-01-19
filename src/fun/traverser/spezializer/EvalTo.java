package fun.traverser.spezializer;

import java.util.List;

import fun.expression.Expression;
import fun.expression.reference.RefTemplCall;
import fun.expression.reference.Reference;
import fun.knowledge.KnowledgeBase;
import fun.other.Generator;
import fun.other.Named;
import fun.variable.TemplateParameter;

public class EvalTo {
  public static Named any(Reference obj, KnowledgeBase kb) {
    if (!(obj.getLink() instanceof Generator)) {
      return obj.getLink();
    }
    Generator generator = (Generator) obj.getLink();
    if (obj.getOffset().isEmpty() || !(obj.getOffset().get(0) instanceof RefTemplCall)) {
      assert (generator.getTemplateParam().isEmpty());
      return generator;
    }
    assert (obj.getOffset().size() == 1);
    assert (obj.getOffset().get(0) instanceof RefTemplCall);

    List<Expression> actparam = ((RefTemplCall) obj.getOffset().get(0)).getActualParameter();

    return Specializer.process(generator, actparam, obj.getInfo(), kb);
  }

}
