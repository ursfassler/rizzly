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

package ast.pass.reduction;

import java.math.BigInteger;
import java.util.List;

import main.Configuration;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.expression.ReferenceExpression;
import ast.data.expression.binop.Less;
import ast.data.expression.binop.Plus;
import ast.data.expression.value.BooleanValue;
import ast.data.expression.value.NumberValue;
import ast.data.reference.RefFactory;
import ast.data.statement.AssignmentSingle;
import ast.data.statement.Block;
import ast.data.statement.ForStmt;
import ast.data.statement.IfOption;
import ast.data.statement.IfStatement;
import ast.data.statement.Statement;
import ast.data.statement.VarDefStmt;
import ast.data.statement.WhileStmt;
import ast.data.type.TypeRefFactory;
import ast.data.type.base.RangeType;
import ast.data.variable.FunctionVariable;
import ast.dispatcher.other.StmtReplacer;
import ast.knowledge.KnowType;
import ast.knowledge.KnowUniqueName;
import ast.knowledge.KnowledgeBase;
import ast.meta.MetaList;
import ast.pass.AstPass;
import ast.repository.manipulator.TypeRepo;

/**
 * Replaces for loops with while loops
 *
 * @author urs
 *
 */
public class ForReduction extends AstPass {
  public ForReduction(Configuration configuration) {
    super(configuration);
  }

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    ForReductionWorker worker = new ForReductionWorker(kb);
    worker.traverse(ast, null);
  }

}

class ForReductionWorker extends StmtReplacer<Void> {
  final private TypeRepo kbi;
  final private KnowUniqueName kun;
  final private KnowType kt;

  public ForReductionWorker(KnowledgeBase kb) {
    super();
    kbi = new TypeRepo(kb);
    this.kun = kb.getEntry(KnowUniqueName.class);
    this.kt = kb.getEntry(KnowType.class);
  }

  @Override
  protected List<Statement> visitForStmt(ForStmt obj, Void param) {
    // TODO implement for other types than range types
    MetaList info = obj.metadata(); // TODO use this info for everything

    FunctionVariable itr = obj.iterator;
    RangeType rt = (RangeType) kt.get(itr.type);

    Block block = new Block();

    FunctionVariable loopCond = new FunctionVariable(kun.get("run"), TypeRefFactory.create(kbi.getBooleanType()));

    block.statements.add(new VarDefStmt(loopCond));
    block.statements.add(new AssignmentSingle(RefFactory.oldFull(loopCond), new BooleanValue(true)));

    block.statements.add(new VarDefStmt(itr));
    block.statements.add(new AssignmentSingle(RefFactory.oldFull(itr), new NumberValue(rt.range.low)));

    Block body = new Block();
    block.statements.add(new WhileStmt(new ReferenceExpression(RefFactory.oldFull(loopCond)), body));

    body.statements.add(obj.block);
    AstList<IfOption> option = new AstList<IfOption>();
    Block defblock = new Block();

    Block inc = new Block();
    option.add(new IfOption(new Less(new ReferenceExpression(RefFactory.oldFull(itr)), new NumberValue(rt.range.high)), inc));
    inc.statements.add(new AssignmentSingle(RefFactory.oldFull(itr), new Plus(new ReferenceExpression(RefFactory.oldFull(itr)), new NumberValue(BigInteger.ONE))));
    defblock.statements.add(new AssignmentSingle(RefFactory.oldFull(loopCond), new BooleanValue(false)));

    body.statements.add(new IfStatement(option, defblock));

    return list(block);
  }
}
