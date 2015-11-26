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

package ast.pass.others.behave;

import ast.data.Ast;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.function.header.FuncResponse;
import ast.data.function.header.FuncSlot;
import ast.data.variable.FuncVariable;
import ast.dispatcher.NullDispatcher;
import ast.doc.StreamWriter;
import ast.specification.PublicFunction;

public class InputWriter extends NullDispatcher<Void, Void> {
  final private StreamWriter sw;

  public InputWriter(StreamWriter sw) {
    super();
    this.sw = sw;
  }

  @Override
  protected Void visitDefault(Ast obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitNamespace(Namespace obj, Void param) {
    sw.wr("from queue import Queue");
    sw.nl();
    sw.wr("from ctypes import *");
    sw.nl();
    sw.nl();

    sw.wr("class Inst(Queue):");
    sw.nl();
    sw.incIndent();

    AstList<Ast> funcProvided = ast.repository.query.List.select(obj.children, new PublicFunction());
    if (funcProvided.isEmpty()) {
      sw.wr("pass");
    } else {
      visitList(funcProvided, param);
    }

    sw.decIndent();
    sw.nl();

    return null;
  }

  @Override
  protected Void visitFuncSlot(FuncSlot obj, Void param) {

    sw.wr("def " + obj.name + "(self");
    for (FuncVariable var : obj.param) {
      sw.wr(", ");
      sw.wr(var.name);
    }
    sw.wr("):");
    sw.nl();

    sw.incIndent();

    sw.wr("self._inst." + obj.name + "(");
    boolean first = true;
    for (FuncVariable var : obj.param) {
      if (first) {
        first = false;
      } else {
        sw.wr(", ");
      }
      sw.wr("c_int(");
      sw.wr(var.name);
      sw.wr(")");
    }
    sw.wr(")");
    sw.nl();

    sw.decIndent();
    sw.nl();

    return null;
  }

  @Override
  protected Void visitFuncResponse(FuncResponse obj, Void param) {
    // TODO implement
    return null;
  }

}
