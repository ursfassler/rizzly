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

package evl.traverser.modelcheck;

import evl.DefTraverser;
import evl.Evl;
import evl.composition.ImplComposition;
import evl.expression.Expression;
import evl.function.Function;
import evl.hfsm.ImplHfsm;
import evl.knowledge.KnowledgeBase;
import evl.other.ImplElementary;
import evl.variable.Variable;

public class ModelChecker extends DefTraverser<Void, Void> {
  private KnowledgeBase kb;

  public ModelChecker(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  static public void process(Evl obj, KnowledgeBase kb) {
    ModelChecker adder = new ModelChecker(kb);
    adder.traverse(obj, null);
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Void sym) {
    return null;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, Void sym) {
    return null; // why not type checked (here)? > in CompInterfaceTypeChecker
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, Void param) {
    HfsmModelChecker.process(obj, kb);
    return null;
  }

  @Override
  protected Void visitFunctionImpl(Function obj, Void sym) {
    return null;
  }

  @Override
  protected Void visitVariable(Variable obj, Void sym) {
    return null;
  }

  @Override
  protected Void visitExpression(Expression obj, Void sym) {
    return null;
  }

}
