package evl.knowledge;

import java.math.BigInteger;
import java.util.List;

import util.Range;

import common.ElementInfo;

import evl.Evl;
import evl.NullTraverser;
import evl.expression.reference.SimpleRef;
import evl.other.EvlList;
import evl.type.Type;
import evl.type.base.ArrayType;
import evl.type.base.BooleanType;
import evl.type.base.RangeType;
import evl.type.base.StringType;
import evl.type.special.IntegerType;
import evl.type.special.NaturalType;
import evl.type.special.VoidType;

public class KnowBaseItem extends KnowledgeEntry {

  private KnowledgeBase kb;

  @Override
  public void init(KnowledgeBase kb) {
    this.kb = kb;
  }

  public <T extends Evl> List<T> findItem(Class<T> kind) {
    return kb.getRoot().getItems(kind, false);
  }

  public Evl findItem(String name) {
    return kb.getRoot().getChildren().find(name);
  }

  @Deprecated
  public void addItem(Evl item) {
    kb.getRoot().getChildren().add(item);
  }

  private <T extends Type> T getPlainType(T type) {
    List<? extends Type> types = findItem(type.getClass());
    if (types.isEmpty()) {
      addItem(type);
      return type;
    } else {
      assert (types.size() == 1);
      return (T) types.get(0);
    }
  }

  public VoidType getVoidType() {
    return getPlainType(new VoidType());
  }

  /**
   * Returns R{0,count-1}
   * 
   * @param count
   * @return
   */
  public RangeType getRangeType(int count) {
    BigInteger low = BigInteger.ZERO;
    BigInteger high = BigInteger.valueOf(count - 1);
    return getNumsetType(new Range(low, high));
  }

  public RangeType getNumsetType(Range range) {
    EvlList<RangeType> items = kb.getRoot().getChildren().getItems(RangeType.class);
    for (RangeType itr : items) {
      if (itr.getNumbers().equals(range)) {
        return itr;
      }
    }
    RangeType ret = new RangeType(range);
    kb.getRoot().getChildren().add(ret);
    return ret;
  }

  public ArrayType getArray(BigInteger size, Type type) {
    EvlList<ArrayType> items = kb.getRoot().getChildren().getItems(ArrayType.class);
    for (ArrayType itr : items) {
      if (itr.getSize().equals(size) && itr.getType().getLink().equals(type)) {
        return itr;
      }
    }

    ArrayType ret = new ArrayType(size, new SimpleRef<Type>(ElementInfo.NO, type));
    kb.getRoot().getChildren().add(ret);
    return ret;
  }

  public StringType getStringType() {
    return getPlainType(new StringType());
  }

  public BooleanType getBooleanType() {
    return getPlainType(new BooleanType());
  }

  public IntegerType getIntegerType() {
    return getPlainType(new IntegerType());
  }

  public NaturalType getNaturalType() {
    return getPlainType(new NaturalType());
  }

  public Type getType(Type ct) {
    KnowBaseItemTypeFinder finder = new KnowBaseItemTypeFinder();
    return finder.traverse(ct, this);
  }

}

class KnowBaseItemTypeFinder extends NullTraverser<Type, KnowBaseItem> {

  @Override
  protected Type visitDefault(Evl obj, KnowBaseItem param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Type visitArrayType(ArrayType obj, KnowBaseItem param) {
    return param.getArray(obj.getSize(), obj.getType().getLink());
  }

  @Override
  protected Type visitRangeType(RangeType obj, KnowBaseItem param) {
    return param.getNumsetType(obj.getNumbers());
  }

}
