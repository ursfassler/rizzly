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
import evl.knowledge.KnowType;
import evl.knowledge.KnowledgeBase;
import evl.other.Namespace;
import evl.statement.CaseStmt;
import evl.type.Type;
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
  private final static String ENUM_PREFIX = Designator.NAME_SEP + "enum";
  private final static ElementInfo info = new ElementInfo();

  private Map<UnionType, EnumType> union2enum = new HashMap<UnionType, EnumType>();
  final private KnowChild kc;
  final private KnowType kt;

  public static void process(Namespace aclasses, KnowledgeBase kb) {
    ReduceUnion inst = new ReduceUnion(kb);
    inst.traverse(aclasses, null);
    aclasses.addAll(inst.union2enum.values());
  }

  public ReduceUnion(KnowledgeBase kb) {
    super();
    this.kc = kb.getEntry(KnowChild.class);
    this.kt = kb.getEntry(KnowType.class);
  }

  @Override
  protected Expression visitUnionType(UnionType obj, Void param) {
    assert (!union2enum.containsKey(obj));

    EnumType et = new EnumType(info, ENUM_PREFIX + Designator.NAME_SEP + obj.getName());

    int k = 0;
    for (NamedElement elem : obj.getElement()) {
      EnumElement ee = new EnumElement(info, elem.getName(), new TypeRef(info, et), new Number(info, BigInteger.valueOf(k)));
      et.getElement().add(new EnumDefRef(new ElementInfo(), ee));
      k++;
    }

    obj.getTag().setType(new TypeRef(info, et));

    union2enum.put(obj, et);
    return null;
  }

  @Override
  protected Expression visitCaseStmt(CaseStmt obj, Void param) {
    Type et = kt.get(obj.getCondition());
    if (et instanceof UnionType) {
      assert (obj.getCondition() instanceof Reference);
      Reference cond = (Reference) obj.getCondition();
      cond.getOffset().add(new RefName(info, ((UnionType) et).getTag().getName()));
    }
    return super.visitCaseStmt(obj, param);
  }

  @Override
  protected Expression visitReference(Reference obj, Void param) {
    obj = (Reference) super.visitReference(obj, param);
    if (obj.getLink() instanceof UnionType) {
      UnionType ut = (UnionType) obj.getLink();
      assert (union2enum.containsKey(ut));
      assert (obj.getOffset().size() == 1);
      assert (obj.getOffset().get(0) instanceof RefName);
      EnumType et = union2enum.get(ut);
      Evl ev = kc.get(et, ((RefName) obj.getOffset().get(0)).getName(), ut.getInfo());
      assert (ev instanceof EnumElement);
      return new Reference(obj.getInfo(), (EnumElement) ev);
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

    left = new Reference(left.getInfo(), left.getLink(), new RefName(info, ((UnionType) ut).getTag().getName()));

    return new Equal(obj.getInfo(), left, obj.getRight());
  }

}
