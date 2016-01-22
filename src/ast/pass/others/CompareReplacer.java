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

import main.Configuration;
import util.Pair;
import ast.copy.Copy;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.expression.Expression;
import ast.data.expression.ReferenceExpression;
import ast.data.expression.binop.Equal;
import ast.data.expression.binop.LogicAnd;
import ast.data.expression.binop.NotEqual;
import ast.data.expression.unop.LogicNot;
import ast.data.expression.value.BooleanValue;
import ast.data.expression.value.TupleValue;
import ast.data.function.header.FuncFunction;
import ast.data.function.ret.FunctionReturnType;
import ast.data.reference.RefFactory;
import ast.data.reference.RefName;
import ast.data.reference.LinkedReferenceWithOffset_Implementation;
import ast.data.statement.Block;
import ast.data.statement.ExpressionReturn;
import ast.data.type.Type;
import ast.data.type.TypeRefFactory;
import ast.data.type.TypeReference;
import ast.data.type.base.ArrayType;
import ast.data.type.base.BooleanType;
import ast.data.type.base.EnumType;
import ast.data.type.base.RangeType;
import ast.data.type.base.TupleType;
import ast.data.type.composed.NamedElement;
import ast.data.type.composed.RecordType;
import ast.data.type.composed.UnionType;
import ast.data.variable.FunctionVariable;
import ast.dispatcher.NullDispatcher;
import ast.dispatcher.other.ExprReplacer;
import ast.knowledge.KnowType;
import ast.knowledge.KnowUniqueName;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.repository.manipulator.RepoAdder;
import ast.repository.manipulator.TypeRepo;

/**
 * Introduces compare function for complex types
 *
 * @author urs
 *
 */
public class CompareReplacer extends AstPass {

  public CompareReplacer(Configuration configuration) {
    super(configuration);
  }

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
  protected Expression visitNotequal(NotEqual obj, Void param) {
    obj = (NotEqual) super.visitNotequal(obj, param);
    LogicNot logicNot = new LogicNot(MakeCompareFunction.makeCmpExpr(obj.left, obj.right, kb));
    logicNot.metadata().add(obj.metadata());
    return logicNot;
  }

}

// TODO make this class smarter / cleaner
class MakeCompareFunction extends NullDispatcher<Expression, Pair<Expression, Expression>> {
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

  private Expression make(LinkedReferenceWithOffset_Implementation left, LinkedReferenceWithOffset_Implementation right) {
    return make(new ReferenceExpression(left), new ReferenceExpression(right));
  }

