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

package ast.specification;

import java.util.Collection;
import java.util.Iterator;

import ast.data.Ast;
import ast.data.expression.reference.SimpleRef;
import ast.data.type.base.ArrayType;
import ast.data.type.base.BooleanType;
import ast.data.type.base.EnumType;
import ast.data.type.base.RangeType;
import ast.data.type.base.StringType;
import ast.data.type.composed.RecordType;
import ast.data.type.special.IntegerType;
import ast.data.type.special.NaturalType;
import ast.data.type.special.TypeType;
import ast.data.type.special.VoidType;
import ast.traverser.NullTraverser;

public class Equals extends Specification {
  final private Ast original;
  final static private EqualTraverser TRAVERSER = new EqualTraverser();

  public Equals(Ast original) {
    super();
    this.original = original;
  }

  @Override
  public boolean isSatisfiedBy(Ast candidate) {
    return TRAVERSER.traverse(original, candidate);
  }

}

class EqualTraverser extends NullTraverser<Boolean, Object> {

  @Override
  protected Boolean visitDefault(Ast obj, Object param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getName());
  }

  @Override
  protected Boolean visitList(Collection<? extends Ast> list, Object param) {
    if (param instanceof Collection) {
      Collection other = (Collection) param;

      if (list.size() != other.size()) {
        return false;
      }
      Iterator<? extends Ast> listItr = list.iterator();
      Iterator otherItr = other.iterator();
      for (int i = 0; i < list.size(); i++) {
        if (!visit(listItr.next(), otherItr.next())) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  protected Boolean visit(Ast obj, Object param) {
    if (obj.equals(param)) {
      return true;
    }
    return super.visit(obj, param);
  }

  @Override
  protected Boolean visitVoidType(VoidType obj, Object param) {
    return param instanceof VoidType;
  }

  @Override
  protected Boolean visitBooleanType(BooleanType obj, Object param) {
    return param instanceof BooleanType;
  }

  @Override
  protected Boolean visitIntegerType(IntegerType obj, Object param) {
    return param instanceof IntegerType;
  }

  @Override
  protected Boolean visitNaturalType(NaturalType obj, Object param) {
    return param instanceof NaturalType;
  }

  @Override
  protected Boolean visitStringType(StringType obj, Object param) {
    return param instanceof StringType;
  }

  @Override
  protected Boolean visitSimpleRef(SimpleRef obj, Object param) {
    if (param instanceof SimpleRef) {
      SimpleRef other = (SimpleRef) param;
      return obj.link == other.link;
    } else {
      return false;
    }
  }

  @Override
  protected Boolean visitArrayType(ArrayType obj, Object param) {
    if (param instanceof ArrayType) {
      ArrayType other = (ArrayType) param;
      return obj.size.equals(other.size) && visit(obj.type, other.type);
    } else {
      return false;
    }
  }

  @Override
  protected Boolean visitRangeType(RangeType obj, Object param) {
    if (param instanceof RangeType) {
      RangeType other = (RangeType) param;
      return obj.range.equals(other.range);
    } else {
      return false;
    }
  }

  @Override
  protected Boolean visitRecordType(RecordType obj, Object param) {
    if (param instanceof RecordType) {
      RecordType other = (RecordType) param;
      return visitList(obj.element, other.element);
    } else {
      return false;
    }
  }

  @Override
  protected Boolean visitEnumType(EnumType obj, Object param) {
    // TODO compare strings
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Boolean visitTypeType(TypeType obj, Object param) {
    if (param instanceof TypeType) {
      TypeType other = (TypeType) param;
      return obj.type.equals(other.type);
    } else {
      return false;
    }
  }

}
