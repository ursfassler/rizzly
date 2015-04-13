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

import ast.ElementInfo;
import ast.data.AstList;
import ast.data.expression.reference.RefTemplCall;
import ast.data.expression.reference.Reference;
import ast.data.template.ActualTemplateArgument;
import ast.data.variable.TemplateParameter;

final public class ArrayTemplate extends TypeTemplate {
  public static final String NAME = "Array";
  public static final String[] PARAM = { "S", "T" };

  public ArrayTemplate() {
    super(ElementInfo.NO, NAME);
  }

  static public AstList<TemplateParameter> makeParam() {
    AstList<TemplateParameter> ret = new AstList<TemplateParameter>();

    ret.add(inst(PARAM[0], ast.data.type.special.IntegerType.NAME));

    Reference type = new Reference(ElementInfo.NO, TypeTypeTemplate.NAME);
    AstList<ActualTemplateArgument> typeparam = new AstList<ActualTemplateArgument>();
    typeparam.add(new Reference(ElementInfo.NO, ast.data.type.special.AnyType.NAME));
    type.offset.add(new RefTemplCall(ElementInfo.NO, typeparam));
    ret.add(inst(PARAM[1], type));

    return ret;
  }

}
