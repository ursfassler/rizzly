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
import java.util.List;

import pass.EvlPass;

import common.Designator;
import common.ElementInfo;

import error.RError;
import evl.copy.Copy;
import evl.data.EvlList;
import evl.data.Namespace;
import evl.data.expression.AnyValue;
import evl.data.expression.Expression;
import evl.data.expression.NamedElementsValue;
import evl.data.expression.NamedValue;
import evl.data.expression.TupleValue;
import evl.data.expression.reference.RefName;
import evl.data.expression.reference.Reference;
import evl.data.expression.reference.SimpleRef;
import evl.data.statement.AssignmentMulti;
import evl.data.statement.AssignmentSingle;
import evl.data.statement.Statement;
import evl.data.statement.VarDefStmt;
import evl.data.type.Type;
import evl.data.type.composed.RecordType;
import evl.data.variable.FuncVariable;
import evl.knowledge.KnowType;
import evl.knowledge.KnowledgeBase;
import evl.traverser.other.StmtReplacer;

//TODO merge parts with InitVarTyper
public class TupleAssignReduction extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    TupleAssignReductionWorker worker = new TupleAssignReductionWorker(kb);
    worker.traverse(evl, null);
  }

}

class TupleAssignReductionWorker extends StmtReplacer<Void> {
  final private KnowType kt;

  public TupleAssignReductionWorker(KnowledgeBase kb) {
    super();
    kt = kb.getEntry(KnowType.class);
  }

  @Override
  protected List<Statement> visitAssignmentSingle(AssignmentSingle obj, Void param) {
    int rightCount;

    if (obj.right instanceof TupleValue) {
      rightCount = ((TupleValue) obj.right).value.size();
    } else if (obj.right instanceof NamedElementsValue) {
      rightCount = ((NamedElementsValue) obj.right).value.size();
    } else {
      rightCount = 1;
    }

    if (rightCount > 1) {
      return assignOneOne(obj.left, obj.right);
    } else {
      return null;
    }
  }

  @Override
  protected List<Statement> visitAssignmentMulti(AssignmentMulti obj, Void param) {
    int leftCount, rightCount;

    leftCount = obj.left.size();

    if (obj.right instanceof TupleValue) {
      rightCount = ((TupleValue) obj.right).value.size();
    } else if (obj.right instanceof NamedElementsValue) {
      rightCount = ((NamedElementsValue) obj.right).value.size();
    } else {
      rightCount = 1;
    }

    assert (leftCount > 0);
    assert (rightCount > 0);
    assert ((leftCount == 1) || (rightCount == 1) || (leftCount == rightCount));

    if ((leftCount > 1) && (rightCount > 1)) {
      throw new RuntimeException("not yet implemented");
    } else if (leftCount > 1) {
      return assignMulOne(obj.left, obj.right);
    } else if (rightCount > 1) {
      return assignOneOne(obj.left.get(0), obj.right);
    } else {
      return null;
    }
  }

  private List<Statement> assignOneOne(Reference left, Expression right) {
    if (right instanceof TupleValue) {
      TupleValue gen = (TupleValue) right;
      return assignOne(left, gen.value);
    } else {
      NamedElementsValue gen = (NamedElementsValue) right;
      return assignOneNamed(left, gen.value);
    }
  }

  private List<Statement> assignMulOne(EvlList<Reference> left, Expression right) {
    ElementInfo info = ElementInfo.NO;

    Type rt = kt.get(right);
    if (rt instanceof RecordType) {
      String name = Designator.NAME_SEP + "var";  // XXX add pass to make names unique
      FuncVariable var = new FuncVariable(info, name, new SimpleRef<Type>(info, rt));

      List<Statement> ret = new ArrayList<Statement>();
      ret.add(new VarDefStmt(info, var));
      ret.add(new AssignmentSingle(info, new Reference(info, var), right));

      for (int i = 0; i < left.size(); i++) {
        Reference lr = left.get(i);
        if (!(lr.link instanceof AnyValue)) {
          String elemName = ((RecordType) rt).element.get(i).getName();
          Reference rr = new Reference(info, var, new RefName(info, elemName));
          ret.add(new AssignmentSingle(info, lr, rr));
        }
      }

      return ret;
    } else {
      throw new RuntimeException("not yet implemented: " + rt);
    }
  }

  private List<Statement> assignOneNamed(Reference left, EvlList<NamedValue> value) {
    throw new RuntimeException("not yet implemented");
  }

  private List<Statement> assignOne(Reference left, EvlList<Expression> value) {
    Type rt = kt.get(left);
    if (rt instanceof RecordType) {
      return assignOneRecord(left, (RecordType) rt, value);
    } else {
      throw new RuntimeException("not yet implemented: " + rt);
    }
  }

  private List<Statement> assignOneRecord(Reference left, RecordType rt, EvlList<Expression> value) {
    RError.ass(rt.getSize() == value.size(), left.getInfo(), "expected same number of elementds, got: " + rt.getSize() + " <-> " + value.size());
    List<Statement> ret = new ArrayList<Statement>(value.size());

    for (int i = 0; i < value.size(); i++) {
      Reference subref = Copy.copy(left);
      subref.offset.add(new RefName(ElementInfo.NO, rt.element.get(i).getName()));
      Expression subVal = value.get(i);
      RError.ass(!(subVal instanceof NamedElementsValue), subVal.getInfo(), "Named element values for tuple not yet supported: " + subVal.toString());
      AssignmentSingle ass = new AssignmentSingle(left.getInfo(), subref, subVal);
      ret.add(ass);
    }

    return ret;
  }
}
