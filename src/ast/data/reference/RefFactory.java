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

  public static Reference create(Named link, RefItem item1, RefItem item2, RefItem item3) {
    AstList<RefItem> items = new AstList<RefItem>();
    items.add(item1);
    items.add(item2);
    items.add(item3);
    return new Reference(link, items);
  }

  public static Reference create(Named link, RefItem item1, RefItem item2) {
    AstList<RefItem> items = new AstList<RefItem>();
    items.add(item1);
    items.add(item2);
    return new Reference(link, items);
  }

  public static Reference create(Named link, RefItem item) {
    AstList<RefItem> items = new AstList<RefItem>();
    items.add(item);
    return new Reference(link, items);
  }

  public static Reference create(String name) {
    return full(name);
  }

  @Deprecated
  public static Reference create(MetaList info, String name) {
    return full(info, name);
  }

  public static Reference create(Named link) {
    return full(link);
  }

  @Deprecated
  public static Reference create(MetaList info, Named link) {
    return full(info, link);
  }

  public static Reference full(Named link) {
    Reference reference = new Reference(link, new AstList<RefItem>());
    return reference;
  }

  @Deprecated
  public static Reference full(MetaList info, Named link) {
    Reference reference = new Reference(link, new AstList<RefItem>());
    reference.metadata().add(info);
    return reference;
  }

  public static Reference full(String name) {
    LinkTarget target = new LinkTarget(name);
    return full(target);
  }

  @Deprecated
  public static Reference full(MetaList info, String name) {
    LinkTarget target = new LinkTarget(name);
    target.metadata().add(info);
    return full(info, target);
  }

  public static Reference call(Function func, Expression arg1, Expression arg2) {
    AstList<Expression> arg = new AstList<Expression>();
    arg.add(arg1);
    arg.add(arg2);
    return call(func, arg);
  }

  @Deprecated
  public static Reference call(MetaList info, Function func, Expression arg1, Expression arg2) {
    AstList<Expression> arg = new AstList<Expression>();
    arg.add(arg1);
    arg.add(arg2);
    return call(info, func, arg);
  }

  public static Reference call(Function func, Expression arg1) {
    AstList<Expression> arg = new AstList<Expression>();
    arg.add(arg1);
    return call(func, arg);
  }

  @Deprecated
  public static Reference call(MetaList info, Function func, Expression arg1) {
    AstList<Expression> arg = new AstList<Expression>();
    arg.add(arg1);
    return call(info, func, arg);
  }

  public static Reference call(Function func) {
    return call(func, new AstList<Expression>());
  }

  @Deprecated
  public static Reference call(MetaList info, Function func) {
    return call(info, func, new AstList<Expression>());
  }

  public static Reference call(Function func, AstList<Expression> arg) {
    TupleValue actualParameter = new TupleValue(arg);
    RefCall call = new RefCall(actualParameter);
    Reference ref = create(func, call);
    return ref;
  }

  @Deprecated
  public static Reference call(MetaList info, Function func, AstList<Expression> arg) {
    TupleValue actualParameter = new TupleValue(arg);
    actualParameter.metadata().add(info);
    RefCall call = new RefCall(actualParameter);
    call.metadata().add(info);
    Reference ref = create(func, call);
    ref.metadata().add(info);
    return ref;
  }
}
