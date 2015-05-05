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

import ast.ElementInfo;
import ast.data.Namespace;
import ast.data.Range;
import ast.data.expression.Expression;
import ast.data.expression.TypeCast;
import ast.data.expression.binop.ArithmeticOp;
import ast.data.expression.binop.Relation;
import ast.data.expression.reference.SimpleRef;
import ast.data.type.Type;
import ast.data.type.base.RangeType;
import ast.knowledge.KnowType;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.repository.manipulator.TypeRepo;
import ast.traverser.other.ExprReplacer;

/**
 * Insert typecasts in expressions to fit the destination type
 *
 * @author urs
 *
 */
public class RangeConverter extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    RangeConverterWorker changer = new RangeConverterWorker(kb);
    changer.traverse(ast, null);
  }

}

class RangeConverterWorker extends ExprReplacer<Void> {
  private static final ElementInfo info = ElementInfo.NO;
  private TypeRepo kbi;
  private KnowType kt;

  public RangeConverterWorker(KnowledgeBase kb) {
    super();
    kbi = new TypeRepo(kb);
    this.kt = kb.getEntry(KnowType.class);
  }

  private boolean isNotRange(Type t1, Type t2) {
    if (!(t1 instanceof RangeType)) {
      // TODO implement it nicer
      assert (t1 == t2);
      return true;
    } else {
      assert (t2 instanceof RangeType);
    }
    return false;
  }

  private Expression replaceIfNeeded(Expression val, RangeType valType, RangeType commonType) {
    if (Range.leftIsSmallerEqual(valType.range, commonType.range)) {
      val = new TypeCast(info, new SimpleRef<Type>(info, commonType), val);
    }
    return val;
  }

  @Override
  protected Expression visitArithmeticOp(ArithmeticOp obj, Void param) {
    obj = (ArithmeticOp) super.visitArithmeticOp(obj, param);

    Type lb = kt.get(obj.left);
    Type rb = kt.get(obj.right);

    if (isNotRange(lb, rb)) {
      return obj;
    }

    RangeType lt = (RangeType) lb;
    RangeType rt = (RangeType) rb;
    RangeType dt = (RangeType) kt.get(obj);

    Range it = Range.grow(lt.range, rt.range);
    Range btr = Range.grow(it, dt.range);
    RangeType bt = kbi.getRangeType(btr); // add bt to program

    obj.left = replaceIfNeeded(obj.left, lt, bt);
    obj.right = replaceIfNeeded(obj.right, rt, bt);

    // TODO reimplement downcast, but with different function than conversion
    // if (RangeType.isBigger(bt, dt)) {
    // SsaVariable irv = new SsaVariable(NameFactory.getNew(), new
    // SimpleRef<Type>(bt));
    // TypeCast rex = new TypeCast(obj.getVariable(), new VarRefSimple(irv));
    // obj.setVariable(irv);
    // ret.add(rex);
    // }
    return obj;
  }

  @Override
  protected Expression visitRelation(Relation obj, Void param) {
    obj = (Relation) super.visitRelation(obj, param);

    Type lb = kt.get(obj.left);
    Type rb = kt.get(obj.right);

    if (isNotRange(lb, rb)) {
      return obj;
    }

    RangeType lt = (RangeType) lb;
    RangeType rt = (RangeType) rb;

    Range it = Range.grow(lt.range, rt.range);
    RangeType bt = kbi.getRangeType(it); // add bt to program

    obj.left = replaceIfNeeded(obj.left, lt, bt);
    obj.right = replaceIfNeeded(obj.right, rt, bt);

    return obj;
  }
}
