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

package ast.pass.modelcheck;

import java.util.HashMap;
import java.util.Map;

import pass.AstPass;
import util.GraphHelper;
import util.SimpleGraph;
import ast.data.Ast;
import ast.data.Namespace;
import ast.data.component.hfsm.Transition;
import ast.data.function.Function;
import ast.knowledge.KnowledgeBase;
import ast.pass.modelcheck.io.IoCheck;
import ast.pass.modelcheck.io.StateReaderInfo;
import ast.pass.modelcheck.io.StateWriterInfo;
import ast.traverser.other.CallgraphMaker;
import ast.traverser.other.ClassGetter;
import ast.traverser.other.OutsideReaderInfo;
import ast.traverser.other.OutsideWriterInfo;

/**
 * Checks that only allowed functions change state or write output
 */
public class Io extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    SimpleGraph<Ast> cg = CallgraphMaker.make(ast, kb);
    // printGraph(kb.getRootdir() + "callgraph.gv", cg);

    Map<Ast, Boolean> writes = new HashMap<Ast, Boolean>();
    Map<Ast, Boolean> reads = new HashMap<Ast, Boolean>();
    Map<Ast, Boolean> outputs = new HashMap<Ast, Boolean>();
    Map<Ast, Boolean> inputs = new HashMap<Ast, Boolean>();
    for (Ast header : cg.vertexSet()) {
      writes.put(header, StateWriterInfo.get(header));
      reads.put(header, StateReaderInfo.get(header));
      if (header instanceof Function) {
        inputs.put(header, OutsideReaderInfo.get((Function) header));
        outputs.put(header, OutsideWriterInfo.get((Function) header));
      } else {
        inputs.put(header, false);
        outputs.put(header, false);
      }
    }
    // print(writes, reads, outputs, inputs);

    GraphHelper.doTransitiveClosure(cg);

    writes = doTransStuff(cg, writes);
    reads = doTransStuff(cg, reads);
    outputs = doTransStuff(cg, outputs);
    inputs = doTransStuff(cg, inputs);

    // System.out.println("-------");
    // print(writes, reads, outputs, inputs);

    IoCheck ioCheck = new IoCheck(writes, reads, outputs, inputs);
    ioCheck.check(ClassGetter.getRecursive(Function.class, ast));
    ioCheck.check(ClassGetter.getRecursive(Transition.class, ast));
  }

  private static <T extends Ast> Map<T, Boolean> doTransStuff(SimpleGraph<T> cg, Map<? extends Ast, Boolean> does) {
    Map<T, Boolean> ret = new HashMap<T, Boolean>();
    for (T u : cg.vertexSet()) {
      boolean doThings = does.get(u);
      for (Ast v : cg.getOutVertices(u)) {
        doThings |= does.get(v);
      }
      ret.put(u, doThings);
    }
    return ret;
  }

}
