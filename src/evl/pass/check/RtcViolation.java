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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pass.EvlPass;
import util.GraphHelper;
import util.SimpleGraph;

import common.Direction;
import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.data.Evl;
import evl.data.EvlList;
import evl.data.Namespace;
import evl.data.component.composition.CompUse;
import evl.data.component.composition.Connection;
import evl.data.component.composition.Endpoint;
import evl.data.component.composition.EndpointSub;
import evl.data.component.composition.ImplComposition;
import evl.data.component.elementary.ImplElementary;
import evl.knowledge.KnowledgeBase;
import evl.traverser.other.CallgraphMaker;
import evl.traverser.other.ClassGetter;

// TODO provide a call/connection graph in the error message
/**
 * Checks that Run To Completion semantic is not violated, i.e. that calls on component is a DAG
 */
public class RtcViolation extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    {
      List<ImplElementary> elemset = ClassGetter.get(ImplElementary.class, evl);
      for (ImplElementary elem : elemset) {
        SimpleGraph<Evl> cg = CallgraphMaker.make(elem, kb);
        assert (elem.component.isEmpty());
        // TODO do we need to check here?
        // TODO check somewhere that slots and responses don't call slot and responses
        // checkRtcViolation(cg, 3, elem.getInfo());
      }
    }
    {
      List<ImplComposition> elemset = ClassGetter.get(ImplComposition.class, evl);
      for (ImplComposition elem : elemset) {
        SimpleGraph<CompUse> cg = makeCallgraph(elem.connection);
        checkRtcViolation(cg, 2, elem.getInfo());
      }
    }
    // no need to check for hfsm since they can not have sub-components
  }

  private static SimpleGraph<CompUse> makeCallgraph(List<Connection> connection) {
    SimpleGraph<CompUse> ret = new SimpleGraph<CompUse>();
    for (Connection con : connection) {
      Endpoint src = con.endpoint.get(Direction.in);
      Endpoint dst = con.endpoint.get(Direction.out);
      if ((src instanceof EndpointSub) && (dst instanceof EndpointSub)) {
        CompUse srcComp = ((EndpointSub) src).link;
        CompUse dstComp = ((EndpointSub) dst).link;
        ret.addVertex(srcComp);
        ret.addVertex(dstComp);
        ret.addEdge(srcComp, dstComp);
      }
    }
    return ret;
  }

  private static void checkRtcViolation(SimpleGraph<CompUse> cg, int n, ElementInfo info) {
    GraphHelper.doTransitiveClosure(cg);
    ArrayList<CompUse> vs = new ArrayList<CompUse>(cg.vertexSet());
    Collections.sort(vs);
    EvlList<CompUse> erritems = new EvlList<CompUse>();
    for (CompUse v : cg.vertexSet()) {
      if (cg.containsEdge(v, v)) {
        erritems.add(v);
      }
    }
    if (!erritems.isEmpty()) {
      Collections.sort(erritems);
      for (CompUse v : erritems) {
        RError.err(ErrorType.Hint, v.getInfo(), "Involved component: " + v.name);
      }
      RError.err(ErrorType.Error, info, "Violation of run to completion detected");
    }
  }

}
