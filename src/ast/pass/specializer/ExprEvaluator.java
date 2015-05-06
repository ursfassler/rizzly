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

package ast.pass.specializer;

import java.math.BigInteger;
import java.util.List;

import ast.ElementInfo;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.expression.AnyValue;
import ast.data.expression.BoolValue;
import ast.data.expression.Expression;
import ast.data.expression.NamedElementsValue;
import ast.data.expression.NamedValue;
import ast.data.expression.Number;
import ast.data.expression.StringValue;
import ast.data.expression.TupleValue;
import ast.data.expression.binop.And;
import ast.data.expression.binop.Div;
import ast.data.expression.binop.Equal;
import ast.data.expression.binop.Greater;
import ast.data.expression.binop.Greaterequal;
import ast.data.expression.binop.Is;
import ast.data.expression.binop.Less;
import ast.data.expression.binop.Lessequal;
import ast.data.expression.binop.Minus;
import ast.data.expression.binop.Mod;
import ast.data.expression.binop.Mul;
import ast.data.expression.binop.Notequal;
import ast.data.expression.binop.Or;
import ast.data.expression.binop.Plus;
import ast.data.expression.binop.Shl;
import ast.data.expression.binop.Shr;
import ast.data.expression.reference.RefCall;
import ast.data.expression.reference.RefName;
import ast.data.expression.reference.RefTemplCall;
import ast.data.expression.reference.Reference;
import ast.data.expression.unop.Not;
import ast.data.expression.unop.Uminus;
import ast.data.type.Type;
import ast.data.variable.Constant;
import ast.data.variable.FuncVariable;
import ast.data.variable.TemplateParameter;
import ast.interpreter.Memory;
import ast.knowledge.KnowledgeBase;
import ast.traverser.NullTraverser;
import error.ErrorType;
import error.RError;

public class ExprEvaluator extends NullTraverser<Expression, Void> {
  private final Memory memory;
  private final InstanceRepo ir;
  private final KnowledgeBase kb;

  public ExprEvaluator(Memory memory, InstanceRepo ir, KnowledgeBase kb) {
    super();
    this.memory = memory;
    this.ir = ir;
    this.kb = kb;
  }

  public static Expression evaluate(Expression obj, Memory memory, InstanceRepo ir, KnowledgeBase kb) {
    ExprEvaluator evaluator = new ExprEvaluator(memory, ir, kb);
    return evaluator.traverse(obj, null);
  }

  private void visitExpList(List<Expression> expList, Void param) {
    for (int i = 0; i < expList.size(); i++) {
      Expression expr = expList.get(i);
      expr = visit(expr, param);
      expList.set(i, expr);
    }
  }

  @Override
  protected Expression visitFuncVariable(FuncVariable obj, Void param) {
    assert (memory.contains(obj));
    return memory.get(obj);
  }

  @Override
  protected Expression visitTemplateParameter(TemplateParameter obj, Void param) {
    assert (memory.contains(obj));
    return memory.get(obj);
  }

