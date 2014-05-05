package evl.traverser;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import common.Designator;
import common.ElementInfo;

import evl.Evl;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.binop.Equal;
import evl.expression.binop.Is;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.knowledge.KnowChild;
import evl.knowledge.KnowledgeBase;
import evl.other.Namespace;
import evl.type.TypeRef;
import evl.type.base.EnumDefRef;
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
public class ReduceUnion extends ExprReplacer<Void> {
  private final static String PREFIX = Designator.NAME_SEP + "enum";
  private final static String SELECTOR = Designator.NAME_SEP + "s";
  private final static ElementInfo info = new ElementInfo();

  private Map<UnionType, EnumType> union2enum = new HashMap<UnionType, EnumType>();
  final private KnowChild kc;

  public static void process(Namespace aclasses, KnowledgeBase kb) {
    ReduceUnion inst = new ReduceUnion(kb);
    inst.traverse(aclasses, null);
    aclasses.addAll(inst.union2enum.values());
  }

  public ReduceUnion(KnowledgeBase kb) {
    super();
    this.kc = kb.getEntry(KnowChild.class);
  }

  @Override
  protected Expression visitUnionType(UnionType obj, Void param) {
    assert (!union2enum.containsKey(obj));

    EnumType et = new EnumType(info, PREFIX + Designator.NAME_SEP + obj.getName());

    int k = 0;
    for (NamedElement elem : obj.getElement()) {
      EnumElement ee = new EnumElement(info, elem.getName(), new TypeRef(info, et), new Number(info, BigInteger.valueOf(k)));
      et.getElement().add(new EnumDefRef(new ElementInfo(), ee));
      k++;
    }

    NamedElement selector = new NamedElement(info, SELECTOR, new TypeRef(info, et));
    obj.getElement().add(selector);

    union2enum.put(obj, et);
    return null;
  }

  @Override
  protected Expression visitIs(Is obj, Void param) {
    super.visitIs(obj, param);
    Reference left = (Reference) visit(obj.getLeft(), null);
    Reference right = (Reference) visit(obj.getRight(), null);

    assert (left.getOffset().isEmpty());
    assert (right.getOffset().size() == 1);

    left.getOffset().add(new RefName(info, SELECTOR));

    UnionType ut = (UnionType) right.getLink();
    assert (union2enum.containsKey(ut));
    assert (right.getOffset().size() == 1);
    assert (right.getOffset().get(0) instanceof RefName);
    EnumType et = union2enum.get(ut);
    Evl ev = kc.get(et, ((RefName) right.getOffset().get(0)).getName(), ut.getInfo());
    assert (ev instanceof EnumElement);
    right.getOffset().clear();
    right.setLink((EnumElement) ev);

    return new Equal(info, left, right);
  }

}
