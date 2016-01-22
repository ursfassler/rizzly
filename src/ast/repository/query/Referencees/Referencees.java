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

package ast.repository.query.Referencees;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ast.data.Ast;
import ast.data.Named;
import ast.data.reference.LinkedReferenceWithOffset_Implementation;

public class Referencees implements ReferenceesWriter, ReferenceesReader {
  final private Map<Ast, Set<LinkedReferenceWithOffset_Implementation>> referencees = new HashMap<Ast, Set<LinkedReferenceWithOffset_Implementation>>();

  @Override
  public Set<LinkedReferenceWithOffset_Implementation> getReferencees(Ast target) {
    return referencees.get(target);
  }

  @Override
  public void addTarget(Ast target) {
    if (!referencees.containsKey(target)) {
      referencees.put(target, new HashSet<LinkedReferenceWithOffset_Implementation>());
    }
  }

  @Override
  public void addReferencee(LinkedReferenceWithOffset_Implementation referencee) {
    Named target = referencee.getLink();
    addTarget(target);
    referencees.get(target).add(referencee);
  }

}
