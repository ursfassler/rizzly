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

package ast.pass.reduction.hfsm;

import java.util.Collection;
import java.util.HashSet;

import ast.data.AstList;
import ast.data.Namespace;
import ast.data.component.hfsm.ImplHfsm;
import ast.data.component.hfsm.StateComposite;
import ast.data.component.hfsm.StateContent;
import ast.data.component.hfsm.StateSimple;
import ast.data.function.header.FuncFunction;
import ast.data.function.header.FuncProcedure;
import ast.data.type.Type;
import ast.data.variable.ConstPrivate;
import ast.knowledge.KnowledgeBase;
import ast.manipulator.Manipulate;
import ast.manipulator.PathPrefixer;
import ast.pass.AstPass;
import ast.repository.Collector;
import ast.specification.IsClass;
import ast.specification.OrSpec;
import ast.specification.Specification;

/**
 * Moves items of all states to the top-state.
 *
 * @author urs
 *
 */
public class StateItemUplifter extends AstPass {
  private final static Specification contentSpec = makeContentSpec();
  private final static Specification leafStateSpec = new IsClass(StateSimple.class);
  private final static Specification compStateSpec = new IsClass(StateComposite.class);
  private final static Specification renameSpec = contentSpec.or(leafStateSpec);

  static private Specification makeContentSpec() {
    Collection<Specification> orSpecs = new HashSet<Specification>();
    orSpecs.add(new IsClass(FuncProcedure.class));
    orSpecs.add(new IsClass(FuncFunction.class));
    orSpecs.add(new IsClass(Type.class));
    orSpecs.add(new IsClass(ConstPrivate.class));
    return new OrSpec(orSpecs);
  }

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    AstList<ImplHfsm> hfsmList = Collector.select(ast, new IsClass(ImplHfsm.class)).castTo(ImplHfsm.class);
    for (ImplHfsm hfsm : hfsmList) {
      process(hfsm);
    }
  }

  private void process(ImplHfsm hfsm) {
    PathPrefixer.prefix(hfsm, renameSpec);

    moveToTop(hfsm.topstate, contentSpec);
    moveToTop(hfsm.topstate, leafStateSpec);

    Manipulate.remove(hfsm.topstate, compStateSpec);
  }

  private void moveToTop(StateComposite top, Specification spec) {
    AstList<StateContent> content = Collector.select(top, spec).castTo(StateContent.class);
    Manipulate.remove(top, spec);
    top.item.addAll(content);
  }

}
