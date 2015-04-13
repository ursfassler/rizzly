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

import ast.data.Ast;
import ast.data.expression.Expression;
import ast.data.template.ActualTemplateArgument;
import ast.data.type.Type;
import ast.data.type.base.RangeType;
import ast.data.type.template.TypeType;
import ast.interpreter.Memory;
import ast.knowledge.KnowledgeBase;
import ast.traverser.NullTraverser;
import error.ErrorType;
import error.RError;

public class ArgEvaluator extends NullTraverser<ActualTemplateArgument, ActualTemplateArgument> {
  final private KnowledgeBase kb;

  public ArgEvaluator(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  public static ActualTemplateArgument process(Type type, ActualTemplateArgument acarg, KnowledgeBase kb) {
    ArgEvaluator argEvaluator = new ArgEvaluator(kb);
    return argEvaluator.traverse(type, acarg);
  }

  @Override
  protected ActualTemplateArgument visitDefault(Ast obj, ActualTemplateArgument param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected ActualTemplateArgument visitIntegerType(ast.data.type.special.IntegerType obj, ActualTemplateArgument param) {
    ast.data.expression.Number num = (ast.data.expression.Number) ExprEvaluator.evaluate((Expression) param, new Memory(), kb);
    return num;
  }

  @Override
  protected ActualTemplateArgument visitNaturalType(ast.data.type.special.NaturalType obj, ActualTemplateArgument param) {
    ast.data.expression.Number num = (ast.data.expression.Number) ExprEvaluator.evaluate((Expression) param, new Memory(), kb);
    if (num.value.compareTo(BigInteger.ZERO) < 0) {
      RError.err(ErrorType.Error, param.getInfo(), "Value for Natural type has to be >= 0");
    }
    return num;
  }

  @Override
  protected ActualTemplateArgument visitRangeType(RangeType obj, ActualTemplateArgument param) {
    ast.data.expression.Number num = (ast.data.expression.Number) ExprEvaluator.evaluate((Expression) param, new Memory(), kb);
    // TODO check type
    return num;
  }

  @Override
  protected ActualTemplateArgument visitAnyType(ast.data.type.special.AnyType obj, ActualTemplateArgument param) {
    if (!(param instanceof Type)) {
      param = Specializer.evalType((ast.data.expression.reference.Reference) param, kb);
    }
    assert (param instanceof Type);
    return param;
  }

  @Override
  protected ActualTemplateArgument visitTypeType(TypeType obj, ActualTemplateArgument param) {
    Ast evald = Specializer.eval(param, kb);
    // TODO check type
    return (ActualTemplateArgument) evald;
  }

}
