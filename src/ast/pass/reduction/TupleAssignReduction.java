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

import java.util.ArrayList;
import java.util.List;

import ast.Designator;
import ast.copy.Copy;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.expression.Expression;
import ast.data.expression.ReferenceExpression;
import ast.data.expression.value.NamedElementsValue;
import ast.data.expression.value.NamedValue;
import ast.data.expression.value.TupleValue;
import ast.data.reference.OffsetReference;
import ast.data.reference.RefFactory;
import ast.data.reference.RefName;
import ast.data.reference.Reference;
import ast.data.statement.AssignmentSingle;
import ast.data.statement.MultiAssignment;
import ast.data.statement.Statement;
import ast.data.statement.VarDefStmt;
import ast.data.type.Type;
import ast.data.type.TypeRefFactory;
import ast.data.type.composed.RecordType;
import ast.data.variable.FunctionVariable;
import ast.dispatcher.other.StmtReplacer;
import ast.knowledge.KnowType;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import error.RError;

//TODO merge parts with InitVarTyper
public class TupleAssignReduction implements AstPass {

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

    if (obj.getRight() instanceof TupleValue) {
      rightCount = ((TupleValue) obj.getRight()).value.size();
    } else if (obj.getRight() instanceof NamedElementsValue) {
      rightCount = ((NamedElementsValue) obj.getRight()).value.size();
    } else {
      rightCount = 1;
    }

    if (rightCount > 1) {
      // FIXME do not use cast
      OffsetReference left = (OffsetReference) obj.getLeft();
      return assignOneOne(left, obj.getRight());
    } else {
      return null;
    }
  }

  @Override
  protected List<Statement> visitAssignmentMulti(MultiAssignment obj, Void param) {
    int leftCount, rightCount;

    leftCount = obj.getLeft().size();

    if (obj.getRight() instanceof TupleValue) {
      rightCount = ((TupleValue) obj.getRight()).value.size();
    } else if (obj.getRight() instanceof NamedElementsValue) {
      rightCount = ((NamedElementsValue) obj.getRight()).value.size();
    } else {
      rightCount = 1;
    }

    assert (leftCount > 0);
    assert (rightCount > 0);
    assert ((leftCount == 1) || (rightCount == 1) || (leftCount == rightCount));

    if ((leftCount > 1) && (rightCount > 1)) {
      throw new RuntimeException("not yet implemented");
    } else if (leftCount > 1) {
      return assignMulOne(obj.getLeft(), obj.getRight());
    } else if (rightCount > 1) {
      return assignOneOne((OffsetReference) obj.getLeft().get(0), obj.getRight());
    } else {
      return null;
    }
  }

  private List<Statement> assignOneOne(OffsetReference left, Expression right) {
    if (right instanceof TupleValue) {
      TupleValue gen = (TupleValue) right;
      return assignOne(left, gen.value);
    } else {
      NamedElementsValue gen = (NamedElementsValue) right;
      return assignOneNamed(left, gen.value);
    }
  }

  private List<Statement> assignMulOne(AstList<Reference> left, Expression right) {
    Type rt = kt.get(right);
    if (rt instanceof RecordType) {
      String name = Designator.NAME_SEP + "var"; // XXX add pass to make names
      // unique
      FunctionVariable var = new FunctionVariable(name, TypeRefFactory.create(rt));

      List<Statement> ret = new ArrayList<Statement>();
      ret.add(new VarDefStmt(var));
      ret.add(new AssignmentSingle(RefFactory.withOffset(var), right));

      for (int i = 0; i < left.size(); i++) {
        Reference lr = left.get(i);
        String elemName = ((RecordType) rt).element.get(i).getName();
        Reference rr = RefFactory.create(var, new RefName(elemName));
        ret.add(new AssignmentSingle(lr, new ReferenceExpression(rr)));
      }

      return ret;
    } else {
      throw new RuntimeException("not yet implemented: " + rt);
    }
  }

  private List<Statement> assignOneNamed(OffsetReference left, AstList<NamedValue> value) {
    throw new RuntimeException("not yet implemented");
  }

  private List<Statement> assignOne(OffsetReference left, AstList<Expression> value) {
    Type rt = kt.get(left);
    if (rt instanceof RecordType) {
      return assignOneRecord(left, (RecordType) rt, value);
    } else {
      throw new RuntimeException("not yet implemented: " + rt);
    }
  }

  private List<Statement> assignOneRecord(OffsetReference left, RecordType rt, AstList<Expression> value) {
    RError.ass(rt.element.size() == value.size(), left.metadata(), "expected same number of elementds, got: " + rt.element.size() + " <-> " + value.size());
    List<Statement> ret = new ArrayList<Statement>(value.size());

    for (int i = 0; i < value.size(); i++) {
      OffsetReference subref = Copy.copy(left);
      subref.getOffset().add(new RefName(rt.element.get(i).getName()));
      Expression subVal = value.get(i);
      RError.ass(!(subVal instanceof NamedElementsValue), subVal.metadata(), "Named element values for tuple not yet supported: " + subVal.toString());
      AssignmentSingle ass = new AssignmentSingle(subref, subVal);
      ass.metadata().add(left.metadata());
      ret.add(ass);
    }

    return ret;
  }
}
