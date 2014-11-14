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
import fun.Fun;
import fun.NullTraverser;
import fun.expression.ArithmeticOp;
import fun.expression.ArrayValue;
import fun.expression.BoolValue;
import fun.expression.Expression;
import fun.expression.Number;
import fun.expression.Relation;
import fun.expression.StringValue;
import fun.expression.UnaryExpression;
import fun.expression.reference.RefCall;
import fun.expression.reference.RefIndex;
import fun.expression.reference.RefItem;
import fun.expression.reference.RefName;
import fun.expression.reference.RefTemplCall;
import fun.expression.reference.Reference;
import fun.function.FuncFunction;
import fun.knowledge.KnowledgeBase;
import fun.other.ActualTemplateArgument;
import fun.other.FunList;
import fun.other.Template;
import fun.traverser.Memory;
import fun.type.Type;
import fun.variable.ConstGlobal;
import fun.variable.FuncVariable;
import fun.variable.TemplateParameter;
import fun.variable.Variable;

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

  private Fun executeRef(Fun obj, RefItem param, Memory memory) {
    if (param instanceof RefTemplCall) {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Can not evaluate template");
      return null;
    } else if (param instanceof RefCall) {
      assert (obj instanceof FuncFunction);
      FuncFunction func = (FuncFunction) obj;
      return StmtExecutor.process(func, ((RefCall) param).getActualParameter(), new Memory(), kb);
    } else if (param instanceof RefIndex) {
      Variable var = (Variable) obj;
      Expression value = memory.get(var);

      Expression idx = (Expression) visit(((RefIndex) param).getIndex(), memory);
      Expression elem = ElementGetter.INSTANCE.traverse(idx, value);

      return elem;
    } else {
      throw new RuntimeException("Dont know what to do with: " + param.getClass().getCanonicalName());
    }
  }

  @Override
  protected Expression visitFuncVariable(FuncVariable obj, Memory param) {
    assert (param.contains(obj));
    return param.get(obj);
  }

  @Override
  protected Expression visitTemplateParameter(TemplateParameter obj, Memory param) {
    assert (param.contains(obj));
    return param.get(obj);
  }

  @Override
  protected ActualTemplateArgument visitType(Type obj, Memory param) {
    return obj;
  }

  @Override
  protected Expression visitDefault(Fun obj, Memory param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected ActualTemplateArgument visitDeclaration(Template obj, Memory param) {
    Fun spec = Specializer.process(obj, new FunList<ActualTemplateArgument>(), kb);
    return (ActualTemplateArgument) spec;
  }

  @Override
  protected Expression visitNumber(Number obj, Memory param) {
    return obj;
  }

  @Override
  protected Expression visitStringValue(StringValue obj, Memory param) {
    return obj;
  }

  @Override
  protected Expression visitConstGlobal(ConstGlobal obj, Memory param) {
    return (Expression) visit(obj.getDef(), new Memory()); // new memory because global constant need no context
  }

  @Override
  protected ActualTemplateArgument visitReference(Reference obj, Memory param) {

    Fun item = obj.getLink();

    for (RefItem itr : obj.getOffset()) {
      item = executeRef(item, itr, param);
    }

    return visit(item, param);
  }

  @Override
  protected Expression visitRefCall(RefCall obj, Memory param) {
    visitExpList(obj.getActualParameter(), param);
    return null;
  }

  @Override
  protected Expression visitRefCompcall(RefTemplCall obj, Memory param) {
    RError.err(ErrorType.Fatal, obj.getInfo(), "reimplement");
    // visitExpList(obj.getActualParameter(), param);
    return null;
  }

  @Override
  protected Expression visitRefName(RefName obj, Memory param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Expression visitRefIndex(RefIndex obj, Memory param) {
    obj.setIndex((Expression) visit(obj.getIndex(), param));
    return null;
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
  protected Expression visitArithmeticOp(ArithmeticOp obj, Memory param) {
    Expression left = (Expression) visit(obj.getLeft(), param);
    Expression right = (Expression) visit(obj.getRight(), param);

    if ((left instanceof Number) && (right instanceof Number)) {
      BigInteger lval = ((Number) left).getValue();
      BigInteger rval = ((Number) right).getValue();
      BigInteger res;

      switch (obj.getOp()) {
        case AND:
          res = lval.and(rval);
          break;
        case DIV:
          res = lval.divide(rval);
          break;
        case MINUS:
          res = lval.subtract(rval);
          break;
        case MOD:
          res = lval.mod(rval);
          break;
        case MUL:
          res = lval.multiply(rval);
          break;
        case OR:
          res = lval.or(rval);
          break;
        case PLUS:
          res = lval.add(rval);
          break;
        case SHL:
          res = lval.shiftLeft(getInt(obj.getInfo(), rval));
          break;
        case SHR:
          res = lval.shiftRight(getInt(obj.getInfo(), rval));
          break;
        default:
          RError.err(ErrorType.Fatal, obj.getInfo(), "Operator not yet implemented: " + obj.getOp());
          return obj;
      }
      return new Number(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  @Override
  protected Expression visitRelation(Relation obj, Memory param) {
    Expression left = (Expression) visit(obj.getLeft(), param);
    Expression right = (Expression) visit(obj.getRight(), param);

    if ((left instanceof Number) && (right instanceof Number)) {
      BigInteger lval = ((Number) left).getValue();
      BigInteger rval = ((Number) right).getValue();
      boolean res;

      switch (obj.getOp()) {
        case EQUAL:
          res = lval.compareTo(rval) == 0;
          break;
        case GREATER:
          res = lval.compareTo(rval) > 0;
          break;
        case GREATER_EQUEAL:
          res = lval.compareTo(rval) >= 0;
          break;
        case LESS:
          res = lval.compareTo(rval) < 0;
          break;
        case LESS_EQUAL:
          res = lval.compareTo(rval) <= 0;
          break;
        case NOT_EQUAL:
          res = lval.compareTo(rval) != 0;
          break;
        default:
          RError.err(ErrorType.Fatal, obj.getInfo(), "Operator not yet implemented: " + obj.getOp());
          return null;
      }
      return new BoolValue(obj.getInfo(), res);
    } else if ((left instanceof BoolValue) && (right instanceof BoolValue)) {
      boolean lval = ((BoolValue) left).isValue();
      boolean rval = ((BoolValue) right).isValue();
      boolean res;

      switch (obj.getOp()) {
        case EQUAL:
          res = lval == rval;
          break;
        case NOT_EQUAL:
          res = lval != rval;
          break;
        default:
          RError.err(ErrorType.Fatal, obj.getInfo(), "Operator not yet implemented: " + obj.getOp());
          return obj;
      }
      return new BoolValue(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  @Override
  protected Expression visitArrayValue(ArrayValue obj, Memory param) {
    visitExpList(obj.getValue(), param);
    return obj;
  }

  @Override
  protected Expression visitUnaryExpression(UnaryExpression obj, Memory param) {
    Expression expr = (Expression) visit(obj.getExpr(), param);

    if ((expr instanceof Number)) {
      BigInteger eval = ((Number) expr).getValue();
      BigInteger res;

      switch (obj.getOp()) {
        case MINUS:
          res = eval.negate();
          break;
        default:
          RError.err(ErrorType.Fatal, obj.getInfo(), "Operator not yet implemented: " + obj.getOp());
          return obj;
      }
      return new Number(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

}
