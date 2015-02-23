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

package evl.statement;

import common.ElementInfo;

import evl.expression.Expression;
import evl.expression.reference.Reference;
import evl.other.EvlList;

/**
 *
 * @author urs
 */
final public class AssignmentMulti extends Assignment<EvlList<Reference>> {

  final private EvlList<Reference> left;

  public AssignmentMulti(ElementInfo info, EvlList<Reference> left, Expression right) {
    super(info, right);
    assert (left.size() > 1);
    this.left = left;
  }

  @Override
  public EvlList<Reference> getLeft() {
    return left;
  }

}
