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

package ast.pass.input.xml.parser;

import java.util.Collection;
import java.util.HashSet;

public class Names {

  public static Collection<String> list() {
    return new HashSet<String>();
  }

  public static Collection<String> list(String name1) {
    Collection<String> names = list();
    names.add(name1);
    return names;
  }

  public static Collection<String> list(String name1, String name2) {
    Collection<String> names = list(name1);
    names.add(name2);
    return names;
  }

  public static Object list(String name1, String name2, String name3) {
    Collection<String> names = list(name1, name2);
    names.add(name3);
    return names;
  }

}
