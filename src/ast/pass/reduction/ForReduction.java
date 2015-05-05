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

import ast.ElementInfo;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.expression.BoolValue;
import ast.data.expression.Number;
import ast.data.expression.binop.Less;
import ast.data.expression.binop.Plus;
import ast.data.expression.reference.Reference;
import ast.data.expression.reference.SimpleRef;
import ast.data.statement.AssignmentSingle;
import ast.data.statement.Block;
import ast.data.statement.ForStmt;
import ast.data.statement.IfOption;
import ast.data.statement.IfStmt;
import ast.data.statement.Statement;
import ast.data.statement.VarDefStmt;
import ast.data.statement.WhileStmt;
import ast.data.type.Type;
import ast.data.type.base.RangeType;
import ast.data.variable.FuncVariable;
import ast.knowledge.KnowType;
import ast.knowledge.KnowUniqueName;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.repository.manipulator.TypeRepo;
import ast.traverser.other.StmtReplacer;

/**
 * Replaces for loops with while loops
 *
 * @author urs
 *
 */
public class ForReduction extends AstPass {

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
    ElementInfo info = obj.getInfo();

    FuncVariable itr = obj.iterator;
    RangeType rt = (RangeType) kt.get(itr.type);

    Block block = new Block(obj.getInfo());

    FuncVariable loopCond = new FuncVariable(info, kun.get("run"), new SimpleRef<Type>(info, kbi.getBooleanType()));

    block.statements.add(new VarDefStmt(info, loopCond));
    block.statements.add(new AssignmentSingle(info, new Reference(info, loopCond), new BoolValue(info, true)));

    block.statements.add(new VarDefStmt(info, itr));
    block.statements.add(new AssignmentSingle(info, new Reference(info, itr), new Number(info, rt.range.low)));

    Block body = new Block(info);
    block.statements.add(new WhileStmt(info, new Reference(info, loopCond), body));

    body.statements.add(obj.block);
    AstList<IfOption> option = new AstList<IfOption>();
    Block defblock = new Block(info);

    Block inc = new Block(info);
    option.add(new IfOption(info, new Less(info, new Reference(info, itr), new Number(info, rt.range.high)), inc));
    inc.statements.add(new AssignmentSingle(info, new Reference(info, itr), new Plus(info, new Reference(info, itr), new Number(info, BigInteger.ONE))));
    defblock.statements.add(new AssignmentSingle(info, new Reference(info, loopCond), new BoolValue(info, false)));

    body.statements.add(new IfStmt(info, option, defblock));

    return list(block);
  }
}
