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

package ast.dispatcher.other;

import java.util.Collection;

import ast.data.Ast;
import ast.data.reference.RefCall;
import ast.data.reference.RefIndex;
import ast.data.reference.RefName;
import ast.data.reference.Reference;
import ast.data.type.Type;
import ast.data.type.base.ArrayType;
import ast.data.type.base.EnumType;
import ast.data.type.base.FunctionType;
import ast.data.type.composed.NamedElement;
import ast.dispatcher.NullDispatcher;
import ast.knowledge.KnowType;
import ast.knowledge.KnowledgeBase;
import ast.repository.query.ChildByName;
import error.ErrorType;
import error.RError;

public class RefTypeGetter extends NullDispatcher<Type, Type> {
  final private KnowType kt; // FIXME remove cyclic dependency

  public RefTypeGetter(KnowType kt, KnowledgeBase kb) {
    super();
    this.kt = kt;
  }

  @Override
  public Type traverse(Collection<? extends Ast> list, Type param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public Type traverse(Ast obj, Type param) {
    return super.traverse(obj, param);
  }

  @Override
  protected Type visitDefault(Ast obj, Type param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Type visitReference(Reference obj, Type param) {
    return (Type) obj.getTarget();
  }

  @Override
  protected Type visitRefCall(RefCall obj, Type sub) {
    if (sub instanceof FunctionType) {
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
      NamedElement etype = (NamedElement) ChildByName.find(sub, name);
      if (etype == null) {
        RError.err(ErrorType.Error, "Child not found: " + obj, obj.metadata());
      }
      return kt.get(etype.typeref);
    }
  }

  @Override
  protected Type visitRefIndex(RefIndex obj, Type sub) {
    if (sub instanceof ArrayType) {
      return visit(((ArrayType) sub).type.ref, null);
    } else {
      RError.err(ErrorType.Error, "need array to index, got type: " + sub.getName(), obj.metadata());
      return null;
    }
  }

}
