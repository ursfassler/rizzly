package evl.traverser;

import util.Range;

import common.ElementInfo;

import evl.Evl;
import evl.expression.Expression;
import evl.expression.TypeCast;
import evl.expression.binop.ArithmeticOp;
import evl.expression.binop.Relation;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowType;
import evl.knowledge.KnowledgeBase;
import evl.type.Type;
import evl.type.TypeRef;
import evl.type.base.RangeType;

/**
 * Insert typecasts in expressions to fit the destination type
 * 
 * @author urs
 * 
 */
public class RangeConverter extends ExprReplacer<Void> {
  private static final ElementInfo info = new ElementInfo();
  private KnowBaseItem kbi;
  private KnowType kt;

  public RangeConverter(KnowledgeBase kb) {
    super();
    this.kbi = kb.getEntry(KnowBaseItem.class);
    this.kt = kb.getEntry(KnowType.class);
  }

  public static void process(Evl obj, KnowledgeBase kb) {
    RangeConverter changer = new RangeConverter(kb);
    changer.traverse(obj, null);
  }

  private boolean isNotRange(Type t1, Type t2) {
    if (!(t1 instanceof RangeType)) {
      // TODO implement it nicer
      assert (t1 == t2);
      return true;
    } else {
      assert (t2 instanceof RangeType);
    }
    return false;
  }

  private Expression replaceIfNeeded(Expression val, RangeType valType, RangeType commonType) {
    if (Range.leftIsSmallerEqual(valType.getNumbers(), commonType.getNumbers())) {
      val = new TypeCast(info, new TypeRef(info, commonType), val);
    }
    return val;
  }

  @Override
  protected Expression visitArithmeticOp(ArithmeticOp obj, Void param) {
    obj = (ArithmeticOp) super.visitArithmeticOp(obj, param);
    
    Type lb = kt.get(obj.getLeft());
    Type rb = kt.get(obj.getRight());
    
    if( isNotRange(lb,rb) ){
      return obj;
    }
    
    RangeType lt = (RangeType) lb;
    RangeType rt = (RangeType) rb;
    RangeType dt = (RangeType) kt.get(obj);

    Range it = Range.grow(lt.getNumbers(), rt.getNumbers());
    Range btr = Range.grow(it, dt.getNumbers());
    RangeType bt = kbi.getNumsetType(btr); // add bt to program

    obj.setLeft(replaceIfNeeded(obj.getLeft(), lt, bt));
    obj.setRight(replaceIfNeeded(obj.getRight(), rt, bt));

    // TODO reimplement downcast, but with different function than conversion
    // if (RangeType.isBigger(bt, dt)) {
    // SsaVariable irv = new SsaVariable(NameFactory.getNew(), new TypeRef(bt));
    // TypeCast rex = new TypeCast(obj.getVariable(), new VarRefSimple(irv));
    // obj.setVariable(irv);
    // ret.add(rex);
    // }
    return obj;
  }

  @Override
  protected Expression visitRelation(Relation obj, Void param) {
    obj = (Relation) super.visitRelation(obj, param);
    
    Type lb = kt.get(obj.getLeft());
    Type rb = kt.get(obj.getRight());
    
    if( isNotRange(lb,rb) ){
      return obj;
    }
    
    RangeType lt = (RangeType) lb;
    RangeType rt = (RangeType) rb;

    Range it = Range.grow(lt.getNumbers(), rt.getNumbers());
    RangeType bt = kbi.getNumsetType(it); // add bt to program

    obj.setLeft(replaceIfNeeded(obj.getLeft(), lt, bt));
    obj.setRight(replaceIfNeeded(obj.getRight(), rt, bt));

    return obj;
  }

}
