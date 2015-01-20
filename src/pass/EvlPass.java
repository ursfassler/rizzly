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
import evl.other.Namespace;

public abstract class EvlPass {

  protected final Set<Evl> requires = new HashSet<Evl>(); // need these classes
  protected final Set<Evl> interruptive = new HashSet<Evl>(); // can not work with these classes
  protected final Set<Evl> removes = new HashSet<Evl>(); // remove thes classes
  protected final Set<Evl> adds = new HashSet<Evl>();  // add these classes

  public abstract void process(Namespace evl, KnowledgeBase kb);

  public String getName() {
    return getClass().getName();
  }

}
