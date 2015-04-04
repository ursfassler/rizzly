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

package evl.pass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pass.EvlPass;
import error.ErrorType;
import error.RError;
import evl.expression.ArrayValue;
import evl.expression.Expression;
import evl.expression.NamedElementsValue;
import evl.expression.NamedValue;
import evl.expression.RecordValue;
import evl.expression.TupleValue;
import evl.expression.UnionValue;
import evl.expression.UnsafeUnionValue;
import evl.expression.reference.Reference;
import evl.expression.reference.SimpleRef;
import evl.knowledge.KnowChild;
import evl.knowledge.KnowledgeBase;
import evl.other.EvlList;
import evl.other.Namespace;
import evl.traverser.ExprReplacer;
import evl.type.Type;
import evl.type.base.ArrayType;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.type.composed.NamedElement;
import evl.type.composed.RecordType;
import evl.type.composed.UnionType;
import evl.type.composed.UnsafeUnionType;
import evl.variable.DefVariable;

//TODO clean up
// Type Inference (for union assignments)
public class InitVarTyper extends EvlPass {
  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    InitVarTyperWorker inst = new InitVarTyperWorker(kb);
    inst.traverse(evl, null);
  }
}

class InitVarTyperWorker extends ExprReplacer<Type> {
  private final KnowChild kc;

  public InitVarTyperWorker(KnowledgeBase kb) {
    super();
    this.kc = kb.getEntry(KnowChild.class);
  }

  @Override
  protected Expression visitDefVariable(DefVariable obj, Type param) {
    assert (param == null);
    Type type = obj.type.link;
    obj.def = visit(obj.def, type);
    return null;
  }

  @Override
  protected Expression visitArrayValue(ArrayValue obj, Type param) {
    if (param instanceof ArrayType) {
      ArrayType at = (ArrayType) param;
      int size = at.size.intValue();
      EvlList<Expression> init = new EvlList<Expression>();
      for (int i = 0; i < size; i++) {
        init.add(null);
      }
      int idx = 0;
      for (Expression expr : obj.value) {
        init.set(idx, visit(expr, at.type.link));
        idx++;
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
  protected Expression visitTupleValue(TupleValue obj, Type param) {
    if (param == null) {
      // we propably came from an function call
      return obj;
    }

    if (obj.value.size() == 1) {
      // TODO do that at a different place
      return visit(obj.value.get(0), param);
    } else if (obj.value.size() == 0) {
      return obj;
    } else if (param instanceof ArrayType) {
      ArrayType at = (ArrayType) param;
      if (obj.value.size() != at.size.intValue()) {
        RError.err(ErrorType.Error, obj.getInfo(), "Expected " + at.size.toString() + " elements, got " + obj.value.size());
      }
      return new ArrayValue(obj.getInfo(), obj.value);
    } else {
      throw new RuntimeException("not yet implemented: " + param.getClass().getCanonicalName());
    }
  }

  @Override
  protected Expression visitNamedValue(NamedValue obj, Type type) {
    if (type instanceof UnionType) {
      EnumType et = (EnumType) ((UnionType) type).tag.ref.link;
      EnumElement ele = (EnumElement) kc.get(et, obj.name, obj.getInfo());

      EnumElement value = (EnumElement) kc.get(et, obj.name, obj.getInfo());

      NamedValue tag = new NamedValue(obj.getInfo(), ((UnionType) type).tag.getName(), new Reference(obj.getInfo(), value));

      Expression ov = obj.value;
      NamedElement elem = (NamedElement) kc.get(type, obj.name, obj.getInfo());
      ov = visit(ov, elem.ref.link);

      NamedValue content = new NamedValue(obj.getInfo(), ele.getName(), ov);

      UnionValue uv = new UnionValue(obj.getInfo(), tag, content, new SimpleRef<Type>(obj.getInfo(), type));
      return uv;
    } else if (type instanceof UnsafeUnionType) {
      Expression ov = obj.value;
      NamedElement elem = (NamedElement) kc.get(type, obj.name, obj.getInfo());
      ov = visit(ov, elem.ref.link);

      NamedValue content = new NamedValue(obj.getInfo(), elem.getName(), ov);

      UnsafeUnionValue uv = new UnsafeUnionValue(obj.getInfo(), content, new SimpleRef<Type>(obj.getInfo(), type));
      return uv;
    } else if (type instanceof ArrayType) {
      RError.err(ErrorType.Error, obj.getInfo(), "ArrayType only initializable by TupleValue");
      return null;
    } else {
      throw new RuntimeException("not yet implemented: " + type.getClass().getCanonicalName());
    }
  }

  @Override
  protected Expression visitRecordValue(RecordValue obj, Type param) {
    if (param instanceof RecordType) {
      assert (obj.type.link == param);
      return obj; // we assume it is right
    } else {
      throw new RuntimeException("not yet implemented: " + param.getClass().getCanonicalName());
    }
  }

  @Override
  protected Expression visitNamedElementsValue(NamedElementsValue obj, Type type) {
    EvlList<NamedValue> value = obj.value;
    if (type instanceof RecordType) {
      Map<String, Type> eletype = getTypes(((RecordType) type).element);
      for (NamedValue itm : value) {
        Type et = eletype.get(itm.name);
        if (et == null) {
          RError.err(ErrorType.Error, obj.getInfo(), "Record have no element named " + itm.name);
          return null;
        }
        itm.value = visit(itm.value, et);
      }
      return new RecordValue(obj.getInfo(), value, new SimpleRef<Type>(obj.getInfo(), type));
    } else if ((type instanceof UnionType) || (type instanceof UnsafeUnionType)) {
      if (value.size() != 1) {
        RError.err(ErrorType.Error, obj.getInfo(), "need exactly one entry for union type, got " + value.size());
        return null;
      }
      return visit(value.get(0), type);
    } else {
      throw new RuntimeException("not yet implemented: " + type.getClass().getCanonicalName());
    }
  }

  private Map<String, Type> getTypes(EvlList<NamedElement> element) {
    Map<String, Type> ret = new HashMap<String, Type>();
    for (NamedElement elem : element) {
      RError.ass(!ret.containsKey(elem.getName()), elem.getInfo(), "Entry with name " + elem.getName() + " already defined");
      ret.put(elem.getName(), elem.ref.link);
    }
    return ret;
  }

}
