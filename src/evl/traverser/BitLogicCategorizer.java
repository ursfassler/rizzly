package evl.traverser;

import evl.expression.Expression;
import evl.expression.binop.And;
import evl.expression.binop.BitAnd;
import evl.expression.binop.BitOr;
import evl.expression.binop.LogicAnd;
import evl.expression.binop.LogicOr;
import evl.expression.binop.Or;
import evl.knowledge.KnowType;
import evl.knowledge.KnowledgeBase;
import evl.other.Namespace;
import evl.type.Type;
import evl.type.base.BooleanType;

/**
 * Replaces "and" with "bitand" or "logicand", replaces "or" with "bitor" or "logicor"
 * 
 * @author urs
 * 
 */
public class BitLogicCategorizer extends ExprReplacer<KnowType> {
  private final static BitLogicCategorizer INSTANCE = new BitLogicCategorizer();

  public static void process(Namespace aclasses, KnowledgeBase kb) {
    KnowType kt = kb.getEntry(KnowType.class);
    INSTANCE.traverse(aclasses, kt);
  }

  @Override
  protected Expression visitAnd(And obj, KnowType param) {
    super.visitAnd(obj, param);
    Type lt = param.get(obj.getLeft());
    Type rt = param.get(obj.getRight());
    assert ((lt instanceof BooleanType) == (rt instanceof BooleanType));
    if (lt instanceof BooleanType) {
      return new LogicAnd(obj.getInfo(), obj.getLeft(), obj.getRight());
    } else {
      return new BitAnd(obj.getInfo(), obj.getLeft(), obj.getRight());
    }
  }

  @Override
  protected Expression visitOr(Or obj, KnowType param) {
    super.visitOr(obj, param);
    Type lt = param.get(obj.getLeft());
    Type rt = param.get(obj.getRight());
    assert ((lt instanceof BooleanType) == (rt instanceof BooleanType));
    if (lt instanceof BooleanType) {
      return new LogicOr(obj.getInfo(), obj.getLeft(), obj.getRight());
    } else {
      return new BitOr(obj.getInfo(), obj.getLeft(), obj.getRight());
    }
  }

}
