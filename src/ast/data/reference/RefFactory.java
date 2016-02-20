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

  public static SimpleReference create(String name) {
    Anchor anchor = new UnlinkedAnchor(name);
    return new SimpleReference(anchor);
  }

  public static SimpleReference create(MetaList info, String name) {
    SimpleReference reference = create(name);
    reference.metadata().add(info);
    return reference;
  }

  public static SimpleReference create(Named link) {
    Anchor anchor = new LinkedAnchor(link);
    return new SimpleReference(anchor);
  }

  public static SimpleReference create(MetaList info, Named link) {
    SimpleReference reference = create(link);
    reference.metadata().add(info);
    return reference;
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

  public static LinkedReferenceWithOffset_Implementation oldCreate(Named link, RefItem item1, RefItem item2, RefItem item3) {
    AstList<RefItem> items = new AstList<RefItem>();
    items.add(item1);
    items.add(item2);
    items.add(item3);
    return new LinkedReferenceWithOffset_Implementation(link, items);
  }

  public static LinkedReferenceWithOffset_Implementation oldCreate(Named link, RefItem item1, RefItem item2) {
    AstList<RefItem> items = new AstList<RefItem>();
    items.add(item1);
    items.add(item2);
    return new LinkedReferenceWithOffset_Implementation(link, items);
  }

  public static LinkedReferenceWithOffset_Implementation oldCreate(Named link, RefItem item) {
    AstList<RefItem> items = new AstList<RefItem>();
    items.add(item);
    return new LinkedReferenceWithOffset_Implementation(link, items);
  }

  public static LinkedReferenceWithOffset_Implementation oldCreate(String name) {
    return oldFull(name);
  }

  @Deprecated
  public static LinkedReferenceWithOffset_Implementation oldCreate(MetaList info, String name) {
    return oldFull(info, name);
  }

  public static LinkedReferenceWithOffset_Implementation oldCreate(Named link) {
    return oldFull(link);
  }

  @Deprecated
  public static LinkedReferenceWithOffset_Implementation oldCreate(MetaList info, Named link) {
    return oldFull(info, link);
  }

  public static LinkedReferenceWithOffset_Implementation oldFull(Named link) {
    LinkedReferenceWithOffset_Implementation reference = new LinkedReferenceWithOffset_Implementation(link, new AstList<RefItem>());
    return reference;
  }

  @Deprecated
  public static LinkedReferenceWithOffset_Implementation oldFull(MetaList info, Named link) {
    LinkedReferenceWithOffset_Implementation reference = new LinkedReferenceWithOffset_Implementation(link, new AstList<RefItem>());
    reference.metadata().add(info);
    return reference;
  }

  public static LinkedReferenceWithOffset_Implementation oldFull(String name) {
    LinkTarget target = new LinkTarget(name);
    return oldFull(target);
  }

  @Deprecated
  public static LinkedReferenceWithOffset_Implementation oldFull(MetaList info, String name) {
    LinkTarget target = new LinkTarget(name);
    target.metadata().add(info);
    return oldFull(info, target);
  }

  public static LinkedReferenceWithOffset_Implementation oldCall(Function func, Expression arg1, Expression arg2) {
    AstList<Expression> arg = new AstList<Expression>();
    arg.add(arg1);
    arg.add(arg2);
    return oldCall(func, arg);
  }

  @Deprecated
  public static LinkedReference oldCall(MetaList info, Function func, Expression arg1, Expression arg2) {
    AstList<Expression> arg = new AstList<Expression>();
    arg.add(arg1);
    arg.add(arg2);
    return oldCall(info, func, arg);
  }

  public static LinkedReferenceWithOffset_Implementation oldCall(Function func, Expression arg1) {
    AstList<Expression> arg = new AstList<Expression>();
    arg.add(arg1);
    return oldCall(func, arg);
  }

  @Deprecated
  public static LinkedReference oldCall(MetaList info, Function func, Expression arg1) {
    AstList<Expression> arg = new AstList<Expression>();
    arg.add(arg1);
    return oldCall(info, func, arg);
  }

  public static LinkedReferenceWithOffset_Implementation oldCall(Function func) {
    return oldCall(func, new AstList<Expression>());
  }

  @Deprecated
  public static LinkedReferenceWithOffset_Implementation oldCall(MetaList info, Function func) {
    return oldCall(info, func, new AstList<Expression>());
  }

  public static LinkedReferenceWithOffset_Implementation oldCall(Function func, AstList<Expression> arg) {
    TupleValue actualParameter = new TupleValue(arg);
    RefCall call = new RefCall(actualParameter);
    LinkedReferenceWithOffset_Implementation ref = oldCreate(func, call);
    return ref;
  }

  @Deprecated
  public static LinkedReferenceWithOffset_Implementation oldCall(MetaList info, Function func, AstList<Expression> arg) {
    TupleValue actualParameter = new TupleValue(arg);
    actualParameter.metadata().add(info);
    RefCall call = new RefCall(actualParameter);
    call.metadata().add(info);
    LinkedReferenceWithOffset_Implementation ref = oldCreate(func, call);
    ref.metadata().add(info);
    return ref;
  }

}
