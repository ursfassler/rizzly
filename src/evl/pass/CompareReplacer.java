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

import pass.EvlPass;
import util.Pair;

import common.ElementInfo;

import evl.Evl;
import evl.NullTraverser;
import evl.expression.BoolValue;
import evl.expression.Expression;
import evl.expression.TupleValue;
import evl.expression.binop.Equal;
import evl.expression.binop.LogicAnd;
import evl.expression.binop.Notequal;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.expression.reference.SimpleRef;
import evl.expression.unop.LogicNot;
import evl.function.header.FuncPrivateRet;
import evl.function.ret.FuncReturnType;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowType;
import evl.knowledge.KnowUniqueName;
import evl.knowledge.KnowledgeBase;
import evl.other.EvlList;
import evl.other.Namespace;
import evl.statement.Block;
import evl.statement.ReturnExpr;
import evl.traverser.ExprReplacer;
import evl.type.Type;
import evl.type.base.ArrayType;
import evl.type.base.BooleanType;
import evl.type.base.EnumType;
import evl.type.base.RangeType;
import evl.type.base.TupleType;
import evl.type.composed.NamedElement;
import evl.type.composed.RecordType;
import evl.type.composed.UnionType;
import evl.variable.FuncVariable;

/**
 * Introduces compare function for complex types
 *
 * @author urs
 *
 */
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
    return MakeCompareFunction.makeCmpExpr(obj.left, obj.right, kb);
  }

  @Override
  protected Expression visitNotequal(Notequal obj, Void param) {
    obj = (Notequal) super.visitNotequal(obj, param);
    return new LogicNot(obj.getInfo(), MakeCompareFunction.makeCmpExpr(obj.left, obj.right, kb));
  }

}

// TODO make this class smarter / cleaner
class MakeCompareFunction extends NullTraverser<Expression, Pair<Expression, Expression>> {
  private static final ElementInfo info = ElementInfo.NO;
  private final KnowType kt;
  private final KnowUniqueName kun;
  private final KnowBaseItem kbi;

  public MakeCompareFunction(KnowledgeBase kb) {
    super();
    kt = kb.getEntry(KnowType.class);
    kun = kb.getEntry(KnowUniqueName.class);
    kbi = kb.getEntry(KnowBaseItem.class);
  }

  static public Expression makeCmpExpr(Expression left, Expression right, KnowledgeBase kb) {
    MakeCompareFunction mcf = new MakeCompareFunction(kb);
    return mcf.make(left, right);
  }

