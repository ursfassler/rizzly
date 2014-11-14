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

package evl.traverser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.copy.Copy;
import evl.expression.ArrayValue;
import evl.expression.ExprList;
import evl.expression.Expression;
import evl.expression.NamedElementValue;
import evl.expression.RecordValue;
import evl.expression.UnionValue;
import evl.expression.UnsafeUnionValue;
import evl.expression.reference.Reference;
import evl.expression.reference.SimpleRef;
import evl.knowledge.KnowChild;
import evl.knowledge.KnowledgeBase;
import evl.other.EvlList;
import evl.other.Namespace;
import evl.type.Type;
import evl.type.base.ArrayType;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.type.composed.NamedElement;
import evl.type.composed.NamedElementType;
import evl.type.composed.RecordType;
import evl.type.composed.UnionType;
import evl.type.composed.UnsafeUnionType;
import evl.variable.DefVariable;

//TODO clean up
// Type Inference (for union assignments)
public class InitVarTyper extends ExprReplacer<Type> {
  private final KnowChild kc;

  public static void process(Namespace aclasses, KnowledgeBase kb) {
    InitVarTyper inst = new InitVarTyper(kb);
    inst.traverse(aclasses, null);
  }

  public InitVarTyper(KnowledgeBase kb) {
    super();
    this.kc = kb.getEntry(KnowChild.class);
  }

  @Override
  protected Expression visitDefVariable(DefVariable obj, Type param) {
    assert (param == null);
    Type type = obj.getType().getLink();
    obj.setDef(visit(obj.getDef(), type));
    return null;
  }

  @Override
  protected Expression visitArrayValue(ArrayValue obj, Type param) {
    if (param instanceof ArrayType) {
      ArrayType at = (ArrayType) param;
      int size = at.getSize().intValue();
      EvlList<Expression> init = new EvlList<Expression>();
      for (int i = 0; i < size; i++) {
        init.add(null);
      }
      int idx = 0;
      for (Expression expr : obj.getValue()) {
        if (expr instanceof NamedElementValue) {
          if (!((NamedElementValue) expr).getName().equals("_")) {
            RError.err(ErrorType.Error, expr.getInfo(), "Unknown name: " + ((NamedElementValue) expr).getName());
          } else {
            expr = visit(((NamedElementValue) expr).getValue(), at.getType().getLink());
            for (int i = 0; i < size; i++) {
              if (init.get(i) == null) {
                init.set(i, Copy.copy(expr));
              }
            }
          }
        } else {
          init.set(idx, visit(expr, at.getType().getLink()));
          idx++;
        }
      }

      ArrayList<Integer> missing = new ArrayList<Integer>();
      for (int i = 0; i < size; i++) {
        if (init.get(i) == null) {
          missing.add(i);
        }
      }
      if (!missing.isEmpty()) {
        RError.err(ErrorType.Error, obj.getInfo(), "Elements not initialized: " + missing);
      }

      return new ArrayValue(obj.getInfo(), init);
    } else {
      throw new RuntimeException("not yet implemented: " + param.getClass().getCanonicalName());
    }
  }

  @Override
  protected Expression visitExprList(ExprList obj, Type param) {
    // FIXME recursion does not work
    if (param instanceof RecordType) {
      Collection<NamedElementValue> iv = toElemList(obj, param);
      return new RecordValue(obj.getInfo(), iv, new SimpleRef<Type>(obj.getInfo(), param));
    } else if (param instanceof UnsafeUnionType) {
      Map<String, Expression> map = toMap(obj);
      if (map.size() != 1) {
        RError.err(ErrorType.Error, obj.getInfo(), "Union need exactly 1 value");
      }

      String name = map.keySet().iterator().next();
      Expression value = map.get(name);
      NamedElement elem = ((UnsafeUnionType) param).getElement().find(name);

      value = visit(value, elem.getRef().getLink());

      return new UnsafeUnionValue(obj.getInfo(), new NamedElementValue(obj.getInfo(), name, value), Copy.copy(elem.getRef()));
    } else if (obj.getValue().size() == 1) {
      // TODO do that at a different place
      return visit(obj.getValue().get(0), param);
    } else {
      throw new RuntimeException("not yet implemented: " + param.getClass().getCanonicalName());
    }
  }

