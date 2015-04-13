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

import pass.EvlPass;
import evl.data.Evl;
import evl.data.EvlList;
import evl.data.type.Type;
import evl.data.type.base.BooleanType;
import evl.data.type.base.StringType;
import evl.data.type.special.AnyType;
import evl.data.type.special.IntegerType;
import evl.data.type.special.NaturalType;
import evl.data.type.special.VoidType;
import evl.data.type.template.ArrayTemplate;
import evl.data.type.template.RangeTemplate;
import evl.data.type.template.TypeTemplate;
import evl.data.type.template.TypeTypeTemplate;
import evl.data.variable.TemplateParameter;
import evl.knowledge.KnowledgeBase;
import fun.other.Template;

public class InternTypeAdder extends EvlPass {

  @Override
  public void process(evl.data.Namespace root, KnowledgeBase kb) {
    genPrimitiveTypes(root.children);
    genPrimitiveGenericTypes(root.children);
  }

  private static void genPrimitiveTypes(EvlList<Evl> container) {
    inst(new BooleanType(), container);
    inst(new VoidType(), container);
    inst(new NaturalType(), container);
    inst(new IntegerType(), container);
    inst(new AnyType(), container);
    inst(new StringType(), container);
  }

  private static void genPrimitiveGenericTypes(EvlList<Evl> container) {
    templ(RangeTemplate.NAME, RangeTemplate.makeParam(), new RangeTemplate(), container);
    templ(ArrayTemplate.NAME, ArrayTemplate.makeParam(), new ArrayTemplate(), container);
    templ(TypeTypeTemplate.NAME, TypeTypeTemplate.makeParam(), new TypeTypeTemplate(), container);
  }

  private static void inst(Type object, EvlList<Evl> container) {
    container.add(object);
  }

  private static void templ(String name, EvlList<TemplateParameter> list, TypeTemplate tmpl, EvlList<Evl> container) {
    Template decl = new Template(tmpl.getInfo(), name, list, tmpl);
    container.add(decl);
  }

}
