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

import pass.EvlPass;
import evl.copy.Copy;
import evl.data.Namespace;
import evl.data.expression.Expression;
import evl.data.expression.reference.Reference;
import evl.data.type.Type;
import evl.data.type.base.ArrayType;
import evl.data.type.base.EnumType;
import evl.data.type.base.RangeType;
import evl.data.type.base.StringType;
import evl.data.type.composed.RecordType;
import evl.data.variable.Constant;
import evl.knowledge.KnowledgeBase;
import evl.traverser.other.ExprReplacer;

/**
 * Propagates (some) constant values where they are used
 *
 */
public class ConstantPropagation extends EvlPass {
  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    ConstantPropagationWorker worker = new ConstantPropagationWorker();
    worker.traverse(evl, null);
  }

}

class ConstantPropagationWorker extends ExprReplacer<Void> {

  private boolean doReduce(Type type) {
    if (type instanceof RangeType) {
      return true;
    } else if (type instanceof EnumType) {
      return true;
    } else if (type instanceof ArrayType) {
      return false;
    } else if (type instanceof StringType) {
      return false;
    } else if (type instanceof RecordType) {
      return true;    // Because of C
    }
    throw new RuntimeException("not yet implemented:" + type.getClass().getCanonicalName());
  }

  @Override
  protected Expression visitReference(Reference obj, Void param) {
    if (obj.link instanceof Constant) {
      Constant constant = (Constant) obj.link;
      Type type = constant.type.link;
      if (doReduce(type)) {
        assert (obj.offset.isEmpty());
        return Copy.copy(visit(constant.def, null));
      }
    }
    return super.visitReference(obj, param);
  }

}
