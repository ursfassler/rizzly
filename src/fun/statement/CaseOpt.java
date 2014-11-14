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

package fun.statement;

import common.ElementInfo;

import fun.FunBase;
import fun.other.FunList;

public class CaseOpt extends FunBase {
  private FunList<CaseOptEntry> value;
  private Block code;

  public CaseOpt(ElementInfo info, FunList<CaseOptEntry> value, Block code) {
    super(info);
    this.value = value;
    this.code = code;
  }

  public Block getCode() {
    return code;
  }

  public void setCode(Block code) {
    this.code = code;
  }

  public FunList<CaseOptEntry> getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value.toString();
  }

}
