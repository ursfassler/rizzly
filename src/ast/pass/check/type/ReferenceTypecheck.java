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
package ast.pass.check.type;

import ast.data.Ast;
import ast.data.AstList;
import ast.data.expression.Expression;
import ast.data.reference.RefCall;
import ast.data.reference.RefIndex;
import ast.data.reference.RefItem;
import ast.data.reference.RefName;
import ast.data.reference.Reference;
import ast.data.type.Type;
import ast.data.type.TypeReference;
import ast.data.type.base.ArrayType;
import ast.data.type.base.EnumType;
import ast.data.type.base.FunctionType;
import ast.data.type.base.RangeType;
import ast.data.type.base.RangeTypeFactory;
import ast.dispatcher.NullDispatcher;
import ast.knowledge.KnowLeftIsContainerOfRight;
import ast.knowledge.KnowType;
import ast.knowledge.KnowledgeBase;
import ast.repository.query.ChildByName;
import error.ErrorType;
import error.RError;

//TODO check if this class does not too much (i.e. check and return type)
public class ReferenceTypecheck extends NullDispatcher<Type, Type> {
  final private KnowType kt;
  final private KnowLeftIsContainerOfRight kc;

  public ReferenceTypecheck(KnowledgeBase kb) {
    super();
    kt = kb.getEntry(KnowType.class);
    kc = kb.getEntry(KnowLeftIsContainerOfRight.class);
  }

  static public void process(Reference ast, KnowledgeBase kb) {
    ReferenceTypecheck adder = new ReferenceTypecheck(kb);
    adder.traverse(ast, null);
  }

  @Override
  protected Type visitDefault(Ast obj, Type param) {
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
      AstList<TypeReference> arg = ((FunctionType) sub).arg;
      AstList<Expression> argval = obj.actualParameter.value;
      if (arg.size() != argval.size()) {
        RError.err(ErrorType.Error, "Need " + arg.size() + " arguments, got " + argval.size(), obj.metadata());
        return null;
      }
      for (int i = 0; i < arg.size(); i++) {
        Type partype = kt.get(arg.get(i));
        Type valtype = kt.get(argval.get(i));
        if (!kc.get(partype, valtype)) {
          RError.err(ErrorType.Error, "Data type to big or incompatible (argument " + (i + 1) + ", " + partype.getName() + " := " + valtype.getName() + ")", argval.get(i).metadata());
        }
      }
      return kt.get(((FunctionType) sub).ret);
    } else {
      RError.err(ErrorType.Error, "Not a function: " + obj.toString(), obj.metadata());
      return null;
    }
  }

  @Override
  protected Type visitRefName(RefName obj, Type sub) {
    if (sub instanceof EnumType) {
      return sub;
    } else {
      String name = obj.name;
      Ast etype = ChildByName.find(sub, name);
      if (etype == null) {
        RError.err(ErrorType.Error, "Child not found: " + name, obj.metadata());
      }
      return kt.get(etype);
    }
  }

  @Override
  protected Type visitRefIndex(RefIndex obj, Type sub) {
    Type index = kt.get(obj.index);
    if (sub instanceof ArrayType) {
      RangeType ait = RangeTypeFactory.create(((ArrayType) sub).size.intValue());
      if (!kc.get(ait, index)) {
        RError.err(ErrorType.Error, "array index type is " + ait.getName() + ", got " + index.getName(), obj.metadata());
      }
      return kt.get(((ArrayType) sub).type);
    } else {
      RError.err(ErrorType.Error, "need array to index, got type: " + sub.getName(), obj.metadata());
      return null;
    }
  }

}
