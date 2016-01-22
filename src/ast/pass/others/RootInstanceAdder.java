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

package ast.pass.others;

import main.Configuration;
import ast.data.Namespace;
import ast.data.component.ComponentReference;
import ast.data.component.composition.ComponentUse;
import ast.data.reference.RefFactory;
import ast.data.template.Template;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.repository.query.ChildByName;

public class RootInstanceAdder extends AstPass {
  public RootInstanceAdder(Configuration configuration) {
    super(configuration);
  }

  @Override
  public void process(Namespace root, KnowledgeBase kb) {
    Template rootdecl = (Template) ChildByName.staticGet(root, kb.getOptions().getRootComp(), root.metadata());
    ast.data.component.composition.ComponentUse rootinst = new ComponentUse("inst", new ComponentReference(RefFactory.full(rootdecl)));
    root.children.add(rootinst);
  }

}
