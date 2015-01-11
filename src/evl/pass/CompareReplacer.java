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

import java.util.Iterator;

import pass.EvlPass;
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
import evl.other.Namespace;
import evl.traverser.ExprReplacer;
import evl.type.Type;
import evl.type.base.ArrayType;
import evl.type.base.BooleanType;
import evl.type.base.EnumType;
import evl.type.base.RangeType;
import evl.type.composed.NamedElement;
import evl.type.composed.RecordType;
import evl.type.composed.UnionType;

public class CompareReplacer extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    CompareReplacerWorker replacer = new CompareReplacerWorker(kb);
    replacer.traverse(evl, null);
  }

}

class CompareReplacerWorker extends ExprReplacer<Void> {

  private final KnowledgeBase kb;

  public CompareReplacerWorker(KnowledgeBase kb) {
    super();
    this.kb = kb;
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

  private static final ElementInfo info = ElementInfo.NO;
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
