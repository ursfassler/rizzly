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

package fun.toevl;

import evl.data.Evl;
import evl.data.component.Component;
import evl.data.expression.reference.SimpleRef;
import evl.data.type.Type;
import evl.data.variable.Variable;
import fun.Fun;
import fun.NullTraverser;
import fun.expression.Expression;
import fun.expression.reference.Reference;
import fun.other.CompImpl;
import fun.variable.CompUse;
import fun.variable.ConstGlobal;
import fun.variable.ConstPrivate;
import fun.variable.FuncVariable;
import fun.variable.StateVariable;

public class FunToEvlVariable extends NullTraverser<Evl, Void> {
  private FunToEvl fta;

  public FunToEvlVariable(FunToEvl fta) {
    super();
    this.fta = fta;
  }

  @Override
  protected Variable visitDefault(Fun obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  // ----------------------------------------------------------------------------

  private SimpleRef<Type> copyType(Expression typeRef) {
    Reference typeref = (Reference) typeRef;
    fun.type.Type nt = FunToEvl.getRefType(typeref);
    Type ecomp = (Type) fta.traverse(nt, null);
    return new SimpleRef<Type>(typeref.getInfo(), ecomp);
  }

  @Override
  protected Evl visitFuncVariable(FuncVariable obj, Void param) {
    return new evl.data.variable.FuncVariable(obj.getInfo(), obj.getName(), copyType(obj.getType()));
  }

  @Override
  protected Evl visitStateVariable(StateVariable obj, Void param) {
    return new evl.data.variable.StateVariable(obj.getInfo(), obj.getName(), copyType(obj.getType()), (evl.data.expression.Expression) fta.traverse(obj.getDef(), null));
  }

  @Override
  protected Evl visitConstPrivate(ConstPrivate obj, Void param) {
    return new evl.data.variable.ConstPrivate(obj.getInfo(), obj.getName(), copyType(obj.getType()), (evl.data.expression.Expression) fta.traverse(obj.getDef(), null));
  }

  @Override
  protected Evl visitConstGlobal(ConstGlobal obj, Void param) {
    return new evl.data.variable.ConstGlobal(obj.getInfo(), obj.getName(), copyType(obj.getType()), (evl.data.expression.Expression) fta.traverse(obj.getDef(), null));
  }

  @Override
  protected Evl visitCompUse(CompUse obj, Void param) {
    Reference typeref = obj.getType();
    assert (typeref.getOffset().isEmpty());
    assert (typeref.getLink() instanceof CompImpl);
    CompImpl nt = (CompImpl) typeref.getLink();
    Component ecomp = (Component) fta.traverse(nt, null);
    return new evl.data.component.composition.CompUse(obj.getInfo(), obj.getName(), new SimpleRef<Component>(obj.getInfo(), ecomp));
  }

}
