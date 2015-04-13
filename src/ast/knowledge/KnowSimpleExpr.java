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

package ast.knowledge;

import ast.data.Ast;
import ast.data.AstList;
import ast.data.component.composition.CompUse;
import ast.data.expression.BoolValue;
import ast.data.expression.Expression;
import ast.data.expression.Number;
import ast.data.expression.StringValue;
import ast.data.expression.binop.BinaryExp;
import ast.data.expression.reference.RefCall;
import ast.data.expression.reference.RefIndex;
import ast.data.expression.reference.RefName;
import ast.data.expression.reference.Reference;
import ast.data.expression.reference.SimpleRef;
import ast.data.expression.unop.UnaryExp;
import ast.data.function.Function;
import ast.data.type.base.EnumElement;
import ast.data.type.base.EnumType;
import ast.data.type.base.RangeType;
import ast.data.type.composed.RecordType;
import ast.data.type.special.NaturalType;
import ast.data.variable.ConstGlobal;
import ast.data.variable.FuncVariable;
import ast.data.variable.StateVariable;
import ast.traverser.NullTraverser;

/**
 *
 * @author urs
 */
public class KnowSimpleExpr {

  static final SimpleGetter getter = new SimpleGetter();

  public static boolean isSimple(Expression expr) {
    return getter.traverse(expr, null);
  }
}

class SimpleGetter extends NullTraverser<Boolean, Void> {

  @Override
  protected Boolean visitDefault(Ast obj, Void param) {
    throw new UnsupportedOperationException("Not supported yet: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Boolean visitList(AstList<? extends Ast> list, Void param) {
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
  protected Boolean visitSimpleRef(SimpleRef obj, Void param) {
    return visit(obj.link, param);
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
    return visit(obj.type, param); // TODO ok?
  }

  @Override
  protected Boolean visitFuncVariable(FuncVariable obj, Void param) {
    return true;
    // return visit(obj.getType(),param);
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
  protected Boolean visitNumber(Number obj, Void param) {
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
