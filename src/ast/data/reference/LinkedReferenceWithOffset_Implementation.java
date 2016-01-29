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

package ast.data.reference;

import ast.data.Ast;
import ast.data.AstBase;
import ast.data.AstList;
import ast.data.Named;
import ast.visitor.Visitor;

public class LinkedReferenceWithOffset_Implementation extends AstBase implements Reference, LinkedReferenceWithOffset {
  private Named link;
  private final AstList<RefItem> offset;

  public LinkedReferenceWithOffset_Implementation(Named link, AstList<RefItem> offset) {
    super();
    this.link = link;
    this.offset = offset;
  }

  @Deprecated
  public Ast getTarget() {
    assert (getOffset().isEmpty()); // FIXME make it correct or remove function
    return getLink();
  }

  @Override
  public Named getLink() {
    return link;
  }

  @Override
  public void setLink(Named value) {
    link = value;
  }

  @Override
  public AstList<RefItem> getOffset() {
    return offset;
  }

  @Override
  public String toString() {
    String ret = "->" + getLink();
    for (RefItem item : getOffset()) {
      ret += item.toString();
    }
    return ret;
  }

}
