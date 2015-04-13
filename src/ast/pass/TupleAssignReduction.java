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

package ast.pass;

import java.util.ArrayList;
import java.util.List;

import pass.AstPass;
import ast.copy.Copy;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.expression.Expression;
import ast.data.expression.NamedElementsValue;
import ast.data.expression.NamedValue;
import ast.data.expression.TupleValue;
import ast.data.expression.reference.RefName;
import ast.data.expression.reference.Reference;
import ast.data.expression.reference.SimpleRef;
import ast.data.statement.AssignmentMulti;
import ast.data.statement.AssignmentSingle;
import ast.data.statement.Statement;
import ast.data.statement.VarDefStmt;
import ast.data.type.Type;
import ast.data.type.composed.RecordType;
import ast.data.variable.FuncVariable;
import ast.knowledge.KnowType;
import ast.knowledge.KnowledgeBase;
import ast.traverser.other.StmtReplacer;

import common.Designator;
import common.ElementInfo;

import error.RError;

//TODO merge parts with InitVarTyper
public class TupleAssignReduction extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    TupleAssignReductionWorker worker = new TupleAssignReductionWorker(kb);
    worker.traverse(ast, null);
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

  private List<Statement> assignMulOne(AstList<Reference> left, Expression right) {
    ElementInfo info = ElementInfo.NO;

    Type rt = kt.get(right);
    if (rt instanceof RecordType) {
      String name = Designator.NAME_SEP + "var"; // XXX add pass to make names
      // unique
      FuncVariable var = new FuncVariable(info, name, new SimpleRef<Type>(info, rt));

      List<Statement> ret = new ArrayList<Statement>();
      ret.add(new VarDefStmt(info, var));
      ret.add(new AssignmentSingle(info, new Reference(info, var), right));

      for (int i = 0; i < left.size(); i++) {
        Reference lr = left.get(i);
        String elemName = ((RecordType) rt).element.get(i).name;
        Reference rr = new Reference(info, var, new RefName(info, elemName));
        ret.add(new AssignmentSingle(info, lr, rr));
      }

      return ret;
    } else {
      throw new RuntimeException("not yet implemented: " + rt);
    }
  }

  private List<Statement> assignOneNamed(Reference left, AstList<NamedValue> value) {
    throw new RuntimeException("not yet implemented");
  }

  private List<Statement> assignOne(Reference left, AstList<Expression> value) {
    Type rt = kt.get(left);
    if (rt instanceof RecordType) {
      return assignOneRecord(left, (RecordType) rt, value);
    } else {
      throw new RuntimeException("not yet implemented: " + rt);
    }
  }

  private List<Statement> assignOneRecord(Reference left, RecordType rt, AstList<Expression> value) {
    RError.ass(rt.element.size() == value.size(), left.getInfo(), "expected same number of elementds, got: " + rt.element.size() + " <-> " + value.size());
    List<Statement> ret = new ArrayList<Statement>(value.size());

    for (int i = 0; i < value.size(); i++) {
      Reference subref = Copy.copy(left);
      subref.offset.add(new RefName(ElementInfo.NO, rt.element.get(i).name));
      Expression subVal = value.get(i);
      RError.ass(!(subVal instanceof NamedElementsValue), subVal.getInfo(), "Named element values for tuple not yet supported: " + subVal.toString());
      AssignmentSingle ass = new AssignmentSingle(left.getInfo(), subref, subVal);
      ret.add(ass);
    }

    return ret;
  }
}
