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

package fun.other;

import java.util.List;

import common.Designator;
import common.ElementInfo;

import fun.Fun;
import fun.FunBase;

/**
 *
 * @author urs
 */
final public class RizzlyFile extends FunBase implements Named {
  private String name;
  final private List<Designator> imports;
  final private FunList<Fun> objects = new FunList<Fun>();

  public RizzlyFile(ElementInfo info, String name, List<Designator> imports) {
    super(info);
    this.name = name;
    this.imports = imports;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  public List<Designator> getImports() {
    return imports;
  }

  public FunList<Fun> getObjects() {
    return objects;
  }

  @Override
  public String toString() {
    return name;
  }

}
