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

package ast.data.template;

import java.util.List;

import ast.data.AstList;
import ast.data.Named;
import ast.data.component.hfsm.StateContent;
import ast.data.variable.TemplateParameter;
import ast.meta.MetaList;
import ast.visitor.Visitor;

//TODO do we need anonymous templates? it is easier if they are named.
public class Template extends Named implements StateContent {
  private final AstList<TemplateParameter> templ = new AstList<TemplateParameter>();
  private final Named object;

  public Template(String name, List<TemplateParameter> genpam, Named object) {
    setName(name);
    this.templ.addAll(genpam);
    this.object = object;
  }

  @Deprecated
  public Template(MetaList info, String name, List<TemplateParameter> genpam, Named object) {
    metadata().add(info);
    setName(name);
    this.templ.addAll(genpam);
    this.object = object;
  }

  public AstList<TemplateParameter> getTempl() {
    return templ;
  }

  public Named getObject() {
    return object;
  }

  @Override
  public String toString() {
    return getName() + templ.toString();
  }


}
