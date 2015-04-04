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

package evl.pass.check;

import pass.EvlPass;
import evl.data.Namespace;
import evl.data.component.composition.ImplComposition;
import evl.data.component.elementary.ImplElementary;
import evl.data.component.hfsm.ImplHfsm;
import evl.data.expression.Expression;
import evl.data.function.Function;
import evl.data.variable.Variable;
import evl.knowledge.KnowledgeBase;
import evl.pass.check.type.specific.HfsmModelChecker;
import evl.traverser.DefTraverser;

public class ModelChecker extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    ModelCheckerWorker adder = new ModelCheckerWorker(kb);
    adder.traverse(evl, null);
  }

}

class ModelCheckerWorker extends DefTraverser<Void, Void> {
  private KnowledgeBase kb;

  public ModelCheckerWorker(KnowledgeBase kb) {
    super();
    this.kb = kb;
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
  protected Void visitFunction(Function obj, Void sym) {
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
