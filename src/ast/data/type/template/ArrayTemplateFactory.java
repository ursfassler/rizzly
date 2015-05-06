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
import ast.data.template.Template;
import ast.data.template.TemplateFactory;
import ast.data.type.special.IntegerType;
import ast.data.type.special.TypeType;
import ast.data.variable.TemplateParameter;

public class ArrayTemplateFactory extends TemplateFactory {
  public static final String NAME = "Array";
  public static final String[] PARAM = { "S", "T" };

  public static Template create(IntegerType intType, TypeType typeTypeAny) {
    return new Template(ElementInfo.NO, NAME, getParameter(intType, typeTypeAny), new ArrayTemplate());
  }

  private static AstList<TemplateParameter> getParameter(IntegerType intType, TypeType typeTypeAny) {
    AstList<TemplateParameter> ret = new AstList<TemplateParameter>();
    ret.add(makeParam(PARAM[0], intType));
    ret.add(makeParam(PARAM[1], typeTypeAny));
    return ret;
  }

}
