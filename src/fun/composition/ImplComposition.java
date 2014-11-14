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

package fun.composition;

import common.ElementInfo;

import fun.Fun;
import fun.content.CompIfaceContent;
import fun.other.CompImpl;
import fun.other.FunList;

public class ImplComposition extends CompImpl {
  final private FunList<Fun> instantiation = new FunList<Fun>();
  final private FunList<Connection> connection = new FunList<Connection>();

  public ImplComposition(ElementInfo info, String name) {
    super(info, name);
  }

  public FunList<Connection> getConnection() {
    return connection;
  }

  public FunList<Fun> getInstantiation() {
    return instantiation;
  }

  @Override
  public FunList<CompIfaceContent> getInterface() {
    return findInterface(instantiation);
  }
}
