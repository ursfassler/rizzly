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

package evl.pass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pass.EvlPass;

import common.ElementInfo;

import evl.DefTraverser;
import evl.expression.Expression;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.expression.reference.SimpleRef;
import evl.function.Function;
import evl.function.ret.FuncReturnTuple;
import evl.function.ret.FuncReturnType;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowUniqueName;
import evl.knowledge.KnowledgeBase;
import evl.other.EvlList;
import evl.other.Namespace;
import evl.statement.AssignmentSingle;
import evl.statement.ReturnExpr;
import evl.statement.ReturnVoid;
import evl.statement.Statement;
import evl.statement.VarDefStmt;
import evl.traverser.ExprReplacer;
import evl.traverser.StmtReplacer;
import evl.type.Type;
import evl.type.composed.NamedElement;
import evl.type.composed.RecordType;
import evl.variable.FuncVariable;

/**
 * Replaces function FuncReturnTuple with introduced record and FuncReturnType
 *
 * @author urs
 *
 */
public class RetStructIntroducer extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    RetStructIntroducerWorker worker = new RetStructIntroducerWorker(kb);
    worker.traverse(evl, null);

    kb.clear();
  }

}

class RetStructIntroducerWorker extends DefTraverser<Void, Void> {
  final private KnowBaseItem kbi;
  final private KnowUniqueName kun;

  public RetStructIntroducerWorker(KnowledgeBase kb) {
    super();
    kbi = kb.getEntry(KnowBaseItem.class);
    kun = kb.getEntry(KnowUniqueName.class);
  }

  @Override
  protected Void visitFunction(Function func, Void param) {
    if (!(func.getRet() instanceof FuncReturnTuple)) {
      return null;
    }

    ElementInfo info = ElementInfo.NO;

    Map<FuncVariable, NamedElement> varMap = new HashMap<FuncVariable, NamedElement>();
    RecordType type = makeRecord(((FuncReturnTuple) func.getRet()), varMap);

    FuncVariable retVar = new FuncVariable(func.getRet().getInfo(), kun.get("ret"), new SimpleRef<Type>(func.getRet().getInfo(), type));
    func.setRet(new FuncReturnType(func.getRet().getInfo(), new SimpleRef<Type>(info, type)));
    func.getBody().getStatements().add(0, new VarDefStmt(info, retVar));

    VarReplacer varRepl = new VarReplacer(retVar, varMap);
    varRepl.traverse(func, null);

    RetReplacer retRepl = new RetReplacer(retVar);
    retRepl.traverse(func, null);

    return null;
  }

  private RecordType makeRecord(FuncReturnTuple furet, Map<FuncVariable, NamedElement> varMap) {
    EvlList<NamedElement> element = new EvlList<NamedElement>();
    for (FuncVariable var : furet.getParam()) {
      NamedElement elem = new NamedElement(var.getInfo(), var.getName(), var.getType());
      element.add(elem);
    }
    RecordType type = kbi.getRecord(element);
    for (int i = 0; i < type.getSize(); i++) {
      FuncVariable var = furet.getParam().get(i);
      NamedElement elem = type.getElement().get(i);
      assert (var.getName().equals(elem.getName()));
      varMap.put(var, elem);
    }
    return type;
  }
}

/**
 * replace reference to return values
 *
 * @author urs
 */
class VarReplacer extends ExprReplacer<Void> {
  final private FuncVariable retVar;
  final private Map<FuncVariable, NamedElement> varMap;

  public VarReplacer(FuncVariable retVar, Map<FuncVariable, NamedElement> varMap) {
    super();
    this.retVar = retVar;
    this.varMap = varMap;
  }

  @Override
  protected Expression visitSimpleRef(SimpleRef obj, Void param) {
    super.visitSimpleRef(obj, param);
    NamedElement elem = varMap.get(obj.getLink());
    if (elem == null) {
      return obj;
    } else {
      return new Reference(obj.getInfo(), retVar, new RefName(obj.getInfo(), elem.getName()));
    }
  }

  @Override
  protected Expression visitReference(Reference obj, Void param) {
    super.visitReference(obj, param);
    NamedElement elem = varMap.get(obj.getLink());
    if (elem != null) {
      obj.setLink(retVar);
      obj.getOffset().add(0, new RefName(obj.getInfo(), elem.getName()));
    }
    return obj;
  }

}

/**
 * replace return statement
 *
 * @author urs
 */
class RetReplacer extends StmtReplacer<Void> {
  final private FuncVariable retVar;

  public RetReplacer(FuncVariable retVar) {
    super();
    this.retVar = retVar;
  }

  @Override
  protected List<Statement> visitReturnExpr(ReturnExpr obj, Void param) {
    List<Statement> ret = new ArrayList<Statement>();
    ret.add(new AssignmentSingle(obj.getExpr().getInfo(), new Reference(obj.getInfo(), retVar), obj.getExpr()));
    ret.add(new ReturnExpr(obj.getInfo(), new SimpleRef<FuncVariable>(obj.getInfo(), retVar)));
    return ret;
  }

  @Override
  protected List<Statement> visitReturnVoid(ReturnVoid obj, Void param) {
    return list(new ReturnExpr(obj.getInfo(), new SimpleRef<FuncVariable>(obj.getInfo(), retVar)));
  }

}