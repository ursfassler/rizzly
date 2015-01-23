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

package fun.pass;

import pass.FunPass;
import fun.Fun;
import fun.knowledge.KnowledgeBase;
import fun.other.FunList;
import fun.other.Namespace;
import fun.other.Template;
import fun.type.Type;
import fun.type.base.AnyType;
import fun.type.base.BooleanType;
import fun.type.base.IntegerType;
import fun.type.base.NaturalType;
import fun.type.base.StringType;
import fun.type.base.VoidType;
import fun.type.template.ArrayTemplate;
import fun.type.template.RangeTemplate;
import fun.type.template.TypeTemplate;
import fun.type.template.TypeTypeTemplate;
import fun.variable.TemplateParameter;

public class InternTypeAdder extends FunPass {

  @Override
  public void process(Namespace root, KnowledgeBase kb) {
    genPrimitiveTypes(root.getChildren());
    genPrimitiveGenericTypes(root.getChildren());
  }

  private static void genPrimitiveTypes(FunList<Fun> container) {
    inst(new BooleanType(), container);
    inst(VoidType.INSTANCE, container);
    inst(new NaturalType(), container);
    inst(new IntegerType(), container);
    inst(new AnyType(), container);
    inst(new StringType(), container);
  }

  private static void genPrimitiveGenericTypes(FunList<Fun> container) {
    templ(RangeTemplate.NAME, RangeTemplate.makeParam(), new RangeTemplate(), container);
    templ(ArrayTemplate.NAME, ArrayTemplate.makeParam(), new ArrayTemplate(), container);
    templ(TypeTypeTemplate.NAME, TypeTypeTemplate.makeParam(), new TypeTypeTemplate(), container);
  }

  private static void inst(Type object, FunList<Fun> container) {
    container.add(object);
  }

  private static void templ(String name, FunList<TemplateParameter> list, TypeTemplate tmpl, FunList<Fun> container) {
    Template decl = new Template(tmpl.getInfo(), name, list, tmpl);
    container.add(decl);
  }

}
