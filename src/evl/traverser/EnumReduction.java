package evl.traverser;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import util.Range;

import common.Designator;
import common.ElementInfo;

import evl.copy.Relinker;
import evl.expression.Number;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowParent;
import evl.knowledge.KnowledgeBase;
import evl.other.Named;
import evl.other.Namespace;
import evl.other.RizzlyProgram;
import evl.type.TypeRef;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.type.base.RangeType;
import evl.variable.ConstGlobal;

public class EnumReduction {

  public static void process(Namespace prg, KnowledgeBase kb) {
    KnowBaseItem kbi = kb.getEntry(KnowBaseItem.class);
    KnowParent kp = kb.getEntry(KnowParent.class);
    Map<Named, Named> map = new HashMap<Named, Named>();

    for (EnumType et : ClassGetter.get(EnumType.class, prg)) {
      Namespace parent = (Namespace) kp.getParent(et);

      BigInteger idx = BigInteger.ZERO;
      for (EnumElement elem : et.getElement()) {
        RangeType rt = kbi.getNumsetType(new Range(idx, idx));

        ElementInfo info = new ElementInfo();
        ConstGlobal val = new ConstGlobal(elem.getInfo(), et.getName() + Designator.NAME_SEP + elem.getName(), new TypeRef(info, rt), new Number(info, idx));
        parent.add(val);
        map.put(elem, val);

        idx = idx.add(BigInteger.ONE);
      }

      RangeType rt = kbi.getRangeType(et.getElement().size());
      map.put(et, rt);
    }

    Relinker.relink(prg, map);
  }

  public static void process(RizzlyProgram prg) {
    Map<Named, Named> map = new HashMap<Named, Named>();

    for (EnumType et : prg.getType().getItems(EnumType.class)) {

      BigInteger idx = BigInteger.ZERO;
      for (EnumElement elem : et.getElement()) {
        RangeType rt = makeRangeType(prg, new Range(idx, idx));

        ElementInfo info = new ElementInfo();
        ConstGlobal val = new ConstGlobal(elem.getInfo(), et.getName() + Designator.NAME_SEP + elem.getName(), new TypeRef(info, rt), new Number(info, idx));
        prg.getConstant().add(val);
        map.put(elem, val);

        idx = idx.add(BigInteger.ONE);
      }

      RangeType rt = makeRangeType(prg, new Range(BigInteger.ZERO, BigInteger.valueOf(et.getElement().size())));
      map.put(et, rt);
    }

    Relinker.relink(prg, map);
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
