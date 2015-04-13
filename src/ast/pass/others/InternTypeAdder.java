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

package ast.pass.others;

import ast.data.Ast;
import ast.data.AstList;
import ast.data.template.Template;
import ast.data.type.Type;
import ast.data.type.base.BooleanType;
import ast.data.type.base.StringType;
import ast.data.type.special.AnyType;
import ast.data.type.special.IntegerType;
import ast.data.type.special.NaturalType;
import ast.data.type.special.VoidType;
import ast.data.type.template.ArrayTemplate;
import ast.data.type.template.RangeTemplate;
import ast.data.type.template.TypeTemplate;
import ast.data.type.template.TypeTypeTemplate;
import ast.data.variable.TemplateParameter;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;

public class InternTypeAdder extends AstPass {

  @Override
  public void process(ast.data.Namespace root, KnowledgeBase kb) {
    genPrimitiveTypes(root.children);
    genPrimitiveGenericTypes(root.children);
  }

  private static void genPrimitiveTypes(AstList<Ast> container) {
    inst(new BooleanType(), container);
    inst(new VoidType(), container);
    inst(new NaturalType(), container);
    inst(new IntegerType(), container);
    inst(new AnyType(), container);
    inst(new StringType(), container);
  }

  private static void genPrimitiveGenericTypes(AstList<Ast> container) {
    templ(RangeTemplate.NAME, RangeTemplate.makeParam(), new RangeTemplate(), container);
    templ(ArrayTemplate.NAME, ArrayTemplate.makeParam(), new ArrayTemplate(), container);
    templ(TypeTypeTemplate.NAME, TypeTypeTemplate.makeParam(), new TypeTypeTemplate(), container);
  }

  private static void inst(Type object, AstList<Ast> container) {
    container.add(object);
  }

  private static void templ(String name, AstList<TemplateParameter> list, TypeTemplate tmpl, AstList<Ast> container) {
    Template decl = new Template(tmpl.getInfo(), name, list, tmpl);
    container.add(decl);
  }

}
