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

package evl.pass.check;

import java.util.HashMap;
import java.util.Map;

import pass.EvlPass;
import util.GraphHelper;
import util.SimpleGraph;
import evl.data.Evl;
import evl.data.Namespace;
import evl.data.component.hfsm.Transition;
import evl.data.function.Function;
import evl.knowledge.KnowledgeBase;
import evl.pass.check.io.IoCheck;
import evl.pass.check.io.StateReaderInfo;
import evl.pass.check.io.StateWriterInfo;
import evl.traverser.other.CallgraphMaker;
import evl.traverser.other.ClassGetter;
import evl.traverser.other.OutsideReaderInfo;
import evl.traverser.other.OutsideWriterInfo;

/**
 * Checks that only allowed functions change state or write output
 */
public class Io extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    SimpleGraph<Evl> cg = CallgraphMaker.make(evl, kb);
    // printGraph(kb.getRootdir() + "callgraph.gv", cg);

    Map<Evl, Boolean> writes = new HashMap<Evl, Boolean>();
    Map<Evl, Boolean> reads = new HashMap<Evl, Boolean>();
    Map<Evl, Boolean> outputs = new HashMap<Evl, Boolean>();
    Map<Evl, Boolean> inputs = new HashMap<Evl, Boolean>();
    for (Evl header : cg.vertexSet()) {
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
    ioCheck.check(ClassGetter.get(Function.class, evl));
    ioCheck.check(ClassGetter.get(Transition.class, evl));
  }

  private static <T extends Evl> Map<T, Boolean> doTransStuff(SimpleGraph<T> cg, Map<? extends Evl, Boolean> does) {
    Map<T, Boolean> ret = new HashMap<T, Boolean>();
    for (T u : cg.vertexSet()) {
      boolean doThings = does.get(u);
      for (Evl v : cg.getOutVertices(u)) {
        doThings |= does.get(v);
      }
      ret.put(u, doThings);
    }
    return ret;
  }

}
