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

package evl.pass.modelcheck;

import pass.EvlPass;
import error.ErrorType;
import error.RError;
import evl.data.EvlList;
import evl.data.Namespace;
import evl.data.component.Component;
import evl.data.function.InterfaceFunction;
import evl.data.function.header.FuncQuery;
import evl.knowledge.KnowledgeBase;

/**
 * Throws an error if an interface in the top component contains a query. Because we have to be sure that queries are
 * implement correctly.
 */
public class Root extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    Component root = (Component) kb.getRootComp().compRef.getTarget();

    EvlList<FuncQuery> queries = new EvlList<FuncQuery>();
    for (InterfaceFunction itr : root.iface) {
      if (itr instanceof FuncQuery) {
        queries.add((FuncQuery) itr);
      }
    }
    for (FuncQuery func : queries) {
      RError.err(ErrorType.Hint, func.getInfo(), func.name);
    }
    if (!queries.isEmpty()) {
      RError.err(ErrorType.Error, root.getInfo(), "Top component is not allowed to have queries in output");
    }
  }

}
