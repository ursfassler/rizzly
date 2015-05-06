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

import ast.data.Ast;
import ast.data.AstList;
import ast.data.expression.Expression;
import ast.data.expression.Number;
import ast.data.expression.reference.RefCall;
import ast.data.expression.reference.RefIndex;
import ast.data.expression.reference.RefItem;
import ast.data.expression.reference.RefTemplCall;
import ast.data.expression.reference.Reference;
import ast.data.function.Function;
import ast.data.function.header.FuncFunction;
import ast.data.template.Template;
import ast.data.type.Type;
import ast.data.type.base.BaseType;
import ast.data.variable.Constant;
import ast.data.variable.Variable;
import ast.interpreter.Memory;
import ast.knowledge.KnowledgeBase;
import ast.traverser.NullTraverser;
import error.ErrorType;
import error.RError;

public class RefEvaluator extends NullTraverser<Expression, Ast> {
  private final InstanceRepo ir;
  final private KnowledgeBase kb;
  final private Memory memory;

  public RefEvaluator(Memory memory, InstanceRepo ir, KnowledgeBase kb) {
    super();
    this.memory = memory;
    this.ir = ir;
    this.kb = kb;
  }

  public static Ast execute(Reference ref, Memory memory, InstanceRepo ir, KnowledgeBase kb) {
    if (ref.link instanceof Constant) {
      Ast val = ((Constant) ref.link).def;
      Ast item = RefEvaluator.execute(val, ref.offset, memory, ir, kb);
      return item;
    } else if (ref.link instanceof Variable) {
      Expression val = memory.get((Variable) ref.link);
      Ast item = RefEvaluator.execute(val, ref.offset, memory, ir, kb);
      return item;
    } else if (ref.link instanceof Function) {
      Ast item = RefEvaluator.execute(ref.link, ref.offset, memory, ir, kb);
      return item;
    } else if (ref.link instanceof Type) {
      Ast item = RefEvaluator.execute(ref.link, ref.offset, memory, ir, kb);
      return item;
    } else if (ref.link instanceof Template) {
      Ast item = RefEvaluator.execute(ref.link, ref.offset, memory, ir, kb);
      return item;
    } else {
      assert (ref.offset.isEmpty());
      return ref.link;
    }
  }

  public static Ast execute(Ast root, AstList<RefItem> offset, Memory memory, InstanceRepo ir, KnowledgeBase kb) {
    RefEvaluator evaluator = new RefEvaluator(memory, ir, kb);

    for (ast.data.expression.reference.RefItem ri : offset) {
      root = evaluator.traverse(ri, root);
    }
    return root;
  }

  @Override
  protected ast.data.expression.Expression visitDefault(Ast obj, Ast param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getName());
  }

  private Expression eval(Expression expr) {
    return ExprEvaluator.evaluate(expr, memory, ir, kb);
  }

  private ast.data.expression.Expression call(FuncFunction func, AstList<Expression> value) {
    return StmtExecutor.process(func, value, new Memory(), ir, kb);
  }

  @Override
  protected ast.data.expression.Expression visitRefCall(RefCall obj, Ast param) {
    if (param instanceof BaseType) {
      AstList<Expression> values = obj.actualParameter.value;
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
  protected ast.data.expression.Expression visitRefName(ast.data.expression.reference.RefName obj, Ast param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected ast.data.expression.Expression visitRefIndex(RefIndex obj, Ast param) {
    ast.data.expression.TupleValue value = (ast.data.expression.TupleValue) param;

    ast.data.expression.Expression idx = eval(obj.index);
    assert (idx instanceof Number);
    int ii = ((ast.data.expression.Number) idx).value.intValue();
    ast.data.expression.Expression elem = value.value.get(ii);

    return elem;
  }

  @Override
  protected ast.data.expression.Expression visitRefTemplCall(RefTemplCall obj, Ast param) {
    RError.err(ErrorType.Fatal, obj.getInfo(), "Can not evaluate template");
    return null;
  }

}
