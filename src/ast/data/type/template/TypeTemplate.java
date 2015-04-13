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

import ast.data.Named;
import ast.data.expression.reference.Reference;
import ast.data.variable.TemplateParameter;

import common.ElementInfo;

public class TypeTemplate extends Named {

  public TypeTemplate(ElementInfo info, String name) {
    super(info, name);
  }

  static protected TemplateParameter inst(String name, String type) {
    return new TemplateParameter(ElementInfo.NO, name, new Reference(ElementInfo.NO, type));
  }

  protected static TemplateParameter inst(String name, Reference type) {
    return new TemplateParameter(ElementInfo.NO, name, type);
  }
}
