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

package fun.other;

import java.util.List;

import common.ElementInfo;

import fun.Fun;
import fun.FunBase;
import fun.hfsm.StateContent;
import fun.variable.TemplateParameter;

//TODO do we need anonymous templates? it is easier if they are named.
public class Template extends FunBase implements Named, StateContent {
  private String name;
  private final FunList<TemplateParameter> templ = new FunList<TemplateParameter>();
  private final Fun object;

  public Template(ElementInfo info, String name, List<TemplateParameter> genpam, Fun object) {
    super(info);
    this.name = name;
    this.templ.addAll(genpam);
    this.object = object;
  }

  public FunList<TemplateParameter> getTempl() {
    return templ;
  }

  public Fun getObject() {
    return object;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name + templ.toString();
  }

}
