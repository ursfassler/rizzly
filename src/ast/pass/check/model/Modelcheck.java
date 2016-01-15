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

import main.Configuration;
import ast.pass.others.GroupPass;

public class Modelcheck extends GroupPass {

  public Modelcheck(Configuration configuration) {
    super(configuration);
    append(new Root(configuration));
    append(new Usefullness(configuration));
    append(new RtcViolation(configuration));
    append(new Io(configuration));
    append(new HfsmTransScopeCheck(configuration));
    append(new CompInterfaceTypeChecker(configuration));
    append(new HfsmModelChecker(configuration));
  }

}
