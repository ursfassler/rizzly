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

package evl.data.function.template;

import common.ElementInfo;

import evl.data.EvlBase;
import evl.data.EvlList;
import evl.data.expression.reference.Reference;
import evl.data.variable.TemplateParameter;

public abstract class FunctionTemplate extends EvlBase {

  public FunctionTemplate(ElementInfo info) {
    super(info);
  }

  abstract public EvlList<TemplateParameter> makeParam();

  abstract public String getName();

  static protected TemplateParameter inst(String name, String type) {
    return new TemplateParameter(ElementInfo.NO, name, new Reference(ElementInfo.NO, type));
  }

  protected static TemplateParameter inst(String name, Reference type) {
    return new TemplateParameter(ElementInfo.NO, name, type);
  }
}