  private Expression make(Expression left, Expression right) {
    Type lt = kt.get(left);
    return visit(lt, new Pair<Expression, Expression>(left, right));
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
  protected Expression visitTupleType(TupleType lt, Pair<Expression, Expression> param) {
    Type rt = kt.get(param.second);

    if (rt instanceof TupleType) {
      assert (param.first instanceof TupleValue);
      assert (param.second instanceof TupleValue);
      FuncPrivateRet func = makeCompare(lt.types, ((TupleType) rt).types);
      TupleValue acpar = new TupleValue(info);
      acpar.value.addAll(((TupleValue) param.first).value);
      acpar.value.addAll(((TupleValue) param.second).value);
      Reference call = new Reference(info, func, new RefCall(info, acpar));
      return call;
    } else if (rt instanceof RecordType) {
      FuncPrivateRet func = makeCompare(lt.types, (RecordType) rt);
      TupleValue acpar = new TupleValue(info);
      acpar.value.addAll(((TupleValue) param.first).value);
      acpar.value.add(param.second);
      Reference call = new Reference(info, func, new RefCall(info, acpar));
      return call;
    } else {
      throw new RuntimeException("not yet implemented: " + rt.getClass().getCanonicalName());
    }
  }

  private FuncPrivateRet makeCompare(EvlList<SimpleRef<Type>> lt, RecordType rt) {
    EvlList<FuncVariable> param = new EvlList<FuncVariable>();
    EvlList<FuncVariable> left = new EvlList<FuncVariable>();
    for (SimpleRef<Type> rtr : lt) {
      left.add(new FuncVariable(info, "left" + left.size(), new SimpleRef<Type>(info, rtr.link)));
    }
    FuncVariable right = new FuncVariable(info, "right", new SimpleRef<Type>(info, rt));
    param.addAll(left);
    param.add(right);

    Expression expr = new BoolValue(info, true);

    for (int i = 0; i < rt.element.size(); i++) {
      Reference leftVal = new Reference(info, left.get(i));
      Reference rightVal = new Reference(info, right, new RefName(info, rt.element.get(i).getName()));
      Expression ac = make(leftVal, rightVal);
      expr = new LogicAnd(info, expr, ac);
    }

    return makeFunc(param, expr);
  }

  private FuncPrivateRet makeCompare(EvlList<SimpleRef<Type>> lt, EvlList<SimpleRef<Type>> rt) {
    EvlList<FuncVariable> param = new EvlList<FuncVariable>();
    EvlList<FuncVariable> left = new EvlList<FuncVariable>();
    for (SimpleRef<Type> ltr : lt) {
      left.add(new FuncVariable(info, "left" + left.size(), new SimpleRef<Type>(info, ltr.link)));
    }
    EvlList<FuncVariable> right = new EvlList<FuncVariable>();
    for (SimpleRef<Type> rtr : rt) {
      right.add(new FuncVariable(info, "right" + right.size(), new SimpleRef<Type>(info, rtr.link)));
    }
    param.addAll(left);
    param.addAll(right);

    Expression expr = new BoolValue(info, true);

    for (int i = 0; i < left.size(); i++) {
      Reference leftVal = new Reference(info, left.get(i));
      Reference rightVal = new Reference(info, right.get(i));
      Expression ac = make(leftVal, rightVal);
      expr = new LogicAnd(info, expr, ac);
    }

    return makeFunc(param, expr);
  }

  @Override
  protected Expression visitRecordType(RecordType lt, Pair<Expression, Expression> param) {
    Type rt = kt.get(param.second);

    if (rt instanceof RecordType) {
      assert (lt == rt);
      FuncPrivateRet func = makeCompare(lt);
      TupleValue acpar = new TupleValue(info);
      acpar.value.add(param.first);
      acpar.value.add(param.second);
      Reference call = new Reference(info, func, new RefCall(info, acpar));
      return call;
    } else if (rt instanceof TupleType) {
      assert (param.second instanceof TupleValue);
      FuncPrivateRet func = makeCompare(lt, ((TupleType) rt).types);
      TupleValue acpar = new TupleValue(info);
      acpar.value.add(param.first);
      acpar.value.addAll(((TupleValue) param.second).value);
      Reference call = new Reference(info, func, new RefCall(info, acpar));
      return call;
    } else {
      throw new RuntimeException("not yet implemented: " + rt.getClass().getCanonicalName());
    }
  }

  private FuncPrivateRet makeCompare(RecordType lt, EvlList<SimpleRef<Type>> rt) {
    EvlList<FuncVariable> param = new EvlList<FuncVariable>();
    FuncVariable left = new FuncVariable(info, "left", new SimpleRef<Type>(info, lt));
    EvlList<FuncVariable> right = new EvlList<FuncVariable>();
    for (SimpleRef<Type> rtr : rt) {
      right.add(new FuncVariable(info, "right" + right.size(), new SimpleRef<Type>(info, rtr.link)));
    }
    param.add(left);
    param.addAll(right);

    Expression expr = new BoolValue(info, true);

    for (int i = 0; i < lt.element.size(); i++) {
      Reference leftVal = new Reference(info, left, new RefName(info, lt.element.get(i).getName()));
      Reference rightVal = new Reference(info, right.get(i));
      Expression ac = make(leftVal, rightVal);
      expr = new LogicAnd(info, expr, ac);
    }

    return makeFunc(param, expr);
  }

  private FuncPrivateRet makeCompare(RecordType both) {
    EvlList<FuncVariable> param = new EvlList<FuncVariable>();
    FuncVariable left = new FuncVariable(info, "left", new SimpleRef<Type>(info, both));
    FuncVariable right = new FuncVariable(info, "right", new SimpleRef<Type>(info, both));
    param.add(left);
    param.add(right);

    Expression expr = new BoolValue(info, true);
    for (NamedElement itr : both.element) {
      String name = itr.getName();
      Reference lr = new Reference(info, left, new RefName(info, name));
      Reference rr = new Reference(info, right, new RefName(info, name));
      Expression ac = make(lr, rr);
      expr = new LogicAnd(info, expr, ac);
    }

    return makeFunc(param, expr);
  }

  private FuncPrivateRet makeFunc(EvlList<FuncVariable> param, Expression expr) {
    Block body = new Block(info);
    body.statements.add(new ReturnExpr(info, expr));
    FuncPrivateRet func = new FuncPrivateRet(info, kun.get("cmp"), param, new FuncReturnType(info, new SimpleRef<Type>(info, kbi.getBooleanType())), body);
    kbi.addItem(func);
    return func;
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
