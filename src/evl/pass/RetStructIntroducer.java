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

import evl.data.EvlList;
import evl.data.Namespace;
import evl.data.expression.Expression;
import evl.data.expression.reference.RefName;
import evl.data.expression.reference.Reference;
import evl.data.expression.reference.SimpleRef;
import evl.data.function.Function;
import evl.data.function.ret.FuncReturnTuple;
import evl.data.function.ret.FuncReturnType;
import evl.data.statement.AssignmentSingle;
import evl.data.statement.ReturnExpr;
import evl.data.statement.ReturnVoid;
import evl.data.statement.Statement;
import evl.data.statement.VarDefStmt;
import evl.data.type.Type;
import evl.data.type.composed.NamedElement;
import evl.data.type.composed.RecordType;
import evl.data.variable.FuncVariable;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowUniqueName;
import evl.knowledge.KnowledgeBase;
import evl.traverser.DefTraverser;
import evl.traverser.other.ExprReplacer;
import evl.traverser.other.StmtReplacer;

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
    if (!(func.ret instanceof FuncReturnTuple)) {
      return null;
    }

    ElementInfo info = ElementInfo.NO;

    Map<FuncVariable, NamedElement> varMap = new HashMap<FuncVariable, NamedElement>();
    RecordType type = makeRecord(((FuncReturnTuple) func.ret), varMap);

    FuncVariable retVar = new FuncVariable(func.ret.getInfo(), kun.get("ret"), new SimpleRef<Type>(func.ret.getInfo(), type));
    func.ret = new FuncReturnType(func.ret.getInfo(), new SimpleRef<Type>(info, type));
    func.body.statements.add(0, new VarDefStmt(info, retVar));

    VarReplacer varRepl = new VarReplacer(retVar, varMap);
    varRepl.traverse(func, null);

    RetReplacer retRepl = new RetReplacer(retVar);
    retRepl.traverse(func, null);

    return null;
  }

  private RecordType makeRecord(FuncReturnTuple furet, Map<FuncVariable, NamedElement> varMap) {
    EvlList<NamedElement> element = new EvlList<NamedElement>();
    for (FuncVariable var : furet.param) {
      NamedElement elem = new NamedElement(var.getInfo(), var.name, var.type);
      element.add(elem);
    }
    RecordType type = kbi.getRecord(element);
    for (int i = 0; i < type.getSize(); i++) {
      FuncVariable var = furet.param.get(i);
      NamedElement elem = type.element.get(i);
      assert (var.name.equals(elem.name));
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
    NamedElement elem = varMap.get(obj.link);
    if (elem == null) {
      return obj;
    } else {
      return new Reference(obj.getInfo(), retVar, new RefName(obj.getInfo(), elem.name));
    }
  }

  @Override
  protected Expression visitReference(Reference obj, Void param) {
    super.visitReference(obj, param);
    NamedElement elem = varMap.get(obj.link);
    if (elem != null) {
      obj.link = retVar;
      obj.offset.add(0, new RefName(obj.getInfo(), elem.name));
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
    ret.add(new AssignmentSingle(obj.expr.getInfo(), new Reference(obj.getInfo(), retVar), obj.expr));
    ret.add(new ReturnExpr(obj.getInfo(), new SimpleRef<FuncVariable>(obj.getInfo(), retVar)));
    return ret;
  }

  @Override
  protected List<Statement> visitReturnVoid(ReturnVoid obj, Void param) {
    return list(new ReturnExpr(obj.getInfo(), new SimpleRef<FuncVariable>(obj.getInfo(), retVar)));
  }

}
