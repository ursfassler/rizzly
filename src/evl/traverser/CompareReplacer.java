package evl.traverser;

import java.util.Iterator;

import util.Pair;

import common.ElementInfo;

import evl.Evl;
import evl.NullTraverser;
import evl.copy.Copy;
import evl.expression.BoolValue;
import evl.expression.Expression;
import evl.expression.binop.Equal;
import evl.expression.binop.LogicAnd;
import evl.expression.binop.Notequal;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.expression.unop.LogicNot;
import evl.knowledge.KnowType;
import evl.knowledge.KnowledgeBase;
import evl.type.Type;
import evl.type.base.ArrayType;
import evl.type.base.BooleanType;
import evl.type.base.EnumType;
import evl.type.base.RangeType;
import evl.type.composed.NamedElement;
import evl.type.composed.RecordType;
import evl.type.composed.UnionType;

public class CompareReplacer extends ExprReplacer<Void> {

  private final KnowledgeBase kb;

  public CompareReplacer(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  public static void process(Evl obj, KnowledgeBase kb) {
    CompareReplacer replacer = new CompareReplacer(kb);
    replacer.traverse(obj, null);
  }

  @Override
  protected Expression visitEqual(Equal obj, Void param) {
    obj = (Equal) super.visitEqual(obj, param);
    return MakeCompareFunction.makeCmpExpr(obj.getLeft(), obj.getRight(), kb);
  }

  @Override
  protected Expression visitNotequal(Notequal obj, Void param) {
    obj = (Notequal) super.visitNotequal(obj, param);
    return new LogicNot(obj.getInfo(), MakeCompareFunction.makeCmpExpr(obj.getLeft(), obj.getRight(), kb));
  }

}

class MakeCompareFunction extends NullTraverser<Expression, Pair<Expression, Expression>> {

  private static final ElementInfo info = new ElementInfo();
  private final KnowType kt;

  public MakeCompareFunction(KnowledgeBase kb) {
    super();
    kt = kb.getEntry(KnowType.class);
  }

  static public Expression makeCmpExpr(Expression left, Expression right, KnowledgeBase kb) {
    MakeCompareFunction mcf = new MakeCompareFunction(kb);
    return mcf.makeCmpExpr(left, right);

  }

  private Expression makeCmpExpr(Expression left, Expression right) {
    Type lt = kt.get(left);
    Type rt = kt.get(right);

    assert (lt == rt);

    return traverse(lt, new Pair<Expression, Expression>(left, right));

  }

  static public String getCompFuncName(Type type) {
    return "_equals_" + type.getName();
  }

  @Override
  protected Expression visitDefault(Evl obj, Pair<Expression, Expression> param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Expression visitEnumType(EnumType obj, Pair<Expression, Expression> param) {
    return new Equal(info, param.first, param.second);
  }

  @Override
  protected Expression visitBooleanType(BooleanType obj, Pair<Expression, Expression> param) {
    return new Equal(info, param.first, param.second);
  }

  @Override
  protected Expression visitRangeType(RangeType obj, Pair<Expression, Expression> param) {
    return new Equal(info, param.first, param.second);
  }

  @Override
  protected Expression visitRecordType(RecordType obj, Pair<Expression, Expression> param) {
    Iterator<NamedElement> itr = obj.getElement().iterator();
    if (!itr.hasNext()) {
      return new BoolValue(info, true);
    }

    Expression last = makeCmp(itr.next().getName(), (Reference) param.first, (Reference) param.second);

    while (itr.hasNext()) {
      Expression ac = makeCmp(itr.next().getName(), (Reference) param.first, (Reference) param.second);
      last = new LogicAnd(info, last, ac);
    }

    return last;
  }

  private Expression makeCmp(String name, Reference left, Reference right) {
    // FIXME ok if reference is a function call? Should since functions with return values do not change state, but
    // could be optimized
    left = Copy.copy(left);
    right = Copy.copy(right);
    left.getOffset().add(new RefName(info, name));
    right.getOffset().add(new RefName(info, name));
    return makeCmpExpr(left, right);
  }

  @Override
  protected Expression visitUnionType(UnionType obj, Pair<Expression, Expression> param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Expression visitArrayType(ArrayType obj, Pair<Expression, Expression> param) {
    throw new RuntimeException("not yet implemented");
  }

}
