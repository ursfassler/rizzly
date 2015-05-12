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

package ast.specification;

import java.util.Collection;

import ast.data.Ast;
import ast.data.component.composition.CompUse;
import ast.data.expression.binop.BinaryExp;
import ast.data.expression.unop.UnaryExp;
import ast.data.expression.value.BoolValue;
import ast.data.expression.value.NumberValue;
import ast.data.expression.value.StringValue;
import ast.data.function.Function;
import ast.data.reference.RefCall;
import ast.data.reference.RefIndex;
import ast.data.reference.RefName;
import ast.data.reference.Reference;
import ast.data.type.base.EnumElement;
import ast.data.type.base.EnumType;
import ast.data.type.base.RangeType;
import ast.data.type.composed.RecordType;
import ast.data.type.special.NaturalType;
import ast.data.variable.ConstGlobal;
import ast.data.variable.FuncVariable;
import ast.data.variable.StateVariable;
import ast.dispatcher.NullDispatcher;

public class SimpleExpression extends Specification {
  static public final SimpleExpression INSTANCE = new SimpleExpression();
  static private final SimpleGetter getter = new SimpleGetter();

  @Override
  public boolean isSatisfiedBy(Ast candidate) {
    return getter.traverse(candidate, null);
  }

}

class SimpleGetter extends NullDispatcher<Boolean, Void> {

  @Override
  protected Boolean visitDefault(Ast obj, Void param) {
    throw new UnsupportedOperationException("Not supported yet: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Boolean visitList(Collection<? extends Ast> list, Void param) {
    for (Ast ast : list) {
      if (!visit(ast, param)) {
        return false;
      }
    }
    return true;
  }

  @Override
  protected Boolean visitReference(Reference obj, Void param) {
    boolean ret = visit(obj.link, param) & visitList(obj.offset, param);
    return ret;
  }

  @Override
  protected Boolean visitRefCall(RefCall obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitRefIndex(RefIndex obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitRefName(RefName obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitNaturalType(NaturalType obj, Void param) {
    throw new UnsupportedOperationException("Not supported yet");
  }

  @Override
  protected Boolean visitRangeType(RangeType obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitEnumType(EnumType obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitEnumElement(EnumElement obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitRecordType(RecordType obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitCompUse(CompUse obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitConstGlobal(ConstGlobal obj, Void param) {
    return visit(obj.type, param);
  }

  @Override
  protected Boolean visitFuncVariable(FuncVariable obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitStateVariable(StateVariable obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitBinaryExp(BinaryExp obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitBoolValue(BoolValue obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitNumber(NumberValue obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitStringValue(StringValue obj, Void param) {
    throw new UnsupportedOperationException("Not supported yet");
  }

  @Override
  protected Boolean visitUnaryExp(UnaryExp obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitFunction(Function obj, Void param) {
    return false;
  }

}
