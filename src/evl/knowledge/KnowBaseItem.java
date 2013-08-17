package evl.knowledge;

import java.math.BigInteger;

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.expression.reference.Reference;
import evl.other.Named;
import evl.type.Type;
import evl.type.base.Array;
import evl.type.base.BooleanType;
import evl.type.base.Range;
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

  private Named findItem(String name) {
    return kb.getRoot().find(name);
  }

  private void addItem(Named item) {
    assert (findItem(item.getName()) == null);
    kb.getRoot().add(item);

  }

  // --------------------------------------------------------------------------

  public <T extends Named> T get(Class<T> kind, String name) {
    Named item = findItem(name);
    if (item == null) {
      RError.err(ErrorType.Fatal, "Base item not found: " + name);
      return null;
    }
    if (!kind.isAssignableFrom(item.getClass())) {
      RError.err(ErrorType.Fatal, "Base item is of wrong type. Expected: " + kind.getCanonicalName() + "; got: " + item.getClass().getCanonicalName());
      return null;
    }
    return (T) item;
  }

  public VoidType getVoidType() {
    VoidType ret = (VoidType) findItem(VoidType.NAME);
    if (ret == null) {
      ret = new VoidType();
      addItem(ret);
    }
    return ret;
  }

  /**
   * Returns R{0,count-1}
   *
   * @param count
   * @return
   */
  public Range getRangeType(int count) {
    BigInteger low = BigInteger.ZERO;
    BigInteger high = BigInteger.valueOf(count - 1);
    return getRangeType(low, high);
  }

  /**
   * Returns R{low,high}
   *
   * @param count
   * @return
   */
  public Range getRangeType(BigInteger low, BigInteger high) {
    Range ret = (Range) findItem(Range.makeName(low, high));
    if (ret == null) {
      ret = new Range(low, high);
      addItem(ret);
    }
    return ret;
  }

  public Array getArray(int size, Type type) {
    Array ret = (Array) findItem(Array.makeName(size, type.getName()));
    if (ret == null) {
      ret = new Array(size, new Reference(new ElementInfo(), type));
      addItem(ret);
    }
    return ret;
  }

  public StringType getStringType() {
    StringType ret = (StringType) findItem(StringType.NAME);
    if (ret == null) {
      ret = new StringType();
      addItem(ret);
    }
    return ret;
  }

  public BooleanType getBooleanType() {
    BooleanType ret = (BooleanType) findItem(BooleanType.NAME);
    if (ret == null) {
      ret = new BooleanType();
      addItem(ret);
    }
    return ret;
  }

  public IntegerType getIntegerType() {
    IntegerType ret = (IntegerType) findItem(IntegerType.NAME);
    if (ret == null) {
      ret = new IntegerType();
      addItem(ret);
    }
    return ret;
  }

  public NaturalType getNaturalType() {
    NaturalType ret = (NaturalType) findItem(NaturalType.NAME);
    if (ret == null) {
      ret = new NaturalType();
      addItem(ret);
    }
    return ret;
  }
}