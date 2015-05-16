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

import ast.ElementInfo;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.Named;
import ast.data.expression.Expression;
import ast.data.expression.value.NumberValue;
import ast.data.expression.value.TupleValue;
import ast.data.function.header.FuncFunction;
import ast.data.reference.RefCall;
import ast.data.reference.RefIndex;
import ast.data.reference.RefItem;
import ast.data.reference.RefTemplCall;
import ast.data.reference.Reference;
import ast.data.template.Template;
import ast.data.variable.Constant;
import ast.data.variable.FuncVariable;
import ast.data.variable.Variable;
import ast.dispatcher.NullDispatcher;
import ast.interpreter.Memory;
import ast.knowledge.KnowledgeBase;
import error.ErrorType;
import error.RError;

public class RefEvaluator extends NullDispatcher<Ast, Ast> {
  final private KnowledgeBase kb;
  final private Memory memory;

  public RefEvaluator(Memory memory, KnowledgeBase kb) {
    super();
    this.memory = memory;
    this.kb = kb;
  }

  public static Ast execute(Reference ref, Memory memory, KnowledgeBase kb) {
    Ast base = getBase(ref.link, memory);
    return RefEvaluator.execute(base, ref.offset, memory, kb);
  }

  private static Ast getBase(Named link, Memory memory) {
    if (link instanceof Constant) {
      return ((Constant) link).def;
    } else if (link instanceof Variable) {
      return memory.get((Variable) link);
    } else if (link instanceof Template) {
      return link;
    } else {
      return link;
    }
  }

  public static Ast execute(Ast root, AstList<RefItem> offset, Memory memory, KnowledgeBase kb) {
    RefEvaluator evaluator = new RefEvaluator(memory, kb);

    for (RefItem ri : offset) {
      root = evaluator.traverse(ri, root);
    }
    return root;
  }

  @Override
  protected Expression visitDefault(Ast obj, Ast param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getName());
  }

  private Expression eval(Expression expr) {
    return ExprEvaluator.evaluate(expr, memory, kb);
  }

  private void checkArguments(ElementInfo info, AstList<FuncVariable> param, AstList<Expression> actparam) {
    if (param.size() != actparam.size()) {
      RError.err(ErrorType.Error, info, "Number of function arguments does not match: need " + param.size() + ", got " + actparam.size());
    }
  }

  private Expression call(FuncFunction func, AstList<Expression> value) {
    return StmtExecutor.process(func, value, new Memory(), kb);
  }

  @Override
  protected Expression visitRefCall(RefCall obj, Ast param) {
    RError.ass(param instanceof FuncFunction, param.getInfo(), "expected funtion, got " + param.getClass().getName());
    FuncFunction func = (FuncFunction) param;
    checkArguments(obj.getInfo(), func.param, obj.actualParameter.value);
    return call(func, obj.actualParameter.value);
  }

  @Override
  protected Expression visitRefName(ast.data.reference.RefName obj, Ast param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Expression visitRefIndex(RefIndex obj, Ast param) {
    TupleValue value = (TupleValue) param;

    Expression idx = eval(obj.index);
    assert (idx instanceof NumberValue);
    int ii = ((NumberValue) idx).value.intValue();
    Expression elem = value.value.get(ii);

    return elem;
  }

  @Override
  protected Ast visitRefTemplCall(RefTemplCall obj, Ast param) {
    Template template = (Template) param;

    return Specializer.specialize(template, obj.actualParameter, kb);
  }

}
