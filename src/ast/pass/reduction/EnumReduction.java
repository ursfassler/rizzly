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
import ast.data.Namespace;
import ast.data.Range;
import ast.data.expression.value.NumberValue;
import ast.data.reference.LinkedAnchor;
import ast.data.reference.OffsetReference;
import ast.data.reference.RefName;
import ast.data.type.TypeRefFactory;
import ast.data.type.base.EnumElement;
import ast.data.type.base.EnumType;
import ast.data.type.base.RangeType;
import ast.data.variable.GlobalConstant;
import ast.dispatcher.DfsTraverser;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.repository.manipulator.TypeRepo;
import ast.repository.query.NameFilter;
import ast.repository.query.TypeFilter;

public class EnumReduction implements AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    TypeRepo kbi = new TypeRepo(kb);

    Map<EnumType, RangeType> typeMap = new HashMap<EnumType, RangeType>();
    Map<EnumElement, GlobalConstant> elemMap = new HashMap<EnumElement, GlobalConstant>();

    for (EnumType et : TypeFilter.select(ast.children, EnumType.class)) {

      BigInteger idx = BigInteger.ZERO;
      for (EnumElement elem : et.element) {
        RangeType rt = kbi.getRangeType(new Range(idx, idx));

        String name = et.getName() + Designator.NAME_SEP + elem.getName();
        GlobalConstant val = new GlobalConstant(name, TypeRefFactory.create(rt), new NumberValue(idx));
        val.metadata().add(elem.metadata());
        ast.children.add(val);
        elemMap.put(elem, val);

        idx = idx.add(BigInteger.ONE);
      }

      RangeType rt = kbi.getRangeType(et.element.size());
      typeMap.put(et, rt);
    }

    EnumReduce reduce = new EnumReduce(typeMap, elemMap);
    reduce.traverse(ast, null);
  }

}

class EnumReduce extends DfsTraverser<Void, Void> {
  final private Map<EnumType, RangeType> typeMap;
  final private Map<EnumElement, GlobalConstant> elemMap;

  public EnumReduce(Map<EnumType, RangeType> typeMap, Map<EnumElement, GlobalConstant> elemMap) {
    super();
    this.typeMap = typeMap;
    this.elemMap = elemMap;
  }

  @Override
  protected Void visitOffsetReference(OffsetReference obj, Void param) {
    LinkedAnchor anchor = (LinkedAnchor) obj.getAnchor();

    if (typeMap.containsKey(anchor.getLink())) {
      if (!obj.getOffset().isEmpty() && (obj.getOffset().get(0) instanceof RefName)) {
        // replace a link to EnumType.EnumName with a link to the corresponding
        // constant
        assert (obj.getOffset().size() == 1);
        String elemName = ((RefName) obj.getOffset().get(0)).name;
        EnumType ent = (EnumType) anchor.getLink();
        EnumElement elem = NameFilter.select(ent.element, elemName);
        assert (elem != null);
        assert (elemMap.containsKey(elem));
        obj.getOffset().clear();
        anchor.setLink(elemMap.get(elem));
      }
    }

    super.visitOffsetReference(obj, param);

    // link to type
    if (typeMap.containsKey(anchor.getLink())) {
      anchor.setLink(typeMap.get(anchor.getLink()));
    }
    // direct link to enum element (internal reduction)
    if (elemMap.containsKey(anchor.getLink())) {
      anchor.setLink(elemMap.get(anchor.getLink()));
    }

    return null;
  }

}
