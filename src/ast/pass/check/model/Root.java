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

import ast.data.AstList;
import ast.data.Namespace;
import ast.data.component.Component;
import ast.data.function.InterfaceFunction;
import ast.data.function.header.FuncQuery;
import ast.data.reference.Reference;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import error.ErrorType;
import error.RError;

/**
 * Throws an error if an interface in the top component contains a query. Because we have to be sure that queries are
 * implement correctly.
 */
public class Root implements AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    Reference reference = kb.getRootComp().getCompRef();
    Component root = (Component) reference.getTarget();

    AstList<FuncQuery> queries = new AstList<FuncQuery>();
    for (InterfaceFunction itr : root.iface) {
      if (itr instanceof FuncQuery) {
        queries.add((FuncQuery) itr);
      }
    }
    for (FuncQuery func : queries) {
      RError.err(ErrorType.Hint, func.getName(), func.metadata());
    }
    if (!queries.isEmpty()) {
      RError.err(ErrorType.Error, "Top component is not allowed to have queries in output", root.metadata());
    }
  }

}
