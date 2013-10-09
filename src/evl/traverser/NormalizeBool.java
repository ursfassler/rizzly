package evl.traverser;

import evl.Evl;
import evl.NullTraverser;
import evl.expression.BoolValue;
import evl.expression.Expression;
import evl.expression.binop.And;
import evl.expression.binop.Equal;
import evl.expression.binop.Notequal;
import evl.expression.binop.Or;
import evl.expression.unop.Not;
import evl.knowledge.KnowledgeBase;
import evl.other.Namespace;

public class NormalizeBool extends ExprReplacer<Void> {

  public static void process(Namespace aclasses, KnowledgeBase kb) {
    NormalizeBool normalizeBool = new NormalizeBool();
    normalizeBool.traverse(aclasses, null);
  }

  @Override
  protected Expression visitEqual(Equal obj, Void param) {
    obj = (Equal) super.visitEqual(obj, param);
    boolean lib = obj.getLeft() instanceof BoolValue;
    boolean rib = obj.getRight() instanceof BoolValue;
    if (lib && rib) {
      boolean lv = ((BoolValue) obj.getLeft()).isValue();
      boolean rv = ((BoolValue) obj.getRight()).isValue();
      return new BoolValue(obj.getInfo(), lv == rv);
    }
    if (lib) {
      boolean lv = ((BoolValue) obj.getLeft()).isValue();
      if (lv) {
        return obj.getRight();
      } else {
        return Inverter.INSTANCE.traverse(obj.getRight(), null);
//        return visit(new Not(obj.getInfo(), obj.getRight()), param);
      }
    }
    if (rib) {
      boolean rv = ((BoolValue) obj.getRight()).isValue();
      if (rv) {
        return obj.getLeft();
      } else {
        return Inverter.INSTANCE.traverse(obj.getLeft(), null);
//        return visit(new Not(obj.getInfo(), obj.getLeft()), param);
      }
    }
    return obj;
  }

  @Override
  protected Expression visitNot(Not obj, Void param) {
    return Inverter.INSTANCE.traverse(obj.getExpr(), param);
  }

  @Override
  protected Expression visitAnd(And obj, Void param) {
    obj = (And) super.visitAnd(obj, param);
    boolean lib = obj.getLeft() instanceof BoolValue;
    boolean rib = obj.getRight() instanceof BoolValue;
    if (lib && rib) {
      //TODO check
      boolean lv = ((BoolValue) obj.getLeft()).isValue();
      boolean rv = ((BoolValue) obj.getRight()).isValue();
      return new BoolValue(obj.getInfo(), lv && rv);
    }
    if (lib) {
      //TODO check
      boolean lv = ((BoolValue) obj.getLeft()).isValue();
      if (lv) {
        return obj.getRight();
      } else {
        return new BoolValue(obj.getInfo(), false);
      }
    }
    if (rib) {
      //TODO check
      boolean rv = ((BoolValue) obj.getRight()).isValue();
      if (rv) {
        return obj.getLeft();
      } else {
        return new BoolValue(obj.getInfo(), false);
      }
    }
    return obj;
  }

  @Override
  protected Expression visitNotequal(Notequal obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Expression visitOr(Or obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

}

class Inverter extends NullTraverser<Expression, Void> {
  static final public Inverter INSTANCE = new Inverter();
  
  @Override
  protected Expression visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Expression visitEqual(Equal obj, Void param) {
    return new Notequal(obj.getInfo(), obj.getLeft(), obj.getRight());
  }

}