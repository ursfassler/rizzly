package pir.know;

import java.math.BigInteger;

import pir.NullTraverser;
import pir.Pir;
import pir.PirObject;
import pir.expression.reference.Referencable;
import pir.other.Program;
import pir.type.ArrayType;
import pir.type.BooleanType;
import pir.type.RangeType;
import pir.type.StringType;
import pir.type.Type;
import pir.type.VoidType;

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.expression.reference.Reference;

public class KnowBaseItem extends KnowledgeEntry {
  private KnowChild kc;
  private KnowledgeBase kb;
  private ItemAdder adder = new ItemAdder();

  @Override
  public void init(KnowledgeBase kb) {
    this.kb = kb;
    kc = kb.getEntry(KnowChild.class);
  }

  private Pir findItem(String name) {
    return kc.find(kb.getRoot(), name);
  }

  private void addItem(Referencable item) {
    assert (findItem(item.getName()) == null);
    adder.traverse(kb.getRoot(), item);
  }

  // --------------------------------------------------------------------------

  public <T extends Referencable> T get(Class<T> kind, String name) {
    Pir item = findItem(name);
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
  public RangeType getRangeType(int count) {
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
  public RangeType getRangeType(BigInteger low, BigInteger high) {
    RangeType ret = (RangeType) findItem(RangeType.makeName(low, high));
    if (ret == null) {
      ret = new RangeType(low, high);
      addItem(ret);
    }
    return ret;
  }

  public ArrayType getArray(BigInteger size, Type type) {
    ArrayType ret = (ArrayType) findItem(ArrayType.makeName(size, type));
    if (ret == null) {
      ret = new ArrayType(size, new Reference(new ElementInfo(), type));
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

}

class ItemAdder extends NullTraverser<Void, Referencable> {

  @Override
  protected Void doDefault(PirObject obj, Referencable param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitProgram(Program obj, Referencable param) {
    if (param instanceof Type) {
      obj.getType().add((Type) param);
    } else {
      throw new RuntimeException("not yet implemented: " + param.getClass().getCanonicalName());
    }
    return null;
  }

}
