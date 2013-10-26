package fun.traverser.spezializer;

import java.util.List;

import fun.expression.Expression;
import fun.expression.reference.RefTemplCall;
import fun.expression.reference.Reference;
import fun.expression.reference.ReferenceLinked;
import fun.knowledge.KnowledgeBase;
import fun.other.Generator;
import fun.other.Named;

public class EvalTo {
  public static Named any(Reference expr, KnowledgeBase kb) {
    assert (expr instanceof ReferenceLinked);

    ReferenceLinked obj = (ReferenceLinked) expr;

    if (!(obj.getLink() instanceof Generator)) {
      return (Named) obj.getLink();
    }
    assert (obj.getLink() instanceof Generator);
    assert (obj.getOffset().size() == 1);
    assert (obj.getOffset().get(0) instanceof RefTemplCall);

    Generator generator = (Generator) obj.getLink();
    List<Expression> actparam = ((RefTemplCall) obj.getOffset().get(0)).getActualParameter();

    return Specializer.process(generator, actparam, expr.getInfo(), kb);
  }

}
