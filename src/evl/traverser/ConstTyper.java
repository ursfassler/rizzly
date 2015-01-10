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

import operation.EvlOperation;
import evl.DefTraverser;
import evl.Evl;
import evl.expression.reference.SimpleRef;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowType;
import evl.knowledge.KnowledgeBase;
import evl.type.Type;
import evl.type.special.AnyType;
import evl.type.special.IntegerType;
import evl.type.special.NaturalType;
import evl.variable.Constant;

/**
 * Sets and constrains types for all constants
 *
 */
public class ConstTyper extends EvlPass {
  @Override
  public void process(Evl evl, KnowledgeBase kb) {
    Typer replace = new Typer(kb);
    replace.traverse(evl, null);
  }
}

class Typer extends DefTraverser<Void, Void> {
  private final KnowType kt;
  private final KnowBaseItem kbi;

  public Typer(KnowledgeBase kb) {
    super();
    this.kt = kb.getEntry(KnowType.class);
    this.kbi = kb.getEntry(KnowBaseItem.class);
  }

  public static void process(Evl evl, KnowledgeBase kb) {
  }

  private static boolean isOpenType(Type type) {
    return ((type instanceof AnyType) || (type instanceof NaturalType) || (type instanceof IntegerType));
  }

  @Override
  protected Void visitConstant(Constant obj, Void param) {
    if (isOpenType(obj.getType().getLink())) {
      Type ct = kt.get(obj.getDef());

      ct = kbi.getType(ct);

      obj.setType(new SimpleRef<Type>(obj.getInfo(), ct));
    }
    super.visitConstant(obj, param);
    return null;
  }

}
