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

import evl.data.EvlList;
import evl.data.Namespace;
import evl.data.expression.BoolValue;
import evl.data.expression.Number;
import evl.data.expression.binop.Less;
import evl.data.expression.binop.Plus;
import evl.data.expression.reference.Reference;
import evl.data.expression.reference.SimpleRef;
import evl.data.statement.AssignmentSingle;
import evl.data.statement.Block;
import evl.data.statement.ForStmt;
import evl.data.statement.IfOption;
import evl.data.statement.IfStmt;
import evl.data.statement.Statement;
import evl.data.statement.VarDefStmt;
import evl.data.statement.WhileStmt;
import evl.data.type.Type;
import evl.data.type.base.RangeType;
import evl.data.variable.FuncVariable;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowType;
import evl.knowledge.KnowUniqueName;
import evl.knowledge.KnowledgeBase;
import evl.traverser.other.StmtReplacer;

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
  final private KnowType kt;

  public ForReductionWorker(KnowledgeBase kb) {
    super();
    this.kbi = kb.getEntry(KnowBaseItem.class);
    this.kun = kb.getEntry(KnowUniqueName.class);
    this.kt = kb.getEntry(KnowType.class);
  }

  @Override
  protected List<Statement> visitForStmt(ForStmt obj, Void param) {
    // TODO documentation
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
    EvlList<IfOption> option = new EvlList<IfOption>();
    Block defblock = new Block(info);

    Block inc = new Block(info);
    option.add(new IfOption(info, new Less(info, new Reference(info, itr), new Number(info, rt.range.high)), inc));
    inc.statements.add(new AssignmentSingle(info, new Reference(info, itr), new Plus(info, new Reference(info, itr), new Number(info, BigInteger.ONE))));
    defblock.statements.add(new AssignmentSingle(info, new Reference(info, loopCond), new BoolValue(info, false)));

    body.statements.add(new IfStmt(info, option, defblock));

    return list(block);
  }
}
