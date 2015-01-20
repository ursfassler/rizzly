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

import java.util.HashMap;
import java.util.Map;

import pass.EvlPass;

import common.Designator;
import common.ElementInfo;

import evl.DefTraverser;
import evl.Evl;
import evl.expression.Expression;
import evl.expression.binop.Equal;
import evl.expression.binop.Is;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.knowledge.KnowType;
import evl.knowledge.KnowledgeBase;
import evl.other.Namespace;
import evl.statement.CaseStmt;
import evl.traverser.ExprReplacer;
import evl.type.Type;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.type.composed.NamedElement;
import evl.type.composed.UnionType;

//TODO documentation
// DONE introduce enum E for union U
// DONE add element e of type E to U
// TODO replace access to union instance u in ".. is .." and "case" with access to u.e
// TODO replace also reference to element x of U to x' in E
// DONE replace "is" with "=="
public class ReduceUnion extends EvlPass {
  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    ReduceUnionWorker inst = new ReduceUnionWorker(kb);

    inst.traverse(evl, null);
    // FIXME only provide namespace as evl?
    evl.addAll(inst.getUnion2enum().values());
  }

}

class ReduceUnionWorker extends ExprReplacer<Void> {
  final private Map<UnionType, EnumType> union2enum = new HashMap<UnionType, EnumType>();
  final private KnowType kt;

  public ReduceUnionWorker(KnowledgeBase kb) {
    super();
    this.kt = kb.getEntry(KnowType.class);
  }

  @Override
  public Expression traverse(Evl obj, Void param) {
    Uni2Enum enu2uni = new Uni2Enum();
    enu2uni.traverse(obj, getUnion2enum());
    return super.traverse(obj, param);
  }

  @Override
  protected Expression visitCaseStmt(CaseStmt obj, Void param) {
    Type et = kt.get(obj.getCondition());
    if (et instanceof UnionType) {
      assert (obj.getCondition() instanceof Reference);
      Reference cond = (Reference) obj.getCondition();
      cond.getOffset().add(new RefName(ElementInfo.NO, ((UnionType) et).getTag().getName()));
    }
    return super.visitCaseStmt(obj, param);
  }

  @Override
  protected Expression visitReference(Reference obj, Void param) {
    obj = (Reference) super.visitReference(obj, param);
    if (obj.getLink() instanceof UnionType) {
      UnionType ut = (UnionType) obj.getLink();
      assert (getUnion2enum().containsKey(ut));
      assert (obj.getOffset().size() == 1);
      assert (obj.getOffset().get(0) instanceof RefName);
      EnumType et = getUnion2enum().get(ut);
      String ev = ((RefName) obj.getOffset().get(0)).getName();
      assert (et.getElement().find(ev) != null);
      return new Reference(obj.getInfo(), et, new RefName(ElementInfo.NO, ev));
    }
    return obj;
  }

  @Override
  protected Expression visitIs(Is obj, Void param) {
    super.visitIs(obj, param);
    Reference left = (Reference) visit(obj.getLeft(), null);

    assert (left.getOffset().isEmpty());

    Type ut = kt.get(left);
    assert (ut instanceof UnionType);

    left = new Reference(left.getInfo(), left.getLink(), new RefName(ElementInfo.NO, ((UnionType) ut).getTag().getName()));

    return new Equal(obj.getInfo(), left, obj.getRight());
  }

  public Map<UnionType, EnumType> getUnion2enum() {
    return union2enum;
  }

}

class Uni2Enum extends DefTraverser<Void, Map<UnionType, EnumType>> {
  private final static String ENUM_PREFIX = Designator.NAME_SEP + "enum";

  @Override
  protected Void visitUnionType(UnionType obj, Map<UnionType, EnumType> param) {
    assert (!param.containsKey(obj));

    EnumType et = new EnumType(ElementInfo.NO, ENUM_PREFIX + Designator.NAME_SEP + obj.getName());

    for (NamedElement elem : obj.getElement()) {
      EnumElement ee = new EnumElement(ElementInfo.NO, elem.getName());
      et.getElement().add(ee);
    }

    obj.getTag().getRef().setLink(et);

    param.put(obj, et);
    return null;
  }

}
