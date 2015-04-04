/**
 *  This file is part of Rizzly.
 *
 *  Rizzly is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Rizzly is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Rizzly.  If not, see <http://www.gnu.org/licenses/>.
 */

package evl.pass;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import pass.EvlPass;
import util.Range;

import common.Designator;
import common.ElementInfo;

import evl.DefTraverser;
import evl.expression.Number;
import evl.expression.reference.BaseRef;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.expression.reference.SimpleRef;
import evl.knowledge.KnowledgeBase;
import evl.other.Namespace;
import evl.type.Type;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.type.base.RangeType;
import evl.variable.ConstGlobal;

public class EnumReduction extends EvlPass {
  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    Map<EnumType, RangeType> typeMap = new HashMap<EnumType, RangeType>();
    Map<EnumElement, ConstGlobal> elemMap = new HashMap<EnumElement, ConstGlobal>();

    for (EnumType et : evl.getItems(EnumType.class, false)) {

      BigInteger idx = BigInteger.ZERO;
      for (EnumElement elem : et.getElement()) {
        RangeType rt = makeRangeType(evl, new Range(idx, idx));

        String name = et.getName() + Designator.NAME_SEP + elem.getName();
        ConstGlobal val = new ConstGlobal(elem.getInfo(), name, new SimpleRef<Type>(ElementInfo.NO, rt), new Number(ElementInfo.NO, idx));
        evl.add(val);
        elemMap.put(elem, val);

        idx = idx.add(BigInteger.ONE);
      }

      RangeType rt = makeRangeType(evl, new Range(BigInteger.ZERO, BigInteger.valueOf(et.getElement().size() - 1)));
      typeMap.put(et, rt);
    }

    EnumReduce reduce = new EnumReduce(typeMap, elemMap);
    reduce.traverse(evl, null);
  }

  private static RangeType makeRangeType(Namespace prg, Range range) {
    String name = RangeType.makeName(range);
    RangeType rt = (RangeType) prg.findItem(name);
    if (rt == null) {
      rt = new RangeType(range);
      prg.add(rt);
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
    if (typeMap.containsKey(obj.link)) {
      if (!obj.offset.isEmpty() && (obj.offset.get(0) instanceof RefName)) {
        // replace a link to EnumType.EnumName with a link to the corresponding constant
        assert (obj.offset.size() == 1);
        String elemName = ((RefName) obj.offset.get(0)).name;
        EnumType ent = (EnumType) obj.link;
        EnumElement elem = ent.getElement().find(elemName);
        assert (elem != null);
        assert (elemMap.containsKey(elem));
        obj.offset.clear();
        obj.link = elemMap.get(elem);
      }
    }
    return super.visitReference(obj, param);
  }

  @Override
  protected Void visitBaseRef(BaseRef obj, Void param) {
    super.visitBaseRef(obj, param);
    // link to type
    if (typeMap.containsKey(obj.link)) {
      obj.link = typeMap.get(obj.link);
    }
    // direct link to enum element (internal reduction)
    if (elemMap.containsKey(obj.link)) {
      obj.link = elemMap.get(obj.link);
    }
    return null;
  }

}
