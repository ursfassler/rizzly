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

import pass.EvlPass;
import evl.data.Evl;
import evl.data.Namespace;
import evl.data.expression.reference.SimpleRef;
import evl.data.type.Type;
import evl.data.type.special.AnyType;
import evl.data.type.special.IntegerType;
import evl.data.type.special.NaturalType;
import evl.data.variable.Constant;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowType;
import evl.knowledge.KnowledgeBase;
import evl.traverser.DefTraverser;

/**
 * Sets and constrains types for all constants
 *
 */
public class ConstTyper extends EvlPass {
  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
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
    if (isOpenType(kt.get(obj.type))) {
      Type ct = kt.get(obj.def);

      ct = kbi.getType(ct);

      obj.type = new SimpleRef<Type>(obj.getInfo(), ct);
    }
    super.visitConstant(obj, param);
    return null;
  }

}
