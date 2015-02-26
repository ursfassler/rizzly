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

import error.ErrorType;
import error.RError;
import fun.Fun;
import fun.NullTraverser;
import fun.expression.Expression;
import fun.expression.Number;
import fun.expression.TupleValue;
import fun.expression.reference.RefCall;
import fun.expression.reference.RefIndex;
import fun.expression.reference.RefItem;
import fun.expression.reference.RefName;
import fun.expression.reference.RefTemplCall;
import fun.expression.reference.Reference;
import fun.function.FuncFunction;
import fun.function.FuncImpl;
import fun.knowledge.KnowledgeBase;
import fun.other.FunList;
import fun.traverser.Memory;
import fun.type.Type;
import fun.type.base.BaseType;
import fun.variable.Constant;
import fun.variable.Variable;

public class RefEvaluator extends NullTraverser<Expression, Fun> {
  final private Memory memory;
  final private KnowledgeBase kb;

  public RefEvaluator(Memory memory, KnowledgeBase kb) {
    super();
    this.memory = memory;
    this.kb = kb;
  }

  public static Fun execute(Reference ref, Memory memory, KnowledgeBase kb) {
    if (ref.getLink() instanceof Constant) {
      Fun val = ((Constant) ref.getLink()).getDef();
      Fun item = RefEvaluator.execute(val, ref.getOffset(), memory, kb);
      return item;
    } else if (ref.getLink() instanceof Variable) {
      Expression val = memory.get((Variable) ref.getLink());
      Fun item = RefEvaluator.execute(val, ref.getOffset(), memory, kb);
      return item;
    } else if (ref.getLink() instanceof FuncImpl) {
      Fun item = RefEvaluator.execute(ref.getLink(), ref.getOffset(), memory, kb);
      return item;
    } else if (ref.getLink() instanceof Type) {
      Fun item = RefEvaluator.execute(ref.getLink(), ref.getOffset(), memory, kb);
      return item;
    } else {
      assert (ref.getOffset().isEmpty());
      return ref.getLink();
    }
  }

  public static Fun execute(Fun root, FunList<RefItem> offset, Memory memory, KnowledgeBase kb) {
    RefEvaluator evaluator = new RefEvaluator(memory, kb);

    for (RefItem ri : offset) {
      root = evaluator.traverse(ri, root);
    }
    return root;
  }

  @Override
  protected Expression visitDefault(Fun obj, Fun param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getName());
  }

  private Expression eval(Expression expr) {
    return (Expression) ExprEvaluator.evaluate(expr, memory, kb);
  }

  private Expression call(FuncFunction func, FunList<Expression> value) {
    return StmtExecutor.process(func, value, new Memory(), kb);
  }

  @Override
  protected Expression visitRefCall(RefCall obj, Fun param) {
    if (param instanceof BaseType) {
      FunList<Expression> values = obj.getActualParameter().getValue();
      RError.ass(values.size() == 1, obj.getInfo(), "expected exactly 1 argument, got " + values.size());
      Expression value = eval(values.get(0));
      return CheckTypeCast.check((Type) param, value);
    } else {
      // TODO add execution of type cast (see casual/ctfeCast.rzy)
      RError.ass(param instanceof FuncFunction, param.getInfo(), "expected funtion, got " + param.getClass().getName());
      FuncFunction func = (FuncFunction) param;
      return call(func, obj.getActualParameter().getValue());
    }
  }

  @Override
  protected Expression visitRefName(RefName obj, Fun param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Expression visitRefIndex(RefIndex obj, Fun param) {
    TupleValue value = (TupleValue) param;

    Expression idx = eval(obj.getIndex());
    assert (idx instanceof Number);
    int ii = ((Number) idx).getValue().intValue();
    Expression elem = value.getValue().get(ii);

    return elem;
  }

  @Override
  protected Expression visitRefTemplCall(RefTemplCall obj, Fun param) {
    RError.err(ErrorType.Fatal, obj.getInfo(), "Can not evaluate template");
    return null;
  }

}
