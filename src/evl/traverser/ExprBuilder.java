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
import evl.variable.Variable;

/**
 * 
 * @author urs
 */
public class ExprBuilder extends ExprReplacer<Variable> {
  private final KnowledgeBase kb;
  private final KnowWriter kw;

  public ExprBuilder(KnowledgeBase kb) {
    super();
    this.kb = kb;
    kw = kb.getEntry(KnowWriter.class);
  }

  public static Expression makeTree(Expression expr, Variable var, KnowledgeBase kb) {
    ExprBuilder builder = new ExprBuilder(kb);
    return builder.traverse(expr, var);
  }

  @Override
  public Expression traverse(Evl obj, Variable param) {
    obj = Copy.copy(obj);
    return super.traverse(obj, param);
  }

  @Override
  protected Expression visitNumber(Number obj, Variable param) {
    return new RangeValue(obj.getInfo(), new NumberSet(new Range(obj.getValue(), obj.getValue())));
  }

  @Override
  protected Expression visitReference(Reference obj, Variable param) {
    if (obj.getLink() instanceof SsaVariable) {
      assert (obj.getOffset().isEmpty());
      SsaVariable var = (SsaVariable) obj.getLink();
      if (var == param) {
        return obj;
      }
      if (var.getType().getRef() instanceof BooleanType) {
        Expression writer = kw.get(var);
        writer = Copy.copy(writer);
        return writer;
      } else if (var.getType().getRef() instanceof NumSet) {
        NumSet ns = (NumSet) var.getType().getRef();
        return new RangeValue(var.getInfo(), ns.getNumbers());
      } else {
        throw new RuntimeException("not yet implemented");
      }
    } else {
      throw new RuntimeException("not yet implemented");
      // return super.visitReference(obj, param);
    }
  }

}
