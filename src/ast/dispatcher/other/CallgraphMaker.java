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

package ast.dispatcher.other;

import java.util.HashSet;
import java.util.Set;

import ast.data.Ast;
import ast.data.component.hfsm.Transition;
import ast.data.function.Function;
import ast.data.reference.RefCall;
import ast.data.reference.RefIndex;
import ast.data.reference.RefItem;
import ast.data.reference.RefName;
import ast.data.reference.LinkedReferenceWithOffset_Implementation;
import ast.data.type.Type;
import ast.data.type.TypeReference;
import ast.data.type.base.ArrayType;
import ast.data.type.composed.NamedElement;
import ast.data.variable.Variable;
import ast.dispatcher.DfsTraverser;
import ast.dispatcher.NullDispatcher;
import ast.doc.SimpleGraph;
import ast.knowledge.KnowType;
import ast.knowledge.KnowledgeBase;
import ast.repository.query.ChildByName;

/**
 * Returns a callgraph of the entire (sub-) tree
 *
 * @author urs
 *
 */
public class CallgraphMaker extends DfsTraverser<Void, Ast> {
  private SimpleGraph<Ast> callgraph = new SimpleGraph<Ast>();
  private KnowledgeBase kb;

  public CallgraphMaker(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  public static SimpleGraph<Ast> make(Ast inst, KnowledgeBase kb) {
    CallgraphMaker reduction = new CallgraphMaker(kb);
    reduction.traverse(inst, null);
    return reduction.callgraph;
  }

  @Override
  protected Void visitFunction(Function obj, Ast param) {
    callgraph.addVertex(obj);
    return super.visitFunction(obj, obj);
  }

  @Override
  protected Void visitTransition(Transition obj, Ast param) {
    assert (param == null);

    callgraph.addVertex(obj.guard);
    visit(obj.guard, obj.guard);

    callgraph.addVertex(obj.body);
    visit(obj.body, obj.body);

    return null;
  }

  @Override
  protected Void visitReference(LinkedReferenceWithOffset_Implementation obj, Ast param) {
    super.visitReference(obj, param);

    if (param != null) {
      Set<Function> target = new HashSet<Function>();

      Ast item = obj.getLink();
      for (RefItem itr : obj.getOffset()) {
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

// FIXME make it cleaner
@Deprecated
class RefGetter extends NullDispatcher<Ast, Ast> {
  private Set<Function> target;
  final private KnowType kt;

  static public Ast process(RefItem refitm, Ast last, Set<Function> target, KnowledgeBase kb) {
    RefGetter refChecker = new RefGetter(kb, target);
    return refChecker.traverse(refitm, last);
  }

  public RefGetter(KnowledgeBase kb, Set<Function> target) {
    super();
    this.target = target;
    kt = kb.getEntry(KnowType.class);
  }

  @Override
  protected Ast visitDefault(Ast obj, Ast param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Ast visitRefCall(RefCall obj, Ast param) {
    if (param instanceof Type) {
      // convert function
      return param;
    }
    Function header = (Function) param;
    target.add(header);
    return kt.get(header.ret);
  }

  @Override
  protected Ast visitRefName(RefName obj, Ast param) {

    // FIXME remove this hack
    if (param instanceof Variable) {
      param = (((Variable) param).type).ref.getTarget();
    } else if (param instanceof NamedElement) {
      param = (((NamedElement) param).typeref).ref.getTarget();
    } else if (param instanceof LinkedReferenceWithOffset_Implementation) {
      param = ((LinkedReferenceWithOffset_Implementation) param).getTarget();
    }
    if (param instanceof TypeReference) {
      param = ((TypeReference) param).ref.getTarget();
    }

    return ChildByName.get(param, obj.name, obj.metadata());
  }

  @Override
  protected Ast visitRefIndex(RefIndex obj, Ast param) {
    Variable var = (Variable) param;
    Type type = kt.get(var.type);
    ArrayType arrayType = (ArrayType) type;
    return arrayType.type;
  }
}
