package evl.knowledge;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import util.Range;

import common.Designator;
import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.other.Named;
import evl.other.Namespace;
import evl.type.Type;
import evl.type.TypeRef;
import evl.type.base.ArrayType;
import evl.type.base.BooleanType;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.type.base.NumSet;
import evl.type.base.StringType;
import evl.type.special.IntegerType;
import evl.type.special.NaturalType;
import evl.type.special.PointerType;
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
  public NumSet getRangeType(int count) {
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
  public NumSet getRangeType(BigInteger low, BigInteger high) {
    NumSet ret = (NumSet) findItem(NumSet.makeName(low, high));
    if (ret == null) {
      ret = new NumSet(low, high);
      addItem(ret);
    }
    return ret;
  }

  public NumSet getNumsetType(ArrayList<Range> ranges) {
    NumSet ret = (NumSet) findItem(NumSet.makeName(ranges));
    if (ret == null) {
      ret = new NumSet(ranges);
      addItem(ret);
    }
    return ret;
  }

  public ArrayType getArray(BigInteger size, Type type) {
    TypeRef ref = new TypeRef(new ElementInfo(), type);
    ArrayType ret = (ArrayType) findItem(ArrayType.makeName(size, ref));
    if (ret == null) {
      ret = new ArrayType(size, ref);
      addItem(ret);
    }
    return ret;
  }

  public PointerType getPointerType(Type type) {
    TypeRef ref = new TypeRef(new ElementInfo(), type);
    PointerType ret = (PointerType) findItem(PointerType.makeName(ref));
    if (ret == null) {
      ret = new PointerType(ref);
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

  public EnumType getEnumType(EnumType supertype, List<EnumElement> elements) {
    KnowPath kp = kb.getEntry(KnowPath.class);
    Designator path = kp.get(supertype);
    Namespace parent = kb.getRoot().forceChildPath(path.toList());
    assert (parent.find(supertype.getName()) != null);

    String subName = supertype.makeSubtypeName(elements);

    EnumType type = (EnumType) parent.find(subName);

    if (type == null) {
      type = new EnumType(new ElementInfo(), subName, elements);
      parent.add(type);
    }

    return type;
  }

}
