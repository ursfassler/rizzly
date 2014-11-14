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

package cir.expression.reference;

import java.util.LinkedList;
import java.util.List;

import cir.expression.Expression;

final public class Reference extends Expression {
  private Referencable ref;
  final private LinkedList<RefItem> offset = new LinkedList<RefItem>();

  public Reference(Referencable ref, List<RefItem> offset) {
    super();
    this.ref = ref;
    this.offset.addAll(offset);
  }

  public Reference(Referencable ref, RefItem itm) {
    super();
    this.ref = ref;
    this.offset.add(itm);
  }

  public Reference(Referencable ref) {
    super();
    this.ref = ref;
  }

  public Referencable getRef() {
    return ref;
  }

  public void setRef(Referencable ref) {
    this.ref = ref;
  }

  public LinkedList<RefItem> getOffset() {
    return offset;
  }

  @Override
  public String toString() {
    return ref.toString();
  }

}