  @Override
  protected Expression visitDefault(Ast obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Expression visitType(Type obj, Void param) {
    RError.err(ErrorType.Error, obj.getInfo(), "Expected value, got type");
    return null;
  }

  @Override
  protected Expression visitNamedElementsValue(NamedElementsValue obj, Void param) {
    for (NamedValue itr : obj.value) {
      Expression value = visit(itr.value, param);
      assert (value != null);
      itr.value = value;
    }
    return obj;
  }

  @Override
  protected Expression visitAnyValue(AnyValue obj, Void param) {
    return obj;
  }

  @Override
  protected Expression visitNumber(Number obj, Void param) {
    return obj;
  }

  @Override
  protected Expression visitStringValue(StringValue obj, Void param) {
    return obj;
  }

  @Override
  protected Expression visitBoolValue(BoolValue obj, Void param) {
    return obj;
  }

  @Override
  protected Expression visitConstGlobal(ast.data.variable.ConstGlobal obj, Void param) {
    return visit(obj.def, param);
  }

  @Override
  protected Expression visitConstPrivate(ast.data.variable.ConstPrivate obj, Void param) {
    return visit(obj.def, param);
  }

  @Override
  protected Expression visitTupleValue(TupleValue obj, Void param) {
    if (obj.value.size() == 1) {
      return visit(obj.value.get(0), param);
    } else {
      AstList<Expression> list = new AstList<Expression>();
      for (Expression expr : obj.value) {
        list.add(visit(expr, param));
      }
      return new TupleValue(obj.getInfo(), list);
    }
  }

  @Override
  protected Expression visitReference(Reference obj, Void param) {
    // TODO move constant evaluation to another place
    if (obj.link instanceof Constant) {
      Constant cst = (Constant) obj.link;
      cst.def = visit(cst.def, param);
    }
    return visit(RefEvaluator.execute(obj, memory, ir, kb), param);
  }

  @Override
  protected Expression visitRefCall(RefCall obj, Void param) {
    visitExpList(obj.actualParameter.value, param);
    return null;
  }

  @Override
  protected Expression visitRefTemplCall(RefTemplCall obj, Void param) {
    RError.err(ErrorType.Fatal, obj.getInfo(), "reimplement");
    // visitExpList(obj.getActualParameter(), param);
    return null;
  }

  @Override
  protected Expression visitRefName(RefName obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  private int getInt(ElementInfo info, BigInteger rval) {
    if (rval.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
      RError.err(ErrorType.Error, info, "value to big: " + rval.toString());
    } else if (rval.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) < 0) {
      RError.err(ErrorType.Error, info, "value to small: " + rval.toString());
    }
    return rval.intValue();
  }

  @Override
  protected Expression visitAnd(And obj, Void param) {
    Expression left = visit(obj.left, param);
    Expression right = visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((Number) left).value;
      BigInteger rval = ((Number) right).value;
      BigInteger res;
      res = lval.and(rval);
      return new Number(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  @Override
  protected Expression visitDiv(Div obj, Void param) {
    Expression left = visit(obj.left, param);
    Expression right = visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((Number) left).value;
      BigInteger rval = ((Number) right).value;
      BigInteger res;
      res = lval.divide(rval);
      return new Number(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  @Override
  protected Expression visitMinus(Minus obj, Void param) {
    Expression left = visit(obj.left, param);
    Expression right = visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((Number) left).value;
      BigInteger rval = ((Number) right).value;
      BigInteger res;
      res = lval.subtract(rval);
      return new Number(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  @Override
  protected Expression visitMod(Mod obj, Void param) {
    Expression left = visit(obj.left, param);
    Expression right = visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((Number) left).value;
      BigInteger rval = ((Number) right).value;
      BigInteger res;
      res = lval.mod(rval);
      return new Number(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  @Override
  protected Expression visitMul(Mul obj, Void param) {
    Expression left = visit(obj.left, param);
    Expression right = visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((Number) left).value;
      BigInteger rval = ((Number) right).value;
      BigInteger res;
      res = lval.multiply(rval);
      return new Number(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  @Override
  protected Expression visitOr(Or obj, Void param) {
    Expression left = visit(obj.left, param);
    Expression right = visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((Number) left).value;
      BigInteger rval = ((Number) right).value;
      BigInteger res;
      res = lval.or(rval);
      return new Number(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  @Override
  protected Expression visitPlus(Plus obj, Void param) {
    Expression left = visit(obj.left, param);
    Expression right = visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((Number) left).value;
      BigInteger rval = ((Number) right).value;
      BigInteger res;
      res = lval.add(rval);
      return new Number(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  @Override
  protected Expression visitShl(Shl obj, Void param) {
    Expression left = visit(obj.left, param);
    Expression right = visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((Number) left).value;
      BigInteger rval = ((Number) right).value;
      BigInteger res;
      res = lval.shiftLeft(getInt(obj.getInfo(), rval));
      return new Number(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  @Override
  protected Expression visitShr(Shr obj, Void param) {
    Expression left = visit(obj.left, param);
    Expression right = visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((Number) left).value;
      BigInteger rval = ((Number) right).value;
      BigInteger res;
      res = lval.shiftRight(getInt(obj.getInfo(), rval));
      return new Number(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  @Override
  protected Expression visitEqual(Equal obj, Void param) {
    Expression left = visit(obj.left, param);
    Expression right = visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((Number) left).value;
      BigInteger rval = ((Number) right).value;
      boolean res = lval.compareTo(rval) == 0;
      return new BoolValue(obj.getInfo(), res);
    } else if (areBool(left, right)) {
      boolean lval = ((BoolValue) left).value;
      boolean rval = ((BoolValue) right).value;
      boolean res = lval == rval;
      return new BoolValue(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  private boolean areBool(Expression left, Expression right) {
    return (left instanceof BoolValue) && (right instanceof BoolValue);
  }

  private boolean areNumber(Expression left, Expression right) {
    return (left instanceof Number) && (right instanceof Number);
  }

  @Override
  protected Expression visitGreater(Greater obj, Void param) {
    Expression left = visit(obj.left, param);
    Expression right = visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((Number) left).value;
      BigInteger rval = ((Number) right).value;
      boolean res = lval.compareTo(rval) > 0;
      return new BoolValue(obj.getInfo(), res);
    } else if (areBool(left, right)) {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Operator not yet implemented: " + obj);
      return obj;
    } else {
      return obj;
    }
  }

  @Override
  protected Expression visitGreaterequal(Greaterequal obj, Void param) {
    Expression left = visit(obj.left, param);
    Expression right = visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((Number) left).value;
      BigInteger rval = ((Number) right).value;
      boolean res = lval.compareTo(rval) >= 0;
      return new BoolValue(obj.getInfo(), res);
    } else if (areBool(left, right)) {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Operator not yet implemented: " + obj);
      return obj;
    } else {
      return obj;
    }
  }

  @Override
  protected Expression visitLess(Less obj, Void param) {
    Expression left = visit(obj.left, param);
    Expression right = visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((Number) left).value;
      BigInteger rval = ((Number) right).value;
      boolean res = lval.compareTo(rval) < 0;
      return new BoolValue(obj.getInfo(), res);
    } else if (areBool(left, right)) {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Operator not yet implemented: " + obj);
      return obj;
    } else {
      return obj;
    }
  }

  @Override
  protected Expression visitLessequal(Lessequal obj, Void param) {
    Expression left = visit(obj.left, param);
    Expression right = visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((Number) left).value;
      BigInteger rval = ((Number) right).value;
      boolean res = lval.compareTo(rval) <= 0;
      return new BoolValue(obj.getInfo(), res);
    } else if (areBool(left, right)) {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Operator not yet implemented: " + obj);
      return obj;
    } else {
      return obj;
    }
  }

  @Override
  protected Expression visitNotequal(Notequal obj, Void param) {
    Expression left = visit(obj.left, param);
    Expression right = visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((Number) left).value;
      BigInteger rval = ((Number) right).value;
      boolean res = lval.compareTo(rval) != 0;
      return new BoolValue(obj.getInfo(), res);
    } else if (areBool(left, right)) {
      boolean lval = ((BoolValue) left).value;
      boolean rval = ((BoolValue) right).value;
      boolean res = lval != rval;
      return new BoolValue(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  @Override
  protected Expression visitIs(Is obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Expression visitUminus(Uminus obj, Void param) {
    Expression expr = visit(obj.expr, param);

    if ((expr instanceof Number)) {
      BigInteger eval = ((Number) expr).value;
      BigInteger res;

      res = eval.negate();
      return new Number(obj.getInfo(), res);
    } else {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Can not evaluate unary minus on " + expr.getClass().getName());
      return obj;
    }
  }

  @Override
  protected Expression visitNot(Not obj, Void param) {
    Expression expr = visit(obj.expr, param);

    if ((expr instanceof BoolValue)) {
      return new BoolValue(obj.getInfo(), !((BoolValue) expr).value);
    } else {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Can not evaluate not on " + expr.getClass().getName());
      return obj;
    }
  }

}