  private Collection<NamedElementValue> toElemList(ExprList obj, Type param) {
    Map<String, Expression> map = toMap(obj);

    Collection<NamedElementValue> iv = toNamedElem(obj.getInfo(), (NamedElementType) param, map);

    if (!map.isEmpty()) {
      for (String key : map.keySet()) {
        RError.err(ErrorType.Warning, map.get(key).getInfo(), "Unknown item: " + key);
      }
      RError.err(ErrorType.Error, obj.getInfo(), "Too many items");
    }
    return iv;
  }

  private Collection<NamedElementValue> toNamedElem(ElementInfo info, NamedElementType param, Map<String, Expression> map) {
    Collection<NamedElementValue> iv = new ArrayList<NamedElementValue>();
    for (NamedElement itr : param.getElement()) {
      Expression value = map.remove(itr.getName());
      if (value == null) {
        RError.err(ErrorType.Error, info, "Missing initializer: " + itr.getName());
      } else {
        NamedElement elem = (NamedElement) kc.get(param, itr.getName(), value.getInfo());
        value = visit(value, elem.getRef().getLink());
        iv.add(new NamedElementValue(value.getInfo(), itr.getName(), value));
      }
    }
    return iv;
  }

  private Map<String, Expression> toMap(ExprList obj) {
    Map<String, Expression> map = new HashMap<String, Expression>();
    for (Expression itr : obj.getValue()) {
      if (itr instanceof NamedElementValue) {
        String name = ((NamedElementValue) itr).getName();
        Expression value = ((NamedElementValue) itr).getValue();

        if (map.containsKey(name)) {
          RError.err(ErrorType.Warning, map.get(name).getInfo(), "First defined here");
          RError.err(ErrorType.Error, itr.getInfo(), "Duplicated entry");
        } else {
          map.put(name, value);
        }
      } else {
        RError.err(ErrorType.Error, itr.getInfo(), "Expected named element value, got: " + itr.getClass().getCanonicalName());
      }
    }
    return map;
  }

  @Override
  protected Expression visitNamedElementValue(NamedElementValue obj, Type type) {
    if (type instanceof UnionType) {
      EnumType et = (EnumType) ((UnionType) type).getTag().getRef().getLink();
      EnumElement ele = (EnumElement) kc.get(et, obj.getName(), obj.getInfo());

      EnumElement value = (EnumElement) kc.get(et, obj.getName(), obj.getInfo());

      NamedElementValue tag = new NamedElementValue(obj.getInfo(), ((UnionType) type).getTag().getName(), new Reference(obj.getInfo(), value));

      Expression ov = obj.getValue();
      NamedElement elem = (NamedElement) kc.get(type, obj.getName(), obj.getInfo());
      ov = visit(ov, elem.getRef().getLink());

      NamedElementValue content = new NamedElementValue(obj.getInfo(), ele.getName(), ov);

      UnionValue uv = new UnionValue(obj.getInfo(), tag, content, new SimpleRef<Type>(obj.getInfo(), type));
      return uv;
    } else if (type instanceof UnsafeUnionType) {
      Expression ov = obj.getValue();
      NamedElement elem = (NamedElement) kc.get(type, obj.getName(), obj.getInfo());
      ov = visit(ov, elem.getRef().getLink());

      NamedElementValue content = new NamedElementValue(obj.getInfo(), elem.getName(), ov);

      UnsafeUnionValue uv = new UnsafeUnionValue(obj.getInfo(), content, new SimpleRef<Type>(obj.getInfo(), type));
      return uv;
    } else {
      throw new RuntimeException("not yet implemented: " + type.getClass().getCanonicalName());
    }
  }

  @Override
  protected Expression visitRecordValue(RecordValue obj, Type param) {
    if (param instanceof RecordType) {
      return obj; // we assume it is right
    } else {
      throw new RuntimeException("not yet implemented: " + param.getClass().getCanonicalName());
    }
  }

}
