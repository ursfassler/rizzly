package evl.traverser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import evl.knowledge.KnowChild;
import evl.knowledge.KnowledgeBase;
import evl.other.Named;
import evl.other.Namespace;
import evl.type.Type;
import evl.type.TypeRef;
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
    Type type = obj.getType().getRef();
    obj.setDef(visit(obj.getDef(), type));
    return null;
  }

  @Override
  protected Expression visitArrayValue(ArrayValue obj, Type param) {
    if (param instanceof ArrayType) {
      ArrayType at = (ArrayType) param;
      int size = at.getSize().intValue();
      List<Expression> init = new ArrayList<Expression>(size);
      for (int i = 0; i < size; i++) {
        init.add(null);
      }
      int idx = 0;
      for (Expression expr : obj.getValue()) {
        if (expr instanceof NamedElementValue) {
          if (!((NamedElementValue) expr).getName().equals("_")) {
            RError.err(ErrorType.Error, expr.getInfo(), "Unknown name: " + ((NamedElementValue) expr).getName());
          } else {
            expr = visit(((NamedElementValue) expr).getValue(), at.getType().getRef());
            for (int i = 0; i < size; i++) {
              if (init.get(i) == null) {
                init.set(i, Copy.copy(expr));
              }
            }
          }
        } else {
          init.set(idx, visit(expr, at.getType().getRef()));
          idx++;
        }
      }

      return new ArrayValue(obj.getInfo(), init);
    } else {
      throw new RuntimeException("not yet implemented: " + param.getClass().getCanonicalName());
    }
  }

  @Override
  protected Expression visitExprList(ExprList obj, Type param) {
    if (param instanceof RecordType) {
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

      Collection<NamedElementValue> iv = new ArrayList<NamedElementValue>();
      for (Named itr : ((RecordType) param).getElement()) {
        Expression value = map.remove(itr.getName());
        if (value == null) {
          RError.err(ErrorType.Error, obj.getInfo(), "Missing initializer: " + itr.getName());
        } else {
          NamedElement elem = (NamedElement) kc.get(param, itr.getName(), value.getInfo());
          value = visit(value, elem.getType().getRef());
          iv.add(new NamedElementValue(value.getInfo(), itr.getName(), value));
        }
      }

      if (!map.isEmpty()) {
        for (String key : map.keySet()) {
          RError.err(ErrorType.Warning, map.get(key).getInfo(), "Unknown item: " + key);
        }
        RError.err(ErrorType.Error, obj.getInfo(), "Too many items");
      }

      return new RecordValue(obj.getInfo(), iv, new TypeRef(obj.getInfo(), param));
    } else if (obj.getValue().size() == 1) {
      // TODO do that at a different place
      return visit(obj.getValue().get(0), param);
    } else {
      throw new RuntimeException("not yet implemented: " + param.getClass().getCanonicalName());
    }
  }

  @Override
  protected Expression visitNamedElementValue(NamedElementValue obj, Type type) {
    if (type instanceof UnionType) {
      EnumType et = (EnumType) ((UnionType) type).getTag().getType().getRef();
      EnumElement ele = (EnumElement) kc.get(et, obj.getName(), obj.getInfo());

      EnumElement value = (EnumElement) kc.get(et, obj.getName(), obj.getInfo());

      NamedElementValue tag = new NamedElementValue(obj.getInfo(), ((UnionType) type).getTag().getName(), new Reference(obj.getInfo(), value));

      Expression ov = obj.getValue();
      NamedElement elem = (NamedElement) kc.get(type, obj.getName(), obj.getInfo());
      ov = visit(ov, elem.getType().getRef());

      NamedElementValue content = new NamedElementValue(obj.getInfo(), ele.getName(), ov);

      UnionValue uv = new UnionValue(obj.getInfo(), tag, content, new TypeRef(obj.getInfo(), type));
      return uv;
    } else if (type instanceof UnsafeUnionType) {
      Expression ov = obj.getValue();
      NamedElement elem = (NamedElement) kc.get(type, obj.getName(), obj.getInfo());
      ov = visit(ov, elem.getType().getRef());

      NamedElementValue content = new NamedElementValue(obj.getInfo(), elem.getName(), ov);

      UnsafeUnionValue uv = new UnsafeUnionValue(obj.getInfo(), content, new TypeRef(obj.getInfo(), type));
      return uv;
    } else {
      throw new RuntimeException("not yet implemented: " + type.getClass().getCanonicalName());
    }
  }
}
