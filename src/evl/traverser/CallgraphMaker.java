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

import java.util.HashSet;
import java.util.Set;

import util.SimpleGraph;
import evl.DefTraverser;
import evl.Evl;
import evl.NullTraverser;
import evl.expression.reference.BaseRef;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefIndex;
import evl.expression.reference.RefItem;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.function.Function;
import evl.hfsm.Transition;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowChild;
import evl.knowledge.KnowType;
import evl.knowledge.KnowledgeBase;
import evl.type.Type;
import evl.type.base.ArrayType;
import evl.variable.Variable;

/**
 * Returns a callgraph of the entire (sub-) tree
 *
 * @author urs
 *
 */
public class CallgraphMaker extends DefTraverser<Void, Evl> {
  private SimpleGraph<Evl> callgraph = new SimpleGraph<Evl>();
  private KnowledgeBase kb;

  public CallgraphMaker(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  public static SimpleGraph<Evl> make(Evl inst, KnowledgeBase kb) {
    CallgraphMaker reduction = new CallgraphMaker(kb);
    reduction.traverse(inst, null);
    return reduction.callgraph;
  }

  @Override
  protected Void visitFunction(Function obj, Evl param) {
    callgraph.addVertex(obj);
    return super.visitFunction(obj, obj);
  }

  @Override
  protected Void visitTransition(Transition obj, Evl param) {
    assert (param == null);

    callgraph.addVertex(obj.guard);
    visit(obj.guard, obj.guard);

    callgraph.addVertex(obj.body);
    visit(obj.body, obj.body);

    return null;
  }

  @Override
  protected Void visitBaseRef(BaseRef obj, Evl param) {
    super.visitBaseRef(obj, param);

    if (param != null) {
      Evl head = obj.link;
      if (head instanceof Function) {
        callgraph.addVertex(head);
        callgraph.addEdge(param, head);
      }
    }
    return null;
  }

  @Override
  protected Void visitReference(Reference obj, Evl param) {
    super.visitReference(obj, param);

    if (param != null) {
      Set<Function> target = new HashSet<Function>();

      Evl item = obj.link;
      for (RefItem itr : obj.offset) {
        item = RefGetter.process(itr, item, target, kb);
      }

      for (Function head : target) {
        callgraph.addVertex(head);
        callgraph.addEdge(param, head);
      }
    }
    return null;
  }

}

class RefGetter extends NullTraverser<Evl, Evl> {
  private Set<Function> target;
  private KnowChild kfc;
  private KnowBaseItem kbi;
  final private KnowType kt;

  static public Evl process(RefItem refitm, Evl last, Set<Function> target, KnowledgeBase kb) {
    RefGetter refChecker = new RefGetter(kb, target);
    return refChecker.traverse(refitm, last);
  }

  public RefGetter(KnowledgeBase kb, Set<Function> target) {
    super();
    this.target = target;
    this.kfc = kb.getEntry(KnowChild.class);
    kbi = kb.getEntry(KnowBaseItem.class);
    kt = kb.getEntry(KnowType.class);
  }

  @Override
  protected Evl visitDefault(Evl obj, Evl param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Evl visitRefCall(RefCall obj, Evl param) {
    if (param instanceof Type) {
      // convert function
      return param;
    }
    Function header = (Function) param;
    target.add(header);
    return kt.get(header.ret);
  }

  @Override
  protected Evl visitRefName(RefName obj, Evl param) {
    return kfc.get(param, obj.name, obj.getInfo());
  }

  @Override
  protected Evl visitRefIndex(RefIndex obj, Evl param) {
    Variable var = (Variable) param;
    Type type = var.type.link;
    ArrayType arrayType = (ArrayType) type;
    return arrayType.type.link;
  }

}
