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

import fun.knowledge.KnowledgeBase;
import fun.other.Namespace;

public abstract class FunPass {

  /*
   * protected final Set<Condition> precondition; protected final Set<Condition> postcondition;
   * 
   * Condition may be something like NoClass( Integer )
   * 
   * or TypeChecked, Linked, Reduced
   * 
   * or MostClass( Namespace, 1 )
   */

  public abstract void process(Namespace root, KnowledgeBase kb);

  public String getName() {
    return getClass().getName();
  }

}