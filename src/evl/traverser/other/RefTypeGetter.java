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

package evl.traverser.other;

import error.ErrorType;
import error.RError;
import evl.data.Evl;
import evl.data.expression.reference.RefCall;
import evl.data.expression.reference.RefIndex;
import evl.data.expression.reference.RefName;
import evl.data.expression.reference.Reference;
import evl.data.expression.reference.SimpleRef;
import evl.data.type.Type;
import evl.data.type.base.ArrayType;
import evl.data.type.base.EnumType;
import evl.data.type.base.FunctionType;
import evl.data.type.composed.NamedElement;
import evl.knowledge.KnowChild;
import evl.knowledge.KnowledgeBase;
import evl.traverser.NullTraverser;

public class RefTypeGetter extends NullTraverser<Type, Type> {
  final private KnowChild kc;

  public RefTypeGetter(KnowledgeBase kb) {
    super();
    kc = kb.getEntry(KnowChild.class);
  }

  @Override
  protected Type visitDefault(Evl obj, Type param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Type visitSimpleRef(SimpleRef obj, Type param) {
    return (Type) obj.link;
  }

  @Override
  protected Type visitReference(Reference obj, Type param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
    // Type ret = kt.traverse(obj.getLink(), null);
    // for (RefItem ref : obj.getOffset()) {
    // ret = visit(ref, ret);
    // assert (ret != null);
    // }
    // return ret;
  }

  @Override
  protected Type visitRefCall(RefCall obj, Type sub) {
    if (sub instanceof FunctionType) {
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
      String name = obj.name;
      NamedElement etype = (NamedElement) kc.find(sub, name);
      if (etype == null) {
        RError.err(ErrorType.Error, obj.getInfo(), "Child not found: " + obj);
      }
      return etype.ref.link;
    }
  }

  @Override
  protected Type visitRefIndex(RefIndex obj, Type sub) {
    if (sub instanceof ArrayType) {
      return visit(((ArrayType) sub).type, null);
    } else {
      RError.err(ErrorType.Error, obj.getInfo(), "need array to index, got type: " + sub.name);
      return null;
    }
  }

}
