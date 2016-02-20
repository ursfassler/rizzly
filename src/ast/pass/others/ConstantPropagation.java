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

package ast.pass.others;

import main.Configuration;
import ast.copy.Copy;
import ast.data.Namespace;
import ast.data.expression.Expression;
import ast.data.expression.ReferenceExpression;
import ast.data.reference.LinkedAnchor;
import ast.data.reference.ReferenceOffset;
import ast.data.type.Type;
import ast.data.type.base.ArrayType;
import ast.data.type.base.EnumType;
import ast.data.type.base.RangeType;
import ast.data.type.base.StringType;
import ast.data.type.composed.RecordType;
import ast.data.variable.Constant;
import ast.dispatcher.other.ExprReplacer;
import ast.knowledge.KnowType;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;

/**
 * Propagates (some) constant values where they are used
 *
 */
public class ConstantPropagation extends AstPass {
  public ConstantPropagation(Configuration configuration) {
    super(configuration);
  }

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    ConstantPropagationWorker worker = new ConstantPropagationWorker(kb);
    worker.traverse(ast, null);
  }

}

class ConstantPropagationWorker extends ExprReplacer<Void> {
  final private KnowType kt;

  public ConstantPropagationWorker(KnowledgeBase kb) {
    super();
    this.kt = kb.getEntry(KnowType.class);
  }

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
      return true; // Because of C
    }
    throw new RuntimeException("not yet implemented:" + type.getClass().getCanonicalName());
  }

  @Override
  protected Expression visitRefExpr(ReferenceExpression obj, Void param) {
    LinkedAnchor anchor = (LinkedAnchor) obj.reference.getAnchor();
    if (anchor.getLink() instanceof Constant) {
      ReferenceOffset ref = (ReferenceOffset) obj.reference;
      Constant constant = (Constant) anchor.getLink();
      Type type = kt.get(constant.type);
      if (doReduce(type)) {
        assert (ref.getOffset().isEmpty());
        return Copy.copy(visit(constant.def, null));
      }
    }
    return super.visitRefExpr(obj, param);
  }

}
