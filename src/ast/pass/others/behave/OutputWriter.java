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
import ast.data.function.ret.FunctionReturnType;
import ast.data.variable.FunctionVariable;
import ast.dispatcher.NullDispatcher;
import ast.doc.StreamWriter;
import ast.specification.ExternalFunction;
import util.SeparatorIterator;

public class OutputWriter extends NullDispatcher<Void, Void> {

  final private StreamWriter sw;

  public OutputWriter(StreamWriter sw) {
    super();
    this.sw = sw;
  }

  @Override
  protected Void visitDefault(Ast obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitNamespace(Namespace obj, Void param) {
    sw.wr("#include \"queue.h\"");
    sw.nl();
    sw.nl();
    sw.wr("#include <string>");
    sw.nl();
    sw.nl();
    sw.wr("extern \"C\"");
    sw.nl();
    sw.wr("{");
    sw.nl();
    sw.incIndent();

    AstList<Ast> funcProvided = ast.repository.query.List.select(obj.children, new ExternalFunction());
    visitList(funcProvided, param);

    sw.decIndent();
    sw.nl();
    sw.wr("}");
    sw.nl();

    return null;
  }

  @Override
  protected Void visitFunction(Function obj, Void param) {
    visit(obj.ret, param);
    sw.wr(" ");
    sw.wr(obj.getName());
    sw.wr("(");
    visitArgList(obj.param);
    sw.wr(")");
    sw.nl();

    sw.wr("{");
    sw.nl();
    sw.incIndent();

    sw.wr("push(\"");
    sw.wr(obj.getName());
    sw.wr("(");

    if (!obj.param.isEmpty()) {
      sw.wr("\" + ");
      visitParamList(obj.param);
      sw.wr("\"");
    }

    sw.wr(")\");");
    sw.nl();

    sw.decIndent();
    sw.wr("}");
    sw.nl();

    sw.nl();
    return null;
  }

  private void visitParamList(AstList<FunctionVariable> param) {
    SeparatorIterator.iterate(param, () -> {
      sw.wr("\", \" + ");
    }, (var) -> {
      sw.wr("std::to_string(");
      sw.wr(var.getName());
      sw.wr(")");
      sw.wr(" + ");
    });
  }

  private void visitArgList(AstList<FunctionVariable> param) {
    SeparatorIterator.iterate(param, () -> {
      sw.wr(", ");
    }, (var) -> {
      visit(var, null);
    });
  }

  @Override
  protected Void visitFuncVariable(FunctionVariable obj, Void param) {
    // TODO use correct type
    sw.wr("int ");
    sw.wr(obj.getName());

    return null;
  }

  @Override
  protected Void visitFuncReturnNone(FuncReturnNone obj, Void param) {
    sw.wr("void");
    return null;
  }

  @Override
  protected Void visitFuncReturnType(FunctionReturnType obj, Void param) {
    // TODO implement
    return null;
  }

}
