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

package ast.data.type.template;

import java.util.List;

import ast.data.Ast;
import ast.data.expression.value.NumberValue;
import ast.data.template.ActualTemplateArgument;
import ast.data.type.Type;
import ast.data.type.base.ArrayTypeFactory;
import ast.data.type.base.RangeTypeFactory;
import ast.data.type.special.TypeTypeFactory;
import ast.dispatcher.NullDispatcher;

public class TypeTemplateSpecializer extends NullDispatcher<Type, List<ActualTemplateArgument>> {
  final static private TypeTemplateSpecializer INSTANCE = new TypeTemplateSpecializer();

  public static Type process(TypeTemplate type, List<ActualTemplateArgument> genspec) {
    return INSTANCE.traverse(type, genspec);
  }

  @Override
  protected Type visitDefault(Ast obj, List<ActualTemplateArgument> param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Type visitRangeTemplate(RangeTemplate obj, List<ActualTemplateArgument> param) {
    assert (param.size() == 2);
    assert (param.get(0) instanceof NumberValue);
    assert (param.get(1) instanceof NumberValue);

    NumberValue low = (NumberValue) param.get(0);
    NumberValue high = (NumberValue) param.get(1);
    return RangeTypeFactory.create(low.value, high.value);
  }

  @Override
  protected Type visitArrayTemplate(ArrayTemplate obj, List<ActualTemplateArgument> param) {
    assert (param.size() == 2);
    assert (param.get(0) instanceof NumberValue);
    assert (param.get(1) instanceof Type);

    NumberValue size = (NumberValue) param.get(0);
    Type type = (Type) param.get(1);
    return ArrayTypeFactory.create(size.value, type);
  }

  @Override
  protected Type visitTypeTypeTemplate(TypeTypeTemplate obj, List<ActualTemplateArgument> param) {
    assert (param.size() == 1);
    assert (param.get(0) instanceof Type);

    Type type = (Type) param.get(0);
    return TypeTypeFactory.create(type);
  }
}
