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
import java.util.List;

import ast.data.Ast;
import ast.data.Range;
import ast.data.expression.value.NumberValue;
import ast.data.template.ActualTemplateArgument;
import ast.data.type.Type;
import ast.data.type.template.ArrayTemplate;
import ast.data.type.template.RangeTemplate;
import ast.data.type.template.TypeTemplate;
import ast.data.type.template.TypeTypeTemplate;
import ast.dispatcher.NullDispatcher;
import ast.knowledge.KnowledgeBase;
import ast.repository.manipulator.TypeRepo;

public class TypeTemplateSpecializer extends NullDispatcher<Type, List<ActualTemplateArgument>> {
  private final TypeRepo kbi;

  public TypeTemplateSpecializer(KnowledgeBase kb) {
    kbi = new TypeRepo(kb);
  }

  public static Type process(TypeTemplate type, List<ActualTemplateArgument> genspec, KnowledgeBase kb) {
    TypeTemplateSpecializer specializer = new TypeTemplateSpecializer(kb);
    return specializer.traverse(type, genspec);
  }

  @Override
  protected Type visitDefault(Ast obj, List<ActualTemplateArgument> param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected ast.data.type.Type visitRangeTemplate(RangeTemplate obj, List<ActualTemplateArgument> param) {
    assert (param.size() == 2);
    assert (param.get(0) instanceof NumberValue);
    assert (param.get(1) instanceof NumberValue);

    NumberValue low = (NumberValue) param.get(0);
    NumberValue high = (NumberValue) param.get(1);
    return kbi.getRangeType(new Range(low.value, high.value));
  }

  @Override
  protected ast.data.type.Type visitArrayTemplate(ArrayTemplate obj, List<ActualTemplateArgument> param) {
    assert (param.size() == 2);
    assert (param.get(0) instanceof NumberValue);
    assert (param.get(1) instanceof Type);

    NumberValue size = (NumberValue) param.get(0);
    Type type = (Type) param.get(1);
    BigInteger count = size.value;
    return kbi.getArray(count, type);
  }

  @Override
  protected Type visitTypeTypeTemplate(TypeTypeTemplate obj, List<ActualTemplateArgument> param) {
    assert (param.size() == 1);
    assert (param.get(0) instanceof Type);

    Type type = (Type) param.get(0);
    return kbi.getTypeType(type);
  }
}