  @Override
  protected Expression visitDefault(Ast obj, Pair<Expression, Expression> param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Expression visitEnumType(EnumType obj, Pair<Expression, Expression> param) {
    return new Equal(param.first, param.second);
  }

  @Override
  protected Expression visitBooleanType(BooleanType obj, Pair<Expression, Expression> param) {
    return new Equal(param.first, param.second);
  }

  @Override
  protected Expression visitRangeType(RangeType obj, Pair<Expression, Expression> param) {
    return new Equal(param.first, param.second);
  }

  @Override
  protected Expression visitTupleType(TupleType lt, Pair<Expression, Expression> param) {
    Type rt = kt.get(param.second);

    if (rt instanceof TupleType) {
      assert (param.first instanceof TupleValue);
      assert (param.second instanceof TupleValue);
      FuncFunction func = makeCompare(lt.types, ((TupleType) rt).types);
      AstList<Expression> acpar = new AstList<Expression>();
      acpar.addAll(((TupleValue) param.first).value);
      acpar.addAll(((TupleValue) param.second).value);
      LinkedReferenceWithOffset_Implementation call = RefFactory.call(func, acpar);
      return new ReferenceExpression(call);
    } else if (rt instanceof RecordType) {
      FuncFunction func = makeCompare(lt.types, (RecordType) rt);
      AstList<Expression> acpar = new AstList<Expression>();
      acpar.addAll(((TupleValue) param.first).value);
      acpar.add(param.second);
      LinkedReferenceWithOffset_Implementation call = RefFactory.call(func, acpar);
      return new ReferenceExpression(call);
    } else {
      throw new RuntimeException("not yet implemented: " + rt.getClass().getCanonicalName());
    }
  }

  private FuncFunction makeCompare(AstList<TypeReference> lt, RecordType rt) {
    AstList<FunctionVariable> param = new AstList<FunctionVariable>();
    AstList<FunctionVariable> left = new AstList<FunctionVariable>();
    for (TypeReference rtr : lt) {
      left.add(funcVar("left" + left.size(), Copy.copy(rtr)));
    }
    FunctionVariable right = funcVar("right", rt);
    param.addAll(left);
    param.add(right);

    Expression expr = new BooleanValue(true);

    for (int i = 0; i < rt.element.size(); i++) {
      LinkedReferenceWithOffset_Implementation leftVal = RefFactory.full(left.get(i));
      LinkedReferenceWithOffset_Implementation rightVal = RefFactory.create(right, new RefName(rt.element.get(i).getName()));
      Expression ac = make(leftVal, rightVal);
      expr = new LogicAnd(expr, ac);
    }

    return makeFunc(param, expr);
  }

  private FuncFunction makeCompare(AstList<TypeReference> lt, AstList<TypeReference> rt) {
    AstList<FunctionVariable> param = new AstList<FunctionVariable>();
    AstList<FunctionVariable> left = new AstList<FunctionVariable>();
    for (TypeReference ltr : lt) {
      left.add(funcVar("left" + left.size(), Copy.copy(ltr)));
    }
    AstList<FunctionVariable> right = new AstList<FunctionVariable>();
    for (TypeReference rtr : rt) {
      right.add(funcVar("right" + right.size(), Copy.copy(rtr)));
    }
    param.addAll(left);
    param.addAll(right);

    Expression expr = new BooleanValue(true);

    for (int i = 0; i < left.size(); i++) {
      LinkedReferenceWithOffset_Implementation leftVal = RefFactory.full(left.get(i));
      LinkedReferenceWithOffset_Implementation rightVal = RefFactory.full(right.get(i));
      Expression ac = make(leftVal, rightVal);
      expr = new LogicAnd(expr, ac);
    }

    return makeFunc(param, expr);
  }

  @Override
  protected Expression visitRecordType(RecordType lt, Pair<Expression, Expression> param) {
    Type rt = kt.get(param.second);

    if (rt instanceof RecordType) {
      assert (lt == rt);
      FuncFunction func = makeCompare(lt);
      LinkedReferenceWithOffset_Implementation call = RefFactory.call(func, param.first, param.second);
      return new ReferenceExpression(call);
    } else if (rt instanceof TupleType) {
      assert (param.second instanceof TupleValue);
      FuncFunction func = makeCompare(lt, ((TupleType) rt).types);
      AstList<Expression> acpar = new AstList<Expression>();
      acpar.add(param.first);
      acpar.addAll(((TupleValue) param.second).value);
      LinkedReferenceWithOffset_Implementation call = RefFactory.call(func, acpar);
      return new ReferenceExpression(call);
    } else {
      throw new RuntimeException("not yet implemented: " + rt.getClass().getCanonicalName());
    }
  }

  private FuncFunction makeCompare(RecordType lt, AstList<TypeReference> rt) {
    AstList<FunctionVariable> param = new AstList<FunctionVariable>();
    FunctionVariable left = funcVar("left", lt);
    AstList<FunctionVariable> right = new AstList<FunctionVariable>();
    for (TypeReference rtr : rt) {
      right.add(funcVar("right" + right.size(), Copy.copy(rtr)));
    }
    param.add(left);
    param.addAll(right);

    Expression expr = new BooleanValue(true);

    for (int i = 0; i < lt.element.size(); i++) {
      LinkedReferenceWithOffset_Implementation leftVal = RefFactory.create(left, new RefName(lt.element.get(i).getName()));
      LinkedReferenceWithOffset_Implementation rightVal = RefFactory.full(right.get(i));
      Expression ac = make(leftVal, rightVal);
      expr = new LogicAnd(expr, ac);
    }

    return makeFunc(param, expr);
  }

  private FuncFunction makeCompare(RecordType both) {
    AstList<FunctionVariable> param = new AstList<FunctionVariable>();
    FunctionVariable left = funcVar("left", both);
    FunctionVariable right = funcVar("right", both);
    param.add(left);
    param.add(right);

    Expression expr = new BooleanValue(true);
    for (NamedElement itr : both.element) {
      String name = itr.getName();
      LinkedReferenceWithOffset_Implementation lr = RefFactory.create(left, new RefName(name));
      LinkedReferenceWithOffset_Implementation rr = RefFactory.create(right, new RefName(name));
      Expression ac = make(lr, rr);
      expr = new LogicAnd(expr, ac);
    }

    return makeFunc(param, expr);
  }

  private FunctionVariable funcVar(String name, RecordType type) {
    return new FunctionVariable(name, TypeRefFactory.create(type));
  }

  private FunctionVariable funcVar(String name, TypeReference type) {
    return new FunctionVariable(name, type);
  }

  private FuncFunction makeFunc(AstList<FunctionVariable> param, Expression expr) {
    Block body = new Block();
    body.statements.add(new ExpressionReturn(expr));
    FuncFunction func = new FuncFunction(kun.get("cmp"), param, new FunctionReturnType(TypeRefFactory.create(kbi.getBooleanType())), body);
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
