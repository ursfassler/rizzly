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

package fun.type.template;

import common.ElementInfo;

import fun.FunBase;
import fun.expression.reference.Reference;
import fun.variable.TemplateParameter;

public class TypeTemplate extends FunBase {

  public TypeTemplate(ElementInfo info) {
    super(info);
  }

  static protected TemplateParameter inst(String name, String type) {
    return new TemplateParameter(ElementInfo.NO, name, new Reference(ElementInfo.NO, type));
  }

  protected static TemplateParameter inst(String name, Reference type) {
    return new TemplateParameter(ElementInfo.NO, name, type);
  }
}
