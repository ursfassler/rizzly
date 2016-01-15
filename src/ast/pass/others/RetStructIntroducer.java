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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.Configuration;
import ast.ElementInfo;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.expression.RefExp;
import ast.data.function.Function;
import ast.data.function.ret.FuncReturnTuple;
import ast.data.function.ret.FuncReturnType;
import ast.data.reference.RefFactory;
import ast.data.reference.RefName;
import ast.data.reference.Reference;
import ast.data.statement.AssignmentSingle;
import ast.data.statement.ReturnExpr;
import ast.data.statement.ReturnVoid;
import ast.data.statement.Statement;
import ast.data.statement.VarDefStmt;
import ast.data.type.TypeRefFactory;
import ast.data.type.composed.NamedElement;
import ast.data.type.composed.RecordType;
import ast.data.variable.FuncVariable;
import ast.dispatcher.DfsTraverser;
import ast.dispatcher.other.RefReplacer;
import ast.dispatcher.other.StmtReplacer;
import ast.knowledge.KnowType;
import ast.knowledge.KnowUniqueName;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.repository.manipulator.TypeRepo;

/**
 * Replaces function FuncReturnTuple with introduced record and FuncReturnType
 *
 * @author urs
 *
 */
public class RetStructIntroducer extends AstPass {
  public RetStructIntroducer(Configuration configuration) {
    super(configuration);
  }

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    RetStructIntroducerWorker worker = new RetStructIntroducerWorker(kb);
    worker.traverse(ast, null);

    kb.clear();
  }

}

class RetStructIntroducerWorker extends DfsTraverser<Void, Void> {
  final private TypeRepo kbi;
  final private KnowUniqueName kun;
  final private KnowType kt;

  public RetStructIntroducerWorker(KnowledgeBase kb) {
    super();
    kbi = new TypeRepo(kb);
    kun = kb.getEntry(KnowUniqueName.class);
    kt = kb.getEntry(KnowType.class);
  }

  @Override
  protected Void visitFunction(Function func, Void param) {
    if (!(func.ret instanceof FuncReturnTuple)) {
      return null;
    }

    ElementInfo info = ElementInfo.NO;

    Map<FuncVariable, NamedElement> varMap = new HashMap<FuncVariable, NamedElement>();
    RecordType type = makeRecord(((FuncReturnTuple) func.ret), varMap);

    FuncVariable retVar = new FuncVariable(func.ret.getInfo(), kun.get("ret"), TypeRefFactory.create(func.ret.getInfo(), type));
    func.ret = new FuncReturnType(func.ret.getInfo(), TypeRefFactory.create(info, type));
    func.body.statements.add(0, new VarDefStmt(info, retVar));

    VarReplacer varRepl = new VarReplacer(retVar, varMap);
    varRepl.traverse(func, null);

    RetReplacer retRepl = new RetReplacer(retVar);
    retRepl.traverse(func, null);

    return null;
  }

  private RecordType makeRecord(FuncReturnTuple furet, Map<FuncVariable, NamedElement> varMap) {
    AstList<NamedElement> element = new AstList<NamedElement>();
    for (FuncVariable var : furet.param) {
      NamedElement elem = new NamedElement(var.getInfo(), var.name, var.type);
      element.add(elem);
    }
    RecordType type = kbi.getRecord(element);
    for (int i = 0; i < type.element.size(); i++) {
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
class VarReplacer extends RefReplacer<Void> {
  final private FuncVariable retVar;
  final private Map<FuncVariable, NamedElement> varMap;

  public VarReplacer(FuncVariable retVar, Map<FuncVariable, NamedElement> varMap) {
    super();
    this.retVar = retVar;
    this.varMap = varMap;
  }

  @Override
  protected Reference visitReference(Reference obj, Void param) {
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
    ret.add(new AssignmentSingle(obj.expr.getInfo(), RefFactory.full(obj.getInfo(), retVar), obj.expr));
    ret.add(makeRet(obj.getInfo()));
    return ret;
  }

  @Override
  protected List<Statement> visitReturnVoid(ReturnVoid obj, Void param) {
    return list(makeRet(obj.getInfo()));
  }

  private ReturnExpr makeRet(ElementInfo info) {
    return new ReturnExpr(info, new RefExp(info, RefFactory.create(info, retVar)));
  }

}
