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

package pass;

import java.util.HashSet;
import java.util.Set;

import evl.Evl;
import evl.knowledge.KnowledgeBase;

public abstract class EvlPass {

  public abstract void process(Evl evl, KnowledgeBase kb);

  private final Set<Class<? extends EvlPass>> depends = new HashSet<Class<? extends EvlPass>>();

  public void addDependency(Class<? extends EvlPass> dep) {
    depends.add(dep);
  }

  public Set<Class<? extends EvlPass>> getDependencies() {
    return new HashSet<Class<? extends EvlPass>>(depends);
  }

  public String getName() {
    return getClass().getName();
  }

}
