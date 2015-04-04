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

package fun.function.template;

import common.ElementInfo;

import fun.other.FunList;
import fun.type.base.AnyType;
import fun.variable.TemplateParameter;

public class DefaultValueTemplate extends FunctionTemplate {
  public static final String NAME = "default";
  public static final String[] PARAM = { "type" };

  public DefaultValueTemplate() {
    super(ElementInfo.NO);
  }

  @Override
  public FunList<TemplateParameter> makeParam() {
    FunList<TemplateParameter> ret = new FunList<TemplateParameter>();
    ret.add(inst(PARAM[0], AnyType.NAME));
    return ret;
  }

  @Override
  public String getName() {
    return NAME;
  }

}