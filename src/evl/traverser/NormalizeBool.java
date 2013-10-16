package evl.traverser;

import evl.Evl;
import evl.NullTraverser;
import evl.expression.BoolValue;
import evl.expression.Expression;
import evl.expression.binop.And;
import evl.expression.binop.Equal;
import evl.expression.binop.Greater;
import evl.expression.binop.Greaterequal;
import evl.expression.binop.Less;
import evl.expression.binop.Lessequal;
import evl.expression.binop.Notequal;
import evl.expression.binop.Or;
import evl.expression.reference.Reference;
import evl.expression.unop.Not;
import evl.knowledge.KnowledgeBase;
import evl.other.Namespace;

public class NormalizeBool extends ExprReplacer<Void> {

  public static void process(Namespace evl, KnowledgeBase kb) {
    NormalizeBool normalizeBool = new NormalizeBool();
    normalizeBool.traverse(evl, null);
  }

  public static Expression process(Expression evl, KnowledgeBase kb) {
    NormalizeBool normalizeBool = new NormalizeBool();
    return normalizeBool.traverse(evl, null);
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
      }
    }
    if (rib) {
      boolean rv = ((BoolValue) obj.getRight()).isValue();
      if (rv) {
        return obj.getLeft();
      } else {
        return Inverter.INSTANCE.traverse(obj.getLeft(), null);
      }
    }
    return obj;
  }

  @Override
  protected Expression visitNotequal(Notequal obj, Void param) {
    obj = (Notequal) super.visitNotequal(obj, param);
    boolean lib = obj.getLeft() instanceof BoolValue;
    boolean rib = obj.getRight() instanceof BoolValue;
    if (lib && rib) {
      boolean lv = ((BoolValue) obj.getLeft()).isValue();
      boolean rv = ((BoolValue) obj.getRight()).isValue();
      return new BoolValue(obj.getInfo(), lv != rv);
    }
    if (lib) {
      boolean lv = ((BoolValue) obj.getLeft()).isValue();
      if (lv) {
        return Inverter.INSTANCE.traverse(obj.getRight(), null);
      } else {
        return obj.getRight();
      }
    }
    if (rib) {
      boolean rv = ((BoolValue) obj.getRight()).isValue();
      if (rv) {
        return Inverter.INSTANCE.traverse(obj.getLeft(), null);
      } else {
        return obj.getLeft();
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
  protected Expression visitOr(Or obj, Void param) {
    obj = (Or) super.visitOr(obj, param);
    boolean lib = obj.getLeft() instanceof BoolValue;
    boolean rib = obj.getRight() instanceof BoolValue;
    if (lib && rib) {
      //TODO check
      boolean lv = ((BoolValue) obj.getLeft()).isValue();
      boolean rv = ((BoolValue) obj.getRight()).isValue();
      return new BoolValue(obj.getInfo(), lv || rv);
    }
    if (lib) {
      //TODO check
      boolean lv = ((BoolValue) obj.getLeft()).isValue();
      if (lv) {
        return new BoolValue(obj.getInfo(), true);
      } else {
        return obj.getRight();
      }
    }
    if (rib) {
      //TODO check
      boolean rv = ((BoolValue) obj.getRight()).isValue();
      if (rv) {
        return new BoolValue(obj.getInfo(), true);
      } else {
        return obj.getLeft();
      }
    }
    return obj;
  }

}

class Inverter extends NullTraverser<Expression, Void> {
  static final public Inverter INSTANCE = new Inverter();
  
  @Override
  protected Expression visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Expression visitReference(Reference obj, Void param) {
    return new Not(obj.getInfo(), obj);   //TODO follow link?
  }

  @Override
  protected Expression visitEqual(Equal obj, Void param) {
    return new Notequal(obj.getInfo(), obj.getLeft(), obj.getRight());
  }

  @Override
  protected Expression visitNotequal(Notequal obj, Void param) {
    return new Equal(obj.getInfo(), obj.getLeft(), obj.getRight());
  }

  @Override
  protected Expression visitGreater(Greater obj, Void param) {
    return new Lessequal(obj.getInfo(), obj.getLeft(), obj.getRight());
  }

  @Override
  protected Expression visitGreaterequal(Greaterequal obj, Void param) {
    return new Less(obj.getInfo(), obj.getLeft(), obj.getRight());
  }

  @Override
  protected Expression visitLess(Less obj, Void param) {
    return new Greaterequal(obj.getInfo(), obj.getLeft(), obj.getRight());
  }

  @Override
  protected Expression visitLessequal(Lessequal obj, Void param) {
    return new Greater(obj.getInfo(), obj.getLeft(), obj.getRight());
  }

  @Override
  protected Expression visitNot(Not obj, Void param) {
    return obj.getExpr();
  }

}
