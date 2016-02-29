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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ast.data.Ast;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.component.composition.ComponentUse;
import ast.data.component.composition.Connection;
import ast.data.component.composition.Endpoint;
import ast.data.component.composition.EndpointSub;
import ast.data.component.composition.ImplComposition;
import ast.data.component.elementary.ImplElementary;
import ast.dispatcher.other.CallgraphMaker;
import ast.doc.SimpleGraph;
import ast.knowledge.KnowledgeBase;
import ast.meta.MetaList;
import ast.pass.AstPass;
import ast.pass.helper.GraphHelper;
import ast.repository.query.Collector;
import ast.specification.IsClass;
import error.ErrorType;
import error.RError;

// TODO provide a call/connection graph in the error message
/**
 * Checks that Run To Completion semantic is not violated, i.e. that calls on component is a DAG
 */
public class RtcViolation implements AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    {
      List<ImplElementary> elemset = Collector.select(ast, new IsClass(ImplElementary.class)).castTo(ImplElementary.class);
      for (ImplElementary elem : elemset) {
        SimpleGraph<Ast> cg = CallgraphMaker.make(elem, kb);
        assert (elem.component.isEmpty());
        // TODO do we need to check here?
        // TODO check somewhere that slots and responses don't call slot and
        // responses
        // checkRtcViolation(cg, 3, elem.getInfo());
      }
    }
    {
      List<ImplComposition> elemset = Collector.select(ast, new IsClass(ImplComposition.class)).castTo(ImplComposition.class);
      for (ImplComposition elem : elemset) {
        SimpleGraph<ComponentUse> cg = makeCallgraph(elem.connection);
        checkRtcViolation(cg, 2, elem.metadata());
      }
    }
    // no need to check for hfsm since they can not have sub-components
  }

  private static SimpleGraph<ComponentUse> makeCallgraph(List<Connection> connection) {
    SimpleGraph<ComponentUse> ret = new SimpleGraph<ComponentUse>();
    for (Connection con : connection) {
      Endpoint src = con.getSrc();
      Endpoint dst = con.getDst();
      if ((src instanceof EndpointSub) && (dst instanceof EndpointSub)) {
        ComponentUse srcComp = (ComponentUse) ((EndpointSub) src).getComponent().getTarget();
        ComponentUse dstComp = (ComponentUse) ((EndpointSub) dst).getComponent().getTarget();
        ret.addVertex(srcComp);
        ret.addVertex(dstComp);
        ret.addEdge(srcComp, dstComp);
      }
    }
    return ret;
  }

  private static void checkRtcViolation(SimpleGraph<ComponentUse> cg, int n, MetaList info) {
    GraphHelper.doTransitiveClosure(cg);
    ArrayList<ComponentUse> vs = new ArrayList<ComponentUse>(cg.vertexSet());
    Collections.sort(vs, nameComparator());
    AstList<ComponentUse> erritems = new AstList<ComponentUse>();
    for (ComponentUse v : cg.vertexSet()) {
      if (cg.containsEdge(v, v)) {
        erritems.add(v);
      }
    }
    if (!erritems.isEmpty()) {
      Collections.sort(erritems, nameComparator());
      for (ComponentUse v : erritems) {
        RError.err(ErrorType.Hint, "Involved component: " + v.getName(), v.metadata());
      }
      RError.err(ErrorType.Error, "Violation of run to completion detected", info);
    }
  }

  private static Comparator<ComponentUse> nameComparator() {
    return new Comparator<ComponentUse>() {
      @Override
      public int compare(ComponentUse left, ComponentUse right) {
        return left.getName().compareTo(right.getName());
      }
    };
  }
}
