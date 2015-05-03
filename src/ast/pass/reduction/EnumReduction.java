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

package ast.pass.reduction;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import ast.Designator;
import ast.ElementInfo;
import ast.data.Namespace;
import ast.data.Range;
import ast.data.expression.Number;
import ast.data.expression.reference.BaseRef;
import ast.data.expression.reference.RefName;
import ast.data.expression.reference.Reference;
import ast.data.expression.reference.SimpleRef;
import ast.data.type.Type;
import ast.data.type.base.EnumElement;
import ast.data.type.base.EnumType;
import ast.data.type.base.RangeType;
import ast.data.variable.ConstGlobal;
import ast.knowledge.KnowBaseItem;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.specification.TypeFilter;
import ast.traverser.DefTraverser;

public class EnumReduction extends AstPass {
  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    KnowBaseItem kbi = kb.getEntry(KnowBaseItem.class);

    Map<EnumType, RangeType> typeMap = new HashMap<EnumType, RangeType>();
    Map<EnumElement, ConstGlobal> elemMap = new HashMap<EnumElement, ConstGlobal>();

    for (EnumType et : TypeFilter.select(ast.children, EnumType.class)) {

      BigInteger idx = BigInteger.ZERO;
      for (EnumElement elem : et.getElement()) {
        RangeType rt = kbi.getRangeType(new Range(idx, idx));

        String name = et.name + Designator.NAME_SEP + elem.name;
        ConstGlobal val = new ConstGlobal(elem.getInfo(), name, new SimpleRef<Type>(ElementInfo.NO, rt), new Number(ElementInfo.NO, idx));
        ast.children.add(val);
        elemMap.put(elem, val);

        idx = idx.add(BigInteger.ONE);
      }

      RangeType rt = kbi.getRangeType(et.getElement().size());
      typeMap.put(et, rt);
    }

    EnumReduce reduce = new EnumReduce(typeMap, elemMap);
    reduce.traverse(ast, null);
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
        // replace a link to EnumType.EnumName with a link to the corresponding
        // constant
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
