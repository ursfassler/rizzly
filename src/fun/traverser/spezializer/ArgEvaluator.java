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

import error.ErrorType;
import error.RError;
import evl.data.Evl;
import evl.data.expression.Expression;
import evl.data.type.Type;
import evl.data.type.base.RangeType;
import evl.data.type.template.TypeType;
import evl.knowledge.KnowledgeBase;
import evl.traverser.NullTraverser;
import fun.other.ActualTemplateArgument;
import fun.traverser.Memory;

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
  protected ActualTemplateArgument visitDefault(Evl obj, ActualTemplateArgument param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected ActualTemplateArgument visitIntegerType(evl.data.type.special.IntegerType obj, ActualTemplateArgument param) {
    evl.data.expression.Number num = (evl.data.expression.Number) ExprEvaluator.evaluate((Expression) param, new Memory(), kb);
    return num;
  }

  @Override
  protected ActualTemplateArgument visitNaturalType(evl.data.type.special.NaturalType obj, ActualTemplateArgument param) {
    evl.data.expression.Number num = (evl.data.expression.Number) ExprEvaluator.evaluate((Expression) param, new Memory(), kb);
    if (num.value.compareTo(BigInteger.ZERO) < 0) {
      RError.err(ErrorType.Error, param.getInfo(), "Value for Natural type has to be >= 0");
    }
    return num;
  }

  @Override
  protected ActualTemplateArgument visitRangeType(RangeType obj, ActualTemplateArgument param) {
    evl.data.expression.Number num = (evl.data.expression.Number) ExprEvaluator.evaluate((Expression) param, new Memory(), kb);
    // TODO check type
    return num;
  }

  @Override
  protected ActualTemplateArgument visitAnyType(evl.data.type.special.AnyType obj, ActualTemplateArgument param) {
    if (!(param instanceof Type)) {
      param = Specializer.evalType((evl.data.expression.reference.Reference) param, kb);
    }
    assert (param instanceof Type);
    return param;
  }

  @Override
  protected ActualTemplateArgument visitTypeType(TypeType obj, ActualTemplateArgument param) {
    Evl evald = Specializer.eval(param, kb);
    // TODO check type
    return (ActualTemplateArgument) evald;
  }

}
