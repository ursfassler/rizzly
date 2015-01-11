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

package evl.pass;

import java.util.HashSet;
import java.util.Set;

import pass.EvlPass;
import util.SimpleGraph;

import common.Property;

import evl.Evl;
import evl.function.Function;
import evl.knowledge.KnowledgeBase;
import evl.other.Namespace;
import evl.other.RizzlyProgram;
import evl.traverser.DepGraph;

public class RemoveUnusedProgram extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    for (RizzlyProgram prg : evl.getItems(RizzlyProgram.class, false)) {
      Set<Function> roots = new HashSet<Function>();

      for (Function func : prg.getFunction()) {
        if (func.properties().get(Property.Public) == Boolean.TRUE) {
          roots.add(func);
        }
      }

      SimpleGraph<Evl> g = DepGraph.build(roots);

      Set<Evl> keep = g.vertexSet();

      prg.getConstant().retainAll(keep);
      prg.getVariable().retainAll(keep);
      prg.getFunction().retainAll(keep);
      prg.getType().retainAll(keep);
    }
  }
}
