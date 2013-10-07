package evl.traverser;

import evl.Evl;
import evl.copy.Copy;
import evl.expression.Expression;
import evl.expression.reference.Reference;
import evl.knowledge.KnowledgeBase;
import evl.type.base.BooleanType;
import evl.variable.SsaVariable;

/**
 *
 * @author urs
 */
public class ExprBuilder extends ExprReplacer<Void> {

  public static Expression makeTree(Expression expr, KnowledgeBase kb) {
    ExprBuilder builder = new ExprBuilder();
    return builder.traverse(expr, null);
  }

  @Override
  public Expression traverse(Evl obj, Void param) {
    obj = Copy.copy(obj);
    return super.traverse(obj, param);
  }

  @Override
  protected Expression visitReference(Reference obj, Void param) {
    if( obj.getLink() instanceof SsaVariable ){
      SsaVariable var = (SsaVariable) obj.getLink();
      if( var.getType().getRef() instanceof  BooleanType ){
        
      }
    }
    return super.visitReference(obj, param);
  }

  
  
}
