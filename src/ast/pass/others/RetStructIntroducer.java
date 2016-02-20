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
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.expression.ReferenceExpression;
import ast.data.function.Function;
import ast.data.function.ret.FuncReturnTuple;
import ast.data.function.ret.FunctionReturnType;
import ast.data.reference.LinkedAnchor;
import ast.data.reference.OffsetReference;
import ast.data.reference.RefFactory;
import ast.data.reference.RefName;
import ast.data.reference.Reference;
import ast.data.statement.AssignmentSingle;
import ast.data.statement.ExpressionReturn;
import ast.data.statement.Statement;
import ast.data.statement.VarDefStmt;
import ast.data.statement.VoidReturn;
import ast.data.type.TypeRefFactory;
import ast.data.type.composed.NamedElement;
import ast.data.type.composed.RecordType;
import ast.data.variable.FunctionVariable;
import ast.dispatcher.DfsTraverser;
import ast.dispatcher.other.RefReplacer;
import ast.dispatcher.other.StmtReplacer;
import ast.knowledge.KnowType;
import ast.knowledge.KnowUniqueName;
import ast.knowledge.KnowledgeBase;
import ast.meta.MetaList;
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

    Map<FunctionVariable, NamedElement> varMap = new HashMap<FunctionVariable, NamedElement>();
    RecordType type = makeRecord(((FuncReturnTuple) func.ret), varMap);

    FunctionVariable retVar = new FunctionVariable(kun.get("ret"), TypeRefFactory.create(func.ret.metadata(), type));
    retVar.metadata().add(func.ret.metadata());
    func.ret = new FunctionReturnType(func.ret.metadata(), TypeRefFactory.create(type));
    func.body.statements.add(0, new VarDefStmt(retVar));

    VarReplacer varRepl = new VarReplacer(retVar, varMap);
    varRepl.traverse(func, null);

    RetReplacer retRepl = new RetReplacer(retVar);
    retRepl.traverse(func, null);

    return null;
  }

  private RecordType makeRecord(FuncReturnTuple furet, Map<FunctionVariable, NamedElement> varMap) {
    AstList<NamedElement> element = new AstList<NamedElement>();
    for (FunctionVariable var : furet.param) {
      NamedElement elem = new NamedElement(var.metadata(), var.getName(), var.type);
      element.add(elem);
    }
    RecordType type = kbi.getRecord(element);
    for (int i = 0; i < type.element.size(); i++) {
      FunctionVariable var = furet.param.get(i);
      NamedElement elem = type.element.get(i);
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
class VarReplacer extends RefReplacer<Void> {
  final private FunctionVariable retVar;
  final private Map<FunctionVariable, NamedElement> varMap;

  public VarReplacer(FunctionVariable retVar, Map<FunctionVariable, NamedElement> varMap) {
    super();
    this.retVar = retVar;
    this.varMap = varMap;
  }

  @Override
  protected Reference visitOffsetReference(OffsetReference obj, Void param) {
    super.visitOffsetReference(obj, param);
    LinkedAnchor anchor = (LinkedAnchor) obj.getAnchor();
    NamedElement elem = varMap.get(anchor.getLink());
    if (elem != null) {
      anchor.setLink(retVar);
      obj.getOffset().add(0, new RefName(obj.metadata(), elem.getName()));
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
  final private FunctionVariable retVar;

  public RetReplacer(FunctionVariable retVar) {
    super();
    this.retVar = retVar;
  }

  @Override
  protected List<Statement> visitReturnExpr(ExpressionReturn obj, Void param) {
    List<Statement> ret = new ArrayList<Statement>();
    Reference ref = RefFactory.withOffset(retVar);
    ref.metadata().add(obj.metadata());
    AssignmentSingle assignment = new AssignmentSingle(ref, obj.expression);
    assignment.metadata().add(obj.expression.metadata());
    ret.add(assignment);
    ret.add(makeRet(obj.metadata()));
    return ret;
  }

  @Override
  protected List<Statement> visitReturnVoid(VoidReturn obj, Void param) {
    return list(makeRet(obj.metadata()));
  }

  private ExpressionReturn makeRet(MetaList info) {
    ReferenceExpression expr = new ReferenceExpression(RefFactory.create(info, retVar));
    expr.metadata().add(info);
    ExpressionReturn ret = new ExpressionReturn(expr);
    ret.metadata().add(info);
    return ret;
  }

}
