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

package evl.traverser;

import util.Range;

import common.ElementInfo;

import evl.Evl;
import evl.expression.Expression;
import evl.expression.TypeCast;
import evl.expression.binop.ArithmeticOp;
import evl.expression.binop.Relation;
import evl.expression.reference.SimpleRef;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowType;
import evl.knowledge.KnowledgeBase;
import evl.type.Type;
import evl.type.base.RangeType;

/**
 * Insert typecasts in expressions to fit the destination type
 *
 * @author urs
 *
 */
public class RangeConverter extends ExprReplacer<Void> {
  private static final ElementInfo info = ElementInfo.NO;
  private KnowBaseItem kbi;
  private KnowType kt;

  public RangeConverter(KnowledgeBase kb) {
    super();
    this.kbi = kb.getEntry(KnowBaseItem.class);
    this.kt = kb.getEntry(KnowType.class);
  }

  public static void process(Evl obj, KnowledgeBase kb) {
    RangeConverter changer = new RangeConverter(kb);
    changer.traverse(obj, null);
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
    if (Range.leftIsSmallerEqual(valType.getNumbers(), commonType.getNumbers())) {
      val = new TypeCast(info, new SimpleRef<Type>(info, commonType), val);
    }
    return val;
  }

  @Override
  protected Expression visitArithmeticOp(ArithmeticOp obj, Void param) {
    obj = (ArithmeticOp) super.visitArithmeticOp(obj, param);

    Type lb = kt.get(obj.getLeft());
    Type rb = kt.get(obj.getRight());

    if (isNotRange(lb, rb)) {
      return obj;
    }

    RangeType lt = (RangeType) lb;
    RangeType rt = (RangeType) rb;
    RangeType dt = (RangeType) kt.get(obj);

    Range it = Range.grow(lt.getNumbers(), rt.getNumbers());
    Range btr = Range.grow(it, dt.getNumbers());
    RangeType bt = kbi.getNumsetType(btr); // add bt to program

    obj.setLeft(replaceIfNeeded(obj.getLeft(), lt, bt));
    obj.setRight(replaceIfNeeded(obj.getRight(), rt, bt));

    // TODO reimplement downcast, but with different function than conversion
    // if (RangeType.isBigger(bt, dt)) {
    // SsaVariable irv = new SsaVariable(NameFactory.getNew(), new TypeRef(bt));
    // TypeCast rex = new TypeCast(obj.getVariable(), new VarRefSimple(irv));
    // obj.setVariable(irv);
    // ret.add(rex);
    // }
    return obj;
  }

  @Override
  protected Expression visitRelation(Relation obj, Void param) {
    obj = (Relation) super.visitRelation(obj, param);

    Type lb = kt.get(obj.getLeft());
    Type rb = kt.get(obj.getRight());

    if (isNotRange(lb, rb)) {
      return obj;
    }

    RangeType lt = (RangeType) lb;
    RangeType rt = (RangeType) rb;

    Range it = Range.grow(lt.getNumbers(), rt.getNumbers());
    RangeType bt = kbi.getNumsetType(it); // add bt to program

    obj.setLeft(replaceIfNeeded(obj.getLeft(), lt, bt));
    obj.setRight(replaceIfNeeded(obj.getRight(), rt, bt));

    return obj;
  }
}
