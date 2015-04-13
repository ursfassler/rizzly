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

package ast.data.expression.reference;

import ast.data.Ast;
import ast.data.AstList;
import ast.data.Named;

import common.ElementInfo;

public class Reference extends BaseRef<Named> {
  public final AstList<RefItem> offset;

  public Reference(ElementInfo info, Named link, AstList<RefItem> offset) {
    super(info, link);
    assert (link != null);
    this.offset = new AstList<RefItem>(offset);
  }

  public Reference(ElementInfo info, Named link, RefItem itm) {
    super(info, link);
    assert (link != null);
    this.offset = new AstList<RefItem>();
    this.offset.add(itm);
  }

  public Reference(ElementInfo info, Named link) {
    super(info, link);
    assert (link != null);
    this.offset = new AstList<RefItem>();
  }

  public Reference(ElementInfo info, String name) {
    super(info, new DummyLinkTarget(info, name));
    this.offset = new AstList<RefItem>();
  }

  @Override
  public String toString() {
    String ret = super.toString();
    for (RefItem item : offset) {
      ret += item.toString();
    }
    return ret;
  }

  @Override
  public Ast getTarget() {
    assert (offset.isEmpty()); // FIXME make it correct or remove function
    return link;
  }

}
