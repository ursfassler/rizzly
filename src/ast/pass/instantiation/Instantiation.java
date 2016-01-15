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

package ast.pass.instantiation;

import main.Configuration;
import ast.pass.instantiation.queuereduction.QueueReduction;
import ast.pass.optimize.RemoveUnused;
import ast.pass.others.GroupPass;

public class Instantiation extends GroupPass {
  public Instantiation(Configuration configuration) {
    super(configuration);
    append(new ElementaryInstantiation(configuration));
    append(new LinkReduction(configuration));
    append(new QueueReduction(configuration));
    append(new Flattner(configuration));
    append(new RemoveUnused(configuration));
  }

}
