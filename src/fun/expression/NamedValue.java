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

package fun.expression;

import common.ElementInfo;

import fun.FunBase;

public class NamedValue extends FunBase {
  private String name;
  private Expression value;

  public NamedValue(ElementInfo info, String name, Expression value) {
    super(info);
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setValue(Expression value) {
    assert (value != null);
    this.value = value;
  }

  public Expression getValue() {
    return value;
  }

  @Override
  public String toString() {
    return name + ":=" + value;
  }

}
