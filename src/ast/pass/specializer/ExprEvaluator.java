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
import ast.data.Range;
import ast.data.expression.Expression;
import ast.data.expression.RefExp;
import ast.data.expression.TypeCast;
import ast.data.expression.binop.And;
import ast.data.expression.binop.Div;
import ast.data.expression.binop.Equal;
import ast.data.expression.binop.Greater;
import ast.data.expression.binop.GreaterEqual;
import ast.data.expression.binop.Is;
import ast.data.expression.binop.Less;
import ast.data.expression.binop.LessEqual;
import ast.data.expression.binop.Minus;
import ast.data.expression.binop.Mod;
import ast.data.expression.binop.Mul;
import ast.data.expression.binop.NotEqual;
import ast.data.expression.binop.Or;
import ast.data.expression.binop.Plus;
import ast.data.expression.binop.Shl;
import ast.data.expression.binop.Shr;
import ast.data.expression.unop.Not;
import ast.data.expression.unop.Uminus;
import ast.data.expression.value.AnyValue;
import ast.data.expression.value.BoolValue;
import ast.data.expression.value.NamedElementsValue;
import ast.data.expression.value.NamedValue;
import ast.data.expression.value.NumberValue;
import ast.data.expression.value.StringValue;
import ast.data.expression.value.TupleValue;
import ast.data.expression.value.ValueExpr;
import ast.data.reference.RefCall;
import ast.data.reference.RefName;
import ast.data.reference.RefTemplCall;
import ast.data.reference.Reference;
import ast.data.type.Type;
import ast.data.type.base.RangeType;
import ast.data.variable.Constant;
import ast.data.variable.FuncVariable;
import ast.data.variable.TemplateParameter;
import ast.dispatcher.NullDispatcher;
import ast.interpreter.Memory;
import ast.knowledge.KnowledgeBase;
import error.ErrorType;
import error.RError;

public class ExprEvaluator extends NullDispatcher<ValueExpr, Void> {
  private final Memory memory;
  private final KnowledgeBase kb;

  public ExprEvaluator(Memory memory, KnowledgeBase kb) {
    super();
    this.memory = memory;
    this.kb = kb;
  }

  public static ValueExpr evaluate(Expression obj, Memory memory, KnowledgeBase kb) {
    ExprEvaluator evaluator = new ExprEvaluator(memory, kb);
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
  protected ValueExpr visitFuncVariable(FuncVariable obj, Void param) {
    assert (memory.contains(obj));
    return memory.get(obj);
  }

  @Override
  protected ValueExpr visitTemplateParameter(TemplateParameter obj, Void param) {
    assert (memory.contains(obj));
    return memory.get(obj);
  }

  @Override
  protected ValueExpr visitDefault(Ast obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected ValueExpr visitType(Type obj, Void param) {
    RError.err(ErrorType.Error, obj.getInfo(), "Expected value, got type");
    return null;
  }

  @Override
  protected ValueExpr visitNamedElementsValue(NamedElementsValue obj, Void param) {
    for (NamedValue itr : obj.value) {
      ValueExpr value = visit(itr.value, param);
      assert (value != null);
      itr.value = value;
    }
    return obj;
  }

  @Override
  protected ValueExpr visitAnyValue(AnyValue obj, Void param) {
    return obj;
  }

  @Override
  protected ValueExpr visitNumber(NumberValue obj, Void param) {
    return obj;
  }

  @Override
  protected ValueExpr visitStringValue(StringValue obj, Void param) {
    return obj;
  }

  @Override
  protected ValueExpr visitBoolValue(BoolValue obj, Void param) {
    return obj;
  }

  @Override
  protected ValueExpr visitConstGlobal(ast.data.variable.ConstGlobal obj, Void param) {
    return visit(obj.def, param);
  }

  @Override
  protected ValueExpr visitConstPrivate(ast.data.variable.ConstPrivate obj, Void param) {
    return visit(obj.def, param);
  }

  @Override
  protected ValueExpr visitTupleValue(TupleValue obj, Void param) {
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
  protected ValueExpr visitRefExpr(RefExp obj, Void param) {
    return visit(obj.ref, param);
  }

  @Override
  protected ValueExpr visitReference(Reference obj, Void param) {
    // TODO move constant evaluation to another place
    if (obj.link instanceof Constant) {
      Constant cst = (Constant) obj.link;
      cst.def = visit(cst.def, param);
    }
    return visit(RefEvaluator.execute(obj, memory, kb), param);
  }

  @Override
  protected ValueExpr visitRefCall(RefCall obj, Void param) {
    visitExpList(obj.actualParameter.value, param);
    return null;
  }

  @Override
  protected ValueExpr visitRefTemplCall(RefTemplCall obj, Void param) {
    RError.err(ErrorType.Fatal, obj.getInfo(), "reimplement");
    // visitExpList(obj.getActualParameter(), param);
    return null;
  }

  @Override
  protected ValueExpr visitRefName(RefName obj, Void param) {
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
  protected ValueExpr visitAnd(And obj, Void param) {
    ValueExpr left = visit(obj.left, param);
    ValueExpr right = visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((NumberValue) left).value;
      BigInteger rval = ((NumberValue) right).value;
      BigInteger res;
      res = lval.and(rval);
      return new NumberValue(obj.getInfo(), res);
    } else {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Operator for type not yet implemented: " + obj);
      return null;
    }
  }

  @Override
  protected ValueExpr visitDiv(Div obj, Void param) {
    ValueExpr left = visit(obj.left, param);
    ValueExpr right = visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((NumberValue) left).value;
      BigInteger rval = ((NumberValue) right).value;
      BigInteger res;
      res = lval.divide(rval);
      return new NumberValue(obj.getInfo(), res);
    } else {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Operator for type not yet implemented: " + obj);
      return null;
    }
  }

  @Override
  protected ValueExpr visitMinus(Minus obj, Void param) {
    ValueExpr left = visit(obj.left, param);
    ValueExpr right = visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((NumberValue) left).value;
      BigInteger rval = ((NumberValue) right).value;
      BigInteger res;
      res = lval.subtract(rval);
      return new NumberValue(obj.getInfo(), res);
    } else {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Operator for type not yet implemented: " + obj);
      return null;
    }
  }

  @Override
  protected ValueExpr visitMod(Mod obj, Void param) {
    ValueExpr left = visit(obj.left, param);
    ValueExpr right = visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((NumberValue) left).value;
      BigInteger rval = ((NumberValue) right).value;
      BigInteger res;
      res = lval.mod(rval);
      return new NumberValue(obj.getInfo(), res);
    } else {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Operator for type not yet implemented: " + obj);
      return null;
    }
  }

