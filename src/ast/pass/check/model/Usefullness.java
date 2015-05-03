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

package ast.pass.check.model;

import ast.data.Namespace;
import ast.data.component.Component;
import ast.data.function.header.FuncQuery;
import ast.data.function.header.FuncResponse;
import ast.data.function.header.FuncSignal;
import ast.data.function.header.FuncSlot;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.repository.Collector;
import ast.specification.IsClass;
import ast.specification.List;
import ast.specification.NotSpec;
import error.ErrorType;
import error.RError;

// TODO do test in FUN part, issues warnings only once for parameterized
// components.
/**
 * Checks if there is a input and output data flow. Gives a warning otherwise.
 *
 * A component with only input or output data flow can not do a lot (or not more than a global function can do).
 */
public class Usefullness extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    for (Component comp : Collector.select(ast, new IsClass(Component.class)).castTo(Component.class)) {
      boolean inEmpty = List.contains(comp.iface, new NotSpec(new IsClass(FuncSlot.class).or(new IsClass(FuncQuery.class))));
      boolean outEmpty = List.contains(comp.iface, new NotSpec(new IsClass(FuncSignal.class).or(new IsClass(FuncResponse.class))));
      String name = comp.name;
      if (inEmpty && outEmpty) {
        RError.err(ErrorType.Warning, comp.getInfo(), "Component " + name + " has no input and no output data flow");
      } else if (outEmpty) {
        RError.err(ErrorType.Warning, comp.getInfo(), "Component " + name + " has no output data flow");
      } else if (inEmpty) {
        RError.err(ErrorType.Warning, comp.getInfo(), "Component " + name + " has no input data flow");
      }
    }
  }
}