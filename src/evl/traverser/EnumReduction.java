package evl.traverser;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import util.Range;

import common.Designator;
import common.ElementInfo;

import evl.DefTraverser;
import evl.expression.Number;
import evl.expression.reference.BaseRef;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.expression.reference.SimpleRef;
import evl.other.RizzlyProgram;
import evl.type.Type;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.type.base.RangeType;
import evl.variable.ConstGlobal;

public class EnumReduction {

  public static void process(RizzlyProgram prg) {
    Map<EnumType, RangeType> typeMap = new HashMap<EnumType, RangeType>();
    Map<EnumElement, ConstGlobal> elemMap = new HashMap<EnumElement, ConstGlobal>();

    for (EnumType et : prg.getType().getItems(EnumType.class)) {

      BigInteger idx = BigInteger.ZERO;
      for (EnumElement elem : et.getElement()) {
        RangeType rt = makeRangeType(prg, new Range(idx, idx));

        String name = et.getName() + Designator.NAME_SEP + elem.getName();
        ConstGlobal val = new ConstGlobal(elem.getInfo(), name, new SimpleRef<Type>(ElementInfo.NO, rt), new Number(ElementInfo.NO, idx));
        prg.getConstant().add(val);
        elemMap.put(elem, val);

        idx = idx.add(BigInteger.ONE);
      }

      RangeType rt = makeRangeType(prg, new Range(BigInteger.ZERO, BigInteger.valueOf(et.getElement().size() - 1)));
      typeMap.put(et, rt);
    }

    EnumReduce reduce = new EnumReduce(typeMap, elemMap);
    reduce.traverse(prg, null);
  }

  private static RangeType makeRangeType(RizzlyProgram prg, Range range) {
    String name = RangeType.makeName(range);
    RangeType rt = (RangeType) prg.getType().find(name);
    if (rt == null) {
      rt = new RangeType(range);
      prg.getType().add(rt);
    }
    return rt;
  }

}

class EnumReduce extends DefTraverser<Void, Void> {
  final private Map<EnumType, RangeType> typeMap;
  final private Map<EnumElement, ConstGlobal> elemMap;

  public EnumReduce(Map<EnumType, RangeType> typeMap, Map<EnumElement, ConstGlobal> elemMap) {
    super();
    this.typeMap = typeMap;
    this.elemMap = elemMap;
  }

  @Override
  protected Void visitReference(Reference obj, Void param) {
    if (typeMap.containsKey(obj.getLink())) {
      if (!obj.getOffset().isEmpty() && (obj.getOffset().get(0) instanceof RefName)) {
        // replace a link to EnumType.EnumName with a link to the corresponding constant
        assert (obj.getOffset().size() == 1);
        String elemName = ((RefName) obj.getOffset().get(0)).getName();
        EnumType ent = (EnumType) obj.getLink();
        EnumElement elem = ent.getElement().find(elemName);
        assert (elem != null);
        assert (elemMap.containsKey(elem));
        obj.getOffset().clear();
        obj.setLink(elemMap.get(elem));
      }
    }
    return super.visitReference(obj, param);
  }

  @Override
  protected Void visitBaseRef(BaseRef obj, Void param) {
    super.visitBaseRef(obj, param);
    // link to type
    if (typeMap.containsKey(obj.getLink())) {
      obj.setLink(typeMap.get(obj.getLink()));
    }
    // direct link to enum element (internal reduction)
    if (elemMap.containsKey(obj.getLink())) {
      obj.setLink(elemMap.get(obj.getLink()));
    }
    return null;
  }

}