  @Override
  protected ValueExpr visitMul(Mul obj, Void param) {
    ValueExpr left = visit(obj.left, param);
    ValueExpr right = visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((NumberValue) left).value;
      BigInteger rval = ((NumberValue) right).value;
      BigInteger res;
      res = lval.multiply(rval);
      return new NumberValue(obj.getInfo(), res);
    } else {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Operator for type not yet implemented: " + obj);
      return null;
    }
  }

  @Override
  protected ValueExpr visitOr(Or obj, Void param) {
    ValueExpr left = visit(obj.left, param);
    ValueExpr right = visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((NumberValue) left).value;
      BigInteger rval = ((NumberValue) right).value;
      BigInteger res;
      res = lval.or(rval);
      return new NumberValue(obj.getInfo(), res);
    } else {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Operator for type not yet implemented: " + obj);
      return null;
    }
  }

  @Override
  protected ValueExpr visitPlus(Plus obj, Void param) {
    ValueExpr left = visit(obj.left, param);
    ValueExpr right = visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((NumberValue) left).value;
      BigInteger rval = ((NumberValue) right).value;
      BigInteger res;
      res = lval.add(rval);
      return new NumberValue(obj.getInfo(), res);
    } else {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Operator for type not yet implemented: " + obj);
      return null;
    }
  }

  @Override
  protected ValueExpr visitShl(Shl obj, Void param) {
    ValueExpr left = visit(obj.left, param);
    ValueExpr right = visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((NumberValue) left).value;
      BigInteger rval = ((NumberValue) right).value;
      BigInteger res;
      res = lval.shiftLeft(getInt(obj.getInfo(), rval));
      return new NumberValue(obj.getInfo(), res);
    } else {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Operator for type not yet implemented: " + obj);
      return null;
    }
  }

  @Override
  protected ValueExpr visitShr(Shr obj, Void param) {
    ValueExpr left = visit(obj.left, param);
    ValueExpr right = visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((NumberValue) left).value;
      BigInteger rval = ((NumberValue) right).value;
      BigInteger res;
      res = lval.shiftRight(getInt(obj.getInfo(), rval));
      return new NumberValue(obj.getInfo(), res);
    } else {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Operator for type not yet implemented: " + obj);
      return null;
    }
  }

  @Override
  protected ValueExpr visitEqual(Equal obj, Void param) {
    ValueExpr left = visit(obj.left, param);
    ValueExpr right = visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((NumberValue) left).value;
      BigInteger rval = ((NumberValue) right).value;
      boolean res = lval.compareTo(rval) == 0;
      return new BoolValue(obj.getInfo(), res);
    } else if (areBool(left, right)) {
      boolean lval = ((BoolValue) left).value;
      boolean rval = ((BoolValue) right).value;
      boolean res = lval == rval;
      return new BoolValue(obj.getInfo(), res);
    } else {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Operator for type not yet implemented: " + obj);
      return null;
    }
  }

  private boolean areBool(ValueExpr left, ValueExpr right) {
    return (left instanceof BoolValue) && (right instanceof BoolValue);
  }

  private boolean areNumber(ValueExpr left, ValueExpr right) {
    return (left instanceof NumberValue) && (right instanceof NumberValue);
  }

  @Override
  protected ValueExpr visitGreater(Greater obj, Void param) {
    ValueExpr left = visit(obj.left, param);
    ValueExpr right = visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((NumberValue) left).value;
      BigInteger rval = ((NumberValue) right).value;
      boolean res = lval.compareTo(rval) > 0;
      return new BoolValue(obj.getInfo(), res);
    } else if (areBool(left, right)) {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Operator not yet implemented: " + obj);
      return null;
    } else {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Operator for type not yet implemented: " + obj);
      return null;
    }
  }

  @Override
  protected ValueExpr visitGreaterequal(GreaterEqual obj, Void param) {
    ValueExpr left = visit(obj.left, param);
    ValueExpr right = visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((NumberValue) left).value;
      BigInteger rval = ((NumberValue) right).value;
      boolean res = lval.compareTo(rval) >= 0;
      return new BoolValue(obj.getInfo(), res);
    } else if (areBool(left, right)) {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Operator not yet implemented: " + obj);
      return null;
    } else {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Operator for type not yet implemented: " + obj);
      return null;
    }
  }

  @Override
  protected ValueExpr visitLess(Less obj, Void param) {
    ValueExpr left = visit(obj.left, param);
    ValueExpr right = visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((NumberValue) left).value;
      BigInteger rval = ((NumberValue) right).value;
      boolean res = lval.compareTo(rval) < 0;
      return new BoolValue(obj.getInfo(), res);
    } else if (areBool(left, right)) {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Operator not yet implemented: " + obj);
      return null;
    } else {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Operator for type not yet implemented: " + obj);
      return null;
    }
  }

  @Override
  protected ValueExpr visitLessequal(LessEqual obj, Void param) {
    ValueExpr left = visit(obj.left, param);
    ValueExpr right = visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((NumberValue) left).value;
      BigInteger rval = ((NumberValue) right).value;
      boolean res = lval.compareTo(rval) <= 0;
      return new BoolValue(obj.getInfo(), res);
    } else if (areBool(left, right)) {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Operator not yet implemented: " + obj);
      return null;
    } else {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Operator for type not yet implemented: " + obj);
      return null;
    }
  }

  @Override
  protected ValueExpr visitNotequal(NotEqual obj, Void param) {
    ValueExpr left = visit(obj.left, param);
    ValueExpr right = visit(obj.right, param);

    if (areNumber(left, right)) {
      BigInteger lval = ((NumberValue) left).value;
      BigInteger rval = ((NumberValue) right).value;
      boolean res = lval.compareTo(rval) != 0;
      return new BoolValue(obj.getInfo(), res);
    } else if (areBool(left, right)) {
      boolean lval = ((BoolValue) left).value;
      boolean rval = ((BoolValue) right).value;
      boolean res = lval != rval;
      return new BoolValue(obj.getInfo(), res);
    } else {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Operator for type not yet implemented: " + obj);
      return null;
    }
  }

  @Override
  protected ValueExpr visitIs(Is obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected ValueExpr visitUminus(Uminus obj, Void param) {
    ValueExpr expr = visit(obj.expr, param);

    if ((expr instanceof NumberValue)) {
      BigInteger eval = ((NumberValue) expr).value;
      BigInteger res;

      res = eval.negate();
      return new NumberValue(obj.getInfo(), res);
    } else {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Can not evaluate unary minus on " + expr.getClass().getName());
      return null;
    }
  }

  @Override
  protected ValueExpr visitNot(Not obj, Void param) {
    ValueExpr expr = visit(obj.expr, param);

    if ((expr instanceof BoolValue)) {
      return new BoolValue(obj.getInfo(), !((BoolValue) expr).value);
    } else {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Can not evaluate not on " + expr.getClass().getName());
      return null;
    }
  }

  @Override
  protected ValueExpr visitTypeCast(TypeCast obj, Void param) {
    ValueExpr expr = visit(obj.value, param);

    if ((expr instanceof NumberValue)) {
      TypeEvalExecutor.eval(obj.cast.ref, kb);
      assert (obj.cast.ref.offset.isEmpty());
      assert (obj.cast.ref.link instanceof RangeType);

      Range range = ((RangeType) obj.cast.ref.link).range;
      BigInteger eval = ((NumberValue) expr).value;

      if (!range.contains(eval)) {
        RError.err(ErrorType.Error, obj.getInfo(), "Value not in range: " + eval + " not in " + range);
        return null;
      }

      return expr;
    } else {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Can not evaluate unary minus on " + expr.getClass().getName());
      return null;
    }
  }
}
