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

import java.math.BigInteger;
import java.util.List;

import pass.EvlPass;

import common.ElementInfo;

import evl.expression.BoolValue;
import evl.expression.Number;
import evl.expression.binop.Less;
import evl.expression.binop.Plus;
import evl.expression.reference.Reference;
import evl.expression.reference.SimpleRef;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowUniqueName;
import evl.knowledge.KnowledgeBase;
import evl.other.EvlList;
import evl.other.Namespace;
import evl.statement.AssignmentSingle;
import evl.statement.Block;
import evl.statement.ForStmt;
import evl.statement.IfOption;
import evl.statement.IfStmt;
import evl.statement.Statement;
import evl.statement.VarDefStmt;
import evl.statement.WhileStmt;
import evl.traverser.StmtReplacer;
import evl.type.Type;
import evl.type.base.RangeType;
import evl.variable.FuncVariable;

/**
 * Replaces for loops with while loops
 *
 * @author urs
 *
 */
public class ForReduction extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    ForReductionWorker worker = new ForReductionWorker(kb);
    worker.traverse(evl, null);
  }

}

class ForReductionWorker extends StmtReplacer<Void> {
  final private KnowBaseItem kbi;
  final private KnowUniqueName kun;

  public ForReductionWorker(KnowledgeBase kb) {
    super();
    this.kbi = kb.getEntry(KnowBaseItem.class);
    this.kun = kb.getEntry(KnowUniqueName.class);
  }

  @Override
  protected List<Statement> visitForStmt(ForStmt obj, Void param) {
    // TODO documentation
    // TODO implement for other types than range types
    ElementInfo info = obj.getInfo();

    FuncVariable itr = obj.getIterator();
    RangeType rt = (RangeType) itr.getType().getLink();

    Block block = new Block(obj.getInfo());

    FuncVariable loopCond = new FuncVariable(info, kun.get("run"), new SimpleRef<Type>(info, kbi.getBooleanType()));

    block.getStatements().add(new VarDefStmt(info, loopCond));
    block.getStatements().add(new AssignmentSingle(info, new Reference(info, loopCond), new BoolValue(info, true)));

    block.getStatements().add(new VarDefStmt(info, itr));
    block.getStatements().add(new AssignmentSingle(info, new Reference(info, itr), new Number(info, rt.getNumbers().getLow())));

    Block body = new Block(info);
    block.getStatements().add(new WhileStmt(info, new Reference(info, loopCond), body));

    body.getStatements().add(obj.getBlock());
    EvlList<IfOption> option = new EvlList<IfOption>();
    Block defblock = new Block(info);

    Block inc = new Block(info);
    option.add(new IfOption(info, new Less(info, new Reference(info, itr), new Number(info, rt.getNumbers().getHigh())), inc));
    inc.getStatements().add(new AssignmentSingle(info, new Reference(info, itr), new Plus(info, new Reference(info, itr), new Number(info, BigInteger.ONE))));
    defblock.getStatements().add(new AssignmentSingle(info, new Reference(info, loopCond), new BoolValue(info, false)));

    body.getStatements().add(new IfStmt(info, option, defblock));

    return list(block);
  }
}
