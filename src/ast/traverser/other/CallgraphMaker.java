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

package ast.traverser.other;

import java.util.HashSet;
import java.util.Set;

import ast.data.Ast;
import ast.data.component.hfsm.Transition;
import ast.data.expression.reference.BaseRef;
import ast.data.expression.reference.RefCall;
import ast.data.expression.reference.RefIndex;
import ast.data.expression.reference.RefItem;
import ast.data.expression.reference.RefName;
import ast.data.expression.reference.Reference;
import ast.data.function.Function;
import ast.data.type.Type;
import ast.data.type.base.ArrayType;
import ast.data.variable.Variable;
import ast.doc.SimpleGraph;
import ast.knowledge.KnowChild;
import ast.knowledge.KnowType;
import ast.knowledge.KnowledgeBase;
import ast.traverser.DefTraverser;
import ast.traverser.NullTraverser;

/**
 * Returns a callgraph of the entire (sub-) tree
 *
 * @author urs
 *
 */
public class CallgraphMaker extends DefTraverser<Void, Ast> {
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
  protected Void visitBaseRef(BaseRef obj, Ast param) {
    super.visitBaseRef(obj, param);

    if (param != null) {
      Ast head = obj.link;
      if (head instanceof Function) {
        callgraph.addVertex(head);
        callgraph.addEdge(param, head);
      }
    }
    return null;
  }

  @Override
  protected Void visitReference(Reference obj, Ast param) {
    super.visitReference(obj, param);

    if (param != null) {
      Set<Function> target = new HashSet<Function>();

      Ast item = obj.link;
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

class RefGetter extends NullTraverser<Ast, Ast> {
  private Set<Function> target;
  private KnowChild kfc;
  final private KnowType kt;

  static public Ast process(RefItem refitm, Ast last, Set<Function> target, KnowledgeBase kb) {
    RefGetter refChecker = new RefGetter(kb, target);
    return refChecker.traverse(refitm, last);
  }

  public RefGetter(KnowledgeBase kb, Set<Function> target) {
    super();
    this.target = target;
    this.kfc = kb.getEntry(KnowChild.class);
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
    return kfc.get(param, obj.name, obj.getInfo());
  }

  @Override
  protected Ast visitRefIndex(RefIndex obj, Ast param) {
    Variable var = (Variable) param;
    Type type = kt.get(var.type);
    ArrayType arrayType = (ArrayType) type;
    return arrayType.type;
  }

}
