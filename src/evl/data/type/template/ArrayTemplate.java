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

package evl.data.type.template;

import common.ElementInfo;

import evl.data.EvlList;
import evl.data.expression.reference.RefTemplCall;
import evl.data.expression.reference.Reference;
import evl.data.variable.TemplateParameter;
import fun.other.ActualTemplateArgument;

final public class ArrayTemplate extends TypeTemplate {
  public static final String NAME = "Array";
  public static final String[] PARAM = { "S", "T" };

  public ArrayTemplate() {
    super(ElementInfo.NO, NAME);
  }

  static public EvlList<TemplateParameter> makeParam() {
    EvlList<TemplateParameter> ret = new EvlList<TemplateParameter>();

    ret.add(inst(PARAM[0], evl.data.type.special.IntegerType.NAME));

    Reference type = new Reference(ElementInfo.NO, TypeTypeTemplate.NAME);
    EvlList<ActualTemplateArgument> typeparam = new EvlList<ActualTemplateArgument>();
    typeparam.add(new Reference(ElementInfo.NO, evl.data.type.special.AnyType.NAME));
    type.offset.add(new RefTemplCall(ElementInfo.NO, typeparam));
    ret.add(inst(PARAM[1], type));

    return ret;
  }

}
