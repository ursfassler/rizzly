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

import java.util.ArrayList;
import java.util.List;

import ast.data.Namespace;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;

abstract public class GroupPass extends AstPass {
  final private List<AstPass> passes = new ArrayList<AstPass>();

  protected void append(AstPass pass) {
    passes.add(pass);
  }

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    for (AstPass pass : passes) {
      pass.process(ast, kb);
    }
  }
}
