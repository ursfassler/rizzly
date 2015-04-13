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

package evl.data.file;

import java.util.List;

import common.Designator;
import common.ElementInfo;

import evl.data.Evl;
import evl.data.EvlList;
import evl.data.Named;

/**
 *
 * @author urs
 */
final public class RizzlyFile extends Named {
  final private List<Designator> imports;
  final private EvlList<Evl> objects = new EvlList<Evl>();

  public RizzlyFile(ElementInfo info, String name, List<Designator> imports) {
    super(info, name);
    this.imports = imports;
  }

  public List<Designator> getImports() {
    return imports;
  }

  public EvlList<Evl> getObjects() {
    return objects;
  }

  @Override
  public String toString() {
    return name;
  }

}
