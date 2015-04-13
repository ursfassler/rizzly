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
import evl.data.Evl;
import evl.data.EvlList;
import evl.data.expression.Expression;
import evl.data.expression.Number;
import evl.data.expression.reference.RefCall;
import evl.data.expression.reference.RefIndex;
import evl.data.expression.reference.RefItem;
import evl.data.expression.reference.RefTemplCall;
import evl.data.function.Function;
import evl.data.function.header.FuncFunction;
import evl.data.type.Type;
import evl.data.type.base.BaseType;
import evl.data.variable.Constant;
import evl.data.variable.Variable;
import evl.knowledge.KnowledgeBase;
import evl.traverser.NullTraverser;
import fun.traverser.Memory;

public class RefEvaluator extends NullTraverser<Expression, Evl> {
  final private Memory memory;
  final private KnowledgeBase kb;

  public RefEvaluator(Memory memory, KnowledgeBase kb) {
    super();
    this.memory = memory;
    this.kb = kb;
  }

  public static Evl execute(evl.data.expression.reference.Reference ref, Memory memory, KnowledgeBase kb) {
    if (ref.link instanceof Constant) {
      Evl val = ((evl.data.variable.Constant) ref.link).def;
      Evl item = RefEvaluator.execute(val, ref.offset, memory, kb);
      return item;
    } else if (ref.link instanceof Variable) {
      Expression val = memory.get((evl.data.variable.Variable) ref.link);
      Evl item = RefEvaluator.execute(val, ref.offset, memory, kb);
      return item;
    } else if (ref.link instanceof Function) {
      Evl item = RefEvaluator.execute(ref.link, ref.offset, memory, kb);
      return item;
    } else if (ref.link instanceof Type) {
      Evl item = RefEvaluator.execute(ref.link, ref.offset, memory, kb);
      return item;
    } else {
      assert (ref.offset.isEmpty());
      return ref.link;
    }
  }

  public static Evl execute(Evl root, EvlList<RefItem> offset, Memory memory, KnowledgeBase kb) {
    RefEvaluator evaluator = new RefEvaluator(memory, kb);

    for (evl.data.expression.reference.RefItem ri : offset) {
      root = evaluator.traverse(ri, root);
    }
    return root;
  }

  @Override
  protected evl.data.expression.Expression visitDefault(Evl obj, Evl param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getName());
  }

  private Expression eval(Expression expr) {
    return (Expression) ExprEvaluator.evaluate(expr, memory, kb);
  }

  private evl.data.expression.Expression call(FuncFunction func, EvlList<Expression> value) {
    return StmtExecutor.process(func, value, new Memory(), kb);
  }

  @Override
  protected evl.data.expression.Expression visitRefCall(RefCall obj, Evl param) {
    if (param instanceof BaseType) {
      EvlList<Expression> values = obj.actualParameter.value;
      RError.ass(values.size() == 1, obj.getInfo(), "expected exactly 1 argument, got " + values.size());
      Expression value = eval(values.get(0));
      return CheckTypeCast.check((Type) param, value);
    } else {
      // TODO add execution of type cast (see casual/ctfeCast.rzy)
      RError.ass(param instanceof FuncFunction, param.getInfo(), "expected funtion, got " + param.getClass().getName());
      FuncFunction func = (FuncFunction) param;
      return call(func, obj.actualParameter.value);
    }
  }

  @Override
  protected evl.data.expression.Expression visitRefName(evl.data.expression.reference.RefName obj, Evl param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected evl.data.expression.Expression visitRefIndex(RefIndex obj, Evl param) {
    evl.data.expression.TupleValue value = (evl.data.expression.TupleValue) param;

    evl.data.expression.Expression idx = eval(obj.index);
    assert (idx instanceof Number);
    int ii = ((evl.data.expression.Number) idx).value.intValue();
    evl.data.expression.Expression elem = value.value.get(ii);

    return elem;
  }

  @Override
  protected evl.data.expression.Expression visitRefTemplCall(RefTemplCall obj, Evl param) {
    RError.err(ErrorType.Fatal, obj.getInfo(), "Can not evaluate template");
    return null;
  }

}
