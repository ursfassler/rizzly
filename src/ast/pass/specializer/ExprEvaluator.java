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

import ast.data.Ast;
import ast.data.AstList;
import ast.data.expression.BoolValue;
import ast.data.expression.Expression;
import ast.data.expression.Number;
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
import ast.data.expression.reference.RefTemplCall;
import ast.data.expression.reference.Reference;
import ast.data.expression.unop.Not;
import ast.data.expression.unop.Uminus;
import ast.data.template.ActualTemplateArgument;
import ast.data.template.Template;
import ast.data.type.Type;
import ast.data.variable.Constant;
import ast.data.variable.FuncVariable;
import ast.data.variable.TemplateParameter;
import ast.interpreter.Memory;
import ast.knowledge.KnowledgeBase;
import ast.traverser.NullTraverser;

import common.ElementInfo;

import error.ErrorType;
import error.RError;

public class ExprEvaluator extends NullTraverser<ActualTemplateArgument, Memory> {
  private final KnowledgeBase kb;

  public ExprEvaluator(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  public static ActualTemplateArgument evaluate(Expression obj, Memory mem, KnowledgeBase kb) {
    ExprEvaluator evaluator = new ExprEvaluator(kb);
    return evaluator.traverse(obj, mem);
  }

  private void visitExpList(List<Expression> expList, Memory param) {
    for (int i = 0; i < expList.size(); i++) {
      Expression expr = expList.get(i);
      expr = (Expression) visit(expr, param);
      expList.set(i, expr);
    }
  }

  @Override
  protected ast.data.expression.Expression visitFuncVariable(FuncVariable obj, Memory param) {
    assert (param.contains(obj));
    return param.get(obj);
  }

  @Override
  protected ast.data.expression.Expression visitTemplateParameter(TemplateParameter obj, Memory param) {
    assert (param.contains(obj));
    return param.get(obj);
  }

  @Override
  protected ActualTemplateArgument visitType(Type obj, Memory param) {
    return obj;
  }

  @Override
  protected ast.data.expression.Expression visitDefault(Ast obj, Memory param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected ActualTemplateArgument visitTemplate(Template obj, Memory param) {
    Ast spec = Specializer.process(obj, new AstList<ActualTemplateArgument>(), kb);
    return (ActualTemplateArgument) spec;
  }

  @Override
  protected ActualTemplateArgument visitNamedElementsValue(ast.data.expression.NamedElementsValue obj, Memory param) {
    for (ast.data.expression.NamedValue itr : obj.value) {
      Expression value = (Expression) visit(itr.value, param);
      assert (value != null);
      itr.value = value;
    }
    return obj;
  }

  @Override
  protected ActualTemplateArgument visitAnyValue(ast.data.expression.AnyValue obj, Memory param) {
    return obj;
  }

  @Override
  protected ast.data.expression.Expression visitNumber(ast.data.expression.Number obj, Memory param) {
    return obj;
  }

  @Override
  protected ast.data.expression.Expression visitStringValue(ast.data.expression.StringValue obj, Memory param) {
    return obj;
  }

  @Override
  protected ActualTemplateArgument visitBoolValue(ast.data.expression.BoolValue obj, Memory param) {
    return obj;
  }

  @Override
  protected ast.data.expression.Expression visitConstGlobal(ast.data.variable.ConstGlobal obj, Memory param) {
    return (ast.data.expression.Expression) visit(obj.def, new Memory()); // new
    // memory
    // because
    // global
    // constant
    // need no context
  }

  @Override
  protected ActualTemplateArgument visitConstPrivate(ast.data.variable.ConstPrivate obj, Memory param) {
    return visit(obj.def, param);
  }

  @Override
  protected ast.data.expression.Expression visitTupleValue(ast.data.expression.TupleValue obj, Memory param) {
    if (obj.value.size() == 1) {
      return (ast.data.expression.Expression) visit(obj.value.get(0), param);
    } else {
      AstList<Expression> list = new AstList<Expression>();
      for (Expression expr : obj.value) {
        list.add((Expression) visit(expr, param));
      }
      return new TupleValue(obj.getInfo(), list);
    }
  }

  @Override
  protected ActualTemplateArgument visitReference(Reference obj, Memory param) {
    // TODO move constant evaluation to another place
    if (obj.link instanceof Constant) {
      ast.data.variable.Constant cst = (ast.data.variable.Constant) obj.link;
      ActualTemplateArgument eco = visit(cst.def, param);
      cst.def = (Expression) eco;
    }
    return visit(RefEvaluator.execute(obj, param, kb), param);
  }

  @Override
  protected ast.data.expression.Expression visitRefCall(RefCall obj, Memory param) {
    visitExpList(obj.actualParameter.value, param);
    return null;
  }

  @Override
  protected ast.data.expression.Expression visitRefTemplCall(RefTemplCall obj, Memory param) {
    RError.err(ErrorType.Fatal, obj.getInfo(), "reimplement");
    // visitExpList(obj.getActualParameter(), param);
    return null;
  }

  @Override
  protected ast.data.expression.Expression visitRefName(ast.data.expression.reference.RefName obj, Memory param) {
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
  protected ActualTemplateArgument visitAnd(And obj, Memory param) {
    ast.data.expression.Expression left = (ast.data.expression.Expression) visit(obj.left, param);
    ast.data.expression.Expression right = (ast.data.expression.Expression) visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((ast.data.expression.Number) left).value;
      BigInteger rval = ((ast.data.expression.Number) right).value;
      BigInteger res;
      res = lval.and(rval);
      return new Number(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  @Override
  protected ActualTemplateArgument visitDiv(Div obj, Memory param) {
    ast.data.expression.Expression left = (ast.data.expression.Expression) visit(obj.left, param);
    ast.data.expression.Expression right = (ast.data.expression.Expression) visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((ast.data.expression.Number) left).value;
      BigInteger rval = ((ast.data.expression.Number) right).value;
      BigInteger res;
      res = lval.divide(rval);
      return new Number(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  @Override
  protected ActualTemplateArgument visitMinus(Minus obj, Memory param) {
    ast.data.expression.Expression left = (ast.data.expression.Expression) visit(obj.left, param);
    ast.data.expression.Expression right = (ast.data.expression.Expression) visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((ast.data.expression.Number) left).value;
      BigInteger rval = ((ast.data.expression.Number) right).value;
      BigInteger res;
      res = lval.subtract(rval);
      return new Number(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  @Override
  protected ActualTemplateArgument visitMod(Mod obj, Memory param) {
    ast.data.expression.Expression left = (ast.data.expression.Expression) visit(obj.left, param);
    ast.data.expression.Expression right = (ast.data.expression.Expression) visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((ast.data.expression.Number) left).value;
      BigInteger rval = ((ast.data.expression.Number) right).value;
      BigInteger res;
      res = lval.mod(rval);
      return new Number(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  @Override
  protected ActualTemplateArgument visitMul(Mul obj, Memory param) {
    ast.data.expression.Expression left = (ast.data.expression.Expression) visit(obj.left, param);
    ast.data.expression.Expression right = (ast.data.expression.Expression) visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((ast.data.expression.Number) left).value;
      BigInteger rval = ((ast.data.expression.Number) right).value;
      BigInteger res;
      res = lval.multiply(rval);
      return new Number(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  @Override
  protected ActualTemplateArgument visitOr(Or obj, Memory param) {
    ast.data.expression.Expression left = (ast.data.expression.Expression) visit(obj.left, param);
    ast.data.expression.Expression right = (ast.data.expression.Expression) visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((ast.data.expression.Number) left).value;
      BigInteger rval = ((ast.data.expression.Number) right).value;
      BigInteger res;
      res = lval.or(rval);
      return new Number(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  @Override
  protected ActualTemplateArgument visitPlus(Plus obj, Memory param) {
    ast.data.expression.Expression left = (ast.data.expression.Expression) visit(obj.left, param);
    ast.data.expression.Expression right = (ast.data.expression.Expression) visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((ast.data.expression.Number) left).value;
      BigInteger rval = ((ast.data.expression.Number) right).value;
      BigInteger res;
      res = lval.add(rval);
      return new Number(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  @Override
  protected ActualTemplateArgument visitShl(Shl obj, Memory param) {
    ast.data.expression.Expression left = (ast.data.expression.Expression) visit(obj.left, param);
    ast.data.expression.Expression right = (ast.data.expression.Expression) visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((ast.data.expression.Number) left).value;
      BigInteger rval = ((ast.data.expression.Number) right).value;
      BigInteger res;
      res = lval.shiftLeft(getInt(obj.getInfo(), rval));
      return new Number(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  @Override
  protected ActualTemplateArgument visitShr(Shr obj, Memory param) {
    ast.data.expression.Expression left = (ast.data.expression.Expression) visit(obj.left, param);
    ast.data.expression.Expression right = (ast.data.expression.Expression) visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((ast.data.expression.Number) left).value;
      BigInteger rval = ((ast.data.expression.Number) right).value;
      BigInteger res;
      res = lval.shiftRight(getInt(obj.getInfo(), rval));
      return new Number(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  @Override
  protected ActualTemplateArgument visitEqual(Equal obj, Memory param) {
    ast.data.expression.Expression left = (ast.data.expression.Expression) visit(obj.left, param);
    ast.data.expression.Expression right = (ast.data.expression.Expression) visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((ast.data.expression.Number) left).value;
      BigInteger rval = ((ast.data.expression.Number) right).value;
      boolean res = lval.compareTo(rval) == 0;
      return new BoolValue(obj.getInfo(), res);
    } else if (areBool(left, right)) {
      boolean lval = ((ast.data.expression.BoolValue) left).value;
      boolean rval = ((ast.data.expression.BoolValue) right).value;
      boolean res = lval == rval;
      return new BoolValue(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  private boolean areBool(ast.data.expression.Expression left, ast.data.expression.Expression right) {
    return (left instanceof BoolValue) && (right instanceof BoolValue);
  }

  private boolean areNumber(ast.data.expression.Expression left, ast.data.expression.Expression right) {
    return (left instanceof Number) && (right instanceof Number);
  }

  @Override
  protected ActualTemplateArgument visitGreater(Greater obj, Memory param) {
    ast.data.expression.Expression left = (ast.data.expression.Expression) visit(obj.left, param);
    ast.data.expression.Expression right = (ast.data.expression.Expression) visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((ast.data.expression.Number) left).value;
      BigInteger rval = ((ast.data.expression.Number) right).value;
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
  protected ActualTemplateArgument visitGreaterequal(Greaterequal obj, Memory param) {
    ast.data.expression.Expression left = (ast.data.expression.Expression) visit(obj.left, param);
    ast.data.expression.Expression right = (ast.data.expression.Expression) visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((ast.data.expression.Number) left).value;
      BigInteger rval = ((ast.data.expression.Number) right).value;
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
  protected ActualTemplateArgument visitLess(Less obj, Memory param) {
    ast.data.expression.Expression left = (ast.data.expression.Expression) visit(obj.left, param);
    ast.data.expression.Expression right = (ast.data.expression.Expression) visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((ast.data.expression.Number) left).value;
      BigInteger rval = ((ast.data.expression.Number) right).value;
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
  protected ActualTemplateArgument visitLessequal(Lessequal obj, Memory param) {
    ast.data.expression.Expression left = (ast.data.expression.Expression) visit(obj.left, param);
    ast.data.expression.Expression right = (ast.data.expression.Expression) visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((ast.data.expression.Number) left).value;
      BigInteger rval = ((ast.data.expression.Number) right).value;
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
  protected ActualTemplateArgument visitNotequal(Notequal obj, Memory param) {
    ast.data.expression.Expression left = (ast.data.expression.Expression) visit(obj.left, param);
    ast.data.expression.Expression right = (ast.data.expression.Expression) visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((ast.data.expression.Number) left).value;
      BigInteger rval = ((ast.data.expression.Number) right).value;
      boolean res = lval.compareTo(rval) != 0;
      return new BoolValue(obj.getInfo(), res);
    } else if (areBool(left, right)) {
      boolean lval = ((ast.data.expression.BoolValue) left).value;
      boolean rval = ((ast.data.expression.BoolValue) right).value;
      boolean res = lval != rval;
      return new BoolValue(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  @Override
  protected ActualTemplateArgument visitIs(Is obj, Memory param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected ActualTemplateArgument visitUminus(Uminus obj, Memory param) {
    ast.data.expression.Expression expr = (ast.data.expression.Expression) visit(obj.expr, param);

    if ((expr instanceof Number)) {
      BigInteger eval = ((ast.data.expression.Number) expr).value;
      BigInteger res;

      res = eval.negate();
      return new Number(obj.getInfo(), res);
    } else {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Can not evaluate unary minus on " + expr.getClass().getName());
      return obj;
    }
  }

  @Override
  protected ActualTemplateArgument visitNot(Not obj, Memory param) {
    ast.data.expression.Expression expr = (ast.data.expression.Expression) visit(obj.expr, param);

    if ((expr instanceof BoolValue)) {
      return new BoolValue(obj.getInfo(), !((BoolValue) expr).value);
    } else {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Can not evaluate not on " + expr.getClass().getName());
      return obj;
    }
  }

}
