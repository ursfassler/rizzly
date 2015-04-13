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
import ast.data.variable.TemplateParameter;

final public class RangeTemplate extends TypeTemplate {
  public static final String NAME = "R";
  public static final String[] PARAM = { "low", "high" };

  public RangeTemplate() {
    super(ElementInfo.NO, NAME);
  }

  static public AstList<TemplateParameter> makeParam() {
    AstList<TemplateParameter> ret = new AstList<TemplateParameter>();
    ret.add(inst(PARAM[0], ast.data.type.special.IntegerType.NAME));
    ret.add(inst(PARAM[1], ast.data.type.special.IntegerType.NAME));
    return ret;
  }

}
