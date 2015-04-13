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

import evl.data.Evl;
import evl.data.EvlList;
import evl.data.Named;
import evl.data.component.hfsm.StateContent;
import evl.data.variable.TemplateParameter;

//TODO do we need anonymous templates? it is easier if they are named.
public class Template extends Named implements StateContent {
  private final EvlList<TemplateParameter> templ = new EvlList<TemplateParameter>();
  private final Evl object;

  public Template(ElementInfo info, String name, List<TemplateParameter> genpam, Evl object) {
    super(info, name);
    this.templ.addAll(genpam);
    this.object = object;
  }

  public EvlList<TemplateParameter> getTempl() {
    return templ;
  }

  public Evl getObject() {
    return object;
  }

  @Override
  public String toString() {
    return name + templ.toString();
  }

}
