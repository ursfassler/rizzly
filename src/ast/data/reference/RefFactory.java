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

import ast.data.AstList;
import ast.data.Named;
import ast.data.expression.Expression;
import ast.data.expression.value.TupleValue;
import ast.data.function.Function;
import ast.meta.MetaList;

public class RefFactory {

  public static Reference create(String name) {
    Anchor anchor = new UnlinkedAnchor(name);
    // return new SimpleReference(anchor);
    return new OffsetReference(anchor, new AstList<RefItem>());
  }

  public static Reference create(MetaList info, String name) {
    Reference reference = create(name);
    reference.metadata().add(info);
    return reference;
  }

  public static Reference create(Named link) {
    return withOffset(link);
    // Anchor anchor = new LinkedAnchor(link);
    // return new SimpleReference(anchor);
  }

  public static Reference create(MetaList info, Named link) {
    return withOffset(info, link);
    // SimpleReference reference = create(link);
    // reference.metadata().add(info);
    // return reference;
  }

  public static OffsetReference create(MetaList info, String name, AstList<RefItem> offset) {
    Anchor anchor = new UnlinkedAnchor(name);
    OffsetReference reference = new OffsetReference(anchor, offset);
    reference.metadata().add(info);
    return reference;
  }

  public static OffsetReference create(MetaList info, Named link, AstList<RefItem> offset) {
    Anchor anchor = new LinkedAnchor(link);
    OffsetReference reference = new OffsetReference(anchor, offset);
    reference.metadata().add(info);
    return reference;
  }

  public static OffsetReference create(Named link, AstList<RefItem> offset) {
    Anchor anchor = new LinkedAnchor(link);
    OffsetReference reference = new OffsetReference(anchor, offset);
    return reference;
  }

  public static OffsetReference withOffset(Named link) {
    return create(link, new AstList<RefItem>());
  }

  public static OffsetReference withOffset(MetaList info, Named link) {
    OffsetReference reference = withOffset(link);
    reference.metadata().add(info);
    return reference;
  }

  public static OffsetReference call(Function func, Expression arg1) {
    AstList<Expression> arg = new AstList<Expression>();
    arg.add(arg1);
    return call(func, arg);
  }

  public static OffsetReference call(Function func, Expression arg1, Expression arg2) {
    AstList<Expression> arg = new AstList<Expression>();
    arg.add(arg1);
    arg.add(arg2);
    return call(func, arg);
  }

  public static OffsetReference call(Function func, AstList<Expression> arg) {
    TupleValue actualParameter = new TupleValue(arg);
    RefCall call = new RefCall(actualParameter);
    OffsetReference ref = create(func, call);
    return ref;
  }

  public static Reference call(Function func) {
    return call(func, new AstList<Expression>());
  }

  public static OffsetReference create(Named link, RefItem item) {
    AstList<RefItem> items = new AstList<RefItem>();
    items.add(item);
    return create(link, items);
  }

  public static OffsetReference create(Named link, RefItem item1, RefItem item2, RefItem item3) {
    AstList<RefItem> items = new AstList<RefItem>();
    items.add(item1);
    items.add(item2);
    items.add(item3);
    return create(link, items);
  }

  public static OffsetReference create(Named link, RefItem item1, RefItem item2) {
    AstList<RefItem> items = new AstList<RefItem>();
    items.add(item1);
    items.add(item2);
    return create(link, items);
  }

}
