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
package evl.pass.check.type.specific;

import error.ErrorType;
import error.RError;
import evl.data.Evl;
import evl.data.EvlList;
import evl.data.expression.Expression;
import evl.data.expression.reference.RefCall;
import evl.data.expression.reference.RefIndex;
import evl.data.expression.reference.RefItem;
import evl.data.expression.reference.RefName;
import evl.data.expression.reference.Reference;
import evl.data.expression.reference.SimpleRef;
import evl.data.type.Type;
import evl.data.type.base.ArrayType;
import evl.data.type.base.EnumType;
import evl.data.type.base.FunctionType;
import evl.data.type.base.RangeType;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowChild;
import evl.knowledge.KnowLeftIsContainerOfRight;
import evl.knowledge.KnowType;
import evl.knowledge.KnowledgeBase;
import evl.traverser.NullTraverser;

//TODO check if this class does not too much (i.e. check and return type)
public class RefTypeChecker extends NullTraverser<Type, Type> {
  final private KnowledgeBase kb;
  final private KnowBaseItem kbi;
  final private KnowType kt;
  final private KnowLeftIsContainerOfRight kc;

  public RefTypeChecker(KnowledgeBase kb) {
    super();
    this.kb = kb;
    kbi = kb.getEntry(KnowBaseItem.class);
    kt = kb.getEntry(KnowType.class);
    kc = kb.getEntry(KnowLeftIsContainerOfRight.class);
  }

  static public void process(Reference ast, KnowledgeBase kb) {
    RefTypeChecker adder = new RefTypeChecker(kb);
    adder.traverse(ast, null);
  }

  @Override
  protected Type visitDefault(Evl obj, Type param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Type visitReference(Reference obj, Type param) {
    Type ret = kt.get(obj.link);
    for (RefItem ref : obj.offset) {
      ret = visit(ref, ret);
      assert (ret != null);
    }
    return ret;
  }

  @Override
  protected Type visitRefCall(RefCall obj, Type sub) {
    if (sub instanceof FunctionType) {
      EvlList<SimpleRef<Type>> arg = ((FunctionType) sub).arg;
      EvlList<Expression> argval = obj.actualParameter.value;
      if (arg.size() != argval.size()) {
        RError.err(ErrorType.Error, obj.getInfo(), "Need " + arg.size() + " arguments, got " + argval.size());
        return null;
      }
      for (int i = 0; i < arg.size(); i++) {
        Type partype = arg.get(i).link;
        Type valtype = kt.get(argval.get(i));
        if (!kc.get(partype, valtype)) {
          RError.err(ErrorType.Error, argval.get(i).getInfo(), "Data type to big or incompatible (argument " + (i + 1) + ", " + partype.name + " := " + valtype.name + ")");
        }
      }
      return ((FunctionType) sub).ret.link;
    } else {
      RError.err(ErrorType.Error, obj.getInfo(), "Not a function: " + obj.toString());
      return null;
    }
  }

  @Override
  protected Type visitRefName(RefName obj, Type sub) {
    if (sub instanceof EnumType) {
      return sub;
    } else {
      KnowChild kc = kb.getEntry(KnowChild.class);
      String name = obj.name;
      Evl etype = kc.find(sub, name);
      if (etype == null) {
        RError.err(ErrorType.Error, obj.getInfo(), "Child not found: " + name);
      }
      return kt.get(etype);
    }
  }

  @Override
  protected Type visitRefIndex(RefIndex obj, Type sub) {
    Type index = kt.get(obj.index);
    if (sub instanceof ArrayType) {
      RangeType ait = kbi.getRangeType(((ArrayType) sub).size.intValue());
      if (!kc.get(ait, index)) {
        RError.err(ErrorType.Error, obj.getInfo(), "array index type is " + ait.name + ", got " + index.name);
      }
      return ((ArrayType) sub).type.link;
    } else {
      RError.err(ErrorType.Error, obj.getInfo(), "need array to index, got type: " + sub.name);
      return null;
    }
  }

}
