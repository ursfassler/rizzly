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
import ast.data.function.Function;
import ast.data.function.ret.FuncReturnNone;
import ast.data.function.ret.FuncReturnType;
import ast.data.variable.FuncVariable;
import ast.dispatcher.NullDispatcher;
import ast.doc.StreamWriter;
import ast.specification.PublicFunction;

public class InputWriter extends NullDispatcher<Void, Function> {
  final private StreamWriter sw;

  public InputWriter(StreamWriter sw) {
    super();
    this.sw = sw;
  }

  @Override
  protected Void visitDefault(Ast obj, Function param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitNamespace(Namespace obj, Function param) {
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
  protected Void visitFunction(Function obj, Function param) {
    writeHeader(obj);
    writeBody(obj);

    return null;
  }

  private void writeHeader(Function obj) {
    sw.wr("def " + obj.name + "(self");
    for (FuncVariable var : obj.param) {
      sw.wr(", ");
      sw.wr(var.name);
    }
    sw.wr("):");
    sw.nl();
  }

  private void writeBody(Function obj) {
    sw.incIndent();

    visit(obj.ret, obj);

    sw.nl();

    sw.decIndent();
    sw.nl();
  }

  private void writeCall(Function obj) {
    sw.wr("self._inst." + obj.name + "(");
    boolean first = true;
    for (FuncVariable var : obj.param) {
      if (first) {
        first = false;
      } else {
        sw.wr(", ");
      }
      // TODO use correct type
      sw.wr("c_int(");
      sw.wr(var.name);
      sw.wr(")");
    }
    sw.wr(")");
  }

  @Override
  protected Void visitFuncReturnType(FuncReturnType obj, Function param) {
    // TODO use correct type
    sw.wr("return int(");
    writeCall(param);
    sw.wr(")");
    return null;
  }

  @Override
  protected Void visitFuncReturnNone(FuncReturnNone obj, Function param) {
    writeCall(param);
    return null;
  }

}
