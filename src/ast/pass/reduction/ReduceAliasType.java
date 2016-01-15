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

package ast.pass.reduction;

import java.util.ArrayList;
import java.util.List;

import main.Configuration;
import ast.data.Named;
import ast.data.Namespace;
import ast.data.reference.Reference;
import ast.data.type.out.AliasType;
import ast.dispatcher.DfsTraverser;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import error.ErrorType;
import error.RError;

public class ReduceAliasType extends AstPass {
  public ReduceAliasType(Configuration configuration) {
    super(configuration);
  }

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    ReduceAliasTypeWorker worker = new ReduceAliasTypeWorker();
    worker.traverse(ast, null);
  }

}

class ReduceAliasTypeWorker extends DfsTraverser<Void, Void> {

  @Override
  protected Void visitReference(Reference obj, Void param) {
    super.visitReference(obj, param);
    Named link = obj.link;
    List<Named> checked = new ArrayList<Named>();
    while (link instanceof AliasType) {
      checked.add(link);
      link = ((AliasType) link).ref.getTarget();
      if (checked.contains(link)) {
        for (Named itr : checked) {
          RError.err(ErrorType.Hint, itr.getInfo(), "part of recursive type alias: " + itr.name);
        }
        RError.err(ErrorType.Error, obj.getInfo(), "recursive type alias found: " + obj.link.name);
      }
    }
    obj.link = link;
    return null;
  }
}
