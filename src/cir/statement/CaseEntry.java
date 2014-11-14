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

package cir.statement;

import java.util.ArrayList;
import java.util.List;

import util.Range;
import cir.CirBase;

public class CaseEntry extends CirBase {
  final private List<Range> values;
  private Statement code;

  public CaseEntry(Block code) {
    super();
    this.values = new ArrayList<Range>();
    this.code = code;
  }

  public List<Range> getValues() {
    return values;
  }

  public Statement getCode() {
    return code;
  }

  public void setCode(Statement code) {
    this.code = code;
  }

  @Override
  public String toString() {
    return values.toString();
  }

}
