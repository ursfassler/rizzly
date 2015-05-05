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

package ast.pass.others;

import util.Pair;
import ast.ElementInfo;
import ast.copy.Copy;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.expression.BoolValue;
import ast.data.expression.Expression;
import ast.data.expression.TupleValue;
import ast.data.expression.binop.Equal;
import ast.data.expression.binop.LogicAnd;
import ast.data.expression.binop.Notequal;
import ast.data.expression.reference.RefCall;
import ast.data.expression.reference.RefName;
import ast.data.expression.reference.Reference;
import ast.data.expression.reference.SimpleRef;
import ast.data.expression.reference.TypeRef;
import ast.data.expression.unop.LogicNot;
import ast.data.function.header.FuncFunction;
import ast.data.function.ret.FuncReturnType;
import ast.data.statement.Block;
import ast.data.statement.ReturnExpr;
import ast.data.type.Type;
import ast.data.type.base.ArrayType;
import ast.data.type.base.BooleanType;
import ast.data.type.base.EnumType;
import ast.data.type.base.RangeType;
import ast.data.type.base.TupleType;
import ast.data.type.composed.NamedElement;
import ast.data.type.composed.RecordType;
import ast.data.type.composed.UnionType;
import ast.data.variable.FuncVariable;
import ast.knowledge.KnowType;
import ast.knowledge.KnowUniqueName;
import ast.knowledge.KnowledgeBase;
import ast.manipulator.RepoAdder;
import ast.manipulator.TypeRepo;
import ast.pass.AstPass;
import ast.traverser.NullTraverser;
import ast.traverser.other.ExprReplacer;

/**
 * Introduces compare function for complex types
 *
 * @author urs
 *
 */
public class CompareReplacer extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    CompareReplacerWorker replacer = new CompareReplacerWorker(kb);
    replacer.traverse(ast, null);
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
  private final TypeRepo kbi;
  private final RepoAdder ra;

  public MakeCompareFunction(KnowledgeBase kb) {
    super();
    kt = kb.getEntry(KnowType.class);
    kun = kb.getEntry(KnowUniqueName.class);
    kbi = new TypeRepo(kb);
    ra = new RepoAdder(kb);
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
  protected Expression visitDefault(Ast obj, Pair<Expression, Expression> param) {
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
      FuncFunction func = makeCompare(lt.types, ((TupleType) rt).types);
      TupleValue acpar = new TupleValue(info);
      acpar.value.addAll(((TupleValue) param.first).value);
      acpar.value.addAll(((TupleValue) param.second).value);
      Reference call = new Reference(info, func, new RefCall(info, acpar));
      return call;
    } else if (rt instanceof RecordType) {
      FuncFunction func = makeCompare(lt.types, (RecordType) rt);
      TupleValue acpar = new TupleValue(info);
      acpar.value.addAll(((TupleValue) param.first).value);
      acpar.value.add(param.second);
      Reference call = new Reference(info, func, new RefCall(info, acpar));
      return call;
    } else {
      throw new RuntimeException("not yet implemented: " + rt.getClass().getCanonicalName());
    }
  }

  private FuncFunction makeCompare(AstList<TypeRef> lt, RecordType rt) {
    AstList<FuncVariable> param = new AstList<FuncVariable>();
    AstList<FuncVariable> left = new AstList<FuncVariable>();
    for (TypeRef rtr : lt) {
      left.add(new FuncVariable(info, "left" + left.size(), Copy.copy(rtr)));
    }
    FuncVariable right = new FuncVariable(info, "right", new SimpleRef<Type>(info, rt));
    param.addAll(left);
    param.add(right);

    Expression expr = new BoolValue(info, true);

    for (int i = 0; i < rt.element.size(); i++) {
      Reference leftVal = new Reference(info, left.get(i));
      Reference rightVal = new Reference(info, right, new RefName(info, rt.element.get(i).name));
      Expression ac = make(leftVal, rightVal);
      expr = new LogicAnd(info, expr, ac);
    }

    return makeFunc(param, expr);
  }

  private FuncFunction makeCompare(AstList<TypeRef> lt, AstList<TypeRef> rt) {
    AstList<FuncVariable> param = new AstList<FuncVariable>();
    AstList<FuncVariable> left = new AstList<FuncVariable>();
    for (TypeRef ltr : lt) {
      left.add(new FuncVariable(info, "left" + left.size(), Copy.copy(ltr)));
    }
    AstList<FuncVariable> right = new AstList<FuncVariable>();
    for (TypeRef rtr : rt) {
      right.add(new FuncVariable(info, "right" + right.size(), Copy.copy(rtr)));
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
      FuncFunction func = makeCompare(lt);
      TupleValue acpar = new TupleValue(info);
      acpar.value.add(param.first);
      acpar.value.add(param.second);
      Reference call = new Reference(info, func, new RefCall(info, acpar));
      return call;
    } else if (rt instanceof TupleType) {
      assert (param.second instanceof TupleValue);
      FuncFunction func = makeCompare(lt, ((TupleType) rt).types);
      TupleValue acpar = new TupleValue(info);
      acpar.value.add(param.first);
      acpar.value.addAll(((TupleValue) param.second).value);
      Reference call = new Reference(info, func, new RefCall(info, acpar));
      return call;
    } else {
      throw new RuntimeException("not yet implemented: " + rt.getClass().getCanonicalName());
    }
  }

  private FuncFunction makeCompare(RecordType lt, AstList<TypeRef> rt) {
    AstList<FuncVariable> param = new AstList<FuncVariable>();
    FuncVariable left = new FuncVariable(info, "left", new SimpleRef<Type>(info, lt));
    AstList<FuncVariable> right = new AstList<FuncVariable>();
    for (TypeRef rtr : rt) {
      right.add(new FuncVariable(info, "right" + right.size(), Copy.copy(rtr)));
    }
    param.add(left);
    param.addAll(right);

    Expression expr = new BoolValue(info, true);

    for (int i = 0; i < lt.element.size(); i++) {
      Reference leftVal = new Reference(info, left, new RefName(info, lt.element.get(i).name));
      Reference rightVal = new Reference(info, right.get(i));
      Expression ac = make(leftVal, rightVal);
      expr = new LogicAnd(info, expr, ac);
    }

    return makeFunc(param, expr);
  }

  private FuncFunction makeCompare(RecordType both) {
    AstList<FuncVariable> param = new AstList<FuncVariable>();
    FuncVariable left = new FuncVariable(info, "left", new SimpleRef<Type>(info, both));
    FuncVariable right = new FuncVariable(info, "right", new SimpleRef<Type>(info, both));
    param.add(left);
    param.add(right);

    Expression expr = new BoolValue(info, true);
    for (NamedElement itr : both.element) {
      String name = itr.name;
      Reference lr = new Reference(info, left, new RefName(info, name));
      Reference rr = new Reference(info, right, new RefName(info, name));
      Expression ac = make(lr, rr);
      expr = new LogicAnd(info, expr, ac);
    }

    return makeFunc(param, expr);
  }

  private FuncFunction makeFunc(AstList<FuncVariable> param, Expression expr) {
    Block body = new Block(info);
    body.statements.add(new ReturnExpr(info, expr));
    FuncFunction func = new FuncFunction(info, kun.get("cmp"), param, new FuncReturnType(info, new SimpleRef<Type>(info, kbi.getBooleanType())), body);
    ra.add(func);
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
