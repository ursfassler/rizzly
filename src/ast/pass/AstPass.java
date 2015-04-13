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

package ast.pass;

import java.util.HashSet;
import java.util.Set;

import ast.data.Namespace;
import ast.knowledge.KnowledgeBase;

public abstract class AstPass {
  /*
   * Condition may be something like NoClass( Integer )
   * 
   * or TypeChecked, Linked, Reduced
   * 
   * or MostClass( Namespace, 1 )
   */

  protected final Set<Condition> precondition = new HashSet<Condition>();
  protected final Set<Condition> postcondition = new HashSet<Condition>();

  public Set<Condition> getPrecondition() {
    return precondition;
  }

  public Set<Condition> getPostcondition() {
    return postcondition;
  }

  public abstract void process(Namespace ast, KnowledgeBase kb);

  @Deprecated
  public String getName() {
    return getClass().getName();
  }
}
