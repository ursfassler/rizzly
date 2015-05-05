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

import ast.data.Ast;
import ast.data.Namespace;
import ast.data.expression.reference.SimpleRef;
import ast.data.type.Type;
import ast.data.type.special.AnyType;
import ast.data.type.special.IntegerType;
import ast.data.type.special.NaturalType;
import ast.data.variable.Constant;
import ast.knowledge.KnowType;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.repository.manipulator.TypeRepo;
import ast.traverser.DefTraverser;

/**
 * Sets and constrains types for all constants
 *
 */
public class ConstTyper extends AstPass {
  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    Typer replace = new Typer(kb);
    replace.traverse(ast, null);
  }
}

class Typer extends DefTraverser<Void, Void> {
  private final KnowType kt;
  private final TypeRepo kbi;

  public Typer(KnowledgeBase kb) {
    super();
    this.kt = kb.getEntry(KnowType.class);
    kbi = new TypeRepo(kb);
  }

  public static void process(Ast ast, KnowledgeBase kb) {
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
