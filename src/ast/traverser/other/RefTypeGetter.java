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

package ast.traverser.other;

import ast.data.Ast;
import ast.data.expression.reference.RefCall;
import ast.data.expression.reference.RefIndex;
import ast.data.expression.reference.RefName;
import ast.data.expression.reference.Reference;
import ast.data.expression.reference.SimpleRef;
import ast.data.type.Type;
import ast.data.type.base.ArrayType;
import ast.data.type.base.EnumType;
import ast.data.type.base.FunctionType;
import ast.data.type.composed.NamedElement;
import ast.knowledge.KnowChild;
import ast.knowledge.KnowType;
import ast.knowledge.KnowledgeBase;
import ast.traverser.NullTraverser;
import error.ErrorType;
import error.RError;

public class RefTypeGetter extends NullTraverser<Type, Type> {
  final private KnowChild kc;
  final private KnowType kt; // FIXME remove cyclic dependency

  public RefTypeGetter(KnowType kt, KnowledgeBase kb) {
    super();
    kc = kb.getEntry(KnowChild.class);
    this.kt = kt;
  }

  @Override
  protected Type visitDefault(Ast obj, Type param) {
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
      return kt.get(((FunctionType) sub).ret);
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
      return kt.get(etype.typeref);
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
