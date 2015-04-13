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

package fun.traverser.spezializer;

import java.math.BigInteger;
import java.util.List;

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.data.Evl;
import evl.data.EvlList;
import evl.data.expression.BoolValue;
import evl.data.expression.Expression;
import evl.data.expression.Number;
import evl.data.expression.TupleValue;
import evl.data.expression.binop.And;
import evl.data.expression.binop.Div;
import evl.data.expression.binop.Equal;
import evl.data.expression.binop.Greater;
import evl.data.expression.binop.Greaterequal;
import evl.data.expression.binop.Is;
import evl.data.expression.binop.Less;
import evl.data.expression.binop.Lessequal;
import evl.data.expression.binop.Minus;
import evl.data.expression.binop.Mod;
import evl.data.expression.binop.Mul;
import evl.data.expression.binop.Notequal;
import evl.data.expression.binop.Or;
import evl.data.expression.binop.Plus;
import evl.data.expression.binop.Shl;
import evl.data.expression.binop.Shr;
import evl.data.expression.reference.RefCall;
import evl.data.expression.reference.RefTemplCall;
import evl.data.expression.reference.Reference;
import evl.data.expression.unop.Not;
import evl.data.expression.unop.Uminus;
import evl.data.type.Type;
import evl.data.variable.Constant;
import evl.data.variable.FuncVariable;
import evl.data.variable.TemplateParameter;
import evl.knowledge.KnowledgeBase;
import evl.traverser.NullTraverser;
import fun.other.ActualTemplateArgument;
import fun.other.Template;
import fun.traverser.Memory;

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
  protected evl.data.expression.Expression visitFuncVariable(FuncVariable obj, Memory param) {
    assert (param.contains(obj));
    return param.get(obj);
  }

  @Override
  protected evl.data.expression.Expression visitTemplateParameter(TemplateParameter obj, Memory param) {
    assert (param.contains(obj));
    return param.get(obj);
  }

  @Override
  protected ActualTemplateArgument visitType(Type obj, Memory param) {
    return obj;
  }

  @Override
  protected evl.data.expression.Expression visitDefault(Evl obj, Memory param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected ActualTemplateArgument visitTemplate(Template obj, Memory param) {
    Evl spec = Specializer.process(obj, new EvlList<ActualTemplateArgument>(), kb);
    return (ActualTemplateArgument) spec;
  }

  @Override
  protected ActualTemplateArgument visitNamedElementsValue(evl.data.expression.NamedElementsValue obj, Memory param) {
    for (evl.data.expression.NamedValue itr : obj.value) {
      Expression value = (Expression) visit(itr.value, param);
      assert (value != null);
      itr.value = value;
    }
    return obj;
  }

  @Override
  protected ActualTemplateArgument visitAnyValue(evl.data.expression.AnyValue obj, Memory param) {
    return obj;
  }

  @Override
  protected evl.data.expression.Expression visitNumber(evl.data.expression.Number obj, Memory param) {
    return obj;
  }

  @Override
  protected evl.data.expression.Expression visitStringValue(evl.data.expression.StringValue obj, Memory param) {
    return obj;
  }

  @Override
  protected ActualTemplateArgument visitBoolValue(evl.data.expression.BoolValue obj, Memory param) {
    return obj;
  }

  @Override
  protected evl.data.expression.Expression visitConstGlobal(evl.data.variable.ConstGlobal obj, Memory param) {
    return (evl.data.expression.Expression) visit(obj.def, new Memory()); // new
    // memory
    // because
    // global
    // constant
    // need no context
  }

  @Override
  protected ActualTemplateArgument visitConstPrivate(evl.data.variable.ConstPrivate obj, Memory param) {
    return visit(obj.def, param);
  }

  @Override
  protected evl.data.expression.Expression visitTupleValue(evl.data.expression.TupleValue obj, Memory param) {
    if (obj.value.size() == 1) {
      return (evl.data.expression.Expression) visit(obj.value.get(0), param);
    } else {
      EvlList<Expression> list = new EvlList<Expression>();
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
      evl.data.variable.Constant cst = (evl.data.variable.Constant) obj.link;
      ActualTemplateArgument eco = visit(cst.def, param);
      cst.def = (Expression) eco;
    }
    return visit(RefEvaluator.execute(obj, param, kb), param);
  }

  @Override
  protected evl.data.expression.Expression visitRefCall(RefCall obj, Memory param) {
    visitExpList(obj.actualParameter.value, param);
    return null;
  }

  @Override
  protected evl.data.expression.Expression visitRefTemplCall(RefTemplCall obj, Memory param) {
    RError.err(ErrorType.Fatal, obj.getInfo(), "reimplement");
    // visitExpList(obj.getActualParameter(), param);
    return null;
  }

  @Override
  protected evl.data.expression.Expression visitRefName(evl.data.expression.reference.RefName obj, Memory param) {
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
    evl.data.expression.Expression left = (evl.data.expression.Expression) visit(obj.left, param);
    evl.data.expression.Expression right = (evl.data.expression.Expression) visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((evl.data.expression.Number) left).value;
      BigInteger rval = ((evl.data.expression.Number) right).value;
      BigInteger res;
      res = lval.and(rval);
      return new Number(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  @Override
  protected ActualTemplateArgument visitDiv(Div obj, Memory param) {
    evl.data.expression.Expression left = (evl.data.expression.Expression) visit(obj.left, param);
    evl.data.expression.Expression right = (evl.data.expression.Expression) visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((evl.data.expression.Number) left).value;
      BigInteger rval = ((evl.data.expression.Number) right).value;
      BigInteger res;
      res = lval.divide(rval);
      return new Number(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  @Override
  protected ActualTemplateArgument visitMinus(Minus obj, Memory param) {
    evl.data.expression.Expression left = (evl.data.expression.Expression) visit(obj.left, param);
    evl.data.expression.Expression right = (evl.data.expression.Expression) visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((evl.data.expression.Number) left).value;
      BigInteger rval = ((evl.data.expression.Number) right).value;
      BigInteger res;
      res = lval.subtract(rval);
      return new Number(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  @Override
  protected ActualTemplateArgument visitMod(Mod obj, Memory param) {
    evl.data.expression.Expression left = (evl.data.expression.Expression) visit(obj.left, param);
    evl.data.expression.Expression right = (evl.data.expression.Expression) visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((evl.data.expression.Number) left).value;
      BigInteger rval = ((evl.data.expression.Number) right).value;
      BigInteger res;
      res = lval.mod(rval);
      return new Number(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  @Override
  protected ActualTemplateArgument visitMul(Mul obj, Memory param) {
    evl.data.expression.Expression left = (evl.data.expression.Expression) visit(obj.left, param);
    evl.data.expression.Expression right = (evl.data.expression.Expression) visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((evl.data.expression.Number) left).value;
      BigInteger rval = ((evl.data.expression.Number) right).value;
      BigInteger res;
      res = lval.multiply(rval);
      return new Number(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  @Override
  protected ActualTemplateArgument visitOr(Or obj, Memory param) {
    evl.data.expression.Expression left = (evl.data.expression.Expression) visit(obj.left, param);
    evl.data.expression.Expression right = (evl.data.expression.Expression) visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((evl.data.expression.Number) left).value;
      BigInteger rval = ((evl.data.expression.Number) right).value;
      BigInteger res;
      res = lval.or(rval);
      return new Number(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  @Override
  protected ActualTemplateArgument visitPlus(Plus obj, Memory param) {
    evl.data.expression.Expression left = (evl.data.expression.Expression) visit(obj.left, param);
    evl.data.expression.Expression right = (evl.data.expression.Expression) visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((evl.data.expression.Number) left).value;
      BigInteger rval = ((evl.data.expression.Number) right).value;
      BigInteger res;
      res = lval.add(rval);
      return new Number(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  @Override
  protected ActualTemplateArgument visitShl(Shl obj, Memory param) {
    evl.data.expression.Expression left = (evl.data.expression.Expression) visit(obj.left, param);
    evl.data.expression.Expression right = (evl.data.expression.Expression) visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((evl.data.expression.Number) left).value;
      BigInteger rval = ((evl.data.expression.Number) right).value;
      BigInteger res;
      res = lval.shiftLeft(getInt(obj.getInfo(), rval));
      return new Number(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  @Override
  protected ActualTemplateArgument visitShr(Shr obj, Memory param) {
    evl.data.expression.Expression left = (evl.data.expression.Expression) visit(obj.left, param);
    evl.data.expression.Expression right = (evl.data.expression.Expression) visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((evl.data.expression.Number) left).value;
      BigInteger rval = ((evl.data.expression.Number) right).value;
      BigInteger res;
      res = lval.shiftRight(getInt(obj.getInfo(), rval));
      return new Number(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  @Override
  protected ActualTemplateArgument visitEqual(Equal obj, Memory param) {
    evl.data.expression.Expression left = (evl.data.expression.Expression) visit(obj.left, param);
    evl.data.expression.Expression right = (evl.data.expression.Expression) visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((evl.data.expression.Number) left).value;
      BigInteger rval = ((evl.data.expression.Number) right).value;
      boolean res = lval.compareTo(rval) == 0;
      return new BoolValue(obj.getInfo(), res);
    } else if (areBool(left, right)) {
      boolean lval = ((evl.data.expression.BoolValue) left).value;
      boolean rval = ((evl.data.expression.BoolValue) right).value;
      boolean res = lval == rval;
      return new BoolValue(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  private boolean areBool(evl.data.expression.Expression left, evl.data.expression.Expression right) {
    return (left instanceof BoolValue) && (right instanceof BoolValue);
  }

  private boolean areNumber(evl.data.expression.Expression left, evl.data.expression.Expression right) {
    return (left instanceof Number) && (right instanceof Number);
  }

  @Override
  protected ActualTemplateArgument visitGreater(Greater obj, Memory param) {
    evl.data.expression.Expression left = (evl.data.expression.Expression) visit(obj.left, param);
    evl.data.expression.Expression right = (evl.data.expression.Expression) visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((evl.data.expression.Number) left).value;
      BigInteger rval = ((evl.data.expression.Number) right).value;
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
    evl.data.expression.Expression left = (evl.data.expression.Expression) visit(obj.left, param);
    evl.data.expression.Expression right = (evl.data.expression.Expression) visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((evl.data.expression.Number) left).value;
      BigInteger rval = ((evl.data.expression.Number) right).value;
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
    evl.data.expression.Expression left = (evl.data.expression.Expression) visit(obj.left, param);
    evl.data.expression.Expression right = (evl.data.expression.Expression) visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((evl.data.expression.Number) left).value;
      BigInteger rval = ((evl.data.expression.Number) right).value;
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
    evl.data.expression.Expression left = (evl.data.expression.Expression) visit(obj.left, param);
    evl.data.expression.Expression right = (evl.data.expression.Expression) visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((evl.data.expression.Number) left).value;
      BigInteger rval = ((evl.data.expression.Number) right).value;
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
    evl.data.expression.Expression left = (evl.data.expression.Expression) visit(obj.left, param);
    evl.data.expression.Expression right = (evl.data.expression.Expression) visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((evl.data.expression.Number) left).value;
      BigInteger rval = ((evl.data.expression.Number) right).value;
      boolean res = lval.compareTo(rval) != 0;
      return new BoolValue(obj.getInfo(), res);
    } else if (areBool(left, right)) {
      boolean lval = ((evl.data.expression.BoolValue) left).value;
      boolean rval = ((evl.data.expression.BoolValue) right).value;
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
    evl.data.expression.Expression expr = (evl.data.expression.Expression) visit(obj.expr, param);

    if ((expr instanceof Number)) {
      BigInteger eval = ((evl.data.expression.Number) expr).value;
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
    evl.data.expression.Expression expr = (evl.data.expression.Expression) visit(obj.expr, param);

    if ((expr instanceof BoolValue)) {
      return new BoolValue(obj.getInfo(), !((BoolValue) expr).value);
    } else {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Can not evaluate not on " + expr.getClass().getName());
      return obj;
    }
  }

}
