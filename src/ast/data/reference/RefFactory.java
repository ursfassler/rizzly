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

import ast.ElementInfo;
import ast.data.AstList;
import ast.data.Named;
import ast.data.expression.Expression;
import ast.data.expression.value.TupleValue;
import ast.data.function.Function;

public class RefFactory {

  public static Reference create(ElementInfo info, Named link, RefItem item1, RefItem item2, RefItem item3) {
    AstList<RefItem> items = new AstList<RefItem>();
    items.add(item1);
    items.add(item2);
    items.add(item3);
    return new Reference(info, link, items);
  }

  public static Reference create(ElementInfo info, Named link, RefItem item1, RefItem item2) {
    AstList<RefItem> items = new AstList<RefItem>();
    items.add(item1);
    items.add(item2);
    return new Reference(info, link, items);
  }

  public static Reference create(ElementInfo info, Named link, RefItem item) {
    AstList<RefItem> items = new AstList<RefItem>();
    items.add(item);
    return new Reference(info, link, items);
  }

  public static Reference create(ElementInfo info, String name) {
    return full(info, name);
  }

  public static Reference create(ElementInfo info, Named link) {
    return full(info, link);
  }

  public static Reference full(ElementInfo info, Named link) {
    return new Reference(info, link, new AstList<RefItem>());
  }

  public static Reference full(ElementInfo info, String name) {
    return full(info, new DummyLinkTarget(info, name));
  }

  public static Reference call(ElementInfo info, Function func, Expression arg1, Expression arg2) {
    AstList<Expression> arg = new AstList<Expression>();
    arg.add(arg1);
    arg.add(arg2);
    return call(info, func, arg);
  }

  public static Reference call(ElementInfo info, Function func, Expression arg1) {
    AstList<Expression> arg = new AstList<Expression>();
    arg.add(arg1);
    return call(info, func, arg);
  }

  public static Reference call(ElementInfo info, Function func) {
    return call(info, func, new AstList<Expression>());
  }

  public static Reference call(ElementInfo info, Function func, AstList<Expression> arg) {
    return create(info, func, new RefCall(info, new TupleValue(info, arg)));
  }

}
