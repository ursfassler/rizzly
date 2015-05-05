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

package ast.knowledge;

import ast.Designator;
import ast.ElementInfo;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.Named;
import ast.data.function.FunctionProperty;
import ast.data.function.header.FuncSignal;
import ast.data.function.ret.FuncReturnNone;
import ast.data.statement.Block;
import ast.data.variable.FuncVariable;
import ast.repository.query.NameFilter;

//TODO rename
//TODO rename trap to runtime exception and provide arguments
public class KnowLlvmLibrary extends KnowledgeEntry {
  private KnowledgeBase kb;

  @Override
  public void init(KnowledgeBase kb) {
    this.kb = kb;
  }

  private Ast findItem(String name) {
    return NameFilter.select(kb.getRoot().children, name);
  }

  private void addItem(Named item) {
    kb.getRoot().children.add(item);
  }

  public FuncSignal getTrap() {
    final String NAME = Designator.NAME_SEP + "trap";
    final ElementInfo info = new ElementInfo(NAME, 0, 0);

    FuncSignal ret = (FuncSignal) findItem(NAME);

    if (ret == null) {
      ret = new FuncSignal(info, NAME, new AstList<FuncVariable>(), new FuncReturnNone(info), new Block(info));
      ret.property = FunctionProperty.External;
      addItem(ret);
    }

    return ret;
  }
}
