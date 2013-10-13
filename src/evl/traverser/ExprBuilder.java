package evl.traverser;

import util.NumberSet;
import util.Range;
import evl.Evl;
import evl.copy.Copy;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.RangeValue;
import evl.expression.reference.Reference;
import evl.knowledge.KnowWriter;
import evl.knowledge.KnowledgeBase;
import evl.type.base.BooleanType;
import evl.type.base.NumSet;
import evl.variable.SsaVariable;

/**
 * 
 * @author urs
 */
public class ExprBuilder extends ExprReplacer<Void> {
  private final KnowWriter kw;

  public ExprBuilder(KnowledgeBase kb) {
    super();
    kw = kb.getEntry(KnowWriter.class);
  }

  public static Expression makeTree(Expression expr, KnowledgeBase kb) {
    ExprBuilder builder = new ExprBuilder(kb);
    return builder.traverse(expr, null);
  }

  @Override
  public Expression traverse(Evl obj, Void param) {
    obj = Copy.copy(obj);
    return super.traverse(obj, param);
  }

  @Override
  protected Expression visitNumber(Number obj, Void param) {
    return new RangeValue(obj.getInfo(), new NumberSet(new Range(obj.getValue(), obj.getValue())));
  }

  @Override
  protected Expression visitReference(Reference obj, Void param) {
    if (obj.getLink() instanceof SsaVariable) {
      assert (obj.getOffset().isEmpty());
      SsaVariable var = (SsaVariable) obj.getLink();
      if (var.getType().getRef() instanceof BooleanType) {
        Expression writer = kw.get(var);
        writer = Copy.copy(writer);
        writer = visit(writer, param);
        return writer;
      } else if (var.getType().getRef() instanceof NumSet) {
        return obj;
      } else {
        throw new RuntimeException("not yet implemented");
      }
    } else {
      throw new RuntimeException("not yet implemented");
      // return super.visitReference(obj, param);
    }
  }

}
